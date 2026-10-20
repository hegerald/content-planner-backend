// PreToolUse-Hook: Vor jedem `git push` müssen alle Quarkus-Tests (inkl. @QuarkusTest-Integrationstests) grün sein.
// Schlägt `mvn test` fehl, wird der Push mit Exit-Code 2 blockiert und Claude bekommt die Fehlerausgabe zurück.
import { spawnSync } from "node:child_process";

let input = "";
for await (const chunk of process.stdin) input += chunk;

const command = JSON.parse(input).tool_input?.command ?? "";
if (!/\bgit\b[^\n;&|]*\bpush\b/.test(command)) process.exit(0);

const result = spawnSync("mvn", ["-B", "test"], {
  cwd: process.env.CLAUDE_PROJECT_DIR || process.cwd(),
  encoding: "utf8",
  shell: process.platform === "win32",
  maxBuffer: 64 * 1024 * 1024,
});

if (result.status === 0) process.exit(0);

const output = `${result.stdout ?? ""}${result.stderr ?? ""}`;
const relevant = output
  .split(/\r?\n/)
  .filter((line) => /ERROR|FAIL|Tests run:/.test(line))
  .slice(-60)
  .join("\n");
console.error(
  "Push blockiert: `mvn test` ist fehlgeschlagen – nicht alle Quarkus-Tests laufen.\n" +
    "Erst die Tests reparieren, dann erneut pushen.\n\n" +
    (relevant || output.slice(-4000))
);
process.exit(2);
