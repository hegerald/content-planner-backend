---
name: start-backend
description: Startet das Content-Planner-Backend lokal im Quarkus-Dev-Modus (Port 8081) und prüft, ob es antwortet. Verwenden, wenn das Backend laufen soll – zum Ausprobieren, für die Swagger UI oder als Voraussetzung für UI-Service und Frontend.
---

# Backend lokal starten

Die ganze Kette (Backend + UI-Service + Frontend) startet der Skill `start-services`
im Repo `content-planner-ui`.

## Schritte

1. **Läuft es schon?** `curl -s http://localhost:8081/q/health/ready` – liefert es
   `"status": "UP"`, nichts tun. Ist Port 8081 belegt, aber ohne gültige Antwort, den Nutzer
   fragen, statt den Prozess zu beenden.
2. **Voraussetzung:** JDK 25 oder neuer (`java -version`) und Maven global im PATH (`mvn -v`).
3. **Starten** im Repo-Root mit `run_in_background: true` (blockiert sonst die Sitzung):
   ```bash
   mvn quarkus:dev -Dquarkus.console.enabled=false
   ```
   Im PowerShell-Tool: `mvn quarkus:dev "-Dquarkus.console.enabled=false"`.
   `-Dquarkus.console.enabled=false` schaltet die interaktive Dev-Konsole ab, die ohne
   Terminal-Eingabe nur stört. Live-Reload funktioniert trotzdem.
4. **Warten** auf die Logzeile `Listening on: http://localhost:8081` (erster Start mit
   Download der Abhängigkeiten: bis ca. 1 Minute), dann erneut den Health-Check aus Schritt 1.
5. **Melden:**
   - API: http://localhost:8081/api/posts
   - Swagger UI: http://localhost:8081/q/swagger-ui

## Hinweise
- Beim ersten Start legt der Dev-Modus Beispiel-Posts an (`Demo-Daten angelegt`).
  Daten liegen in `./data/`; zum Zurücksetzen Backend stoppen und den Ordner löschen –
  vorher nachfragen, die Daten gehen dabei verloren.
- Startfehler stehen in der Ausgabe des Hintergrundprozesses; häufig: falsches JDK oder Port belegt.
