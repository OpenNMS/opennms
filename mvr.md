# Notes

## Liquibase update
OpenNMs baut und startet auch.
- Die ganzen Custom-Changes müssten entfernt werden (liquibase klassen in core/schema/liquibase)
- Offline Updater aktualisiert die monitoringlocatino id von 0000-000-0 auf einen anderen Wert, das ist aktuell kaputt und müsste über sql changelogs abgebildet werden)
- Offline Updater alle Implementierungen rauswerfen, Code möglicherweise behalten?
- Überprüfen ob die Tests noch gehen, da der Migrator auch angefasst wurde


## Jasper removal

### Validate these files

 - [X] ./core/upgrade/src/test/resources/etc/ksc-performance-reports.xml
 - [X] ./core/schema/src/main/liquibase/1.6.0/tables/statisticsreportdata.xml
 - [X] ./core/schema/src/main/liquibase/1.6.0/tables/statisticsreport.xml
 - [X] ./core/schema/src/main/liquibase/1.6.0/tables/reportlocator.xml
 - [X] ./core/schema/src/main/liquibase/1.7.10/reportcatalog.xml
 - [X] ./core/test-api/services/src/main/resources/opennms-reports-testdata.xml
 - [X] ./opennms-web-api/src/test/resources/ksc-performance-reports-legacy.xml
 - [X] ./opennms-web-api/src/test/resources/ksc-performance-reports-test.xml
 - [X] ./features/system-report/src/test/resources/applicationContext-test-systemReport.xml
 - [X] ./features/system-report/src/main/resources/META-INF/opennms/applicationContext-systemReport.xml
 - [X] ./opennms-correlation/drools-correlation-engine/src/test/opennms-home/etc/availability-reports.xml
 - [X] ./opennms-webapp/src/test/opennms-home/etc/availability-reports.xml
 - [X] ./opennms-webapp-rest/src/test/resources/ksc-performance-reports.xml
 - [X] ./opennms-base-assembly/src/main/filtered/etc/availability-reports.xml
 - [X] ./opennms-base-assembly/src/main/filtered/etc/ksc-performance-reports.xml
 - [X] ./opennms-base-assembly/src/main/filtered/etc/events/opennms.reportd.events.xml

### Verify these packages

 - [X] org.opennms.netmgt.config.reporting.*;version="${project.version}",

### remove
 - [X] opennms.reportd.events.xml
 - [X] update eventconf
 - [ ] create changelog to delete existing database entries in db, if they exist

### Open Issues

 - [ ] How to deal with service-configuration.xml updates?
 - [ ] remove quartz as well?


### Mark tables as deprecated or drop them in general

 - [ ] statisticsreportdata
 - [ ] statisticsreport
 - [ ] reportcatalog

remember to also remove corresponding entities