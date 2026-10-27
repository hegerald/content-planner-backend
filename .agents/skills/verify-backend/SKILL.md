---
name: verify-backend
description: Prüft das Content-Planner-Backend lokal wie die CI – Build, alle Tests und ob der OpenAPI-Contract in `openapi/` aktuell ist – über `scripts/verify.mjs` und wertet das Ergebnis aus. Verwenden vor Commit, Push oder Pull Request und immer, wenn gefragt wird, ob Build und Tests grün sind.
---

# Backend prüfen (wie CI)

Die Prüfung selbst steckt in `scripts/verify.mjs`; die CI und der Git-Hook `pre-push` rufen dasselbe
Skript auf. Dieser Skill führt es aus und wertet aus. Alle Teile über beide Repos prüft der
Skill `verify-all` im Repo `content-planner-ui`.

## Schritte

1. Im Repo-Root: `node scripts/verify.mjs --quiet` (ca. 45–90 s, mit leerem Maven-Cache länger).
   `--quiet` zeigt nur die Zusammenfassung; die volle Maven-Ausgabe steht in der Log-Datei,
   deren Pfad das Skript ausgibt. Ein laufendes `mvn quarkus:dev` stört nicht (Tests: Port 8091).
2. **Auswerten** – bei Exit-Code ≠ 0:
   - Fehlgeschlagene Tests: Zusammenfassung nennt Test und Meldung; Details in
     `target/surefire-reports/*.txt`. Ursache suchen, nicht den Test anpassen, ohne nachzufragen.
   - `Contract geändert`: Die API hat sich geändert. Diff von `openapi/` zeigen und prüfen, ob die
     Änderung rückwärtskompatibel ist (Regeln in `AGENTS.md`). Gewollt → Dateien gehören in
     denselben Commit; den UI-Repo-Skill `contract-change` erwähnen.
   - Startet `mvn` nicht: JDK 25+ (`java -version`) und Maven (`mvn -v`) im PATH prüfen.
3. **Melden:** Zeile der Zusammenfassung, dazu Fehlschläge mit Ursache – nichts automatisch
   reparieren oder committen.

## Hinweise
- Unter Windows (`core.autocrlf=true`) zeigt `git status` nach dem Build `M openapi/openapi.yaml`.
  Meldet das Skript `Contract aktuell`, ist das nur der Zeilenumbruch – nicht committen.
  `git checkout -- openapi/` räumt die Anzeige auf (nur wenn `git diff -- openapi/` leer ist).
- Ändert sich, was geprüft wird, das Skript anpassen – nicht diesen Skill oder die CI.
