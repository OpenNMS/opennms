package org.opennms.netmgt.config.api.collection;

public interface ISystemDef {
    String getName();
    String getSysoidMask();
    String getSysoid();
    String[] getIncludes();
    IGroup[] getGroups();
    ITable[] getTables();

}
