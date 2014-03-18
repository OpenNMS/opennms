package org.opennms.netmgt.config.api.collection;

public interface ITable {

    String getName();
    String getInstance();
    String getIfType();
    IColumn[] getColumns();

}
