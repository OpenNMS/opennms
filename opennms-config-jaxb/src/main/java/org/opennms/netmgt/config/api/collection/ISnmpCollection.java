package org.opennms.netmgt.config.api.collection;


public interface ISnmpCollection {

    String getName();
    String getSnmpStorageFlag();
    IGroupReference[] getIncludedGroups();
    IDataCollectionGroup[] getDataCollectionGroups();
    IRrd getRrd();

}
