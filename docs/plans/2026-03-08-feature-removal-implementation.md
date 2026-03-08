# Feature Removal Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove Tl1d, Charts, Device Config Backup, and Database Reports/Jasper to simplify the OpenNMS core for Delta-V.

**Architecture:** Sequential deletion of 4 features in order of complexity. Each deletion removes module declarations, Java code, config files, Karaf features, menu entries, and documentation. A compile check (`./compile.pl -DskipTests`) validates each deletion before proceeding.

**Tech Stack:** Java 17, Maven, OSGi/Karaf, Spring, JAXB, Vue 3, Liquibase

---

## Task 1: Delete Tl1d Daemon

**Files:**
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/tl1d/` (7 files)
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/tl1d/jmx/` (2 files)
- Delete: `opennms-services/src/main/resources/META-INF/opennms/applicationContext-tl1Daemon.xml`
- Delete: `opennms-services/src/test/java/org/opennms/netmgt/tl1d/` (test files)
- Delete: `opennms-config-model/src/main/java/org/opennms/netmgt/config/tl1d/` (3 files)
- Delete: `opennms-config-model/src/main/resources/xsds/tl1d-configuration.xsd`
- Delete: `opennms-config-model/src/test/java/org/opennms/netmgt/config/tl1d/`
- Delete: `opennms-dao-api/src/main/java/org/opennms/netmgt/dao/api/Tl1ConfigurationDao.java`
- Delete: `opennms-dao/src/main/java/org/opennms/netmgt/dao/jaxb/DefaultTl1ConfigurationDao.java`
- Delete: `opennms-base-assembly/src/main/filtered/etc/tl1d-configuration.xml`
- Delete: `opennms-base-assembly/src/main/filtered/etc/examples/tl1d-configuration.xml`
- Delete: `docs/modules/reference/pages/daemons/daemon-config-files/tl1d.adoc`
- Delete: `docs/modules/operation/pages/deep-dive/events/sources/tl1.adoc`
- Modify: `opennms-config-model/pom.xml:83` — remove `org.opennms.netmgt.config.tl1d.*` export
- Modify: `features/events/api/src/main/java/org/opennms/netmgt/events/api/EventConstants.java:503` — remove `TL1_AUTONOMOUS_MESSAGE_UEI`
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/scriptd/helper/SnmpTrapHelper.java` — remove `sendTL1AutonomousMsgTrap()` method
- Modify: `smoke-test/src/main/resources/opennms-overlay/etc/eventconf.xml:32` — remove tl1d events line
- Modify: `features/api-layer/core/src/test/resources/poller/eventconf.xml:31` — remove tl1d events line
- Modify: test eventconf files — remove tl1d event references

**Step 1: Delete Tl1d Java source and config files**

Delete these directories and files:
```
rm -rf opennms-services/src/main/java/org/opennms/netmgt/tl1d/
rm -rf opennms-services/src/test/java/org/opennms/netmgt/tl1d/
rm -f  opennms-services/src/main/resources/META-INF/opennms/applicationContext-tl1Daemon.xml
rm -rf opennms-config-model/src/main/java/org/opennms/netmgt/config/tl1d/
rm -rf opennms-config-model/src/test/java/org/opennms/netmgt/config/tl1d/
rm -f  opennms-config-model/src/main/resources/xsds/tl1d-configuration.xsd
rm -f  opennms-dao-api/src/main/java/org/opennms/netmgt/dao/api/Tl1ConfigurationDao.java
rm -f  opennms-dao/src/main/java/org/opennms/netmgt/dao/jaxb/DefaultTl1ConfigurationDao.java
rm -f  opennms-base-assembly/src/main/filtered/etc/tl1d-configuration.xml
rm -f  opennms-base-assembly/src/main/filtered/etc/examples/tl1d-configuration.xml
```

**Step 2: Remove Tl1d references from shared files**

In `opennms-config-model/pom.xml`, remove line 83:
```
org.opennms.netmgt.config.tl1d.*;version="${project.version}",
```

In `features/events/api/src/main/java/org/opennms/netmgt/events/api/EventConstants.java`, remove the `TL1_AUTONOMOUS_MESSAGE_UEI` constant.

In `opennms-services/src/main/java/org/opennms/netmgt/scriptd/helper/SnmpTrapHelper.java`, remove the `sendTL1AutonomousMsgTrap()` method and any `import` of Tl1 classes.

In `smoke-test/src/main/resources/opennms-overlay/etc/eventconf.xml`, remove:
```xml
<event-file>events/opennms.tl1d.events.xml</event-file>
```

In `features/api-layer/core/src/test/resources/poller/eventconf.xml`, remove the same line.

Search all other eventconf XML files for `tl1d` references and remove them.

**Step 3: Delete Tl1d documentation**

```
rm -f docs/modules/reference/pages/daemons/daemon-config-files/tl1d.adoc
rm -f docs/modules/operation/pages/deep-dive/events/sources/tl1.adoc
```

Remove references from `docs/modules/reference/nav.adoc` and `docs/modules/operation/nav.adoc` if they link to these pages.

**Step 4: Clean up upgrade test fixtures**

In `core/upgrade/src/test/resources/etc/service-configuration*.xml` files, remove the Tl1d service entry if present:
```xml
<service enabled="false">
  <name>OpenNMS:Name=Tl1d</name>
  <class-name>org.opennms.netmgt.tl1d.jmx.Tl1d</class-name>
  ...
