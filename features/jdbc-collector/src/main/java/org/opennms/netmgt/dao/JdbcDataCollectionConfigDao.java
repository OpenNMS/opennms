package org.opennms.netmgt.dao;

import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;

public interface JdbcDataCollectionConfigDao {
    public JdbcDataCollectionConfig getConfig();
    public JdbcDataCollection getDataCollectionByName(String name);
    public JdbcDataCollection getDataCollectionByIndex(int idx);
}
