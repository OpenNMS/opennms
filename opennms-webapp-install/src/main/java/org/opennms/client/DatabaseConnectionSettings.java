package org.opennms.client;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BeanModelTag;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *  This class is a facade for the <code>opennms-datasources.xml</code> content.
 *
 * @author ranger
 * @version $Id: $
 */
public class DatabaseConnectionSettings implements BeanModelTag, Serializable, IsSerializable {

    private static final long serialVersionUID = -8871717943285227508L;

    private String m_dbName;
    private String m_adminUser;
    private String m_adminPassword;
    private String m_driver;
    private String m_adminUrl;
    private String m_nmsUrl;
    private String m_nmsUser;
    private String m_nmsPassword;

    /**
     * Zero-argument constructor is necessary for GWT serialization.
     *
     * @deprecated Only for use by GWT serialization.
     */
    public DatabaseConnectionSettings() {}

    /**
     * <p>Constructor for DatabaseConnectionSettings.</p>
     *
     * @param driver a {@link java.lang.String} object.
     * @param dbName a {@link java.lang.String} object.
     * @param dbAdminUser a {@link java.lang.String} object.
     * @param dbAdminPassword a {@link java.lang.String} object.
     * @param dbAdminUrl a {@link java.lang.String} object.
     * @param dbNmsUser a {@link java.lang.String} object.
     * @param dbNmsPassword a {@link java.lang.String} object.
     * @param dbNmsUrl a {@link java.lang.String} object.
     */
    public DatabaseConnectionSettings(String driver, String dbName, String dbAdminUser, String dbAdminPassword, String dbAdminUrl, String dbNmsUser, String dbNmsPassword, String dbNmsUrl) {
        m_dbName = dbName; 
        m_adminUser = dbAdminUser;
        m_adminPassword = dbAdminPassword;
        m_driver = driver;
        m_adminUrl = dbAdminUrl;
        m_nmsUser = dbNmsUser;
        m_nmsPassword = dbNmsPassword;
        m_nmsUrl = dbNmsUrl;
    }

    /**
     * <p>getDbName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDbName() { return m_dbName; }
    /**
     * <p>getAdminUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAdminUser() { return m_adminUser; }
    /**
     * <p>getAdminPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAdminPassword() { return m_adminPassword; }
    /**
     * <p>getDriver</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDriver() { return m_driver; }
    /**
     * <p>getAdminUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAdminUrl() { return m_adminUrl; }
    /**
     * <p>getNmsUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNmsUser() { return m_nmsUser; }
    /**
     * <p>getNmsPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNmsPassword() { return m_nmsPassword; }
    /**
     * <p>getNmsUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNmsUrl() { return m_nmsUrl; }
}
