package org.opennms.netmgt.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.MibObject;
import org.opennms.netmgt.collectd.SnmpCollector;
import org.opennms.netmgt.config.DataCollectionConfig;
import org.opennms.netmgt.snmp.SnmpCollectorTestCase;

public class MockDataCollectionConfig implements DataCollectionConfig {
    
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
            "sysOid",      ".1.3.6.1.2.1.1.2", "0", "objectid"
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
    
    private List m_attrList;
    private Map m_attrMap;

    
    
    public MockDataCollectionConfig() {
        setAttrList(new ArrayList());
        setAttrMap(new TreeMap());
        addInitialAttributes();
    }

    public void setAttrList(List attrList) {
        m_attrList = attrList;
    }

    public List getAttrList() {
        return m_attrList;
    }

    public void setAttrMap(Map attrMap) {
        m_attrMap = attrMap;
    }

    public Map getAttrMap() {
        return m_attrMap;
    }
    
    private MibObject createMibObject(String alias, String oid, String instance, String type) {
        MibObject mibObj = new MibObject();
        mibObj.setAlias(alias);
        mibObj.setOid(oid);
        mibObj.setType(type);
        mibObj.setInstance(instance);
        return mibObj;
    }
    public CollectionAttribute createAttribute(String alias, String oid, String instance, String type) {
        MibObject mibObj = createMibObject(alias, oid, instance, type);
        CollectionAttribute attr = new CollectionAttribute("default", mibObj);
        return attr;
    }
    public CollectionAttribute defineAttribute(String alias, String oid, String instance, String type) {
        CollectionAttribute attr = createAttribute(alias, oid, instance, type);
        getAttrMap().put(attr.getAlias(), attr);
        getAttrMap().put(attr.getOid(), attr);
        return attr;
    }
    public void addInitialAttributes() {
        for (int i = 0; i < MockDataCollectionConfig.initalMibObjects.length; i++) {
            String[] mibData = MockDataCollectionConfig.initalMibObjects[i];
            defineAttribute(mibData[0], mibData[1], mibData[2], mibData[3]);
            
        }
    }

    public CollectionAttribute getAttribute(SnmpCollectorTestCase case1, String alias, String oid, String inst, String type) {
        CollectionAttribute attr = case1.m_config.getAttribute(alias);
        if (attr != null) return attr;
        return defineAttribute(alias, oid, inst, type);
        
    }

    public CollectionAttribute getAttribute(String aliasOrOid) {
        return (CollectionAttribute)getAttrMap().get(aliasOrOid);
    }

    public void addAttribute(SnmpCollectorTestCase case1, String alias, String oid, String inst, String type) {
        CollectionAttribute attr = getAttribute(case1, alias,    oid, inst, type);
        case1.getAttributeList().add(attr);
    }

    public List buildCollectionAttributes(String collectionName, String sysObjectId, String hostAddress, int type) {
        return new ArrayList(m_attrList);
    }

    public int getMaxVarsPerPdu(String collectionName) {
        return 10;
    }

    public List getRRAList(String collectionName) {
        return Collections.EMPTY_LIST;
    }

    public String getRrdPath() {
        return "/tmp";
    }

    public String getSnmpStorageFlag(String collectionName) {
        return SnmpCollector.SNMP_STORAGE_PRIMARY;
    }

    public int getStep(String collectionName) {
        return 300;
    }

}
