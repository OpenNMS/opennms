package org.opennms.netmgt.poller.remote.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.opennms.netmgt.poller.remote.PollerSettings;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

public class DefaultPollerSettings implements InitializingBean, PollerSettings {
    
    private final String MONITOR_ID_KEY = "locationMonitorId";
    
    private Resource m_configResource;
    
    private Properties m_settings;

    public Integer getMonitorId() {
        String monIdStr = m_settings.getProperty(MONITOR_ID_KEY);
        return (monIdStr == null ? null : Integer.decode(monIdStr));
    }

    public void setMonitorId(Integer monitorId) {
        if (monitorId == null)
            m_settings.remove(MONITOR_ID_KEY);
        else
            m_settings.setProperty(MONITOR_ID_KEY, monitorId.toString());
        
        save();
    }
    
    private void save() {
        FileOutputStream out = null;
        try {
            File configFile = m_configResource.getFile();
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(configFile);
            m_settings.store(out, "Properties File for OpenNMS Remote Poller");
        } catch (IOException e) {
            throw new DataAccessResourceFailureException("Unable to save properties to "+m_configResource, e);
        } finally {
            if (out != null) {
                try { out.close(); } catch (IOException e) {};
            }
        }
    }

    public void setConfigurationResource(Resource configResource) {
        m_configResource = configResource;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_configResource, "The configurationDir property must be set");
        // ensure that this is a file resource since we need to save to it
        m_configResource.getFile();
        
        if (m_configResource.exists()) {
            m_settings = PropertiesLoaderUtils.loadProperties(m_configResource);
        } else {
            m_settings = new Properties();
        }
        
    }
    
    

}
