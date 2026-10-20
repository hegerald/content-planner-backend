#!/usr/bin/env node
// Prüft das Backend genau wie die CI: Build und alle Tests (`mvn -B verify`), danach ob der
// OpenAPI-Contract in `openapi/` aktuell ist. Die CI ruft dieses Skript selbst auf – lokal grün
// heißt damit auch in der CI grün.
//
// Aufruf: node scripts/verify.mjs [--quiet] [--json]
//   --quiet  Maven-Ausgabe nur in die Log-Datei, auf dem Terminal nur die Zusammenfassung
//   --json   als letzte Zeile das Ergebnis als JSON (liest scripts/verify.mjs im UI-Repo)
// Exit-Code 0 = alles grün, 1 = Build, Tests oder Contract fehlgeschlagen.
import { spawn } from "node:child_process";
import { createWriteStream, mkdirSync } from "node:fs";
import { tmpdir } from "node:os";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const root = dirname(dirname(fileURLToPath(import.meta.url)));
const quiet = process.argv.includes("--quiet");
const json = process.argv.includes("--json");
const inCi = !!process.env.GITHUB_ACTIONS;

const logDir = join(tmpdir(), "content-planner-verify");
mkdirSync(logDir, { recursive: true });
const logPath = join(logDir, "backend.log");

// Kommandozeile über die Shell, damit unter Windows `mvn.cmd` gefunden wird.
// Nur feste Argumente ohne Leerzeichen, daher kein Quoting nötig.
function run(commandLine, cwd, log) {
  return new Promise((resolve) => {
    const child = spawn(commandLine, { cwd, shell: true });
    let output = "";
    const onData = (chunk) => {
      output += chunk;
      log?.write(chunk);
      if (!quiet && log) process.stdout.write(chunk);
    };
    child.stdout.on("data", onData);
    child.stderr.on("data", onData);
    child.on("close", (status) => resolve({ status, output }));
    child.on("error", (error) => resolve({ status: -1, output: String(error) }));
  });
}

const started = Date.now();
const log = createWriteStream(logPath);
const build = await run("mvn -B verify", root, log);
await new Promise((resolve) => log.end(resolve));

const lines = build.output.split(/\r?\n/);
// Mavens allgemeine Hilfe-Hinweise am Ende gehören nicht in die Zusammenfassung.
const MAVEN_NOISE = /^\[ERROR\]\s*$|\[Help 1\]|-e switch|-X switch|please read|dump files|surefire-reports for/;
// Die Gesamtsumme ist die letzte „Tests run“-Zeile ohne „-- in <Klasse>“.
const totals = lines
  .map((line) => line.match(/Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)\s*$/))
  .filter(Boolean)
  .at(-1);
const tests = totals ? `${totals[1] - totals[2] - totals[3] - totals[4]}/${totals[1]}` : "–";

// Unter Windows (core.autocrlf) zeigt `git status` nach dem Build oft „M openapi/…“, obwohl nur
// der Zeilenumbruch abweicht. `git diff --exit-code` vergleicht den Inhalt und ist dafür maßgeblich.
let contract = "–";
if (build.status === 0) {
  const diff = await run("git diff --quiet --exit-code -- openapi/", root);
  contract = diff.status === 0 ? "aktuell" : diff.status === 1 ? "geändert" : "unbekannt";
}

const ok = build.status === 0 && contract === "aktuell";
const result = {
  part: "backend",
  status: ok ? "ok" : "failed",
  tests,
  contract,
  seconds: Math.round((Date.now() - started) / 1000),
  log: logPath,
  details: ok
    ? ""
    : build.status !== 0
      ? lines.filter((line) => /ERROR|FAIL/.test(line) && !MAVEN_NOISE.test(line)).slice(-40).join("\n")
      : "openapi/ ist veraltet – `mvn package` ausführen und die Dateien mit committen.",
};

if (inCi && contract === "geändert") {
  console.log("::error::openapi/ ist veraltet – mvn package ausführen und committen");
}
console.log(
  `\nBackend: ${ok ? "grün" : "ROT"} – Tests ${result.tests}, Contract ${result.contract}, ${result.seconds} s` +
    `\nLog: ${logPath}`
);
if (result.details) console.log(`\n${result.details}`);
if (json) console.log(JSON.stringify(result));
process.exit(ok ? 0 : 1);
