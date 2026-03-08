# Feature Removal for Delta-V Core Simplification

## Goal

Remove 4 unused/legacy features to reduce the OpenNMS monolith's surface area, making it feasible to eventually run core on the lightweight `opennms/daemon` image (Delta-V Option 3).

## Features to Remove

| Feature | Complexity | Files | Reason |
|---------|-----------|-------|--------|
| Tl1d | Small | ~15 | TL1 protocol (telco) — no known users |
| Charts | Medium | ~25 | Legacy JFreeChart bar charts — superseded by Grafana |
| Device Config Backup | Large | ~80 (17 modules) | Separate concern, not part of core monitoring |
| Database Reports / Jasper | Large | ~100+ (12+ modules) | Heavy dependency (Jasper/iText/Castor), superseded by Grafana reporting |

## Approach

Sequential deletion in order of complexity. Each deletion follows the proven pattern from Vacuumd/Statsd/Actiond/Ackd removal:

1. Remove module declarations from parent POMs
2. Delete Java source code (impl, config model, DAO, tests)
3. Delete configuration files (XML, XSD, examples)
4. Remove from Karaf features.xml
5. Remove from menu templates / navigation
6. Remove from service-configuration.xml
7. Add Liquibase migrations where needed (drop tables)
8. Delete documentation
9. Clean up dangling references
10. Compile verification: `./compile.pl -DskipTests`

## Deletion 1: Tl1d (~15 files)

### Delete
- `opennms-services/src/main/java/org/opennms/netmgt/tl1d/` — daemon impl (7 classes)
- `opennms-services/src/main/java/org/opennms/netmgt/tl1d/jmx/` — JMX wrapper (2 classes)
- `opennms-services/src/main/resources/META-INF/opennms/applicationContext-tl1Daemon.xml`
- `opennms-services/src/test/java/org/opennms/netmgt/tl1d/` — tests
- `opennms-config-model/src/main/java/org/opennms/netmgt/config/tl1d/` — config model (3 classes)
- `opennms-config-model/src/main/resources/xsds/tl1d-configuration.xsd`
- `opennms-config-model/src/test/java/org/opennms/netmgt/config/tl1d/`
- `opennms-dao-api/src/main/java/org/opennms/netmgt/dao/api/Tl1ConfigurationDao.java`
- `opennms-dao/src/main/java/org/opennms/netmgt/dao/jaxb/DefaultTl1ConfigurationDao.java`
- `opennms-base-assembly/src/main/filtered/etc/tl1d-configuration.xml`
- `opennms-base-assembly/src/main/filtered/etc/examples/tl1d-configuration.xml`
- Documentation: `docs/modules/reference/pages/daemons/daemon-config-files/tl1d.adoc`, `docs/modules/operation/pages/deep-dive/events/sources/tl1.adoc`

### Modify
- `features/events/api/src/main/java/org/opennms/netmgt/events/api/EventConstants.java` — remove `TL1_AUTONOMOUS_MESSAGE_UEI`
- `opennms-services/src/main/java/org/opennms/netmgt/scriptd/helper/SnmpTrapHelper.java` — remove `sendTL1AutonomousMsgTrap()`
- `opennms-config-model/pom.xml` — remove tl1d package export
- `core/upgrade/` test fixtures — remove Tl1d from service-configuration XML files
- Eventconf files — remove tl1d events references
- `core/lib/src/main/java/org/opennms/core/utils/ConfigFileConstants.java` — remove tl1d config constant if present

## Deletion 2: Charts (~25 files)

### Delete
- `features/charts/` — entire module (6 classes + tests)
- `features/vaadin-dashlets/dashlet-charts/` — entire dashlet module (3 classes + blueprint)
- `opennms-config-model/src/main/java/org/opennms/netmgt/config/charts/` — config model (17 classes)
- `opennms-config-model/src/main/resources/xsds/chart-configuration.xsd`
- `opennms-config-model/src/test/java/org/opennms/netmgt/config/charts/`
- `opennms-config/src/main/java/org/opennms/netmgt/config/ChartConfigFactory.java`
- `opennms-config/src/main/java/org/opennms/netmgt/config/ChartConfigManager.java`
- `opennms-webapp/src/main/java/org/opennms/web/graph/PurdyChartServlet.java`
- `opennms-webapp/src/main/webapp/charts/index.jsp`
- `opennms-base-assembly/src/main/filtered/etc/chart-configuration.xml`
- Documentation: `docs/modules/operation/pages/deep-dive/visualizations/opsboard/dashlets/charts.adoc`

### Modify
- `features/pom.xml` — remove `charts` module
- `features/vaadin-dashlets/pom.xml` — remove `dashlet-charts` module
- `container/features/src/main/resources/features.xml` — remove `dashlet-charts` feature
- `opennms-config-model/pom.xml` — remove charts package export
- `opennms-webapp/pom.xml` — remove charts dependency
- `opennms-webapp/src/main/webapp/WEB-INF/web.xml` — remove PurdyChartServlet mapping
- Menu template JSONs (4 files) — remove Charts entry
- `pom.xml` (root) — remove charts dependency management
- `smoke-test/.../MenuHeaderIT.java` — remove charts menu test

## Deletion 3: Device Config Backup (~80 files, 17 modules)

