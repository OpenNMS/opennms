package org.opennms.netmgt.config.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.core.utils.ThreadCategory;

public class JdbcDataCollectionConfigFactory {
    private JdbcDataCollectionConfig m_jdbcDataCollectionConfig = null;
    
    public JdbcDataCollectionConfigFactory() {
        try {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.JDBC_COLLECTION_CONFIG_FILE_NAME);
            log().debug("init: config file path: " + cfgFile.getPath());
            InputStream reader = new FileInputStream(cfgFile);
            unmarshall(reader);
            reader.close();
        } catch(IOException e) {
            // TODO rethrow.
        }
    }
    
    public JdbcDataCollectionConfig unmarshall(InputStream configFile) {
        try {
            InputStream jdccStream = configFile;
            JAXBContext context = JAXBContext.newInstance(JdbcDataCollectionConfig.class);
            Unmarshaller um = context.createUnmarshaller();
            um.setSchema(null);
            JdbcDataCollectionConfig jdcc = (JdbcDataCollectionConfig) um.unmarshal(jdccStream);
            m_jdbcDataCollectionConfig = jdcc;
            return jdcc;
        } catch (Exception e) {
            // TODO!!
            //throw new ForeignSourceRepositoryException("unable to access default foreign source resource", e);
        }
        return m_jdbcDataCollectionConfig;
    }
    
    protected static ThreadCategory log() {
        return ThreadCategory.getInstance(JdbcDataCollectionConfig.class);
    }

}
