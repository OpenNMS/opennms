package org.opennms.netmgt.collectd;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;

public class OnmsSnmpCollection {
    
    private String m_collectionName;

    public OnmsSnmpCollection(String collectionName) {
        m_collectionName = collectionName;
    }

    public String getName() {
        return m_collectionName;
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    int getMaxVarsPerPdu() {
    	// Retrieve configured value for max number of vars per PDU
    	int maxVarsPerPdu = DataCollectionConfigFactory.getInstance().getMaxVarsPerPdu(getName());
    	if (maxVarsPerPdu == -1) {
            Category log = log();
            if (log.isEnabledFor(Priority.WARN)) {
    			log.warn(
    					"initialize: Configuration error, failed to "
    							+ "retrieve max vars per pdu from collection: "
    							+ getName());
    		}
    		maxVarsPerPdu = SnmpCollector.DEFAULT_MAX_VARS_PER_PDU;
    	} 
        return maxVarsPerPdu;
    }

    public String getStorageFlag() {
        String collectionName = getName();
    	String storageFlag = DataCollectionConfigFactory.getInstance().getSnmpStorageFlag(collectionName);
    	if (storageFlag == null) {
            Category log = log();
            if (log.isEnabledFor(Priority.WARN)) {
    			log.warn(
    					"initialize: Configuration error, failed to "
    							+ "retrieve SNMP storage flag for collection: "
    							+ collectionName);
    		}
    		storageFlag = SnmpCollector.SNMP_STORAGE_PRIMARY;
    	}
    	return storageFlag;
    }
    
    public String toString() {
        return getName();
    }

    CollectionSet createCollectionSet(CollectionAgent agent) {
        return new CollectionSet(agent, this);
    }

    public List getAttributeTypes(CollectionAgent agent, int ifType) {
        String sysObjectId = agent.getSysObjectId();
        String hostAddress = agent.getHostAddress();
        List oidList = DataCollectionConfigFactory.getInstance().getMibObjectList(getName(), sysObjectId, hostAddress, ifType);
        
        List attrList = new LinkedList();
        for (Iterator it = oidList.iterator(); it.hasNext();) {
            MibObject mibObject = (MibObject) it.next();
            attrList.add(new AttributeType(ResourceType.getResourceType(mibObject.getInstance(), agent, this), getName(), mibObject));
        }
        return attrList;
    }
    

}
