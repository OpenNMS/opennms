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
package org.opennms.netmgt.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.config.api.ConfigReloadContainer;
import org.opennms.core.spring.FileReloadContainer;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.api.DataCollectionConfigLookupUtils;
import org.opennms.netmgt.config.datacollection.DataCollectionGroups;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.Groups;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.config.datacollection.Systems;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultDataCollectionConfigDao
 * 
 * <p>This class is the main repository for SNMP data collection configuration
 * information used by the SNMP service monitor. When this class is loaded it
 * reads the SNNMP data collection configuration into memory.</p>
 *
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultDataCollectionConfigDao extends AbstractJaxbConfigDao<DatacollectionConfig, DatacollectionConfig> implements DataCollectionConfigDao {
    
    public static final Logger LOG = LoggerFactory.getLogger(DefaultDataCollectionConfigDao.class);
    
    private String m_configDirectory;

    private volatile List<String> dataCollectionGroups = List.of();
    private volatile Map<String, ResourceType> resourceTypes = Map.of();
    private ConfigReloadContainer<DataCollectionGroups> m_extContainer;

    private volatile DatacollectionConfig dbConfig;
    private volatile Date lastDbUpdate = new Date();

    private static final String DEFAULT_RRD_REPOSITORY =
            System.getProperty("rrd.base.dir", "/opt/opennms/share/rrd") + "/snmp/";

    public DefaultDataCollectionConfigDao() {
        super(DatacollectionConfig.class, "data-collection");
        initExtensions();
    }

    /**
     * Try to load from XML. If the XML file doesn't exist (archived after DB migration),
     * start with an empty config that will be populated via {@link #loadFromDatabase}.
     */
    @Override
    public void afterPropertiesSet() {
        if (getConfigResource() != null && getConfigResource().exists()) {
            // XML file exists: load from it (tests and pre-migration)
            super.afterPropertiesSet();
            return;
        }
        if (getConfigResource() != null) {
            LOG.info("Datacollection XML config not found (archived after DB migration) — will load from DB.");
        }
        // DB-backed: start empty, persistence service will populate via loadFromDatabase()
        final DatacollectionConfig emptyConfig = new DatacollectionConfig();
        emptyConfig.setRrdRepository(DEFAULT_RRD_REPOSITORY);
        this.dbConfig = emptyConfig;
        LOG.info("DataCollectionConfigDao initialized with empty config — awaiting DB config load.");
    }

    @Override
    protected DatacollectionConfig translateConfig(final DatacollectionConfig config) {
        final DataCollectionConfigParser parser = new DataCollectionConfigParser(getConfigDirectory());
        final Map<String, ResourceType> localResourceTypes = new HashMap<>();

        Map<String,DatacollectionGroup> externalGroupMap = parser.loadExternalGroupMap();
        // Create a special collection to hold all resource types, because they should be defined only once.
        final SnmpCollection resourceTypeCollection = new SnmpCollection();
        resourceTypeCollection.setName("__resource_type_collection");

        // Load data collection groups from container.
        DataCollectionGroups dataCollectionGroupObj = m_extContainer.getObject();
        if (dataCollectionGroupObj != null) {
            // Add data collection groups loaded from container to external group map.
            dataCollectionGroupObj.getSnmpCollectionNames().forEach(collectionName -> {
                List<DatacollectionGroup> datacollectionGroupList = dataCollectionGroupObj.getDataCollectionGroup(collectionName);
                datacollectionGroupList.stream().forEach(group -> externalGroupMap.put(group.getName(), group));
            });
        }

        // Updating Configured Collections
        for (final SnmpCollection collection : config.getSnmpCollections()) {
            if(dataCollectionGroupObj != null) {
                // Set include-collection for the specific collection so that parseCollection will load all resources.
                Set<String> collectionNames = dataCollectionGroupObj.getSnmpCollectionNames();
                if (collectionNames.contains(collection.getName())) {
                    List<DatacollectionGroup> datacollectionGroupList = dataCollectionGroupObj.getDataCollectionGroup(collection.getName());
                    datacollectionGroupList.stream().forEach(datacollectionGroup -> {
                        IncludeCollection includeCollection = new IncludeCollection();
                        includeCollection.setDataCollectionGroup(datacollectionGroup.getName());
                        collection.addIncludeCollection(includeCollection);
                    });
                }
            }
            parser.parseCollection(collection);
            // Save local resource types
            for (final ResourceType rt : collection.getResourceTypes()) {
                resourceTypeCollection.addResourceType(rt);
                localResourceTypes.put(rt.getName(), rt);
            }

            // Remove local resource types
            collection.setResourceTypes(new ArrayList<ResourceType>());
            // Save external Resource Types
            for (IncludeCollection include : collection.getIncludeCollections()) {
                if (include.getDataCollectionGroup() != null) {
                    DatacollectionGroup group = externalGroupMap.get(include.getDataCollectionGroup());
                    for (final ResourceType rt : group.getResourceTypes()) {
                        resourceTypeCollection.addResourceType(rt);
                        localResourceTypes.put(rt.getName(), rt);
                    }
                }
            }
        }

        resourceTypeCollection.setGroups(new Groups());
        resourceTypeCollection.setSystems(new Systems());
        config.insertSnmpCollection(resourceTypeCollection);

        // Atomic publish — readers see either the old snapshot or the new one, never half-built state.
        this.resourceTypes = Map.copyOf(localResourceTypes);
        this.dataCollectionGroups = List.copyOf(externalGroupMap.keySet());

        DataCollectionConfigLookupUtils.validateResourceTypes(config.getSnmpCollections(), localResourceTypes.keySet());

        return config;
    }

    public void setConfigDirectory(String configDirectory) {
        this.m_configDirectory = configDirectory;
    }

    public String getConfigDirectory() {
        if (m_configDirectory == null) {
            final StringBuilder sb = new StringBuilder(ConfigFileConstants.getHome());
            sb.append(File.separator);
            sb.append("etc");
            sb.append(File.separator);
            sb.append("datacollection");
            sb.append(File.separator);
            m_configDirectory = sb.toString();
        }
        return m_configDirectory;
    }

    @Override
    public String getSnmpStorageFlag(final String collectionName) {
        final SnmpCollection collection = getSnmpCollection(collectionName);
        return collection == null ? null : collection.getSnmpStorageFlag();
    }

    @Override
    public List<MibObject> getMibObjectList(final String cName, final String aSysoid, final String anAddress, final int ifType) {
        LOG.debug("getMibObjectList: collection: {} sysoid: {} address: {} ifType: {}", cName, aSysoid, anAddress, ifType);

        if (aSysoid == null) {
            LOG.debug("getMibObjectList: aSysoid parameter is NULL...");
            return new ArrayList<>();
        }

        final SnmpCollection collection = getSnmpCollection(cName);
        if (collection == null) {
            return Collections.emptyList();
        }

        final Systems systems = collection.getSystems();
        if (systems == null) {
            return Collections.emptyList();
        }

        final List<SystemDef> systemList = new ArrayList<>();
        for (final SystemDef system : systems.getSystemDefs()) {
            if (DataCollectionConfigLookupUtils.systemDefMatches(system, aSysoid, anAddress)) {
                LOG.debug("getMibObjectList: MATCH!! adding system '{}'", system.getName());
                systemList.add(system);
            }
        }

        final Map<String, Group> groupMap = DataCollectionConfigLookupUtils.buildCollectionGroupMap(getActiveConfig()).get(cName);
        final List<MibObject> mibObjectList = new ArrayList<>();
        if (groupMap != null) {
            for (final SystemDef system : systemList) {
                for (final String grpName : system.getCollect().getIncludeGroups()) {
                    DataCollectionConfigLookupUtils.processGroupName(groupMap, grpName, ifType, mibObjectList, resourceTypes);
                }
            }
        }

        return mibObjectList;
    }

    @Override
    public List<MibObjProperty> getMibObjProperties(final String cName, final String aSysoid, final String anAddress) {
        LOG.debug("getMibObjProperties: collection: {} sysoid: {} address: {}", cName, aSysoid, anAddress);

        if (aSysoid == null) {
            LOG.debug("getMibObjProperties: aSysoid parameter is NULL...");
            return new ArrayList<>();
        }

        final SnmpCollection collection = getSnmpCollection(cName);
        if (collection == null) {
            return Collections.emptyList();
        }

        final Systems systems = collection.getSystems();
        if (systems == null) {
            return Collections.emptyList();
        }

        final List<SystemDef> systemList = new ArrayList<>();
        for (final SystemDef system : systems.getSystemDefs()) {
            if (DataCollectionConfigLookupUtils.systemDefMatches(system, aSysoid, anAddress)) {
                systemList.add(system);
            }
        }

        final Map<String, Group> groupMap = DataCollectionConfigLookupUtils.buildCollectionGroupMap(getActiveConfig()).get(cName);
        final List<MibObjProperty> mibProperties = new ArrayList<>();
        if (groupMap != null) {
            for (final SystemDef system : systemList) {
                for (final String grpName : system.getCollect().getIncludeGroups()) {
                    DataCollectionConfigLookupUtils.processGroupForProperties(groupMap, grpName, mibProperties);
                }
            }
        }

        return mibProperties;
    }

    @Override
    public Map<String, ResourceType> getConfiguredResourceTypes() {
        return Collections.unmodifiableMap(resourceTypes);
    }

    @Override
    public RrdRepository getRrdRepository(final String collectionName) {
        final RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File(getRrdPath()));
        repo.setRraList(getRRAList(collectionName));
        repo.setStep(getStep(collectionName));
        repo.setHeartBeat((2 * getStep(collectionName)));
        return repo;
    }

    @Override
    public int getStep(final String collectionName) {
        final SnmpCollection collection = getSnmpCollection(collectionName);
        return collection == null ? -1 : collection.getRrd().getStep();
    }

    @Override
    public List<String> getRRAList(final String collectionName) {
        final SnmpCollection collection = getSnmpCollection(collectionName);
        return collection == null ? null : collection.getRrd().getRras();
    }

    @Override
    public String getRrdPath() {
        final String rrdPath = getActiveConfig().getRrdRepository();
        if (rrdPath == null) {
            throw new RuntimeException("Configuration error, failed to retrieve path to RRD repository.");
        }

        /*
         * TODO: make a path utils class that has the below in it strip the
         * File.separator char off of the end of the path.
         */
        if (rrdPath.endsWith(File.separator)) {
            return rrdPath.substring(0, (rrdPath.length() - File.separator.length()));
        }
        return rrdPath;
    }

    /* Private Methods */

    private SnmpCollection getSnmpCollection(final String collectionName) {
        return DataCollectionConfigLookupUtils.findSnmpCollection(getActiveConfig(), collectionName);
    }

    @Override
    public DatacollectionConfig getRootDataCollection() {
        return getActiveConfig();
    }
    
    @Override
    public List<String> getAvailableDataCollectionGroups() {
        return dataCollectionGroups;
    }

    @Override
    public List<String> getAvailableSystemDefs() {
        List<String> systemDefs = new ArrayList<>();
        for (final SnmpCollection collection : getActiveConfig().getSnmpCollections()) {
            if (collection.getSystems() != null) {
                for (final SystemDef systemDef : collection.getSystems().getSystemDefs()) {
                    systemDefs.add(systemDef.getName());
                }
            }
        }
        return systemDefs;
    }

    @Override
    public List<String> getAvailableMibGroups() {
        List<String> groups = new ArrayList<>();
        for (final SnmpCollection collection : getActiveConfig().getSnmpCollections()) {
            if (collection.getGroups() != null) {
                for (final Group group : collection.getGroups().getGroups()) {
                    groups.add(group.getName());
                }
            }
        }
        return groups;
    }

    @Override
    public void reload() {
        if (dbConfig != null) {
            LOG.debug("reload() called — DB-backed config; no-op until persistence service reloads.");
        } else {
            getContainer().reload();
        }
    }

    @Override
    public Date getLastUpdate() {
        if (dbConfig != null) {
            return lastDbUpdate;
        }
        getContainer().getObject();
        return new Date(getContainer().getLastUpdate());
    }


    @Override
    public void loadFromDatabase(final DatacollectionConfig config,
                                 final Map<String, ResourceType> configuredResourceTypes,
                                 final List<String> groups) {
        LOG.info("Loading SNMP data collection config from database ({} collections, {} resource types, {} groups)",
                config.getSnmpCollections().size(), configuredResourceTypes.size(), groups.size());
        this.dbConfig = config;
        this.resourceTypes = Map.copyOf(configuredResourceTypes);
        this.dataCollectionGroups = List.copyOf(groups);
        this.lastDbUpdate = new Date();
    }

    private DatacollectionConfig getActiveConfig() {
        return dbConfig != null ? dbConfig : getContainer().getObject();
    }

    private void initExtensions() {
        m_extContainer = new ConfigReloadContainer.Builder<>(DataCollectionGroups.class)
                .withFolder((accumulator, next) -> accumulator.getDataCollectionGroupByName()
                        .putAll(next.getDataCollectionGroupByName()))
                .build();
    }
}
