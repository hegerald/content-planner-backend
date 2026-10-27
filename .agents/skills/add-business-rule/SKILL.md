---
name: add-business-rule
description: Fügt dem Content-Planner-Backend eine neue Fachregel hinzu (z. B. Zeichenlimit, Statuswechsel, Terminprüfung) – testgetrieben in PostRules, mit stabilem Fehlercode, Integrationstest und aktualisiertem OpenAPI-Contract. Verwenden, wenn sich fachliches Verhalten des Backends ändert.
---

# Neue Fachregel im Backend

## Schritte

1. **Regel benennen.** Einen stabilen Fehlercode in `UPPER_SNAKE_CASE` wählen,
   z. B. `HASHTAG_LIMIT_EXCEEDED`. Bestehende Codes nie umbenennen.
2. **Unit-Test zuerst** in `src/test/java/.../post/PostRulesTest.java`:
   erlaubter Fall + verletzter Fall (prüft den `code` der `BusinessRuleException`).
   Feste Uhr verwenden: `Clock.fixed(...)`.
3. **Regel umsetzen** in `src/main/java/.../post/PostRules.java` – ohne Datenbankzugriff,
   Zeit nur über `now()`. Meldung auf Deutsch, mit konkreten Werten.
4. **Anwenden** in `PostService` an der passenden Stelle (anlegen, ändern, Statuswechsel).
5. **Integrationstest** in `PostResourceTest`: HTTP 422 und `code` prüfen.
6. **Erlaubte Aktionen**: Beeinflusst die Regel, was eine Oberfläche anbieten darf, das
   berechnete Feld in `PostResponse` (`editable`, `deletable`, `allowedTransitions`) anpassen.
7. **Contract**: `mvn package` ausführen, `openapi/openapi.yaml` mit committen und den
   neuen Fehlercode im Wissens-Bundle des UI-Repos ergänzen
   (`docs/downstream/content-planner-backend/errors/<code>.md`, Open Knowledge Format).
8. `mvn test` muss grün sein.
