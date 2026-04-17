/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.config.upgrade.datacollection;

import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrates SNMP data collection XML configuration from filesystem
 * (datacollection-config.xml + datacollection/*.xml) into the database tables
 * created by the 36.0.0 Liquibase schema changesets.
 *
 * Called from the OpenNMS upgrade framework via
 * {@code SnmpDataCollectionDbMigratorOffline} after Liquibase schema creation
 * and after older upgrade tasks (e.g. DataCollectionConfigMigratorOffline)
 * have already patched the XML files.
 *
 * Uses an idempotency check (SELECT COUNT(*) FROM snmp_collection_sources = 0)
 * so it is safe to call on every upgrade.
 */
public class SnmpDataCollectionMigration {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpDataCollectionMigration.class);

    private static final String DATACOLLECTION_CONFIG_FILE = "datacollection-config.xml";
    private static final String DATACOLLECTION_DIR = "datacollection";
    private static final String ARCHIVE_DIR = "etc_archive";
    private static final String MIGRATION_USER = "system-migration";
    private static final String INLINE_SOURCE_PREFIX = "__inline_";

    private boolean migrated = false;

    /**
     * Run the migration using the given JDBC connection.
     * Safe to call multiple times — skips if data already exists.
     * @return true if data was actually imported
     */
    public boolean execute(final Connection conn) throws SQLException {
        if (!shouldRun(conn)) {
            LOG.info("SNMP data collection tables already populated or not yet created — skipping XML migration.");
            return false;
        }

        final File etcDir = resolveEtcDirectory();
        final File configFile = new File(etcDir, DATACOLLECTION_CONFIG_FILE);

        if (!configFile.exists()) {
            LOG.info("No {} found at {} — fresh install, skipping SNMP data collection migration.",
                    DATACOLLECTION_CONFIG_FILE, configFile.getAbsolutePath());
            return false;
        }

        LOG.info("Starting SNMP data collection XML-to-database migration from {}", etcDir.getAbsolutePath());

        try {
            // Parse the main config
            final DatacollectionConfig config = unmarshal(DatacollectionConfig.class, configFile);

            // Parse all datacollection group XML files
            final Map<String, DatacollectionGroup> groupsByName = parseDatacollectionGroups(etcDir);

            // Migrate sources (datacollection groups)
            int sourceCount = 0;
            int mibGroupCount = 0;
            int resourceTypeCount = 0;
            int systemDefCount = 0;

            for (final Map.Entry<String, DatacollectionGroup> entry : groupsByName.entrySet()) {
                final String groupName = entry.getKey();
                final DatacollectionGroup group = entry.getValue();

                final int sourceId = SnmpDataCollectionSqlHelper.insertSource(
                        conn, groupName, group.getName(), null, MIGRATION_USER);

                mibGroupCount += SnmpDataCollectionSqlHelper.batchInsertMibGroups(
                        conn, sourceId, group.getGroups());
                resourceTypeCount += SnmpDataCollectionSqlHelper.batchInsertResourceTypes(
                        conn, sourceId, group.getResourceTypes());
                systemDefCount += SnmpDataCollectionSqlHelper.batchInsertSystemDefs(
                        conn, sourceId, group.getSystemDefs());

                sourceCount++;
            }

            // Migrate profiles (snmp-collections)
            int profileCount = 0;
            for (final SnmpCollection snmpCollection : config.getSnmpCollections()) {
                migrateSnmpCollection(conn, snmpCollection, groupsByName);
                profileCount++;
            }

            LOG.info("SNMP data collection migration complete: {} profiles, {} sources, " +
                            "{} MIB groups, {} resource types, {} system definitions.",
                    profileCount, sourceCount, mibGroupCount, resourceTypeCount, systemDefCount);

            // Archival happens after commit in UpgradeConfigService

        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("SNMP data collection migration failed", e);
        }
        migrated = true;
        return true;
    }

    /**
     * Check if migration should run: tables exist and are empty.
     */
    private boolean shouldRun(final Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM snmp_collection_sources");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            // Table doesn't exist yet — schema not applied
            LOG.debug("snmp_collection_sources table not accessible — migration will be skipped: {}",
                    e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * Migrate a single snmp-collection element to a profile row.
     */
    private void migrateSnmpCollection(final Connection conn,
                                       final SnmpCollection snmpCollection,
                                       final Map<String, DatacollectionGroup> groupsByName) throws SQLException {

        final List<String> sourceNames = new ArrayList<>();

        // Process include-collection references
        for (final IncludeCollection include : snmpCollection.getIncludeCollections()) {
            if (include.getDataCollectionGroup() != null && !include.getDataCollectionGroup().isEmpty()) {
                final String groupName = include.getDataCollectionGroup();
                if (include.getExcludeFilters() != null && !include.getExcludeFilters().isEmpty()) {
                    LOG.warn("snmp-collection '{}': exclude-filter on include-collection for '{}' " +
                            "is not supported in DB migration — importing full group.",
                            snmpCollection.getName(), groupName);
                }
                sourceNames.add(groupName);
            } else if (include.getSystemDef() != null && !include.getSystemDef().isEmpty()) {
                LOG.warn("snmp-collection '{}': systemDef-targeted include-collection '{}' " +
                        "is not supported in DB migration — skipping.",
                        snmpCollection.getName(), include.getSystemDef());
            }
        }

        // Handle inline definitions (groups/systems/resourceTypes directly in snmp-collection)
        if (hasInlineDefinitions(snmpCollection)) {
            final String inlineSourceName = INLINE_SOURCE_PREFIX + snmpCollection.getName();
            migrateInlineDefinitions(conn, snmpCollection, inlineSourceName);
            sourceNames.add(inlineSourceName);
        }

        final String sourceNamesJson = DataCollectionGroupMapper.mapSourceNames(sourceNames);
        final String rrdRrasJson = DataCollectionGroupMapper.mapRras(snmpCollection.getRrd());
        final int rrdStep = DataCollectionGroupMapper.mapRrdStep(snmpCollection);

        SnmpDataCollectionSqlHelper.insertProfile(conn,
                snmpCollection.getName(),
                rrdStep,
                rrdRrasJson,
                snmpCollection.getSnmpStorageFlag(),
                sourceNamesJson,
                snmpCollection.getMaxVarsPerPdu());
    }

    /**
     * Check if an SnmpCollection has inline group/system/resourceType definitions.
     */
    private boolean hasInlineDefinitions(final SnmpCollection coll) {
        if (coll.getGroups() != null && coll.getGroups().getGroups() != null
                && !coll.getGroups().getGroups().isEmpty()) {
            return true;
        }
        if (coll.getSystems() != null && coll.getSystems().getSystemDefs() != null
                && !coll.getSystems().getSystemDefs().isEmpty()) {
            return true;
        }
        return coll.getResourceTypes() != null && !coll.getResourceTypes().isEmpty();
    }

    /**
     * Extract inline definitions from an SnmpCollection into a synthetic source.
     */
    private void migrateInlineDefinitions(final Connection conn,
                                          final SnmpCollection coll,
                                          final String sourceName) throws SQLException {

        LOG.info("Extracting inline definitions from snmp-collection '{}' to source '{}'",
                coll.getName(), sourceName);

        final int sourceId = SnmpDataCollectionSqlHelper.insertSource(
                conn, sourceName, null, "Inline definitions from snmp-collection " + coll.getName(),
                MIGRATION_USER);

        // Inline groups
        final List<Group> inlineGroups = (coll.getGroups() != null && coll.getGroups().getGroups() != null)
                ? coll.getGroups().getGroups()
                : Collections.emptyList();
        SnmpDataCollectionSqlHelper.batchInsertMibGroups(conn, sourceId, inlineGroups);

        // Inline system defs
        final List<SystemDef> inlineSystemDefs = (coll.getSystems() != null
                && coll.getSystems().getSystemDefs() != null)
                ? coll.getSystems().getSystemDefs()
                : Collections.emptyList();
        SnmpDataCollectionSqlHelper.batchInsertSystemDefs(conn, sourceId, inlineSystemDefs);

        // Inline resource types
        final List<ResourceType> inlineResourceTypes = coll.getResourceTypes() != null
                ? coll.getResourceTypes()
                : Collections.emptyList();
        SnmpDataCollectionSqlHelper.batchInsertResourceTypes(conn, sourceId, inlineResourceTypes);
    }

    /**
     * Parse all datacollection group XML files from etc/datacollection/.
     */
    private Map<String, DatacollectionGroup> parseDatacollectionGroups(final File etcDir) {
        final File dcDir = new File(etcDir, DATACOLLECTION_DIR);
        if (!dcDir.exists() || !dcDir.isDirectory()) {
            LOG.info("No {} directory found — no datacollection groups to migrate.", dcDir.getAbsolutePath());
            return Collections.emptyMap();
        }

        final File[] xmlFiles = dcDir.listFiles((dir, name) -> name.endsWith(".xml"));
        if (xmlFiles == null || xmlFiles.length == 0) {
            LOG.info("No XML files found in {}", dcDir.getAbsolutePath());
            return Collections.emptyMap();
        }

        final Map<String, DatacollectionGroup> groups = new LinkedHashMap<>();
        for (final File xmlFile : xmlFiles) {
            try {
                final DatacollectionGroup group = unmarshal(DatacollectionGroup.class, xmlFile);
                if (group.getName() != null) {
                    groups.put(group.getName(), group);
                    LOG.debug("Parsed datacollection group '{}' from {}", group.getName(), xmlFile.getName());
                } else {
                    LOG.warn("Datacollection group in {} has no name — skipping.", xmlFile.getName());
                }
            } catch (Exception e) {
                LOG.error("Failed to parse datacollection group file {}: {} — skipping.",
                        xmlFile.getName(), e.getMessage(), e);
            }
        }

        LOG.info("Parsed {} datacollection group files from {}", groups.size(), dcDir.getAbsolutePath());
        return groups;
    }

    private static final String DATACOLLECTION_NAMESPACE = "http://xmlns.opennms.org/xsd/config/datacollection";

    /**
     * Unmarshal an XML file to a JAXB object.
     * Uses a SAX filter to inject the expected namespace for XML files that omit it.
     * External entities and DOCTYPE declarations are disabled to prevent XXE attacks.
     */
    <T> T unmarshal(final Class<T> clazz, final File file) throws SQLException {
        try (final FileInputStream fis = new FileInputStream(file)) {
            final JAXBContext ctx = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = ctx.createUnmarshaller();

            final XMLReader reader = createHardenedXmlReader();
            final NamespaceFilter filter = new NamespaceFilter(DATACOLLECTION_NAMESPACE);
            filter.setParent(reader);

            final InputSource is = new InputSource(fis);
            final SAXSource source = new SAXSource(filter, is);

            @SuppressWarnings("unchecked")
            final T result = (T) unmarshaller.unmarshal(source);
            return result;
        } catch (JAXBException | SAXException | ParserConfigurationException | java.io.IOException e) {
            throw new SQLException("Failed to unmarshal " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Build a SAX XMLReader with XXE-related features disabled:
     * no DOCTYPE, no external entities, no external DTD loading.
     */
    private static XMLReader createHardenedXmlReader() throws SAXException, ParserConfigurationException {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setXIncludeAware(false);
        spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return spf.newSAXParser().getXMLReader();
    }

    /**
     * SAX filter that injects a default namespace on elements that have no namespace.
     * This allows parsing XML files that omit the xmlns declaration.
     */
    private static class NamespaceFilter extends XMLFilterImpl {
        private final String defaultNamespace;

        NamespaceFilter(final String defaultNamespace) {
            this.defaultNamespace = defaultNamespace;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (uri == null || uri.isEmpty()) {
                uri = defaultNamespace;
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (uri == null || uri.isEmpty()) {
                uri = defaultNamespace;
            }
            super.endElement(uri, localName, qName);
        }
    }

    /**
     * Archive original XML files after successful migration and commit.
     * Should be called after the DB transaction is committed.
     */
    public void archiveFiles() {
        archiveOriginalFiles(resolveEtcDirectory());
    }

    /**
     * Resolve the OpenNMS etc directory from system property or default.
     */
    private File resolveEtcDirectory() {
        final String opennmsHome = System.getProperty("opennms.home", "/opt/opennms");
        return new File(opennmsHome, "etc");
    }

    /**
     * Archive original XML files to etc_archive/.
     */
    private void archiveOriginalFiles(final File etcDir) {
        final Path etcPath = etcDir.toPath();
        final Path archivePath = etcPath.getParent().resolve(ARCHIVE_DIR);

        try {
            Files.createDirectories(archivePath);

            final Path configSource = etcPath.resolve(DATACOLLECTION_CONFIG_FILE);
            if (Files.exists(configSource)) {
                Files.move(configSource, archivePath.resolve(DATACOLLECTION_CONFIG_FILE),
                        StandardCopyOption.REPLACE_EXISTING);
                LOG.info("Archived {} to {}", configSource, archivePath.resolve(DATACOLLECTION_CONFIG_FILE));
            }

            final Path dcDirSource = etcPath.resolve(DATACOLLECTION_DIR);
            if (Files.exists(dcDirSource) && Files.isDirectory(dcDirSource)) {
                final Path dcDirTarget = archivePath.resolve(DATACOLLECTION_DIR);
                moveDirectory(dcDirSource, dcDirTarget);
                LOG.info("Archived {} to {}", dcDirSource, dcDirTarget);
            }
        } catch (IOException e) {
            LOG.warn("Failed to archive datacollection files: {}", e.getMessage(), e);
        }
    }

    private void moveDirectory(final Path source, final Path target) throws IOException {
        Files.createDirectories(target);
        try (var stream = Files.list(source)) {
            stream.forEach(child -> {
                try {
                    final Path dest = target.resolve(source.relativize(child));
                    if (Files.isDirectory(child)) {
                        moveDirectory(child, dest);
                    } else {
                        Files.move(child, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    LOG.warn("Failed to move {}: {}", child, e.getMessage());
                }
            });
        }
        try {
            Files.deleteIfExists(source);
        } catch (IOException e) {
            LOG.warn("Failed to remove directory {}: {}", source, e.getMessage());
        }
    }
}
