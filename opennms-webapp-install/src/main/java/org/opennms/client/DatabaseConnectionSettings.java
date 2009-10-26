package org.opennms.client;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BeanModelTag;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *  This class is a facade for the <code>opennms-datasources.xml</code> content.
 */
public class DatabaseConnectionSettings implements BeanModelTag, Serializable, IsSerializable {

    private static final long serialVersionUID = -8871717943285227508L;

    private String m_dbName;
    private String m_adminUser;
    private String m_adminPassword;
    private String m_driver;
    private String m_adminUrl;
    private String m_nmsUrl;

    /**
     * Zero-argument constructor is necessary for GWT serialization
     */
    public DatabaseConnectionSettings() {}

    public DatabaseConnectionSettings(String driver, String dbName, String dbAdminUser, String dbAdminPassword, String dbAdminUrl, String dbNmsUrl) {
        m_dbName = dbName; 
        m_adminUser = dbAdminUser;
        m_adminPassword = dbAdminPassword;
        m_driver = driver;
        m_adminUrl = dbAdminUrl;
        m_nmsUrl = dbNmsUrl;
    }

    public String getDbName() { return m_dbName; }
    public String getAdminUser() { return m_adminUser; }
    public String getAdminPassword() { return m_adminPassword; }
    public String getDriver() { return m_driver; }
    public String getAdminUrl() { return m_adminUrl; }
    public String getNmsUrl() { return m_nmsUrl; }
}
