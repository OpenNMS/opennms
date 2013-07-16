/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.mock;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.netmgt.collectd.SnmpCollector;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.model.RrdRepository;

public class MockDataCollectionConfig implements DataCollectionConfigDao {
    
    public static final String initalMibObjects[][] = {
        {
            "sysLocation", ".1.3.6.1.2.1.1.6", "0", "string"
        },
    
        {
            "sysName",     ".1.3.6.1.2.1.1.5", "0", "string"
        },
    
        {
            "sysContact",  ".1.3.6.1.2.1.1.4", "0", "string"
        },
    
        {
            "sysUptime",   ".1.3.6.1.2.1.1.3", "0", "timeTicks"
        },
    
        {
            "sysOid",      ".1.3.6.1.2.1.1.2", "0", "string"
        },
    
        {
            "sysDescr", ".1.3.6.1.2.1.1.1", "0", "string"
        },
        
        { 
            "ifNumber",    ".1.3.6.1.2.1.2.1", "0", "integer" 
        },
        
        {
            "ifInDiscards", ".1.3.6.1.2.1.2.2.1.13", "ifIndex", "counter"
        },
    
        {
            "ifOutErrors", ".1.3.6.1.2.1.2.2.1.20", "ifIndex", "counter"
        },
    
        {
            "ifInErrors", ".1.3.6.1.2.1.2.2.1.14", "ifIndex", "counter"
        },
    
        {
            "ifOutOctets", ".1.3.6.1.2.1.2.2.1.16", "ifIndex", "counter"
        },
    
        {
            "ifInOctets", ".1.3.6.1.2.1.2.2.1.10", "ifIndex", "counter"
        },
    
        {
            "ifSpeed", ".1.3.6.1.2.1.2.2.1.5", "ifIndex", "gauge"
        },
        
    
    };
    
    private List<MibObject> m_attrList;
    private Map<String, MibObject> m_attrMap;

    
    
    public MockDataCollectionConfig() {
        setAttrList(new ArrayList<MibObject>());
        setAttrMap(new TreeMap<String, MibObject>());
        addInitialAttributeTypes();
    }

    public void setAttrList(List<MibObject> attrList) {
        m_attrList = attrList;
    }

    public List<MibObject> getAttrList() {
        return m_attrList;
    }

    public void setAttrMap(Map<String, MibObject> attrMap) {
        m_attrMap = attrMap;
    }

    public Map<String, MibObject> getAttrMap() {
        return m_attrMap;
    }
    
    private MibObject createMibObject(String alias, String oid, String instance, String type) {
        MibObject mibObj = new MibObject();
        mibObj.setGroupName("test");
        mibObj.setAlias(alias);
        mibObj.setOid(oid);
        mibObj.setType(type);
        mibObj.setInstance(instance);
        mibObj.setGroupName("ifIndex".equals(instance) ? "interface" : "node");
        mibObj.setGroupIfType("ifIndex".equals(instance) ? "all" : "ignored");
        return mibObj;
    }
    public MibObject createAttributeType(String alias, String oid, String instance, String type) {
        return createMibObject(alias, oid, instance, type);
    }
    public MibObject defineAttributeType(String alias, String oid, String instance, String type) {
        MibObject mibObj = createAttributeType(alias, oid, instance, type);
        getAttrMap().put(mibObj.getAlias(), mibObj);
        getAttrMap().put(mibObj.getOid(), mibObj);
        return mibObj;
    }
    public void addInitialAttributeTypes() {
        for (int i = 0; i < MockDataCollectionConfig.initalMibObjects.length; i++) {
            String[] mibData = MockDataCollectionConfig.initalMibObjects[i];
            defineAttributeType(mibData[0], mibData[1], mibData[2], mibData[3]);
            
        }
    }

    public MibObject getAttributeType(String alias, String oid, String inst, String type) {
        MibObject attrType = getAttributeType(alias);
        if (attrType != null) return attrType;
        return defineAttributeType(alias, oid, inst, type);
        
    }

    public MibObject getAttributeType(String aliasOrOid) {
        return getAttrMap().get(aliasOrOid);
    }

    public void addAttributeType(String alias, String oid, String inst, String type) {
        MibObject attrType = getAttributeType(alias, oid,    inst, type);
        getAttrList().add(attrType);
    }

    @Override
    public List<String> getRRAList(String collectionName) {
        return new ArrayList<String>(0);
    }

    @Override
    public String getRrdPath() {
        return "/tmp";
    }

    @Override
    public String getSnmpStorageFlag(String collectionName) {
        return SnmpCollector.SNMP_STORAGE_PRIMARY;
    }

    @Override
    public int getStep(String collectionName) {
        return 300;
    }

    @Override
    public List<MibObject> getMibObjectList(String cName, String aSysoid, String anAddress, int ifType) {
        return getAttrList();
    }

    @Override
    public Map<String,ResourceType> getConfiguredResourceTypes() {
        return new TreeMap<String, ResourceType>();
    }

    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File(getRrdPath()));
        repo.setRraList(getRRAList(collectionName));
        repo.setStep(getStep(collectionName));
        repo.setHeartBeat((2 * getStep(collectionName)));
        return repo;
    }

    @Override
    public DatacollectionConfig getRootDataCollection() {
        return new DatacollectionConfig();
    }

    @Override
    public List<String> getAvailableDataCollectionGroups() {
        return null;
    }

    @Override
    public List<String> getAvailableSystemDefs() {
        return null;
    }

    @Override
    public List<String> getAvailableMibGroups() {
        return null;
    }

}
