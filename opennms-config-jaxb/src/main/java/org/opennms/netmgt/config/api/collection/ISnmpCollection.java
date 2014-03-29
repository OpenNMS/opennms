package org.opennms.netmgt.config.api.collection;


public interface ISnmpCollection {

    String getName();
    IGroupReference[] getIncludedGroups();
    IDataCollectionGroup[] getDataCollectionGroups();

}
