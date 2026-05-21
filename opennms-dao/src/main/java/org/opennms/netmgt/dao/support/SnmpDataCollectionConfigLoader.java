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
package org.opennms.netmgt.dao.support;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.api.DataCollectionConfigLookupUtils;
import org.opennms.netmgt.config.api.DatacollectionJsonHelper;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.Groups;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.config.datacollection.Systems;
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionProfileDao;
import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.opennms.netmgt.model.SnmpCollectionProfile;
import org.opennms.netmgt.model.SnmpCollectionResourceType;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.netmgt.model.SnmpCollectionSystemDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Builds the in-memory SNMP data collection config from the database and
 * publishes it into {@link DataCollectionConfigDao}.
 *
 * <p>Lives in the DAO application context so that both the config DAO and
 * the Hibernate DAOs it needs are available at init time. The initial load
 * runs synchronously in {@link #afterPropertiesSet()} so that downstream
 * components (collectd, pollerd) see a populated config before they start
 * scheduling work.
 *
 * <p>Post-startup reloads (triggered by CRUD on the REST layer) are submitted
 * asynchronously via a single-threaded executor, which serializes concurrent
 * reload requests and keeps reloads off the caller's transaction thread.
 */
public class SnmpDataCollectionConfigLoader implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpDataCollectionConfigLoader.class);

    private static final String RESOURCE_TYPE_COLLECTION_NAME = "__resource_type_collection";

    private DataCollectionConfigDao dataCollectionConfigDao;
    private SnmpCollectionProfileDao snmpCollectionProfileDao;
    private SnmpCollectionSourceDao snmpCollectionSourceDao;
    private SnmpCollectionResourceTypeDao snmpCollectionResourceTypeDao;
    private SnmpCollectionMibGroupDao snmpCollectionMibGroupDao;
    private SnmpCollectionSystemDefDao snmpCollectionSystemDefDao;

    private final ExecutorService dataCollectionReloadExecutor =
            Executors.newSingleThreadExecutor(r -> new Thread(r, "load-DataCollection-Config"));

    private volatile boolean hasPublishedFromDb = false;

    @Override
    public void afterPropertiesSet() {
        try {
            reloadDataCollectionConfigFromDb();
        } catch (Exception e) {
            LOG.error("Failed to load SNMP data collection config from DB at startup — "
                    + "collection will not work until config is reloaded.", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        dataCollectionReloadExecutor.shutdownNow();
    }

    /**
     * Submit a reload on the dedicated single-threaded executor so it runs
     * outside any active transaction on the caller thread and serializes
     * concurrent requests.
     */
    public void scheduleDataCollectionConfigReload() {
        dataCollectionReloadExecutor.submit(() -> {
            try {
                reloadDataCollectionConfigFromDb();
            } catch (Exception e) {
                LOG.error("Failed to reload SNMP data collection config: {}", e.getMessage(), e);
            }
        });
    }


    public void reloadDataCollectionConfigFromDb() {
        final MaterializedConfig materialized = materializeFromDb();

        // First-time load with no DB profiles: don't clobber XML-loaded state.
        // The "admin cleared all profiles" case (publish empty to drop stale DB state)
        // is gated on a previous successful DB publish having happened.
        if (materialized.profileCount == 0 && !hasPublishedFromDb) {
            return;
        }

        try {
            DataCollectionConfigLookupUtils.validateResourceTypes(
                    materialized.config.getSnmpCollections(),
                    materialized.allResourceTypes.keySet());
        } catch (IllegalArgumentException e) {
            LOG.warn("Resource type validation warning during DB config load: {}", e.getMessage());
        }

        dataCollectionConfigDao.loadFromDatabase(
                materialized.config, materialized.allResourceTypes, materialized.allGroups);
        hasPublishedFromDb = true;
        LOG.info("Loaded SNMP data collection config from DB: {} profiles, {} resource types, {} sources",
                materialized.profileCount, materialized.allResourceTypes.size(), materialized.allGroups.size());
    }

    /**
     * Build the in-memory {@link DatacollectionConfig} from the DB without
     * publishing it. When there are no enabled profiles, returns an empty
     * config so the caller can replace any stale in-memory state on reload.
     *
     * <p>Package-private for tests
     */
    MaterializedConfig materializeFromDb() {
        final List<SnmpCollectionProfile> profiles = snmpCollectionProfileDao.findAllEnabled();

        final DatacollectionConfig config = new DatacollectionConfig();
        String rrdPath = dataCollectionConfigDao.getRrdPath();
        if (!rrdPath.endsWith("/")) {
            rrdPath = rrdPath + "/";
        }
        config.setRrdRepository(rrdPath);
        final Map<String, ResourceType> allResourceTypes = new HashMap<>();
        final List<String> allGroups = new ArrayList<>();

        if (profiles == null || profiles.isEmpty()) {
            LOG.info("No SNMP collection profiles in the database — publishing empty config; "
                    + "SNMP data collection is inactive until profiles and sources are uploaded.");
            return new MaterializedConfig(config, allResourceTypes, allGroups, 0);
        }

        LOG.info("Loading SNMP data collection config from database ({} profiles)...", profiles.size());

        final SnmpCollection rtCollection = new SnmpCollection();
        rtCollection.setName(RESOURCE_TYPE_COLLECTION_NAME);
        rtCollection.setSnmpStorageFlag("select");
        rtCollection.setGroups(new Groups());
        rtCollection.setSystems(new Systems());
        final Rrd rtRrd = new Rrd();
        rtRrd.setStep(300);
        rtCollection.setRrd(rtRrd);

        for (final SnmpCollectionProfile profile : profiles) {
            final SnmpCollection coll = new SnmpCollection();
            coll.setName(profile.getName());
            coll.setSnmpStorageFlag(profile.getStorageFlag());
            coll.setMaxVarsPerPdu(profile.getMaxVarsPerPdu());

            final Rrd rrd = new Rrd();
            rrd.setStep(profile.getRrdStep());
            final List<String> rras = DatacollectionJsonHelper.fromJson(
                    profile.getRrdRras(), new TypeReference<List<String>>() {});
            if (rras != null) rras.forEach(rrd::addRra);
            coll.setRrd(rrd);

            final Groups groups = new Groups();
            final Systems systems = new Systems();
            coll.setGroups(groups);
            coll.setSystems(systems);

            // Dedupe by name as we merge content from multiple sources into this collection.
            // Mirrors DataCollectionConfigParser.addSystemDef's contains-check behavior.
            final Set<String> addedGroupNames = new HashSet<>();
            final Set<String> addedSystemDefNames = new HashSet<>();

            final List<String> sourceNames = DatacollectionJsonHelper.fromJson(
                    profile.getSourceNames(), new TypeReference<>() {});
            if (sourceNames != null) {
                for (final String sourceName : sourceNames) {
                    final SnmpCollectionSource source = snmpCollectionSourceDao.findByName(sourceName);
                    if (source == null) {
                        LOG.warn("Profile '{}' references source '{}' not found — skipping.",
                                profile.getName(), sourceName);
                        continue;
                    }
                    if (!source.getEnabled()) {
                        LOG.debug("Profile '{}': source '{}' is disabled — skipping.",
                                profile.getName(), sourceName);
                        continue;
                    }
                    if (!allGroups.contains(sourceName)) {
                        allGroups.add(sourceName);
                    }

                    final DatacollectionGroup dcGroup = buildDataCollectionGroupFromDb(source);
                    for (final Group g : dcGroup.getGroups()) {
                        if (g.getName() != null && addedGroupNames.add(g.getName())) {
                            groups.addGroup(g);
                        }
                    }
                    for (final SystemDef sd : dcGroup.getSystemDefs()) {
                        if (sd.getName() != null && addedSystemDefNames.add(sd.getName())) {
                            systems.addSystemDef(sd);
                        }
                    }
                    for (final ResourceType rt : dcGroup.getResourceTypes()) {
                        coll.addResourceType(rt);
                        rtCollection.addResourceType(rt);
                        allResourceTypes.put(rt.getName(), rt);
                    }
                }
            }

            config.addSnmpCollection(coll);
        }

        config.insertSnmpCollection(rtCollection);
        return new MaterializedConfig(config, allResourceTypes, allGroups, profiles.size());
    }

    /** Bundle of values produced by {@link #materializeFromDb()}. */
    static final class MaterializedConfig {
        final DatacollectionConfig config;
        final Map<String, ResourceType> allResourceTypes;
        final List<String> allGroups;
        final int profileCount;

        MaterializedConfig(final DatacollectionConfig config,
                           final Map<String, ResourceType> allResourceTypes,
                           final List<String> allGroups,
                           final int profileCount) {
            this.config = config;
            this.allResourceTypes = allResourceTypes;
            this.allGroups = allGroups;
            this.profileCount = profileCount;
        }
    }

    /**
     * Materialize a {@link DatacollectionGroup} from a persisted
     * {@link SnmpCollectionSource} plus its child rows.
     */
    public DatacollectionGroup buildDataCollectionGroupFromDb(final SnmpCollectionSource source) {
        final DatacollectionGroup group = new DatacollectionGroup();
        group.setName(source.getName());

        final List<SnmpCollectionResourceType> rtEntities =
                snmpCollectionResourceTypeDao.findAllEnabledBySource(source.getId());
        group.setResourceTypes(rtEntities.stream().map(e -> {
            final ResourceType rt = new ResourceType();
            rt.setName(e.getName());
            rt.setLabel(e.getLabel());
            if (e.getResourceLabel() != null) {
                rt.setResourceLabel(e.getResourceLabel());
            }
            if (e.getStorageStrategy() != null) {
                final StorageStrategy ss = new StorageStrategy();
                ss.setClazz(e.getStorageStrategy());
                final var ssParams = DatacollectionJsonHelper.fromJsonToParameters(e.getStorageStrategyParams());
                if (ssParams != null) {
                    ss.setParameters(ssParams);
                }
                rt.setStorageStrategy(ss);
            }
            if (e.getPersistenceSelectorStrategy() != null) {
                final PersistenceSelectorStrategy ps = new PersistenceSelectorStrategy();
                ps.setClazz(e.getPersistenceSelectorStrategy());
                final var psParams = DatacollectionJsonHelper.fromJsonToParameters(e.getPersistenceSelectorParams());
                if (psParams != null) {
                    ps.setParameters(psParams);
                }
                rt.setPersistenceSelectorStrategy(ps);
            }
            return rt;
        }).toList());

        final List<SnmpCollectionMibGroup> mgEntities =
                snmpCollectionMibGroupDao.findAllEnabledBySource(source.getId());
        group.setGroups(mgEntities.stream().map(e -> {
            final Group g = new Group();
            g.setName(e.getName());
            g.setIfType(e.getIfType());
            // Preserve JAXB's empty-list defaults when the JSON is null/empty.
            // DataCollectionConfigLookupUtils#processGroupForProperties (and
            // similar collectd hot-path code) calls getProperties().forEach
            // without a null check, so a null here would NPE at runtime.
            final var mibObjs = DatacollectionJsonHelper.fromJsonToMibObjs(e.getMibObjects());
            if (mibObjs != null) g.setMibObjs(mibObjs);
            final var props = DatacollectionJsonHelper.fromJsonToProperties(e.getMibObjProperties());
            if (props != null) g.setProperties(props);
            final List<String> includeGroups = DatacollectionJsonHelper.fromJson(
                    e.getMibGroupNames(), new TypeReference<List<String>>() {});
            if (includeGroups != null) {
                g.setIncludeGroups(includeGroups);
            }
            return g;
        }).toList());

        final List<SnmpCollectionSystemDef> sdEntities =
                snmpCollectionSystemDefDao.findAllEnabledBySource(source.getId());
        group.setSystemDefs(sdEntities.stream()
                .filter(e -> {
                    final boolean hasSysoid = e.getSysoid() != null && !e.getSysoid().isBlank();
                    final boolean hasSysoidMask = e.getSysoidMask() != null && !e.getSysoidMask().isBlank();
                    if (!hasSysoid && !hasSysoidMask) {
                        LOG.warn("Skipping SystemDef '{}' (id={}) in source '{}': no sysoid or sysoidMask in DB.",
                                e.getName(), e.getId(), source.getName());
                        return false;
                    }
                    return true;
                })
                .map(e -> {
            final SystemDef sd = new SystemDef();
            sd.setName(e.getName());
            if (e.getSysoid() != null && !e.getSysoid().isBlank()) {
                sd.setSysoid(e.getSysoid());
            } else {
                sd.setSysoidMask(e.getSysoidMask());
            }
            final List<String> includeGroups = DatacollectionJsonHelper.fromJson(
                    e.getMibGroupNames(), new TypeReference<List<String>>() {});
            final Collect collect = new Collect();
            if (includeGroups != null) {
                collect.setIncludeGroups(includeGroups);
            }
            sd.setCollect(collect);
            sd.setIpList(DatacollectionJsonHelper.fromJsonToIpList(e.getIpAddresses()));
            return sd;
        }).toList());

        return group;
    }

    public void setDataCollectionConfigDao(final DataCollectionConfigDao dataCollectionConfigDao) {
        this.dataCollectionConfigDao = dataCollectionConfigDao;
    }

    public void setSnmpCollectionProfileDao(final SnmpCollectionProfileDao snmpCollectionProfileDao) {
        this.snmpCollectionProfileDao = snmpCollectionProfileDao;
    }

    public void setSnmpCollectionSourceDao(final SnmpCollectionSourceDao snmpCollectionSourceDao) {
        this.snmpCollectionSourceDao = snmpCollectionSourceDao;
    }

    public void setSnmpCollectionResourceTypeDao(final SnmpCollectionResourceTypeDao snmpCollectionResourceTypeDao) {
        this.snmpCollectionResourceTypeDao = snmpCollectionResourceTypeDao;
    }

    public void setSnmpCollectionMibGroupDao(final SnmpCollectionMibGroupDao snmpCollectionMibGroupDao) {
        this.snmpCollectionMibGroupDao = snmpCollectionMibGroupDao;
    }

    public void setSnmpCollectionSystemDefDao(final SnmpCollectionSystemDefDao snmpCollectionSystemDefDao) {
        this.snmpCollectionSystemDefDao = snmpCollectionSystemDefDao;
    }
}
