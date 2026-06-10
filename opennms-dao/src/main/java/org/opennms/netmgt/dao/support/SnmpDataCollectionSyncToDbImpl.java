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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;

import org.opennms.netmgt.config.api.DatacollectionJsonHelper;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.dao.api.SessionUtils;
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

/**
 * Default implementation of {@link SnmpDataCollectionSyncToDb}.
 *
 * <p>See the interface's javadoc for the design contract (one source per
 * plugin group, {@code uploaded_by} marker, child-entity wholesale replace,
 * preserved {@code enabled} flag, no profile attachment).
 */
public class SnmpDataCollectionSyncToDbImpl implements SnmpDataCollectionSyncToDb {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpDataCollectionSyncToDbImpl.class);

    private final SnmpCollectionSourceDao sourceDao;
    private final SnmpCollectionMibGroupDao mibGroupDao;
    private final SnmpCollectionResourceTypeDao resourceTypeDao;
    private final SnmpCollectionSystemDefDao systemDefDao;
    private final SnmpCollectionProfileDao profileDao;
    private final SessionUtils sessionUtils;

    public SnmpDataCollectionSyncToDbImpl(final SnmpCollectionSourceDao sourceDao,
                                          final SnmpCollectionMibGroupDao mibGroupDao,
                                          final SnmpCollectionResourceTypeDao resourceTypeDao,
                                          final SnmpCollectionSystemDefDao systemDefDao,
                                          final SnmpCollectionProfileDao profileDao,
                                          final SessionUtils sessionUtils) {
        this.sourceDao = Objects.requireNonNull(sourceDao);
        this.mibGroupDao = Objects.requireNonNull(mibGroupDao);
        this.resourceTypeDao = Objects.requireNonNull(resourceTypeDao);
        this.systemDefDao = Objects.requireNonNull(systemDefDao);
        this.profileDao = Objects.requireNonNull(profileDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    @Override
    public boolean syncPluginGroupsToDb(final Map<String, List<DatacollectionGroup>> groupsByCollection) {
        return sessionUtils.withTransaction(() ->
                doSync(groupsByCollection == null ? Collections.emptyMap() : groupsByCollection));
    }

    private boolean doSync(final Map<String, List<DatacollectionGroup>> groupsByCollection) {
        boolean changed = false;
        final Date now = new Date();

        // Orphan removal only operates on rows already under the plugin marker:
        // we never auto-delete user/migration sources just because no plugin
        // contributes a group of that name.
        final List<SnmpCollectionSource> existingPluginSources =
                sourceDao.findByUploadedBy(PLUGIN_UPLOADED_BY);

        final Set<String> incomingNames = new HashSet<>();

        for (final Map.Entry<String, List<DatacollectionGroup>> entry : groupsByCollection.entrySet()) {
            final String targetCollection = entry.getKey();
            final List<DatacollectionGroup> groups = entry.getValue();
            if (groups == null) continue;

            for (final DatacollectionGroup group : groups) {
                if (group == null || group.getName() == null || group.getName().isBlank()) {
                    LOG.warn("Skipping plugin DatacollectionGroup with null/blank name");
                    continue;
                }

                // Collision on a non-plugin row → take over (matches pre-DB override behavior
                // in DefaultDataCollectionConfigDao#translateConfig).
                SnmpCollectionSource source = sourceDao.findByName(group.getName());
                if (source == null) {
                    source = new SnmpCollectionSource();
                    source.setName(group.getName());
                    source.setUploadedBy(PLUGIN_UPLOADED_BY);
                    source.setEnabled(Boolean.TRUE);
                    source.setCreatedTime(now);
                    source.setLastModified(now);
                    sourceDao.save(source);
                    LOG.info("Created plugin-sourced datacollection source '{}'", group.getName());
                } else if (!PLUGIN_UPLOADED_BY.equals(source.getUploadedBy())) {
                    LOG.warn("Plugin group '{}' is taking over existing non-plugin source (was uploadedBy='{}'); "
                                    + "previous content will be replaced.",
                            group.getName(), source.getUploadedBy());
                    source.setUploadedBy(PLUGIN_UPLOADED_BY);
                    source.setLastModified(now);
                    // enabled is intentionally not reset — admin disable must survive takeover.
                    sourceDao.saveOrUpdate(source);
                } else {
                    source.setLastModified(now);
                    sourceDao.saveOrUpdate(source);
                }

                // Children are read-only in the UI for plugin rows, so wholesale replace is safe.
                mibGroupDao.deleteBySourceId(source.getId());
                resourceTypeDao.deleteBySourceId(source.getId());
                systemDefDao.deleteBySourceId(source.getId());

                saveMibGroups(source, group.getGroups());
                saveResourceTypes(source, group.getResourceTypes());
                saveSystemDefs(source, group.getSystemDefs());

                if (targetCollection != null && !targetCollection.isBlank()) {
                    attachSourceToProfile(targetCollection, source.getName());
                }

                incomingNames.add(group.getName());
                changed = true;
            }
        }

        for (final SnmpCollectionSource existing : existingPluginSources) {
            if (!incomingNames.contains(existing.getName())) {
                detachSourceFromAllProfiles(existing.getName());
                sourceDao.delete(existing);
                changed = true;
                LOG.info("Removed plugin-sourced datacollection source '{}' (no longer contributed by any plugin)",
                        existing.getName());
            }
        }

        return changed;
    }

    private void detachSourceFromAllProfiles(final String sourceName) {
        for (final SnmpCollectionProfile profile : profileDao.findAll()) {
            final List<String> existing = DatacollectionJsonHelper.fromJson(
                    profile.getSourceNames(), new TypeReference<List<String>>() {});
            if (existing == null || !existing.contains(sourceName)) {
                continue;
            }
            final List<String> filtered = new ArrayList<>(existing.size());
            for (final String name : existing) {
                if (!sourceName.equals(name)) {
                    filtered.add(name);
                }
            }
            profile.setSourceNames(DatacollectionJsonHelper.toJson(filtered));
            profileDao.saveOrUpdate(profile);
            LOG.info("Detached plugin source '{}' from profile '{}' (source being removed).",
                    sourceName, profile.getName());
        }
    }

    private void attachSourceToProfile(final String profileName, final String sourceName) {
        final SnmpCollectionProfile profile = profileDao.findByName(profileName);
        if (profile == null) {
            LOG.warn("Plugin source '{}' targets snmp-collection '{}' but no profile of that name exists; "
                            + "source remains detached. Attach it manually via the admin page if desired.",
                    sourceName, profileName);
            return;
        }
        final List<String> existing = DatacollectionJsonHelper.fromJson(
                profile.getSourceNames(), new TypeReference<List<String>>() {});
        final Set<String> merged = new LinkedHashSet<>();
        if (existing != null) merged.addAll(existing);
        if (merged.add(sourceName)) {
            profile.setSourceNames(DatacollectionJsonHelper.toJson(new ArrayList<>(merged)));
            profileDao.saveOrUpdate(profile);
            LOG.info("Auto-attached plugin source '{}' to profile '{}'.", sourceName, profileName);
        }
    }

    private void saveMibGroups(final SnmpCollectionSource source, final List<Group> groups) {
        if (groups == null || groups.isEmpty()) {
            return;
        }
        final List<SnmpCollectionMibGroup> rows = new ArrayList<>(groups.size());
        for (final Group g : groups) {
            if (g == null) continue;
            final SnmpCollectionMibGroup row = new SnmpCollectionMibGroup();
            row.setCollectionSource(source);
            row.setName(g.getName());
            row.setIfType(g.getIfType());
            row.setMibGroupNames(jsonOrNull(g.getIncludeGroups()));
            // Match migration: mib_objects defaults to "[]" rather than null when empty
            row.setMibObjects(g.getMibObjs() == null || g.getMibObjs().isEmpty() ? "[]"
                    : DatacollectionJsonHelper.toJson(g.getMibObjs()));
            row.setMibObjProperties(jsonOrNull(g.getProperties()));
            row.setEnabled(Boolean.TRUE);
            rows.add(row);
        }
        mibGroupDao.saveAll(rows);
    }

    private void saveResourceTypes(final SnmpCollectionSource source, final List<ResourceType> resourceTypes) {
        if (resourceTypes == null || resourceTypes.isEmpty()) {
            return;
        }
        final List<SnmpCollectionResourceType> rows = new ArrayList<>(resourceTypes.size());
        for (final ResourceType rt : resourceTypes) {
            if (rt == null) continue;
            final SnmpCollectionResourceType row = new SnmpCollectionResourceType();
            row.setCollectionSource(source);
            row.setName(rt.getName());
            // Mirror migration's label fallback so we never violate NOT NULL.
            row.setLabel(rt.getLabel() != null && !rt.getLabel().isBlank() ? rt.getLabel() : rt.getName());
            row.setResourceLabel(rt.getResourceLabel());
            row.setPersistenceSelectorStrategy(rt.getPersistenceSelectorStrategy() != null
                    ? rt.getPersistenceSelectorStrategy().getClazz() : null);
            row.setPersistenceSelectorParams(rt.getPersistenceSelectorStrategy() != null
                    && rt.getPersistenceSelectorStrategy().getParameters() != null
                    && !rt.getPersistenceSelectorStrategy().getParameters().isEmpty()
                    ? DatacollectionJsonHelper.toJson(rt.getPersistenceSelectorStrategy().getParameters()) : null);
            row.setStorageStrategy(rt.getStorageStrategy() != null
                    ? rt.getStorageStrategy().getClazz() : null);
            row.setStorageStrategyParams(rt.getStorageStrategy() != null
                    && rt.getStorageStrategy().getParameters() != null
                    && !rt.getStorageStrategy().getParameters().isEmpty()
                    ? DatacollectionJsonHelper.toJson(rt.getStorageStrategy().getParameters()) : null);
            row.setEnabled(Boolean.TRUE);
            rows.add(row);
        }
        resourceTypeDao.saveAll(rows);
    }

    private void saveSystemDefs(final SnmpCollectionSource source, final List<SystemDef> systemDefs) {
        if (systemDefs == null || systemDefs.isEmpty()) {
            return;
        }
        final List<SnmpCollectionSystemDef> rows = new ArrayList<>(systemDefs.size());
        for (final SystemDef sd : systemDefs) {
            if (sd == null) continue;
            if ((sd.getSysoid() == null || sd.getSysoid().isBlank())
                    && (sd.getSysoidMask() == null || sd.getSysoidMask().isBlank())) {
                LOG.warn("Skipping plugin SystemDef '{}' on source '{}': no sysoid or sysoidMask",
                        sd.getName(), source.getName());
                continue;
            }
            final SnmpCollectionSystemDef row = new SnmpCollectionSystemDef();
            row.setCollectionSource(source);
            row.setName(sd.getName());
            row.setSysoid(sd.getSysoid());
            row.setSysoidMask(sd.getSysoidMask());
            final IpList ipList = sd.getIpList();
            row.setIpAddresses(ipList != null ? DatacollectionJsonHelper.toJson(ipList) : null);
            row.setIpAddressMasks(ipList != null && ipList.getIpAddressMasks() != null
                    && !ipList.getIpAddressMasks().isEmpty()
                    ? DatacollectionJsonHelper.toJson(ipList.getIpAddressMasks()) : null);
            final List<String> includeGroups = Optional.ofNullable(sd.getCollect())
                    .map(Collect::getIncludeGroups)
                    .orElse(Collections.emptyList());
            row.setMibGroupNames(DatacollectionJsonHelper.toJson(includeGroups));
            row.setEnabled(Boolean.TRUE);
            rows.add(row);
        }
        systemDefDao.saveAll(rows);
    }

    private static String jsonOrNull(final List<?> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return DatacollectionJsonHelper.toJson(items);
    }
}
