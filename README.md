# Content Planner – Backend

Demo-Projekt für eine Session zu KI-gestützter Softwareentwicklung.
Verwaltet geplante Social-Media-Posts (Instagram, LinkedIn, X, Facebook) mit Statusablauf
**Entwurf → Eingeplant → Veröffentlicht** und prüft alle fachlichen Regeln.

## Architektur

```
Angular-App ─┐
             ├─► UI-Service (8080, /ui-api) ─► Backend (8081, /api) ─► H2-Datei
Eigene UI  ──┘                                 ▲ dieses Repo
```

Das Backend ist die einzige Stelle mit Business-Logik. Der UI-Service bereitet nur für die
Anzeige auf. Dadurch bleibt das System korrekt, auch wenn Nutzer sich ihre eigene
Oberfläche bauen.

## Voraussetzungen
- JDK 25 oder neuer
- Maven 3.9 oder neuer, global installiert (`mvn -v`)

## Starten
```bash
mvn quarkus:dev
```
- API: http://localhost:8081/api/posts
- Swagger UI: http://localhost:8081/q/swagger-ui
- Im Dev-Modus werden beim ersten Start Beispiel-Posts angelegt. Die Daten liegen in `./data/`.
  Zum Zurücksetzen den Ordner löschen.

## Testen
```bash
mvn test
node scripts/verify.mjs   # wie die CI: Build, Tests, OpenAPI-Contract aktuell?
```

## API-Contract
`mvn package` schreibt den OpenAPI-Contract nach `openapi/openapi.yaml` (und `.json`).
Die Datei wird eingecheckt, damit Änderungen an der API im Pull Request sichtbar sind.

## Für KI-Agents
Projektregeln stehen in [.github/copilot-instructions.md](.github/copilot-instructions.md).
Dieser Branch ist bewusst **nur für GitHub Copilot** (Agent-Modus in VS Code) konfiguriert:
`.github/instructions/` (pfadbezogene Regeln), `.github/skills/` (Skills), `.github/agents/`
(Custom Agents), `.github/hooks/` (Agent-Hook vor `git push`), `.vscode/mcp.json` (GitHub-MCP)
und `.vscode/settings.json` (Terminal-Freigaben).
