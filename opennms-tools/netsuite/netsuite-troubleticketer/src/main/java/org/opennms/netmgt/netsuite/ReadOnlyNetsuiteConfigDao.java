
package org.opennms.netmgt.netsuite;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.opennms.core.utils.LogUtils;

public class ReadOnlyNetsuiteConfigDao implements NetsuiteConfigDao {
    private Configuration m_config = null;
    private long m_lastUpdated = 0L;
    private static final long TIMEOUT = 1000 * 60 * 5; // 5 minutes

    /**
	 * Retrieves the properties defined in the netsuite.properties file.
	 * 
	 * @return a <code>java.util.Properties</code> object containing netsuite plugin defined properties
	 * 
	 * @author <a href="mailto:jcallaghan@towerstream.com">Jerome Callaghan</a>
	 * 
	 */
	protected Configuration getProperties() {
	    final long now = System.currentTimeMillis();
	    if (m_config == null || now > (m_lastUpdated + TIMEOUT)) {
	        String propsFile = getFile();
	        
	        LogUtils.debugf(this, "loading properties from: %s", propsFile);
	        try {
	            m_config = new PropertiesConfiguration(propsFile);
	            m_lastUpdated = now;
	        } catch (final ConfigurationException e) {
	            LogUtils.errorf(this, "Unable to load RT properties", e);
	        }
	    }
	
	    return m_config;
	
	}

    protected String getFile() {
        return System.getProperty("opennms.home") + File.separatorChar + "etc" + File.separatorChar + getPrefix() + ".properties";
    }
	
	protected String getPrefix() {
	    return "netsuite";
	}

	public String getUsername() {
		return getProperties().getString(getPrefix() + ".username");
	}

	public String getPassword() {
		return getProperties().getString(getPrefix() + ".password");
	}

	public String getAccount() {
		return getProperties().getString(getPrefix() + ".account");
	}

	public String getRole() {
		return getProperties().getString(getPrefix() + ".role");
	}

	@SuppressWarnings("unchecked")
	public List<String> getValidClosedStatus() {
		return getProperties().getList(getPrefix() + ".validclosedstatus");
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getValidOpenStatus() {
		return getProperties().getList(getPrefix() + ".validopenstatus");
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getValidCancelledStatus() {
		return getProperties().getList(getPrefix() + ".validcancelledstatus");
	}
	
	public String getOpenStatus() {
		return getProperties().getString(getPrefix() + ".openstatus", "open");
	}
	
	public String getClosedStatus() { 
		return getProperties().getString(getPrefix() + ".closedstatus", "closed");
	}
	
	public String getCancelledStatus() {
		return getProperties().getString(getPrefix() + ".cancelledstatus", "cancelled");
	}

	public String getRequestor() {
        return getProperties().getString(getPrefix() + ".requestor");
    }
	
	public String getBaseURL() {
	    return getProperties().getString(getPrefix() + ".baseURL");
	}
	
	public int getTimeout() {
	    return getProperties().getInt(getPrefix() + ".timeout", 3000);
	}
	
	public int getRetry() {
	    return getProperties().getInt(getPrefix() + ".retry", 0);
	}
	
	public void save() throws IOException {
	    LogUtils.warnf(this, "ReadOnlyRtConfigDao cannot save.");
	}

    protected void clearCache() {
        m_config = null;
    }

    protected String getStringProperty(final String propertyName, final String defaultValue) {
        if (getProperties() != null) {
            return getProperties().getString(propertyName, defaultValue);
        }
        LogUtils.warnf(this, "getProperties() is null, returning the default value instead");
        return defaultValue;
    }

    protected void setProperty(final String propertyName, final Object propertyValue) {
        if (getProperties() == null) {
            LogUtils.warnf(this, "Unable to set the %s property, getProperties() is null!", propertyName);
            return;
        }
        getProperties().setProperty(propertyName, propertyValue);
    }
	
}