</service>
```

Also update `core/upgrade/src/main/resources/default/service-configuration-14.0.0.xml`.

Update `core/upgrade/src/main/java/org/opennms/upgrade/implementations/EOLServiceConfigMigratorOffline.java` — add Tl1d to the list of EOL services if not already there, or remove the Tl1d reference entirely.

**Step 5: Compile and verify**

Run: `./compile.pl -DskipTests`
Expected: BUILD SUCCESS

If compilation fails, search for remaining `tl1d` or `Tl1` references:
```bash
grep -ri "tl1d\|Tl1d\|Tl1Client\|Tl1Message\|Tl1Autonomous\|Tl1ConfigurationDao" --include="*.java" --include="*.xml" --include="*.xsd" .
```
Fix any dangling references.

**Step 6: Commit**

```bash
git add -A
git commit -m "feat: delete Tl1d daemon entirely — legacy TL1 protocol support"
```

---

## Task 2: Delete Charts Feature

**Files:**
- Delete: `features/charts/` (entire module)
- Delete: `features/vaadin-dashlets/dashlet-charts/` (entire module)
- Delete: `opennms-config-model/src/main/java/org/opennms/netmgt/config/charts/` (17 classes)
- Delete: `opennms-config-model/src/main/resources/xsds/chart-configuration.xsd`
- Delete: `opennms-config-model/src/test/java/org/opennms/netmgt/config/charts/`
- Delete: `opennms-config/src/main/java/org/opennms/netmgt/config/ChartConfigFactory.java`
- Delete: `opennms-config/src/main/java/org/opennms/netmgt/config/ChartConfigManager.java`
- Delete: `opennms-webapp/src/main/java/org/opennms/web/graph/PurdyChartServlet.java`
- Delete: `opennms-webapp/src/main/webapp/charts/index.jsp`
- Delete: `opennms-base-assembly/src/main/filtered/etc/chart-configuration.xml`
- Delete: `docs/modules/operation/pages/deep-dive/visualizations/opsboard/dashlets/charts.adoc`
- Modify: `features/pom.xml:178` — remove `<module>charts</module>`
- Modify: `features/vaadin-dashlets/pom.xml` — remove `<module>dashlet-charts</module>`
- Modify: `container/features/src/main/resources/features.xml` — remove `dashlet-charts` feature
- Modify: `opennms-config-model/pom.xml` — remove charts package export
- Modify: `opennms-webapp/pom.xml` — remove charts dependency
- Modify: `opennms-webapp/src/main/webapp/WEB-INF/web.xml:892-899` — remove PurdyChartServlet
- Modify: `pom.xml:2730-2734` — remove charts dependency management
- Modify: 4 menu template JSON files — remove Charts entry

**Step 1: Delete Charts modules and source**

```
rm -rf features/charts/
rm -rf features/vaadin-dashlets/dashlet-charts/
rm -rf opennms-config-model/src/main/java/org/opennms/netmgt/config/charts/
rm -rf opennms-config-model/src/test/java/org/opennms/netmgt/config/charts/
rm -f  opennms-config-model/src/main/resources/xsds/chart-configuration.xsd
rm -f  opennms-config/src/main/java/org/opennms/netmgt/config/ChartConfigFactory.java
rm -f  opennms-config/src/main/java/org/opennms/netmgt/config/ChartConfigManager.java
rm -f  opennms-webapp/src/main/java/org/opennms/web/graph/PurdyChartServlet.java
rm -rf opennms-webapp/src/main/webapp/charts/
rm -f  opennms-base-assembly/src/main/filtered/etc/chart-configuration.xml
```

**Step 2: Remove Charts from parent POMs**

In `features/pom.xml`, remove line 178:
```xml
<module>charts</module>
```
(Also remove the comment on line 177: `<!-- JFreeChart charts -->`)

In `features/vaadin-dashlets/pom.xml`, remove:
```xml
<module>dashlet-charts</module>
```

In `pom.xml` (root), remove lines 2730-2734 (the charts dependency management block):
```xml
<dependency>
  <groupId>org.opennms.features</groupId>
  <artifactId>org.opennms.features.charts</artifactId>
  <version>${project.version}</version>
