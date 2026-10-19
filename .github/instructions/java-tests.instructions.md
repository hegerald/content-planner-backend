---
applyTo: "src/test/**/*.java"
---

# Regeln für Tests (wird nur bei Testdateien geladen)

- Fachregeln als schnelle Unit-Tests in `PostRulesTest` mit `Clock.fixed(...)`, ohne Quarkus-Start.
- REST-Verhalten als `@QuarkusTest` mit RestAssured in `PostResourceTest`; Fehler immer über
  `status` **und** `code` prüfen, nie nur über die Meldung.
- Testnamen beschreiben das Verhalten (`textOverChannelLimitIsRejected`), nicht die Methode.
- Bestehende Tests nicht löschen, abschwächen oder mit `@Disabled` versehen, um einen Build
  grün zu bekommen – stattdessen den Code korrigieren.
