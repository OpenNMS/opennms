package org.opennms.netmgt.filter;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.support.JdbcFilterDao;
import org.springframework.dao.DataAccessResourceFailureException;

public class FilterDaoFactory {
    private static FilterDao m_filterDao;

    // Only static methods, so don't let the constructor be called
    private FilterDaoFactory() {
    }

    public static FilterDao getInstance() {
        if (m_filterDao == null) {
            init();
        }
        
        return m_filterDao;
    }

    public static void setInstance(FilterDao filterDao) {
        m_filterDao = filterDao;
    }

    protected static synchronized void init() {
        if (m_filterDao != null) {
            return;
        }
        
        JdbcFilterDao jdbcFilterDao = new JdbcFilterDao();
        
        try {
            DataSourceFactory.init();
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Could not initialize DataSourceFactory: " + e, e);
        }
        jdbcFilterDao.setDataSource(DataSourceFactory.getInstance());
        
        try {
            DatabaseSchemaConfigFactory.init();
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Could not initialize DatabaseSchemaConfigFactory: " + e, e);
        }
        jdbcFilterDao.setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory.getInstance());
        
        jdbcFilterDao.afterPropertiesSet();
        
        setInstance(jdbcFilterDao);
    }

}