</dependency>
```

**Step 3: Remove Charts from Karaf features and webapp**

In `container/features/src/main/resources/features.xml`, remove the `dashlet-charts` feature definition (search for `dashlet-charts`).

In `opennms-webapp/pom.xml`, remove the dependency on `org.opennms.features.charts`.

In `opennms-webapp/src/main/webapp/WEB-INF/web.xml`, remove the PurdyChartServlet definition (lines 892-899) and its URL mapping.

In `opennms-config-model/pom.xml`, remove the charts package export line:
```
org.opennms.netmgt.config.charts.*;version="${project.version}",
```

**Step 4: Remove Charts from menu templates**

In all 4 menu template files under `opennms-webapp-rest/src/main/webapp/WEB-INF/menu/`:
- `menu-template.json`
- `menu-template-default.json`
- `menu-template-alt.json`
- `menu-template-legacy.json`

Remove the Charts JSON block:
```json
{
  "id": "charts",
  "name": "Charts",
  "url": "charts/index.jsp",
  "locationMatch": "chart",
  "roles": null
},
```

**Step 5: Clean up remaining references**

Remove Charts from smoke test if referenced:
In `smoke-test/src/test/java/org/opennms/smoketest/MenuHeaderIT.java`, remove the charts menu assertion (line ~75-76 referencing `include-charts`).

Remove Charts from `integration-tests/config/src/test/java/org/opennms/netmgt/config/WillItUnmarshalIT.java` if it tests `ChartConfiguration`.

Remove Charts from `core/test-api/karaf/pom.xml` if it references `dashlet-charts`.

Delete documentation:
```
rm -f docs/modules/operation/pages/deep-dive/visualizations/opsboard/dashlets/charts.adoc
```

Search for any remaining references:
```bash
grep -ri "ChartConfig\|PurdyChart\|dashlet-charts\|chart-configuration" --include="*.java" --include="*.xml" --include="*.json" .
```

**Step 6: Compile and verify**

Run: `./compile.pl -DskipTests`
Expected: BUILD SUCCESS

**Step 7: Commit**

```bash
git add -A
git commit -m "feat: delete Charts feature entirely — legacy JFreeChart bar charts"
```

---

## Task 3: Delete Device Config Backup

**Files:**
- Delete: `features/device-config/` (entire module tree, 17 sub-modules)
- Delete: `ui/src/containers/DeviceConfigBackup.vue`
- Delete: `ui/src/components/Device/DCB*.vue` (9 files)
- Delete: `ui/src/types/deviceConfig.d.ts`
- Delete: `ui/src/stores/deviceStore.ts`
- Delete: `ui/src/services/deviceService.ts`
- Delete: `ui/tests/deviceConfigBackup.test.ts`
- Delete: `opennms-base-assembly/src/main/filtered/etc/device-config/`
- Delete: `opennms-base-assembly/src/main/filtered/etc/examples/device-config/`
- Delete: `smoke-test/src/test/java/org/opennms/smoketest/dcb/`
- Delete: `smoke-test/src/test/resources/device-config/`
- Delete: `docs/modules/operation/pages/deep-dive/device-config-backup/` (entire directory)
- Delete: `docs/modules/development/pages/rest/device_config.adoc`
- Delete: `opennms-services/src/test/resources/META-INF/opennms/applicationContext-test-deviceConfig.xml`
- Delete: `opennms-services/src/test/resources/META-INF/opennms/applicationContext-deviceConfigDao.xml`
- Modify: `features/pom.xml:185` — remove `<module>device-config</module>`
- Modify: `container/features/src/main/resources/features.xml` — remove all `opennms-deviceconfig-*` features
- Modify: `ui/src/main/router/index.ts:25,188-202` — remove DCB import and route
- Create: Liquibase migration to drop `device_config` table

**Step 1: Delete Device Config Backup modules and config**

```
rm -rf features/device-config/
rm -f  opennms-services/src/test/resources/META-INF/opennms/applicationContext-test-deviceConfig.xml
rm -f  opennms-services/src/test/resources/META-INF/opennms/applicationContext-deviceConfigDao.xml
rm -rf opennms-base-assembly/src/main/filtered/etc/device-config/
rm -rf opennms-base-assembly/src/main/filtered/etc/examples/device-config/
```

**Step 2: Remove DCB from parent POM and Karaf features**

In `features/pom.xml`, remove line 185:
```xml
<module>device-config</module>
```

In `container/features/src/main/resources/features.xml`, remove all `opennms-deviceconfig-*` feature definitions. Search for `deviceconfig` and remove the entire block of features (~lines 1979-2034).

**Step 3: Delete Vue UI components**

```
rm -f  ui/src/containers/DeviceConfigBackup.vue
rm -f  ui/src/components/Device/DCBTable.vue
rm -f  ui/src/components/Device/DCBSearch.vue
rm -f  ui/src/components/Device/DCBGroupFilters.vue
rm -f  ui/src/components/Device/DCBTableStatusDropdown.vue
rm -f  ui/src/components/Device/DCBModal.vue
rm -f  ui/src/components/Device/DCBModalLastBackupContent.vue
rm -f  ui/src/components/Device/DCBModalConfigDiffContent.vue
rm -f  ui/src/components/Device/DCBModalViewHistoryContent.vue
rm -f  ui/src/components/Device/DCBDiff.vue
rm -f  ui/src/types/deviceConfig.d.ts
rm -f  ui/src/stores/deviceStore.ts
rm -f  ui/src/services/deviceService.ts
rm -f  ui/tests/deviceConfigBackup.test.ts
```

**Step 4: Remove DCB route from Vue router**

In `ui/src/main/router/index.ts`:
- Remove line 25: `import DeviceConfigBackup from '@/containers/DeviceConfigBackup.vue'`
- Remove lines 188-202: the route object for `/device-config-backup`

**Step 5: Add Liquibase migration to drop device_config table**

Create file `core/schema/src/main/liquibase/36.0.0/changelog.xml` (or append to existing):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-2.0.xsd">

  <changeSet author="delta-v" id="36.0.0-drop-device-config">
    <preConditions onFail="MARK_RAN">
      <tableExists tableName="device_config"/>
    </preConditions>
    <dropTable tableName="device_config" cascadeConstraints="true"/>
    <rollback/>
  </changeSet>

  <changeSet author="delta-v" id="36.0.0-drop-device-config-sequence">
    <preConditions onFail="MARK_RAN">
      <sequenceExists sequenceName="deviceconfignxtid"/>
    </preConditions>
    <dropSequence sequenceName="deviceconfignxtid"/>
    <rollback/>
  </changeSet>

</databaseChangeLog>
```