### Delete
- `features/device-config/` — entire module tree (17 sub-modules)
- `ui/src/containers/DeviceConfigBackup.vue`
- `ui/src/components/Device/DCB*.vue` (9 components)
- `ui/src/types/deviceConfig.d.ts`
- `ui/src/stores/deviceStore.ts`
- `ui/src/services/deviceService.ts`
- `ui/tests/deviceConfigBackup.test.ts`
- `opennms-base-assembly/src/main/filtered/etc/device-config/` — DCB scripts
- `opennms-base-assembly/src/main/filtered/etc/examples/device-config/` — vendor scripts
- `smoke-test/.../dcb/DcbEndToEndIT.java`
- Documentation: `docs/modules/operation/pages/deep-dive/device-config-backup/`
- Documentation: `docs/modules/development/pages/rest/device_config.adoc`

### Add
- Liquibase migration: `DROP TABLE IF EXISTS device_config CASCADE; DROP SEQUENCE IF EXISTS deviceconfignxtid;`

### Modify
- `features/pom.xml` — remove `device-config` module
- `container/features/src/main/resources/features.xml` — remove all `opennms-deviceconfig-*` features
- `pom.xml` (root) — remove device-config dependency management entries
- `ui/src/router/index.ts` — remove DCB route
- `ui/src/menu/` — remove DCB menu entry if present
- `opennms-services/src/test/resources/META-INF/opennms/applicationContext-test-deviceConfig.xml` — delete
- `opennms-services/src/test/resources/META-INF/opennms/applicationContext-deviceConfigDao.xml` — delete

## Deletion 4: Database Reports / Jasper / Availability Reports (~100+ files)

### Delete
- `features/reporting/` — entire module tree (api, core, availability, jasper-reports, jasper-reports-compiler, jasper-reports-filter, model, dao, repository, rest, sdo)
- `opennms-enterprise-reporting/opennms-reportd/` — Reportd daemon
- `dependencies/jasper/` — shaded Jasper dependency
- `integrations/opennms-jasper-extensions/` — Jasper measurement extensions
- `integrations/opennms-jasperstudio-extension/` — IDE plugin
- `opennms-config-model/src/main/java/org/opennms/netmgt/config/reportd/` — Reportd config model
- `opennms-config-model/src/main/java/org/opennms/netmgt/config/reporting/` — reporting config model
- `opennms-config-model/src/main/resources/xsds/jasper-reports.xsd`
- `opennms-config-model/src/main/resources/xsds/reportd-configuration.xsd`
- `opennms-config-model/src/main/resources/xsds/reporting.xsd`
- `opennms-dao-api/` — `DatabaseReportConfigDao`, `JasperReportConfigDao`, `ReportdConfigurationDao`, `ReportCatalogDao`, `StatisticsReportDao`, `StatisticsReportDataDao`
- `opennms-dao/src/main/java/org/opennms/netmgt/dao/jaxb/DefaultDatabaseReportConfigDao.java`
- `opennms-base-assembly/src/main/filtered/etc/report-templates/` — all JRXML files
- `opennms-base-assembly/src/main/filtered/etc/jasper-reports.xml`
- `opennms-base-assembly/src/main/filtered/etc/database-reports.xml`
- `opennms-base-assembly/src/main/filtered/etc/reportd-configuration.xml`
- `opennms-base-assembly/src/main/filtered/etc/availability-reports.xml`
- Report web controllers and JSPs from `opennms-webapp`
- Report JS app from `core/web-assets/src/main/assets/js/apps/onms-reports/`
- Report images from `core/web-assets/src/main/assets/images/report*.png`
- Documentation: `docs/modules/operation/pages/deep-dive/database-reports/`, `docs/modules/development/pages/reporting/`

### Add
- Liquibase migration: drop `statisticsreport`, `statisticsreportdata`, `reportlocator`, `reportcatalog` tables

### Modify
- `features/pom.xml` — remove `reporting` module
- `dependencies/pom.xml` — remove `jasper` module
- `integrations/pom.xml` — remove jasper modules
- `opennms-enterprise-reporting/pom.xml` — remove `opennms-reportd` module
- `container/features/src/main/resources/features.xml` — remove `opennms-reporting` feature
- `opennms-config-model/pom.xml` — remove reporting/reportd package exports
- `pom.xml` (root) — remove reporting dependency management entries
- Menu template JSONs — remove Reports entries
- `service-configuration.xml` test fixtures — remove Reportd
- `core/web-assets/` build config — remove report JS app

## Shared Files Modified Across Deletions

| File | Modified By |
|------|-----------|
| `features/pom.xml` | Charts, DCB, Reports |
| `container/features/src/main/resources/features.xml` | Charts, DCB, Reports |
| `opennms-config-model/pom.xml` | Tl1d, Charts, Reports |
| Menu template JSONs (4 files) | Charts, Reports |
| `pom.xml` (root) | Charts, DCB, Reports |
| `core/upgrade/` test fixtures | Tl1d, Reports |

## Out of Scope

- KSC Reports (kept)
- Ops Board / Vaadin dashlets framework (kept, only Charts dashlet removed)
- Enlinkd extraction (deferred to core decomposition phase)
- Statsd/Reportd coupling (Statsd already deleted in prior work)

## Verification

After each deletion: `./compile.pl -DskipTests` must succeed. After all 4: full test run on affected modules.
