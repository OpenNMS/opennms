package org.opennms.netmgt.config;

import java.util.List;

public interface DataCollectionConfig {
    
    public static final int NODE_ATTRIBUTES = -1;
    public static final int ALL_IF_ATTRIBUTES = -2;

    int getMaxVarsPerPdu(String collectionName);

    String getSnmpStorageFlag(String collectionName);

    List buildCollectionAttributes(String collectionName, String sysObjectId, String hostAddress, int type);

    int getStep(String collectionName);

    List getRRAList(String collectionName);

    String getRrdPath();

}