Add this changelog to the master `core/schema/src/main/liquibase/changelog.xml`:
```xml
<include file="36.0.0/changelog.xml" />
```

**Step 6: Delete smoke tests and documentation**

```
rm -rf smoke-test/src/test/java/org/opennms/smoketest/dcb/
rm -rf smoke-test/src/test/resources/device-config/
rm -rf docs/modules/operation/pages/deep-dive/device-config-backup/
rm -f  docs/modules/development/pages/rest/device_config.adoc
```

Remove DCB from `docs/modules/operation/nav.adoc` and `docs/modules/development/nav.adoc` if linked.

**Step 7: Clean up remaining references**

Search for dangling references:
```bash
grep -ri "device.config\|DeviceConfig\|deviceconfig\|DCB" --include="*.java" --include="*.xml" --include="*.ts" --include="*.vue" --include="*.json" . | grep -v node_modules | grep -v target | grep -v ".git"
```

Fix any remaining references. Pay attention to:
- `features/poller/api/src/main/java/org/opennms/netmgt/poller/DeviceConfig.java` — this is the poller's DeviceConfig class, NOT the DCB feature. **Keep this file.**
- Any `import` statements in test files

**Step 8: Compile and verify**

Run: `./compile.pl -DskipTests`
Expected: BUILD SUCCESS

