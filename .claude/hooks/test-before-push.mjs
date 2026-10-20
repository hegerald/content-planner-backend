// PreToolUse-Hook: Vor jedem `git push` muss `scripts/verify.mjs` grün sein – dieselbe Prüfung wie
// die CI (Build, alle Quarkus-Tests inkl. @QuarkusTest-Integrationstests, OpenAPI-Contract aktuell).
// Schlägt sie fehl, wird der Push mit Exit-Code 2 blockiert und Claude bekommt die Zusammenfassung zurück.
import { spawnSync } from "node:child_process";
import { join } from "node:path";

let input = "";
for await (const chunk of process.stdin) input += chunk;

const command = JSON.parse(input).tool_input?.command ?? "";
if (!/\bgit\b[^\n;&|]*\bpush\b/.test(command)) process.exit(0);

const root = process.env.CLAUDE_PROJECT_DIR || process.cwd();
const result = spawnSync(process.execPath, [join(root, "scripts", "verify.mjs"), "--quiet"], {
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
