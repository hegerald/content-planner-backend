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

## Agent-Konfiguration in diesem Repo (offene Standards + GitHub Copilot)
- `AGENTS.md` (diese Datei) – offener Standard, gelesen von Copilot, Codex, Claude Code, Gemini (konfigurierbar) u. a.
- `.agents/skills/` – Agent-Skills-Standard (agentskills.io), z. B. `add-business-rule`, `start-backend`
- `.github/agents/` – Copilot Custom Agents (z. B. `Reviewer`)
- `.github/instructions/` – Copilot-Regeln nur für bestimmte Pfade (`applyTo`)
- `.vscode/mcp.json` – GitHub-MCP für Copilot in VS Code

## Abnehmer (Upstream)
- `content-planner-ui` (UI-Service, Repo `hegerald/content-planner-ui`) ruft `/api/**` per REST auf.
