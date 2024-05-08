# Notes

## Liquibase update
OpenNMs baut und startet auch.
- Die ganzen Custom-Changes müssten entfernt werden (liquibase klassen in core/schema/liquibase)
- Offline Updater aktualisiert die monitoringlocatino id von 0000-000-0 auf einen anderen Wert, das ist aktuell kaputt und müsste über sql changelogs abgebildet werden)
- Offline Updater alle Implementierungen rauswerfen, Code möglicherweise behalten?
- Überprüfen ob die Tests noch gehen, da der Migrator auch angefasst wurde


## Jasper removal

