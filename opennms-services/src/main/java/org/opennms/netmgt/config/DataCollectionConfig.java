package org.opennms.netmgt.config;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.collectd.MibObject;
import org.opennms.netmgt.config.datacollection.ResourceType;

public interface DataCollectionConfig {
    
    public static final int NODE_ATTRIBUTES = -1;
    public static final int ALL_IF_ATTRIBUTES = -2;

    int getMaxVarsPerPdu(String collectionName);

    String getSnmpStorageFlag(String collectionName);

    public List<MibObject> getMibObjectList(String cName, String aSysoid, String anAddress, int ifType);

    public Map<String,ResourceType> getConfiguredResourceTypes();
    
    int getStep(String collectionName);

    List getRRAList(String collectionName);

    String getRrdPath();

}
