package org.opennms.netmgt.dao.jaxb;

import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.dao.JdbcDataCollectionConfigDao;

public class JdbcDataCollectionConfigDaoJaxb extends AbstractJaxbConfigDao<JdbcDataCollectionConfig, JdbcDataCollectionConfig> implements JdbcDataCollectionConfigDao {

    public JdbcDataCollectionConfigDaoJaxb() {
        super(JdbcDataCollectionConfig.class, "JDBC Data Collection Configuration");
    }

    public JdbcDataCollection getDataCollectionByName(String name) {
        JdbcDataCollectionConfig jdcc = getContainer().getObject();
        for (JdbcDataCollection dataCol : jdcc.getJdbcDataCollections()) {
            if(dataCol.getName().equals(name)) {
                return dataCol;
            }
        }

        return null;
    }

    public JdbcDataCollection getDataCollectionByIndex(int idx) {
        JdbcDataCollectionConfig jdcc = getContainer().getObject();
        return jdcc.getJdbcDataCollections().get(idx);
    }

    public JdbcDataCollectionConfig getConfig() {
        return getContainer().getObject();
    }

    @Override
    public JdbcDataCollectionConfig translateConfig(JdbcDataCollectionConfig jaxbConfig) {
        return jaxbConfig;
    }

}
