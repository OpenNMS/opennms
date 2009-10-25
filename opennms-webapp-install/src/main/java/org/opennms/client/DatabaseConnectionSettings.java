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
    private String m_url;

    /**
     * Zero-argument constructor is necessary for GWT serialization
     */
    public DatabaseConnectionSettings() {}

    public DatabaseConnectionSettings(String dbName, String adminUser, String adminPassword, String driver, String adminUrl, String url) {
        m_dbName = dbName; 
        m_adminUser = adminUser;
        m_adminPassword = adminPassword;
        m_driver = driver;
        m_adminUrl = adminUrl;
        m_url = url;
    }

    public String getDbName() { return m_dbName; }
    public String getAdminUser() { return m_adminUser; }
    public String getAdminPassword() { return m_adminPassword; }
    public String getDriver() { return m_driver; }
    public String getAdminUrl() { return m_adminUrl; }
    public String getUrl() { return m_url; }
}
