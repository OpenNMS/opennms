package org.opennms.netmgt.config.api.collection;


public interface IDataCollectionGroup {

    public abstract String getName();
    public abstract IResourceType[] getResourceTypes();
    public abstract ITable[] getTables();
    public abstract IGroup[] getGroups();
    public abstract ISystemDef[] getSystemDefs();

}
