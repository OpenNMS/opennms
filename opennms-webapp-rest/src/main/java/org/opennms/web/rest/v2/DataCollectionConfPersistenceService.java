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

import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.IpList;

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
import org.opennms.netmgt.model.SnmpCollectionResourceTypeDto;
import org.opennms.netmgt.model.SnmpCollectionSystemDefDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Optional;

import java.util.function.BiFunction;
import java.util.function.Consumer;
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
                                           Date now) {

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

        final var entity = SnmpCollectionSystemDefDto.updateEntity(new SnmpCollectionSystemDef(), request);
        entity.setCollectionSource(snmpCollectionSource);

        return snmpCollectionSystemDefDao.save(entity);

    }

    @Transactional
    public void updateMibGroup(
            final Integer id, final Integer snmpCollectionSourceId,
            final SnmpCollectionMibGroupDto request) {

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

        final var snmpCollectionSystemDefEntity =
                snmpCollectionSystemDefDao.findBySnmpSourceCollectionIdAndId(snmpCollectionSourceId, id);

        if (snmpCollectionSystemDefEntity == null) {
            throw new EntityNotFoundException(
                    "No SystemDef found for collectionSourceId=" + snmpCollectionSourceId + ", systemDefId=" + id
            );
        }

        final var entity = SnmpCollectionSystemDefDto.updateEntity(snmpCollectionSystemDefEntity, request);
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
            snmpCollectionSourceDao.delete(source);
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
    public DatacollectionGroup buildDataCollectionGroupFromDb(final SnmpCollectionSource source) {
        DatacollectionGroup group = new DatacollectionGroup();
        group.setName(source.getName());

        // Resource types
        List<SnmpCollectionResourceType> rtEntities = snmpCollectionResourceTypeDao.findAllEnabledBySource(source.getId());
        group.setResourceTypes(rtEntities.stream().map(e -> {
            ResourceType rt = new ResourceType();
            rt.setName(e.getName());
            rt.setLabel(e.getLabel());
            if (e.getResourceLabel() != null)
               rt.setResourceLabel(e.getResourceLabel());

            if (e.getStorageStrategy() != null) {
                StorageStrategy ss = new StorageStrategy();
                ss.setClazz(e.getStorageStrategy());
                ss.setParameters(DatacollectionJsonHelper.fromJsonToParameters(e.getStorageStrategyParams()));
                rt.setStorageStrategy(ss);
            }

            if (e.getPersistenceSelectorStrategy() != null) {
                PersistenceSelectorStrategy ps = new PersistenceSelectorStrategy();
                ps.setClazz(e.getPersistenceSelectorStrategy());
                ps.setParameters(DatacollectionJsonHelper.fromJsonToParameters(e.getPersistenceSelectorParams()));
                rt.setPersistenceSelectorStrategy(ps);
            }
            return rt;
        }).toList());

        // MIB groups
        List<SnmpCollectionMibGroup> mgEntities = snmpCollectionMibGroupDao.findAllEnabledBySource(source.getId());
        List<Group> mibGroups = mgEntities.stream().map(e -> {
            Group g = new Group();
            g.setName(e.getName());
            g.setIfType(e.getIfType());
            g.setMibObjs(DatacollectionJsonHelper.fromJsonToMibObjs(e.getMibObjects()));
            g.setProperties(DatacollectionJsonHelper.fromJsonToProperties(e.getMibObjProperties()));
            return g;
        }).toList();
        group.setGroups(mibGroups);


        // System defs
        List<SnmpCollectionSystemDef> sdEntities = snmpCollectionSystemDefDao.findAllEnabledBySource(source.getId());
        group.setSystemDefs(sdEntities.stream().map(e -> {
            SystemDef sd = new SystemDef();
            sd.setName(e.getName());

            // XSD requirement: one of these MUST be present before <collect>
            if (e.getSysoid() != null && !e.getSysoid().isBlank()) {
                sd.setSysoid(e.getSysoid());
            } else if (e.getSysoidMask() != null && !e.getSysoidMask().isBlank()) {
                sd.setSysoidMask(e.getSysoidMask());
            } else {
                // invalid configuration: fail fast
                throw new IllegalStateException("SystemDef '" + e.getName()
                        + "' has no sysoid or sysoidMask in DB; cannot generate valid XML.");
            }

            // now it is safe to set collect
            List<String> includeGroups = DatacollectionJsonHelper.fromJson(
                    e.getMibGroupNames(),
                    new com.fasterxml.jackson.core.type.TypeReference<>() {
                    }
            );

            Collect collect = new Collect();
            collect.setIncludeGroups(includeGroups);
            sd.setCollect(collect);

            // ipList (optional)
            sd.setIpList(DatacollectionJsonHelper.fromJsonToIpList(e.getIpAddresses()));

            return sd;
        }).toList());

        return group;
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

}
