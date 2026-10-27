// Agent-Hook (PreToolUse): Vor jedem `git push` muss `scripts/verify.mjs` grün sein – dieselbe Prüfung
// wie die CI (Build, alle Quarkus-Tests inkl. @QuarkusTest-Integrationstests, OpenAPI-Contract aktuell).
// Außerdem werden `git push --force` und `git reset --hard` immer blockiert.
// Schlägt die Prüfung fehl, wird der Befehl mit Exit-Code 2 blockiert und der Agent bekommt die Zusammenfassung zurück.
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";

let input = "";
for await (const chunk of process.stdin) input += chunk;

// VS Code und Copilot CLI liefern den Terminal-Befehl unterschiedlich verpackt.
const payload = JSON.parse(input || "{}");
let args = payload.tool_input ?? payload.toolArgs ?? {};
if (typeof args === "string") args = JSON.parse(args);
const command = args.command ?? "";

if (/\bgit\b[^\n;&|]*\b(push\b[^\n;&|]*(--force\b|\s-f\b)|reset\s+--hard\b)/.test(command)) {
  console.error("Blockiert: `git push --force` und `git reset --hard` sind für den Agent nicht erlaubt.");
  process.exit(2);
}
if (!/\bgit\b[^\n;&|]*\bpush\b/.test(command)) process.exit(0);

// Repo-Wurzel relativ zu diesem Skript (.github/hooks/ → ../../), unabhängig vom Arbeitsverzeichnis.
const root = fileURLToPath(new URL("../../", import.meta.url));
const result = spawnSync(process.execPath, ["scripts/verify.mjs", "--quiet"], {
  cwd: root,
  encoding: "utf8",
  maxBuffer: 64 * 1024 * 1024,
});

if (result.status === 0) process.exit(0);

console.error(
  "Push blockiert: `node scripts/verify.mjs` ist fehlgeschlagen (Build, Tests oder OpenAPI-Contract).\n" +
    "Erst reparieren, dann erneut pushen.\n\n" +
    `${result.stdout ?? ""}${result.stderr ?? ""}`.slice(-6000)
);
process.exit(2);
