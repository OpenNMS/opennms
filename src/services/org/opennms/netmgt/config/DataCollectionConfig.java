package org.opennms.netmgt.config;

import java.util.List;

public interface DataCollectionConfig {

    int getMaxVarsPerPdu(String collectionName);

    String getSnmpStorageFlag(String collectionName);

    List buildCollectionAttributes(String collectionName, String sysObjectId, String hostAddress, int type);

    int getStep(String collectionName);

    List getRRAList(String collectionName);

    String getRrdPath();

}
