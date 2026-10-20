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
  und Push-Hook; Auswertung per Skill `/verify-backend`. Was geprüft wird, nur im Skript ändern.
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

## Claude-Code-Konfiguration in diesem Repo
- `CLAUDE.md` (diese Datei) – Projektregeln, in jeder Sitzung geladen
- `.claude/rules/` – Regeln nur für bestimmte Pfade (`paths:`), z. B. `java-tests.md`
- `.claude/skills/` – Skills, z. B. `add-business-rule`, `start-backend`, `verify-backend`
- `.claude/agents/` – Subagents, z. B. `reviewer`
- `.claude/settings.json` + `.claude/hooks/` – Hook: `scripts/verify.mjs` muss vor `git push` grün sein
- `.mcp.json` – GitHub-MCP, Token aus Umgebungsvariable `GITHUB_PERSONAL_ACCESS_TOKEN`
- Persönliches (nicht eingecheckt): `CLAUDE.local.md`, `.claude/settings.local.json`

## Abnehmer (Upstream)
- `content-planner-ui` (UI-Service, Repo `hegerald/content-planner-ui`) ruft `/api/**` per REST auf.
