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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DataCollectionConfPersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionConfPersistenceService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    public PageResponse<SnmpCollectionMibGroup> filterMibGroupByDataCollectionGroupId(Integer dataCollectionGroupId, String mibGroupFilter, String sortBy,
                                                                                      String order, Integer totalRecords, Integer offset,
                                                                                      Integer limit) {
        return snmpCollectionMibGroupDao.findByDataCollectionGroupId(dataCollectionGroupId,mibGroupFilter,sortBy,order,totalRecords,offset,limit);
    }

    public PageResponse<SnmpCollectionResourceType> filterResourceTypeByDataCollectionGroupId(Integer dataCollectionGroupId, String resourceTypeFilter, String sortBy,
                                                                                              String order, Integer totalRecords, Integer offset,
                                                                                              Integer limit) {
        return snmpCollectionResourceTypeDao.findByDataCollectionGroupId(dataCollectionGroupId,resourceTypeFilter,sortBy,order,totalRecords,offset,limit);
    }

    public PageResponse<SnmpCollectionSystemDef> filterSystemDefByDataCollectionGroupId(Integer dataCollectionGroupId, String systemDefFilter, String sortBy,
                                                                                        String order, Integer totalRecords, Integer offset,
                                                                                        Integer limit) {
        return snmpCollectionSystemDefDao.findByDataCollectionGroupId(dataCollectionGroupId,systemDefFilter,sortBy,order,totalRecords,offset,limit);
    }

    public Map<Integer,String> getSnmpCollectionSourceNamesAndIds(){
        return snmpCollectionSourceDao.getIdToNameMap();
    }

    @Transactional
    public Integer addMibGroupToSnmpCollectionSources(final SnmpCollectionSource snmpCollectionSource, final SnmpCollectionMibGroupDto request) {

        final var entity = SnmpCollectionMibGroupDto.toEntity(new SnmpCollectionMibGroup(), request);
        return snmpCollectionMibGroupDao.save(entity);
    }
    @Transactional
    public Integer addResourceTypeToSnmpCollectionSources(
            final SnmpCollectionSource snmpCollectionSource,
            final SnmpCollectionResourceTypeDto request) {

        final var entity = SnmpCollectionResourceTypeDto.toEntity(new SnmpCollectionResourceType(), request);
        entity.setCollectionSource(snmpCollectionSource);

        return snmpCollectionResourceTypeDao.save(entity);

    }

    @Transactional
    public Integer addSystemDefToSnmpCollectionSources(
            final SnmpCollectionSource snmpCollectionSource,
            final SnmpCollectionSystemDefDto request) {

        final var entity = SnmpCollectionSystemDefDto.toEntity(new SnmpCollectionSystemDef(), request);
        entity.setCollectionSource(snmpCollectionSource);

        return snmpCollectionSystemDefDao.save(entity);

    }

    @Transactional
    public void updateMibGroup(
            final Integer id, final Integer snmpCollectionSourceId,
            final SnmpCollectionMibGroupDto request) {

        final var snmpCollectionMibGroupEntity = snmpCollectionMibGroupDao.findBySnmpSourceCollectionIdAndId(id,snmpCollectionSourceId);
        final var entity = SnmpCollectionMibGroupDto.toEntity(snmpCollectionMibGroupEntity, request);
        snmpCollectionMibGroupDao.saveOrUpdate(entity);
    }
    @Transactional
    public void updateResourceType(
            final Integer id, final Integer snmpCollectionSourceId,
            final SnmpCollectionResourceTypeDto request) {

        final var snmpCollectionResourceTypeEntity = snmpCollectionResourceTypeDao.findBySnmpSourceCollectionIdAndId(id,snmpCollectionSourceId);
        final var entity = SnmpCollectionResourceTypeDto.toEntity(snmpCollectionResourceTypeEntity,request);

         snmpCollectionResourceTypeDao.save(entity);

    }

    @Transactional
    public void updateSystemDef(
            final Integer id, final Integer snmpCollectionSourceId,
            final SnmpCollectionSystemDefDto request) {
        final var snmpCollectionSystemDefEntity = snmpCollectionSystemDefDao.findBySnmpSourceCollectionIdAndId(id,snmpCollectionSourceId);
        final var entity = SnmpCollectionSystemDefDto.toEntity(snmpCollectionSystemDefEntity,request);

         snmpCollectionSystemDefDao.save(entity);

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
                                                toJson(s.getParameters()));
                                    });
                            Optional.ofNullable(resourceType.getPersistenceSelectorStrategy())
                                    .ifPresent(p -> {
                                        entity.setPersistenceSelectorStrategy(p.getClazz());
                                        entity.setPersistenceSelectorParams(
                                                toJson(p.getParameters()));
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

        List<String> groupNames = dataCollectionGroup.getGroups()
                .stream()
                .map(Group::getName)
                .collect(Collectors.toList());

        List<SnmpCollectionMibGroup> entities =
                dataCollectionGroup.getGroups().stream()
                        .map(mibGroup -> {
                            SnmpCollectionMibGroup entity =
                                    new SnmpCollectionMibGroup();

                            entity.setCollectionSource(source);
                            entity.setName(mibGroup.getName());
                            entity.setEnabled(true);
                            entity.setIfType(mibGroup.getIfType());
                            entity.setMibObjects(toJson(mibGroup.getMibObjs()));
                            entity.setMibObjProperties(toJson(mibGroup.getProperties()));
                            entity.setMibGroupNames(toJson(groupNames));

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

        List<String> mibGroupNames =
                Optional.ofNullable(dataCollectionGroup.getGroups())
                        .orElse(List.of())
                        .stream()
                        .map(Group::getName)
                        .collect(Collectors.toList());

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
                            entity.setIpAddresses(toJson(ipList));
                            entity.setIpAddressMasks(
                                    Optional.ofNullable(ipList)
                                            .map(IpList::getIpAddressMasks)
                                            .map(this::toJson)
                                            .orElse(null)
                            );
                            entity.setMibGroupNames(toJson(mibGroupNames));
                            return entity;
                        })
                        .collect(Collectors.toList());

        snmpCollectionSystemDefDao.saveAll(entities);
    }


    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON serialization failed", e);
        }
    }

}