/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.apilayer.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.config.datacollection.SnmpCollectionExtension;
import org.opennms.integration.api.v1.config.datacollection.SnmpDataCollection;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DataCollectionGroups;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.config.datacollection.SystemDef;

public class SnmpCollectionExtensionManager extends ConfigExtensionManager<SnmpCollectionExtension, DataCollectionGroups> {

    private final DataCollectionConfigDao dataCollectionConfigDao;

    public SnmpCollectionExtensionManager(DataCollectionConfigDao dataCollectionConfigDao) {
        super(DataCollectionGroups.class, new DataCollectionGroups());
        this.dataCollectionConfigDao = dataCollectionConfigDao;
    }

    @Override
    protected DataCollectionGroups getConfigForExtensions(Set<SnmpCollectionExtension> extensions) {
        DataCollectionGroups dataCollectionGroups = new DataCollectionGroups();
        extensions.forEach(extension ->
            dataCollectionGroups.addDataCollectionGroup(extension.getSnmpCollectionName(), toDataCollectionGroups(extension)));
        return dataCollectionGroups;
    }

    @Override
    protected void triggerReload() {
        dataCollectionConfigDao.reload();
    }


    public static List<DatacollectionGroup> toDataCollectionGroups(SnmpCollectionExtension extension) {
        return extension.getSnmpDataCollectionGroups().stream()
                .map(SnmpCollectionExtensionManager::toDataCollectionGroup).collect(Collectors.toList());
    }


    public static DatacollectionGroup toDataCollectionGroup(SnmpDataCollection snmpDataCollection) {
        DatacollectionGroup datacollectionGroup = new DatacollectionGroup();
        datacollectionGroup.setName(snmpDataCollection.getName());
        datacollectionGroup.setGroups(snmpDataCollection.getGroups().stream()
                .map(SnmpCollectionExtensionManager::toGroup)
                .collect(Collectors.toList()));
        datacollectionGroup.setSystemDefs(snmpDataCollection.getSystemDefs().stream()
                .map(SnmpCollectionExtensionManager::toSystemDef)
                .collect(Collectors.toList()));
        datacollectionGroup.setResourceTypes(snmpDataCollection.getResourceTypes().stream()
                .map(ResourceTypesExtensionManager::toResourceType)
                .collect(Collectors.toList()));
        return datacollectionGroup;
    }

    public static Group toGroup(org.opennms.integration.api.v1.config.datacollection.Group grp) {
        Group group = new Group();
        group.setIfType(grp.getIfType());
        group.setName(grp.getName());
        group.setIncludeGroups(grp.getIncludeGroups());
        group.setMibObjs(grp.getMibObjs().stream()
                .map(SnmpCollectionExtensionManager::toMibObj)
                .collect(Collectors.toList()));
        group.setProperties(grp.getProperties().stream()
                .map(SnmpCollectionExtensionManager::toMibObjProperty)
                .collect(Collectors.toList()));
        return group;
    }

    public static MibObj toMibObj(org.opennms.integration.api.v1.config.datacollection.MibObj mibo) {
        MibObj mibObj = new MibObj();
        mibObj.setAlias(mibo.getAlias());
        mibObj.setInstance(mibo.getInstance());
        mibObj.setMaxval(mibo.getMaxval());
        mibObj.setMinval(mibo.getMinval());
        mibObj.setOid(mibo.getOid());
        mibObj.setType(mibo.getType());
        return mibObj;
    }

    public static MibObjProperty toMibObjProperty(org.opennms.integration.api.v1.config.datacollection.MibObjProperty objProperty) {
        MibObjProperty mibObjProperty = new MibObjProperty();
        mibObjProperty.setAlias(objProperty.getAlias());
        mibObjProperty.setClassName(objProperty.getClassName());
        mibObjProperty.setInstance(objProperty.getInstance());
        mibObjProperty.setParameters(objProperty.getParameters().stream()
                .map(SnmpCollectionExtensionManager::toParameter)
                .collect(Collectors.toList()));
        return mibObjProperty;
    }

    public static SystemDef toSystemDef(org.opennms.integration.api.v1.config.datacollection.SystemDef sysDef) {
        SystemDef systemDef = new SystemDef();
        systemDef.setName(sysDef.getName());
        systemDef.setSysoid(sysDef.getSysoid());
        systemDef.setSysoidMask(sysDef.getSysoidMask());
        if (sysDef.getCollect() != null) {
            Collect collect = new Collect();
            collect.setIncludeGroups(sysDef.getCollect().getIncludeGroups());
            systemDef.setCollect(collect);
        }
        if (sysDef.getIpList() != null) {
            IpList ipList = new IpList();
            ipList.setIpAddresses(sysDef.getIpList().getIpAddresses());
            ipList.setIpAddressMasks(sysDef.getIpList().getIpAddressMasks());
            systemDef.setIpList(ipList);
        }
        return systemDef;
    }

    public static Parameter toParameter(org.opennms.integration.api.v1.config.datacollection.Parameter parameter) {
        return new Parameter() {
            @Override
            public String getKey() {
                return parameter.getKey();
            }

            @Override
            public String getValue() {
                return parameter.getValue();
            }
        };
    }

}