Also verify the Vue UI builds:
```bash
cd ui && pnpm install && pnpm build
```

**Step 9: Commit**

```bash
git add -A
git commit -m "feat: delete Device Config Backup feature entirely"
```

---

## Task 4: Delete Database Reports / Jasper / Availability Reports

This is the largest deletion. Work methodically.

**Files:**
- Delete: `features/reporting/` (entire module tree, 10+ sub-modules)
- Delete: `opennms-enterprise-reporting/opennms-reportd/`
- Delete: `dependencies/jasper/`
- Delete: `integrations/opennms-jasper-extensions/`
- Delete: `integrations/opennms-jasperstudio-extension/`
- Delete: config model classes for reportd/reporting from `opennms-config-model`
- Delete: DAO interfaces from `opennms-dao-api`
- Delete: DAO impls from `opennms-dao`
- Delete: all JRXML templates from `opennms-base-assembly`
- Delete: report config files from `opennms-base-assembly`
- Delete: report web controllers, JSPs from `opennms-webapp`
- Delete: report REST endpoints from `opennms-webapp-rest`
- Delete: report JS app from `core/web-assets`
- Delete: report images from `core/web-assets`
- Delete: documentation
- Modify: multiple parent POMs, features.xml, menu templates
- Create: Liquibase migration to drop report tables

**Step 1: Delete reporting module trees**

```
rm -rf features/reporting/
rm -rf opennms-enterprise-reporting/opennms-reportd/
rm -rf dependencies/jasper/
rm -rf integrations/opennms-jasper-extensions/
rm -rf integrations/opennms-jasperstudio-extension/
```

**Step 2: Remove from parent POMs**

In `features/pom.xml`, remove line 69:
```xml
<module>reporting</module>
```

In `dependencies/pom.xml`, remove line 29:
```xml
<module>jasper</module>
```

In `integrations/pom.xml`, remove lines 20-21:
```xml
<module>opennms-jasper-extensions</module>
<module>opennms-jasperstudio-extension</module>
```

In `opennms-enterprise-reporting/pom.xml`, remove line 13:
```xml
<module>opennms-reportd</module>
```
If `opennms-reportd` is the only module, delete the entire `opennms-enterprise-reporting/` directory instead.

In `pom.xml` (root), remove:
- Line 123: `<module>opennms-enterprise-reporting</module>` (if the directory is deleted)
- Lines 2896-2933: all reporting dependency management entries

**Step 3: Delete config model classes and schemas**

```
rm -rf opennms-config-model/src/main/java/org/opennms/netmgt/config/reportd/
rm -rf opennms-config-model/src/main/java/org/opennms/netmgt/config/reporting/
rm -f  opennms-config-model/src/main/resources/xsds/jasper-reports.xsd
rm -f  opennms-config-model/src/main/resources/xsds/reportd-configuration.xsd
rm -f  opennms-config-model/src/main/resources/xsds/reporting.xsd
```

