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
```

## API-Contract
`mvn package` schreibt den OpenAPI-Contract nach `openapi/openapi.yaml` (und `.json`).
Die Datei wird eingecheckt, damit Änderungen an der API im Pull Request sichtbar sind.

## Für KI-Agents
Projektregeln stehen in [CLAUDE.md](CLAUDE.md). Dieses Repo ist bewusst **nur für Claude Code**
konfiguriert: `.claude/rules/` (pfadbezogene Regeln), `.claude/skills/` (Skills),
`.claude/agents/` (Subagents), `.claude/settings.json` mit Hooks und `.mcp.json`.
