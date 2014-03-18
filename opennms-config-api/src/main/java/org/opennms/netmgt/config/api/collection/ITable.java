package org.opennms.netmgt.config.api.collection;

public interface ITable {

    String getName();
    String getInstance();
    public abstract IColumn[] getColumns();

}
