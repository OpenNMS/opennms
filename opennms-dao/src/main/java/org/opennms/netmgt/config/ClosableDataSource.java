package org.opennms.netmgt.config;

import java.sql.SQLException;

import javax.sql.DataSource;

public interface ClosableDataSource extends DataSource {
    
    public void close() throws SQLException;

}
