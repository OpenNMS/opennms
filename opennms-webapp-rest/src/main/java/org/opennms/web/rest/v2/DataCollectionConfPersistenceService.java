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
package org.opennms.web.rest.v2;

import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;

import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionProfileDao;
import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.netmgt.model.PageResponse;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.opennms.netmgt.model.SnmpCollectionResourceType;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.netmgt.model.SnmpCollectionSystemDef;
import org.opennms.netmgt.model.SnmpCollectionMibGroupDto;
import org.opennms.netmgt.model.SnmpCollectionProfileDto;
import org.opennms.netmgt.model.SnmpCollectionResourceTypeDto;
import org.opennms.netmgt.model.SnmpCollectionSystemDefDto;

import org.opennms.netmgt.config.api.DatacollectionJsonHelper;
import org.opennms.netmgt.model.SnmpCollectionProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@Service
@Transactional
public class DataCollectionConfPersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionConfPersistenceService.class);
    @Autowired
    private  SnmpCollectionSourceDao snmpCollectionSourceDao;
    @Autowired
    private  SnmpCollectionProfileDao snmpCollectionProfileDao;
    @Autowired
    private  SnmpCollectionResourceTypeDao snmpCollectionResourceTypeDao;
    @Autowired
    private  SnmpCollectionMibGroupDao snmpCollectionMibGroupDao;
    @Autowired
    private  SnmpCollectionSystemDefDao snmpCollectionSystemDefDao;

    public Integer addDataCollectionConfig(final String fileName,
                                           final String userName,
                                           DatacollectionGroup dataCollectionGroup,
                                           Date now,
                                           List<String> profileNames) {

        if (dataCollectionGroup == null) {
            throw new IllegalArgumentException("DatacollectionGroup must not be null");
        }

        SnmpCollectionSource source =
                createOrUpdateDataCollectionSource(fileName, dataCollectionGroup, userName, now);

        snmpCollectionMibGroupDao.deleteBySourceId(source.getId());
        snmpCollectionResourceTypeDao.deleteBySourceId(source.getId());
        snmpCollectionSystemDefDao.deleteBySourceId(source.getId());

        persistResourceTypes(source, dataCollectionGroup);
        persistMibGroups(source, dataCollectionGroup);
        persistSystemDefs(source, dataCollectionGroup);

        // The whether-to-apply policy lives in the REST layer (which sees the
        // whole batch's new-vs-update composition). If callers pass non-empty
        // profileNames here, attach the source to all of them — idempotent.
        if (profileNames != null) {
            for (final String profileName : profileNames) {
                if (profileName == null || profileName.isBlank()) {
                    continue;
                }
                final SnmpCollectionProfile profile = snmpCollectionProfileDao.findByName(profileName);
                if (profile == null) {
                    LOG.warn("Profile '{}' not found — source '{}' will not be added to it.",
                            profileName, source.getName());
                    continue;
                }
                appendSourceToProfile(profile, source.getName());
            }
        }

        LOG.info("Added data collection config for source '{}'.", fileName);
        return source.getId();
    }

    public SnmpCollectionSource getSnmpCollectionSourceById(Integer collectionSourceId){
        return snmpCollectionSourceDao.get(collectionSourceId);
    }

    public PageResponse<SnmpCollectionSource> filterSnmpCollectionSources(String filter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit) {
        return snmpCollectionSourceDao.filterDataCollectionSource(filter, sortBy, order, totalRecords, offset, limit);
    }

    public PageResponse<SnmpCollectionMibGroup> filterMibGroupByCollectionSourceId(Integer collectionSourceId, String mibGroupFilter, String sortBy,
                                                                                      String order, Integer totalRecords, Integer offset,
                                                                                      Integer limit) {
        return snmpCollectionMibGroupDao.findByCollectionSourceId(collectionSourceId,mibGroupFilter,sortBy,order,totalRecords,offset,limit);
    }

    public PageResponse<SnmpCollectionResourceType> filterResourceTypeByCollectionSourceId(Integer collectionSourceId, String resourceTypeFilter, String sortBy,
                                                                                              String order, Integer totalRecords, Integer offset,
                                                                                              Integer limit) {
        return snmpCollectionResourceTypeDao.findByCollectionSourceId(collectionSourceId,resourceTypeFilter,sortBy,order,totalRecords,offset,limit);
    }

    public PageResponse<SnmpCollectionSystemDef> filterSystemDefByCollectionSourceId(Integer collectionSourceId, String systemDefFilter, String sortBy,
                                                                                        String order, Integer totalRecords, Integer offset,
                                                                                        Integer limit) {
        return snmpCollectionSystemDefDao.findByCollectionSourceId(collectionSourceId,systemDefFilter,sortBy,order,totalRecords,offset,limit);
    }

    public Map<Integer,String> getSnmpCollectionSourceNamesAndIds(){
        return snmpCollectionSourceDao.getIdToNameMap();
    }

    public List<String> getAllResourceTypeNames() {
        return snmpCollectionResourceTypeDao.findAllResourceTypeNames();
    }

    public List<String> getAllMibGroupNames() {
        return snmpCollectionMibGroupDao.findAllMibGroupNames();
    }

    @Transactional
    public Integer addMibGroupToSnmpCollectionSources(final SnmpCollectionSource snmpCollectionSource, final SnmpCollectionMibGroupDto request) {

        final var entity = SnmpCollectionMibGroupDto.updateEntity(new SnmpCollectionMibGroup(), request);
        entity.setCollectionSource(snmpCollectionSource);
        return snmpCollectionMibGroupDao.save(entity);
    }

    @Transactional
    public Integer addResourceTypeToSnmpCollectionSources(
            final SnmpCollectionSource snmpCollectionSource,
            final SnmpCollectionResourceTypeDto request) {

        final var entity = SnmpCollectionResourceTypeDto.updateEntity(new SnmpCollectionResourceType(), request);
        entity.setCollectionSource(snmpCollectionSource);
        return snmpCollectionResourceTypeDao.save(entity);
    }

    @Transactional
    public Integer addSystemDefToSnmpCollectionSources(
            final SnmpCollectionSource snmpCollectionSource,
            final SnmpCollectionSystemDefDto request) {

        final var entity = applySystemDefDtoToEntity(request, new SnmpCollectionSystemDef());
        entity.setCollectionSource(snmpCollectionSource);
        return snmpCollectionSystemDefDao.save(entity);
    }

    /**
     * Reject DTO updates whose embedded {@code id} or {@code collectionSourceId}
     * disagree with the path-derived ones. Both fields are silently ignored by
     * the {@code updateEntity} helpers, so a mismatch from the client would be
     * a no-op success today — surface it as a 400 instead, and prevent a future
     * refactor from quietly turning the no-op into a mass-assignment bug.
     */
    private static void requirePathMatchesDto(
            final String entityKind,
            final Integer pathId, final Integer pathSourceId,
            final Integer dtoId, final Integer dtoSourceId) {
        if (dtoId != null && !dtoId.equals(pathId)) {
            throw new IllegalArgumentException(entityKind + " id in body (" + dtoId
                    + ") does not match path id (" + pathId + ")");
        }
        if (dtoSourceId != null && !dtoSourceId.equals(pathSourceId)) {
            throw new IllegalArgumentException(entityKind + " collectionSourceId in body (" + dtoSourceId
                    + ") does not match path collectionSourceId (" + pathSourceId
                    + "); reparenting via update is not supported");
        }
    }

    @Transactional
    public void updateMibGroup(
            final Integer id, final Integer snmpCollectionSourceId,
            final SnmpCollectionMibGroupDto request) {

        requirePathMatchesDto("MibGroup", id, snmpCollectionSourceId,
                request.getId(), request.getCollectionSourceId());

        final var snmpCollectionMibGroupEntity = snmpCollectionMibGroupDao.findBySnmpSourceCollectionIdAndId(snmpCollectionSourceId, id);

        if (snmpCollectionMibGroupEntity == null) {
            throw new EntityNotFoundException(
                    "No MibGroup found for collectionSourceId=" + snmpCollectionSourceId + ", mibGroupId=" + id
            );
        }
        final var entity = SnmpCollectionMibGroupDto.updateEntity(snmpCollectionMibGroupEntity, request);
        snmpCollectionMibGroupDao.saveOrUpdate(entity);
    }

    @Transactional
    public void updateResourceType(
            final Integer id,
            final Integer snmpCollectionSourceId,
            final SnmpCollectionResourceTypeDto request) {

        requirePathMatchesDto("ResourceType", id, snmpCollectionSourceId,
                request.getId(), request.getCollectionSourceId());

        final var snmpCollectionResourceTypeEntity =
                snmpCollectionResourceTypeDao.findBySnmpSourceCollectionIdAndId(snmpCollectionSourceId, id);

        if (snmpCollectionResourceTypeEntity == null) {
            throw new EntityNotFoundException(
                    "No ResourceType found for collectionSourceId=" + snmpCollectionSourceId + ", resourceTypeId=" + id
            );
        }

        final var entity = SnmpCollectionResourceTypeDto.updateEntity(snmpCollectionResourceTypeEntity, request);
        snmpCollectionResourceTypeDao.saveOrUpdate(entity);
    }

    @Transactional
    public void updateSystemDef(
            final Integer id,
            final Integer snmpCollectionSourceId,
            final SnmpCollectionSystemDefDto request) {

        requirePathMatchesDto("SystemDef", id, snmpCollectionSourceId,
                request.getId(), request.getCollectionSourceId());

        final var snmpCollectionSystemDefEntity =
                snmpCollectionSystemDefDao.findBySnmpSourceCollectionIdAndId(snmpCollectionSourceId, id);

        if (snmpCollectionSystemDefEntity == null) {
            throw new EntityNotFoundException(
                    "No SystemDef found for collectionSourceId=" + snmpCollectionSourceId + ", systemDefId=" + id
            );
        }

        final var entity = applySystemDefDtoToEntity(request, snmpCollectionSystemDefEntity);
        snmpCollectionSystemDefDao.saveOrUpdate(entity);
    }

    @Transactional
    public void deleteSnmpDataCollectionSources(final List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (final Integer id : ids) {
            if (id == null || id <= 0) {
                continue;
            }

            final var source = snmpCollectionSourceDao.get(id);
            if (source == null) {
                continue;
            }
            removeSourceFromProfiles(source.getName());
            snmpCollectionSourceDao.delete(source);
        }
    }

    /**
     * Remove a source name from all profiles' source_names.
     */
    private void removeSourceFromProfiles(final String sourceName) {
        final List<SnmpCollectionProfile> profiles = snmpCollectionProfileDao.findAll();
        if (profiles == null) {
            return;
        }
        for (final SnmpCollectionProfile profile : profiles) {
            List<String> sourceNames = DatacollectionJsonHelper.fromJson(
                    profile.getSourceNames(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            if (sourceNames != null && sourceNames.contains(sourceName)) {
                sourceNames = new ArrayList<>(sourceNames);
                sourceNames.remove(sourceName);
                profile.setSourceNames(DatacollectionJsonHelper.toJson(sourceNames));
                snmpCollectionProfileDao.saveOrUpdate(profile);
                LOG.info("Removed source '{}' from profile '{}'.", sourceName, profile.getName());
            }
        }
    }

    @Transactional
    public void deleteSnmpDataCollectionMibGroups(final Integer snmpDataCollectionSourceId,
                                                  final List<Integer> ids) {
        final var source = requireSource(snmpDataCollectionSourceId);
        deleteChildren(
                source.getId(),
                ids,
                snmpCollectionMibGroupDao::findBySnmpSourceCollectionIdAndId,
                snmpCollectionMibGroupDao::delete,
                "MibGroup"
        );
    }

    @Transactional
    public void deleteSnmpDataCollectionResourceTypes(final Integer snmpDataCollectionSourceId,
                                                      final List<Integer> ids) {
        final var source = requireSource(snmpDataCollectionSourceId);
        deleteChildren(
                source.getId(),
                ids,
                snmpCollectionResourceTypeDao::findBySnmpSourceCollectionIdAndId,
                snmpCollectionResourceTypeDao::delete,
                "ResourceType"
        );
    }

    @Transactional
    public void deleteSnmpDataCollectionSystemDefs(final Integer snmpDataCollectionSourceId,
                                                   final List<Integer> ids) {
        final var source = requireSource(snmpDataCollectionSourceId);
        deleteChildren(
                source.getId(),
                ids,
                snmpCollectionSystemDefDao::findBySnmpSourceCollectionIdAndId,
                snmpCollectionSystemDefDao::delete,
                "SystemDef"
        );
    }

    public void enableDisableSnmpDataCollectionSources(boolean enabled, List<Integer> ids) {
        snmpCollectionSourceDao.updateEnabledFlag(ids, enabled);
    }

    public void enableDisableMibGroups(final Integer snmpDataCollectionSourceId, boolean enabled, List<Integer> ids) {
        snmpCollectionMibGroupDao.updateMibGroupEnabledFlag(snmpDataCollectionSourceId, ids, enabled);
    }

    public void enableDisableResourceTypes(final Integer snmpDataCollectionSourceId, boolean enabled, List<Integer> ids) {
        snmpCollectionResourceTypeDao.updateResourceTypeEnabledFlag(snmpDataCollectionSourceId, ids, enabled);
    }

    public void enableDisableSystemDefs(final Integer snmpDataCollectionSourceId, boolean enabled, List<Integer> ids) {
        snmpCollectionSystemDefDao.updateSystemDefEnabledFlag(snmpDataCollectionSourceId, ids, enabled);
    }

    private SnmpCollectionSource requireSource(final Integer snmpDataCollectionSourceId) {
        if (snmpDataCollectionSourceId == null || snmpDataCollectionSourceId <= 0) {
            throw new IllegalArgumentException("snmpDataCollectionSourceId must be a positive integer");
        }

        final var source = snmpCollectionSourceDao.get(snmpDataCollectionSourceId);
        if (source == null) {
            throw new EntityNotFoundException("SnmpDataCollectionSource not found for id: " + snmpDataCollectionSourceId);
        }
        return source;
    }

    private <T> void deleteChildren(final Integer sourceId,
                                    final List<Integer> ids,
                                    final BiFunction<Integer, Integer, T> finder,
                                    final Consumer<T> deleter,
                                    final String entityLabel) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (final Integer id : ids) {
            if (id == null || id <= 0) {
                continue;
            }

            final T entity = finder.apply(sourceId, id);
            if (entity == null) {
                continue;
            }
            deleter.accept(entity);
        }
    }
    private SnmpCollectionSource createOrUpdateDataCollectionSource(final String fileName,
                                                                    DatacollectionGroup datacollectionGroup,
                                                                    final String userName,
                                                                    Date now) {
        SnmpCollectionSource source = snmpCollectionSourceDao.findByName(fileName);
        if (source == null) {
            LOG.info("Creating new SNMP collection source: '{}'", fileName);
            source = new SnmpCollectionSource();
            source.setCreatedTime(now);
        } else {
            LOG.info("Updating existing SNMP collection source: '{}'", fileName);
        }
        source.setName(fileName);
        source.setVendor(datacollectionGroup.getName());
        source.setEnabled(true);
        source.setLastModified(now);
        source.setUploadedBy(userName);

        snmpCollectionSourceDao.saveOrUpdate(source);
        // Always fetch the managed entity
        return snmpCollectionSourceDao.get(source.getId());
    }

    private void persistResourceTypes(SnmpCollectionSource source,
                                      DatacollectionGroup dataCollectionGroup) {

        if (dataCollectionGroup.getResourceTypes() == null ||
                dataCollectionGroup.getResourceTypes().isEmpty()) {
            LOG.warn("No resource types found for source '{}'", source.getName());
            return;
        }

        List<SnmpCollectionResourceType> entities =
                dataCollectionGroup.getResourceTypes().stream()
                        .map(resourceType -> {
                            SnmpCollectionResourceType entity =
                                    new SnmpCollectionResourceType();
                            entity.setCollectionSource(source);
                            entity.setName(resourceType.getName());
                            entity.setLabel(resourceType.getLabel());
                            entity.setResourceLabel(resourceType.getResourceLabel());
                            Optional.ofNullable(resourceType.getStorageStrategy())
                                    .ifPresent(s -> {
                                        entity.setStorageStrategy(s.getClazz());
                                        entity.setStorageStrategyParams(
                                                DatacollectionJsonHelper.toJson(s.getParameters()));
                                    });
                            Optional.ofNullable(resourceType.getPersistenceSelectorStrategy())
                                    .ifPresent(p -> {
                                        entity.setPersistenceSelectorStrategy(p.getClazz());
                                        entity.setPersistenceSelectorParams(
                                                DatacollectionJsonHelper.toJson(p.getParameters()));
                                    });
                            return entity;
                        })
                        .collect(Collectors.toList());

        snmpCollectionResourceTypeDao.saveAll(entities);
    }


    private void persistMibGroups(SnmpCollectionSource source,
                                  DatacollectionGroup dataCollectionGroup) {

        if (dataCollectionGroup.getGroups() == null ||
                dataCollectionGroup.getGroups().isEmpty()) {
            LOG.warn("No MIB groups found for source '{}'", source.getName());
            return;
        }

        List<SnmpCollectionMibGroup> entities =
                dataCollectionGroup.getGroups().stream()
                        .map(mibGroup -> {
                            SnmpCollectionMibGroup entity =
                                    new SnmpCollectionMibGroup();

                            entity.setCollectionSource(source);
                            entity.setName(mibGroup.getName());
                            entity.setEnabled(true);
                            entity.setIfType(mibGroup.getIfType());
                            entity.setMibObjects(DatacollectionJsonHelper.toJson(mibGroup.getMibObjs()));
                            entity.setMibObjProperties(DatacollectionJsonHelper.toJson(mibGroup.getProperties()));
                            // Store only this group's nested includeGroup references, not all groups
                            List<String> nestedGroupNames = mibGroup.getIncludeGroups();
                            entity.setMibGroupNames(DatacollectionJsonHelper.toJson(nestedGroupNames));

                            return entity;
                        })
                        .collect(Collectors.toList());

        snmpCollectionMibGroupDao.saveAll(entities);
    }


    private void persistSystemDefs(SnmpCollectionSource source,
                                   DatacollectionGroup dataCollectionGroup) {

        if (dataCollectionGroup.getSystemDefs() == null ||
                dataCollectionGroup.getSystemDefs().isEmpty()) {
            LOG.warn("No system definitions found for source '{}'", source.getName());
            return;
        }

        List<SnmpCollectionSystemDef> entities =
                dataCollectionGroup.getSystemDefs().stream()
                        .map(systemDef -> {
                            SnmpCollectionSystemDef entity =
                                    new SnmpCollectionSystemDef();
                            entity.setCollectionSource(source);
                            entity.setName(systemDef.getName());
                            entity.setEnabled(true);
                            entity.setSysoid(systemDef.getSysoid());
                            entity.setSysoidMask(systemDef.getSysoidMask());
                            IpList ipList = systemDef.getIpList();
                            entity.setIpAddresses(DatacollectionJsonHelper.toJson(ipList));
                            entity.setIpAddressMasks(
                                    Optional.ofNullable(ipList)
                                            .map(IpList::getIpAddressMasks)
                                            .map(DatacollectionJsonHelper::toJson)
                                            .orElse(null)
                            );
                            // Store this systemDef's own includeGroup references
                            List<String> groupNames = Optional.ofNullable(systemDef.getCollect())
                                    .map(Collect::getIncludeGroups)
                                    .orElse(List.of());
                            entity.setMibGroupNames(DatacollectionJsonHelper.toJson(groupNames));
                            return entity;
                        })
                        .collect(Collectors.toList());

        snmpCollectionSystemDefDao.saveAll(entities);
    }

    /**
     * Append a source name to a profile's source_names JSON list (idempotent).
     */
    private void appendSourceToProfile(final SnmpCollectionProfile profile, final String sourceName) {
        List<String> sourceNames = DatacollectionJsonHelper.fromJson(
                profile.getSourceNames(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        sourceNames = (sourceNames == null) ? new ArrayList<>() : new ArrayList<>(sourceNames);

        if (!sourceNames.contains(sourceName)) {
            sourceNames.add(sourceName);
            profile.setSourceNames(DatacollectionJsonHelper.toJson(sourceNames));
            snmpCollectionProfileDao.saveOrUpdate(profile);
            LOG.info("Added source '{}' to profile '{}' source_names.", sourceName, profile.getName());
        }
    }

    public List<SnmpCollectionProfile> getAllProfiles() {
        return snmpCollectionProfileDao.findAll();
    }

    @Transactional
    public void addSourceToProfile(final Integer profileId, final String sourceName) {
        final SnmpCollectionProfile profile = requireProfile(profileId);
        if (sourceName == null || sourceName.isBlank()) {
            throw new IllegalArgumentException("sourceName must not be empty");
        }
        if (snmpCollectionSourceDao.findByName(sourceName) == null) {
            // The URI's profile exists; the body just references a source that doesn't.
            // Caller's input is invalid → 400, not 404 (which would conflate with a missing profile).
            throw new IllegalArgumentException("Unknown source name: " + sourceName);
        }
        appendSourceToProfile(profile, sourceName);
    }

    @Transactional
    public void removeSourceFromProfile(final Integer profileId, final String sourceName) {
        final SnmpCollectionProfile profile = requireProfile(profileId);
        List<String> sourceNames = DatacollectionJsonHelper.fromJson(
                profile.getSourceNames(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        if (sourceNames == null || !sourceNames.contains(sourceName)) {
            return;
        }
        sourceNames = new ArrayList<>(sourceNames);
        sourceNames.remove(sourceName);
        profile.setSourceNames(DatacollectionJsonHelper.toJson(sourceNames));
        snmpCollectionProfileDao.saveOrUpdate(profile);
        LOG.info("Removed source '{}' from profile '{}' source_names.", sourceName, profile.getName());
    }

    private SnmpCollectionProfile requireProfile(final Integer profileId) {
        if (profileId == null || profileId <= 0) {
            throw new IllegalArgumentException("profileId must be a positive integer");
        }
        final SnmpCollectionProfile profile = snmpCollectionProfileDao.get(profileId);
        if (profile == null) {
            throw new EntityNotFoundException("Profile not found for id: " + profileId);
        }
        return profile;
    }

    public SnmpCollectionProfile getProfileById(final Integer profileId) {
        return requireProfile(profileId);
    }

    /**
     * Create a new profile from the given DTO. Validates fields, ensures the
     * name is unique, and that every {@code sourceNames} entry refers to an
     * existing source row.
     *
     * @return the new profile's id
     */
    @Transactional
    public Integer createProfile(final SnmpCollectionProfileDto dto, final Date now) {
        validateProfileDto(dto, /*forCreate=*/true, /*currentName=*/null);

        final SnmpCollectionProfile profile = new SnmpCollectionProfile();
        applyDtoToEntity(dto, profile);
        profile.setCreatedTime(now);
        profile.setLastModified(now);
        snmpCollectionProfileDao.saveOrUpdate(profile);
        LOG.info("Created profile '{}'.", profile.getName());
        return profile.getId();
    }

    @Transactional
    public void updateProfile(final Integer profileId, final SnmpCollectionProfileDto dto, final Date now) {
        final SnmpCollectionProfile profile = requireProfile(profileId);
        validateProfileDto(dto, /*forCreate=*/false, /*currentName=*/profile.getName());
        applyDtoToEntity(dto, profile);
        profile.setLastModified(now);
        snmpCollectionProfileDao.saveOrUpdate(profile);
        LOG.info("Updated profile '{}' (id={}).", profile.getName(), profileId);
    }

    @Transactional
    public void deleteProfiles(final List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (final Integer id : ids) {
            if (id == null || id <= 0) {
                continue;
            }
            final SnmpCollectionProfile p = snmpCollectionProfileDao.get(id);
            if (p == null) {
                continue;
            }
            snmpCollectionProfileDao.delete(p);
            LOG.info("Deleted profile '{}' (id={}).", p.getName(), id);
        }
    }

    @Transactional
    public void enableDisableProfiles(final boolean enabled, final List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (final Integer id : ids) {
            if (id == null || id <= 0) {
                continue;
            }
            final SnmpCollectionProfile p = snmpCollectionProfileDao.get(id);
            if (p == null) {
                continue;
            }
            p.setEnabled(enabled);
            snmpCollectionProfileDao.saveOrUpdate(p);
        }
    }

    /**
     * Validate a profile DTO. {@code forCreate} controls uniqueness checking
     * against an existing row; on update, name collision with the *same* row
     * is permitted but with a *different* row is not.
     */
    private void validateProfileDto(final SnmpCollectionProfileDto dto,
                                    final boolean forCreate,
                                    final String currentName) {
        if (dto == null) {
            throw new IllegalArgumentException("Profile body must not be null");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Profile name must not be empty");
        }
        if (dto.getRrdStep() == null || dto.getRrdStep() <= 0) {
            throw new IllegalArgumentException("rrdStep must be a positive integer");
        }
        if (dto.getRrdRras() == null || dto.getRrdRras().isEmpty()) {
            throw new IllegalArgumentException("At least one RRA is required");
        }
        if (dto.getStorageFlag() == null
                || !(dto.getStorageFlag().equals("select") || dto.getStorageFlag().equals("all"))) {
            throw new IllegalArgumentException("storageFlag must be 'select' or 'all'");
        }

        // Name uniqueness
        final SnmpCollectionProfile clash = snmpCollectionProfileDao.findByName(dto.getName());
        if (clash != null && (forCreate || !dto.getName().equals(currentName))) {
            throw new IllegalArgumentException("A profile with name '" + dto.getName() + "' already exists");
        }

        // Source-name references must exist (skip the synthetic __inline_*
        // sources, which the migration may have created and we don't expect
        // users to manage by name).
        if (dto.getSourceNames() != null) {
            final List<String> unknown = new ArrayList<>();
            for (final String n : dto.getSourceNames()) {
                if (n == null || n.isBlank()) continue;
                if (n.startsWith("__inline_")) continue;
                if (snmpCollectionSourceDao.findByName(n) == null) {
                    unknown.add(n);
                }
            }
            if (!unknown.isEmpty()) {
                throw new IllegalArgumentException("Unknown source names: " + String.join(", ", unknown));
            }
        }
    }

    /**
     * Convert an {@link SnmpCollectionSystemDef} entity into the wire DTO.
     * The entity's {@code ip_addresses} column holds a JSON-encoded
     * {@link IpList}; we decode it once here so the response always exposes
     * structured {@code List<String>} fields, regardless of whether the row
     * was created via XML migration, multipart upload, or direct CRUD.
     */
    public SnmpCollectionSystemDefDto toSystemDefDto(final SnmpCollectionSystemDef entity) {
        if (entity == null) {
            return null;
        }
        final IpList ipList = DatacollectionJsonHelper.fromJsonToIpList(entity.getIpAddresses());
        final List<String> addresses = ipList != null ? new ArrayList<>(ipList.getIpAddresses()) : new ArrayList<>();
        final List<String> masks = ipList != null ? new ArrayList<>(ipList.getIpAddressMasks()) : new ArrayList<>();
        return new SnmpCollectionSystemDefDto(
                entity.getId(),
                entity.getName(),
                entity.getSysoid(),
                entity.getSysoidMask(),
                addresses,
                masks,
                entity.getMibGroupNames(),
                entity.getEnabled(),
                entity.getCollectionSource() != null ? entity.getCollectionSource().getId() : null,
                entity.getCollectionSource() != null ? entity.getCollectionSource().getName() : null
        );
    }

    public List<SnmpCollectionSystemDefDto> toSystemDefDtos(final List<SnmpCollectionSystemDef> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toSystemDefDto).collect(Collectors.toList());
    }

    /**
     * Apply a {@link SnmpCollectionSystemDefDto} onto an entity, building the
     * canonical {@link IpList} JSON the runtime loader expects from the
     * structured wire fields. This is the single place the JSON shape of the
     * {@code ip_addresses} / {@code ip_address_masks} columns is produced for
     * REST CRUD writes — keeping it consistent with the XML migration and
     * multipart upload paths.
     */
    private SnmpCollectionSystemDef applySystemDefDtoToEntity(final SnmpCollectionSystemDefDto dto,
                                                              final SnmpCollectionSystemDef entity) {
        if (dto == null) {
            return null;
        }
        entity.setName(dto.getName());
        entity.setSysoid(dto.getSysoid());
        entity.setSysoidMask(dto.getSysoidMask());

        final IpList ipList = new IpList();
        if (dto.getIpAddresses() != null) {
            ipList.setIpAddresses(new ArrayList<>(dto.getIpAddresses()));
        }
        if (dto.getIpAddressMasks() != null) {
            ipList.setIpAddressMasks(new ArrayList<>(dto.getIpAddressMasks()));
        }
        // Empty IpList → store null so the loader's null-safe path handles it.
        final boolean hasAny = !ipList.getIpAddresses().isEmpty() || !ipList.getIpAddressMasks().isEmpty();
        entity.setIpAddresses(hasAny ? DatacollectionJsonHelper.toJson(ipList) : null);
        // ip_address_masks is a redundant column populated by the existing
        // upload + migration paths for the old REST DTO. Keep it in sync so
        // consumers that read it directly (legacy tooling) stay consistent.
        entity.setIpAddressMasks(
                ipList.getIpAddressMasks().isEmpty()
                        ? null
                        : DatacollectionJsonHelper.toJson(ipList.getIpAddressMasks()));

        entity.setMibGroupNames(dto.getMibGroupNames());
        entity.setEnabled(dto.getEnabled());
        return entity;
    }

    private void applyDtoToEntity(final SnmpCollectionProfileDto dto, final SnmpCollectionProfile entity) {
        entity.setName(dto.getName());
        entity.setRrdStep(dto.getRrdStep());
        entity.setRrdRras(DatacollectionJsonHelper.toJson(dto.getRrdRras()));
        entity.setStorageFlag(dto.getStorageFlag());
        entity.setSourceNames(DatacollectionJsonHelper.toJson(
                dto.getSourceNames() == null ? new ArrayList<String>() : dto.getSourceNames()));
        entity.setMaxVarsPerPdu(dto.getMaxVarsPerPdu());
        entity.setEnabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled());
    }

    /** Result returned to the REST layer for bulk upload. */
    public static final class BulkUploadResult {
        public final List<String> sources = new ArrayList<>();
        public final List<String> profiles = new ArrayList<>();
        public final List<String> errors = new ArrayList<>();
    }

    private static final String INLINE_SOURCE_PREFIX = "__inline_";
    private static final String BULK_USER_FALLBACK = "bulk-upload";

    /**
     * Upsert all sources from {@code uploadedGroups} (creating or replacing
     * children) and, when {@code uploadedConfig} is non-null, upsert all
     * profiles from its {@code <snmp-collection>} entries. Sources are written
     * before profiles so that include-collection references resolve.
     *
     * <p>Per-file errors are collected into the result; a single bad file does
     * not abort the whole transaction. The caller must schedule a reload after
     * a successful return.
     */
    @Transactional
    public BulkUploadResult bulkUploadConfig(final List<DatacollectionGroup> uploadedGroups,
                                             final DatacollectionConfig uploadedConfig,
                                             final String userName,
                                             final Date now) {
        final BulkUploadResult result = new BulkUploadResult();
        final String user = (userName == null || userName.isBlank()) ? BULK_USER_FALLBACK : userName;

        // The whole bulk upload is one transaction: any exception thrown by
        // a child mutation (deleteBySourceId / saveOrUpdate / etc.) propagates
        // out so Spring rolls back, instead of leaving the DB with half-written
        // sources whose children were already wiped. Soft conditions (missing
        // include-collection references, malformed regex) accumulate into
        // result.errors via resolveIncludes — those don't throw.
        final Map<String, DatacollectionGroup> groupsByName = new LinkedHashMap<>();
        if (uploadedGroups != null) {
            for (final DatacollectionGroup g : uploadedGroups) {
                if (g == null || g.getName() == null || g.getName().isBlank()) {
                    result.errors.add("Skipped a datacollection-group with no name attribute");
                    continue;
                }
                addDataCollectionConfig(g.getName(), user, g, now, List.of());
                groupsByName.put(g.getName(), g);
                result.sources.add(g.getName());
            }
        }

        if (uploadedConfig != null && uploadedConfig.getSnmpCollections() != null) {
            for (final SnmpCollection coll : uploadedConfig.getSnmpCollections()) {
                if (coll == null || coll.getName() == null || coll.getName().isBlank()) {
                    result.errors.add("Skipped a snmp-collection with no name attribute");
                    continue;
                }
                upsertProfileFromSnmpCollection(coll, groupsByName, user, now, result);
                result.profiles.add(coll.getName());
            }
        }

        return result;
    }

    /**
     * Upsert a single profile from a parsed {@code <snmp-collection>}.
     * Resolves include-collection entries the same way the migration does, so
     * exclude-filter and systemDef= forms route surviving content through the
     * synthetic {@code __inline_<name>} source. Plain
     * {@code dataCollectionGroup="X"} references go straight into source_names.
     *
     * <p>Inline source rows are upserted (delete-then-recreate child rows) just
     * like {@link #addDataCollectionConfig}.
     */
    private void upsertProfileFromSnmpCollection(final SnmpCollection coll,
                                                 final Map<String, DatacollectionGroup> groupsByName,
                                                 final String userName,
                                                 final Date now,
                                                 final BulkUploadResult result) {
        final ResolvedIncludes resolved = resolveIncludes(coll, groupsByName, result);

        // Materialize inline source if there's any pulled or seeded content.
        if (!resolved.inlineGroups.isEmpty()
                || !resolved.inlineSystemDefs.isEmpty()
                || !resolved.inlineResourceTypes.isEmpty()) {
            final String inlineName = INLINE_SOURCE_PREFIX + coll.getName();
            final DatacollectionGroup inlineGroup = new DatacollectionGroup();
            inlineGroup.setName(inlineName);
            inlineGroup.setGroups(resolved.inlineGroups);
            inlineGroup.setSystemDefs(resolved.inlineSystemDefs);
            inlineGroup.setResourceTypes(resolved.inlineResourceTypes);
            addDataCollectionConfig(inlineName, userName, inlineGroup, now, List.of());
            resolved.sourceNames.add(inlineName);
        }

        // Find or create the profile row.
        SnmpCollectionProfile profile = snmpCollectionProfileDao.findByName(coll.getName());
        final boolean isCreate = (profile == null);
        if (isCreate) {
            profile = new SnmpCollectionProfile();
            profile.setName(coll.getName());
            profile.setCreatedTime(now);
            profile.setEnabled(Boolean.TRUE);
        }

        profile.setRrdStep(coll.getRrd() != null && coll.getRrd().getStep() != null
                ? coll.getRrd().getStep() : 300);
        final List<String> rras = (coll.getRrd() != null && coll.getRrd().getRras() != null)
                ? coll.getRrd().getRras() : new ArrayList<>();
        profile.setRrdRras(DatacollectionJsonHelper.toJson(rras));
        profile.setStorageFlag(coll.getSnmpStorageFlag() != null ? coll.getSnmpStorageFlag() : "select");
        profile.setMaxVarsPerPdu(coll.getMaxVarsPerPdu());
        profile.setSourceNames(DatacollectionJsonHelper.toJson(resolved.sourceNames));
        profile.setLastModified(now);

        snmpCollectionProfileDao.saveOrUpdate(profile);
        LOG.info("{} profile '{}' from bulk upload.", isCreate ? "Created" : "Updated", profile.getName());
    }

    /**
     * Pure resolution of include-collection entries. Mirrors
     * {@code SnmpDataCollectionMigration.resolveIncludes} — see that class for
     * the rationale on each branch. References that can't be resolved (group
     * not in the upload batch and not in DB, missing systemDef, etc.) are
     * recorded in {@code result.errors} as warnings but do not abort.
     */
    private ResolvedIncludes resolveIncludes(final SnmpCollection coll,
                                             final Map<String, DatacollectionGroup> groupsByName,
                                             final BulkUploadResult result) {
        final ResolvedIncludes r = new ResolvedIncludes();

        // Seed with inline definitions declared directly under <snmp-collection>.
        if (coll.getGroups() != null && coll.getGroups().getGroups() != null) {
            for (final Group g : coll.getGroups().getGroups()) {
                if (g.getName() != null && r.seenGroupNames.add(g.getName())) {
                    r.inlineGroups.add(g);
                }
            }
        }
        if (coll.getSystems() != null && coll.getSystems().getSystemDefs() != null) {
            for (final SystemDef sd : coll.getSystems().getSystemDefs()) {
                if (sd.getName() != null && r.seenSystemDefNames.add(sd.getName())) {
                    r.inlineSystemDefs.add(sd);
                }
            }
        }
        if (coll.getResourceTypes() != null) {
            for (final org.opennms.netmgt.config.datacollection.ResourceType rt : coll.getResourceTypes()) {
                if (rt.getName() != null && r.seenResourceTypeNames.add(rt.getName())) {
                    r.inlineResourceTypes.add(rt);
                }
            }
        }

        for (final IncludeCollection inc : coll.getIncludeCollections()) {
            if (inc.getDataCollectionGroup() != null && !inc.getDataCollectionGroup().isEmpty()) {
                final String groupName = inc.getDataCollectionGroup();
                final List<String> excludes = inc.getExcludeFilters();

                if (excludes == null || excludes.isEmpty()) {
                    r.sourceNames.add(groupName);
                    continue;
                }

                final DatacollectionGroup dcGroup = groupsByName.get(groupName);
                if (dcGroup == null) {
                    final String msg = "snmp-collection '" + coll.getName() + "': include-collection group '"
                            + groupName + "' has exclude-filter but the group is not in the upload batch — full group reference kept.";
                    LOG.warn(msg);
                    result.errors.add(msg);
                    r.sourceNames.add(groupName);
                    continue;
                }

                for (final SystemDef sd : dcGroup.getSystemDefs()) {
                    if (matchesAnyRegex(sd.getName(), excludes)) {
                        continue;
                    }
                    if (r.seenSystemDefNames.add(sd.getName())) {
                        r.inlineSystemDefs.add(sd);
                    }
                    addReferencedGroups(sd, dcGroup, groupsByName, r);
                }
                if (dcGroup.getResourceTypes() != null) {
                    for (final org.opennms.netmgt.config.datacollection.ResourceType rt : dcGroup.getResourceTypes()) {
                        if (rt.getName() != null && r.seenResourceTypeNames.add(rt.getName())) {
                            r.inlineResourceTypes.add(rt);
                        }
                    }
                }
            } else if (inc.getSystemDef() != null && !inc.getSystemDef().isEmpty()) {
                final String systemDefName = inc.getSystemDef();
                final SystemDefRef ref = findSystemDef(systemDefName, groupsByName);
                if (ref == null) {
                    final String msg = "snmp-collection '" + coll.getName() + "': systemDef='"
                            + systemDefName + "' not found in upload batch — skipped.";
                    LOG.warn(msg);
                    result.errors.add(msg);
                    continue;
                }
                if (r.seenSystemDefNames.add(ref.systemDef.getName())) {
                    r.inlineSystemDefs.add(ref.systemDef);
                }
                addReferencedGroups(ref.systemDef, ref.owningGroup, groupsByName, r);
            }
        }

        return r;
    }

    private void addReferencedGroups(final SystemDef systemDef,
                                     final DatacollectionGroup preferredGroup,
                                     final Map<String, DatacollectionGroup> allGroups,
                                     final ResolvedIncludes r) {
        if (systemDef.getCollect() == null || systemDef.getCollect().getIncludeGroups() == null) {
            return;
        }
        for (final String name : systemDef.getCollect().getIncludeGroups()) {
            final Group g = findGroup(name, preferredGroup, allGroups);
            if (g == null) continue;
            if (r.seenGroupNames.add(g.getName())) {
                r.inlineGroups.add(g);
            }
        }
    }

    private Group findGroup(final String groupName,
                            final DatacollectionGroup preferred,
                            final Map<String, DatacollectionGroup> all) {
        if (preferred != null && preferred.getGroups() != null) {
            for (final Group g : preferred.getGroups()) {
                if (groupName.equals(g.getName())) return g;
            }
        }
        for (final DatacollectionGroup dg : all.values()) {
            if (dg.getGroups() == null) continue;
            for (final Group g : dg.getGroups()) {
                if (groupName.equals(g.getName())) return g;
            }
        }
        return null;
    }

    private SystemDefRef findSystemDef(final String name, final Map<String, DatacollectionGroup> all) {
        for (final DatacollectionGroup dg : all.values()) {
            if (dg.getSystemDefs() == null) continue;
            for (final SystemDef sd : dg.getSystemDefs()) {
                if (name.equals(sd.getName())) {
                    return new SystemDefRef(sd, dg);
                }
            }
        }
        return null;
    }

    private boolean matchesAnyRegex(final String candidate, final List<String> regexes) {
        if (candidate == null || regexes == null) return false;
        for (final String re : regexes) {
            try {
                if (Pattern.compile(re).matcher(candidate).matches()) return true;
            } catch (PatternSyntaxException e) {
                LOG.warn("Invalid exclude-filter regex '{}': {}", re, e.getMessage());
            }
        }
        return false;
    }

    private static final class ResolvedIncludes {
        final List<String> sourceNames = new ArrayList<>();
        final List<Group> inlineGroups = new ArrayList<>();
        final List<SystemDef> inlineSystemDefs = new ArrayList<>();
        final List<org.opennms.netmgt.config.datacollection.ResourceType> inlineResourceTypes = new ArrayList<>();
        final Set<String> seenGroupNames = new HashSet<>();
        final Set<String> seenSystemDefNames = new HashSet<>();
        final Set<String> seenResourceTypeNames = new HashSet<>();
    }

    private static final class SystemDefRef {
        final SystemDef systemDef;
        final DatacollectionGroup owningGroup;
        SystemDefRef(final SystemDef sd, final DatacollectionGroup g) {
            this.systemDef = sd;
            this.owningGroup = g;
        }
    }

}