Remove corresponding test classes if they exist.

In `opennms-config-model/pom.xml`, remove the package exports for:
```
org.opennms.netmgt.config.reportd.*
org.opennms.netmgt.config.reporting.*
```

**Step 4: Delete DAO interfaces and implementations**

From `opennms-dao-api/src/main/java/org/opennms/netmgt/dao/api/`, delete:
```
rm -f DatabaseReportConfigDao.java
rm -f JasperReportConfigDao.java
rm -f OnmsReportConfigDao.java
rm -f ReportCatalogDao.java
rm -f ReportdConfigurationDao.java
rm -f StatisticsReportDao.java
rm -f StatisticsReportDataDao.java
```

From `opennms-dao/`, delete:
```
rm -f opennms-dao/src/main/java/org/opennms/netmgt/dao/jaxb/DefaultDatabaseReportConfigDao.java
```

Search for and remove any other report DAO implementations in `opennms-dao/`.

**Step 5: Delete config factories**

Search for report-related config factories in `opennms-config/`:
```bash
grep -rl "Reportd\|DatabaseReport\|JasperReport\|StatisticsReport" opennms-config/src/main/java/ --include="*.java"
```
Delete any found files (e.g., `ReportdConfigFactory`, `DatabaseReportConfigFactory`).

**Step 6: Delete assembly config files and templates**

```
rm -rf opennms-base-assembly/src/main/filtered/etc/report-templates/
rm -f  opennms-base-assembly/src/main/filtered/etc/jasper-reports.xml
rm -f  opennms-base-assembly/src/main/filtered/etc/database-reports.xml
rm -f  opennms-base-assembly/src/main/filtered/etc/reportd-configuration.xml
rm -f  opennms-base-assembly/src/main/filtered/etc/availability-reports.xml
```

**Step 7: Remove from Karaf features**

In `container/features/src/main/resources/features.xml`, remove the `opennms-reporting` feature definition. Search for `opennms-reporting` and remove the entire feature block.

**Step 8: Delete webapp report controllers, JSPs, and REST**

Search and delete report-related controllers:
```bash
find opennms-webapp/src/main/java -path "*report*" -o -path "*statisticsReport*" | grep -v "package-info"
```

Delete found controller classes. Also delete:
```
rm -rf opennms-webapp/src/main/webapp/WEB-INF/jsp/statisticsReports/
rm -f  opennms-webapp/src/main/webapp/errors/statisticsreportidnotfound.jsp
```

In `opennms-webapp/pom.xml`, remove any dependency on reporting modules.

In `opennms-webapp-rest/`, search for and remove report REST services:
```bash
grep -rl "KscRestService\|ReportRestService\|StatisticsReport" opennms-webapp-rest/src/main/java/ --include="*.java"
```

Note: **Keep KSC REST services** (`KscRestService.java`) — KSC Reports are out of scope for this removal.

Search dispatcher-servlet.xml for report-related bean definitions and remove them.

**Step 9: Delete report web assets**

```
rm -rf core/web-assets/src/main/assets/js/apps/onms-reports/
rm -f  core/web-assets/src/main/assets/images/reportDeliver.png
rm -f  core/web-assets/src/main/assets/images/reportDeliver_grey.png
rm -f  core/web-assets/src/main/assets/images/reportOnline.png
rm -f  core/web-assets/src/main/assets/images/reportOnline_grey.png
rm -f  core/web-assets/src/main/assets/images/reportSchedule.png
rm -f  core/web-assets/src/main/assets/images/reportSchedule_grey.png
```

Check `core/web-assets/` webpack/build config for references to the removed JS app and remove them.

**Step 10: Remove Database Reports from menu templates**

In all 4 menu template files under `opennms-webapp-rest/src/main/webapp/WEB-INF/menu/`:
- `menu-template.json`
- `menu-template-default.json`
- `menu-template-alt.json`
- `menu-template-legacy.json`

