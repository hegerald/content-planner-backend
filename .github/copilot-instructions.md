# content-planner-backend

Backend-Service des Content Planners: verwaltet geplante Social-Media-Posts.
**Hier lebt die gesamte Business-Logik.** Oberflächen (eigene Angular-App, aber auch von
Nutzern selbst gebaute UIs) greifen nie direkt auf Regeln zu, sondern verlassen sich auf
die Antworten dieses Services.

## Stack
- Java 25 (Bytecode), Quarkus 3.40, Maven
- Hibernate ORM mit Panache, H2 im File-Modus (`./data/`), Tests mit In-Memory-H2
- REST + Jackson, Bean Validation, SmallRye OpenAPI

## Befehle
- Starten (Dev-Modus, Port 8081, Live-Reload): `mvn quarkus:dev`
- Alle Tests: `mvn test`
- Alles wie die CI (Build, Tests, Contract): `node scripts/verify.mjs` – dasselbe Skript nutzen CI
  und Push-Hook; Auswertung per Skill `verify-backend`. Was geprüft wird, nur im Skript ändern.
- Build inkl. OpenAPI-Contract: `mvn package` → schreibt `openapi/openapi.yaml`
- Swagger UI: http://localhost:8081/q/swagger-ui

## Struktur
- `post/PostRules.java` – **alle Fachregeln** (Zeichenlimits, Statuswechsel, Termine). Ohne DB, als Unit-Test prüfbar.
- `post/PostService.java` – lädt/speichert, wendet Regeln an, transaktional
- `post/PostResource.java` – REST-Endpunkte unter `/api/posts`
- `channel/` – Kanäle inkl. Zeichenlimit, `/api/channels`
- `common/` – Fehlerformat (`ErrorResponse`) und Exception-Mapper

## Regeln für Änderungen
- Neue Fachregel → in `PostRules` + Unit-Test in `PostRulesTest`, dann Integrationstest in `PostResourceTest`.
- Verletzte Fachregel = `BusinessRuleException` mit stabilem `code` (HTTP 422). Codes niemals umbenennen – Clients reagieren darauf.
- Antworten enthalten berechnete Felder (`editable`, `deletable`, `allowedTransitions`), damit UIs keine Regeln nachbauen müssen. Neue Aktionen genauso abbilden.
- API-Änderungen sind Contract-Änderungen: `openapi/openapi.yaml` nach `mvn package` mit committen. Nur rückwärtskompatibel erweitern (neue optionale Felder ok, Felder entfernen/umbenennen nicht).
- Zeit immer über die injizierte `Clock`, nie `Instant.now()` in Fachlogik.
- Sprache: Code und Bezeichner Englisch, Kommentare, Meldungen und Doku Deutsch.

## GitHub-Copilot-Konfiguration in diesem Repo
- `.github/copilot-instructions.md` (diese Datei) – Projektregeln, in jeder Sitzung geladen
- `.github/instructions/` – Regeln nur für bestimmte Pfade (`applyTo:`), z. B. `java-tests.instructions.md`
- `.github/skills/` – Skills, z. B. `add-business-rule`, `start-backend`, `verify-backend`
- `.github/agents/` – Custom Agents, z. B. `reviewer`
- `.github/hooks/` – Agent-Hook: `scripts/verify.mjs` muss vor `git push` grün sein; blockiert Force-Push und `git reset --hard`
- `.vscode/mcp.json` – GitHub-MCP-Server (Anmeldung per OAuth über das GitHub-Konto in VS Code, kein Token im Repo)
- `.vscode/settings.json` – Terminal-Befehle, die ohne Rückfrage laufen dürfen (`chat.tools.terminal.autoApprove`)

## Regeln für den Agent
- Vor `git commit` und `git push` immer nachfragen. Kein Force-Push, kein `git reset --hard`.
- `.env`-Dateien nicht lesen und keine Secrets in Code, Issues oder Pull Requests schreiben.

## Abnehmer (Upstream)
- `content-planner-ui` (UI-Service, Repo `hegerald/content-planner-ui`) ruft `/api/**` per REST auf.