Remove the Database Reports JSON block:
```json
{
  "id": "databaseReports",
  "name": "Database Reports",
  "url": "report/database/index.jsp",
  "locationMatch": "database-reports",
  "roles": null
},
```

**Do NOT remove the KSC Reports entry** — it stays.

**Step 11: Add Liquibase migration to drop report tables**

Add to `core/schema/src/main/liquibase/36.0.0/changelog.xml` (created in Task 3, or create if it doesn't exist):

```xml
<changeSet author="delta-v" id="36.0.0-drop-statisticsreport">
  <preConditions onFail="MARK_RAN">
    <tableExists tableName="statisticsreport"/>
  </preConditions>
  <dropTable tableName="statisticsreportdata" cascadeConstraints="true"/>
  <dropTable tableName="statisticsreport" cascadeConstraints="true"/>
  <rollback/>
</changeSet>

<changeSet author="delta-v" id="36.0.0-drop-reportcatalog">
  <preConditions onFail="MARK_RAN">
    <tableExists tableName="reportcatalog"/>
  </preConditions>
  <dropTable tableName="reportlocator" cascadeConstraints="true"/>
  <dropTable tableName="reportcatalog" cascadeConstraints="true"/>
  <rollback/>
</changeSet>
```

**Step 12: Delete documentation**

```
rm -rf docs/modules/operation/pages/deep-dive/database-reports/
rm -f  docs/modules/development/pages/reporting/jasperreport-styleguide.adoc
rm -f  docs/modules/reference/pages/daemons/daemon-config-files/reportd.adoc
```

Remove references from nav.adoc files.

**Step 13: Clean up remaining references**

This is the most critical step. Search broadly:
```bash
grep -ri "jasper\|reportd\|JasperReport\|DatabaseReport\|StatisticsReport\|ReportCatalog\|availability-reports\|database-reports\|jasper-reports" --include="*.java" --include="*.xml" --include="*.json" --include="*.cfg" . | grep -v target | grep -v node_modules | grep -v ".git" | grep -v "docs/"
```

Common places where references linger:
- `opennms-dao/src/main/resources/META-INF/opennms/applicationContext-dao.xml` — report DAO beans
- `opennms-webapp/src/main/webapp/WEB-INF/dispatcher-servlet.xml` — report controller beans
- `opennms-webapp-rest/src/test/resources/dispatcher-servlet.xml` — test report beans
- `opennms-webapp/pom.xml` — jasper/reporting dependencies
- `opennms-config/` — reporting config references
- `core/test-api/` — test data files referencing reports
- `integration-tests/config/.../WillItUnmarshalIT.java` — unmarshaling tests for report configs
- `features/search/providers/` — report search providers (keep KSC, remove others)
- `core/web-assets/webpack.config.js` or similar — report JS app entry point
- `opennms-base-assembly/src/main/filtered/etc/` — any remaining report configs

Fix ALL dangling references. This step will likely require multiple iterations.

**Step 14: Compile and verify**

Run: `./compile.pl -DskipTests`
Expected: BUILD SUCCESS

If fails, the error message will tell you exactly which file has a dangling import or reference. Fix and re-compile.

**Step 15: Commit**

```bash
git add -A
git commit -m "feat: delete Database Reports, Jasper Reports, and Availability Reports entirely"
```

---

## Task 5: Final Verification

**Step 1: Full compile**

Run: `./compile.pl -DskipTests`
Expected: BUILD SUCCESS

**Step 2: Verify no dangling references**

```bash
grep -ri "tl1d\|PurdyChart\|ChartConfig\|dashlet-charts\|deviceconfig\|device-config\|jasperreport\|reportd\|database-reports\|jasper-reports\|availability-reports" --include="*.java" --include="*.xml" --include="*.json" . | grep -v target | grep -v node_modules | grep -v ".git" | grep -v docs/plans
```

Expected: No matches (or only legitimate references like `DeviceConfig` in the poller API).

**Step 3: Verify Vue UI builds**

```bash
cd ui && pnpm install && pnpm build
```

**Step 4: Push and update PR**

```bash
git push delta-v eventbus-redesign
```
