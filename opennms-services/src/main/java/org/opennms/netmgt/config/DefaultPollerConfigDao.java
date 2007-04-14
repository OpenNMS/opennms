package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class DefaultPollerConfigDao implements InitializingBean {
    private Resource m_configResource;
    private String m_localServer;
    private Boolean m_verifyServer;
    
    private PollerConfig m_pollerConfig;
    
    public DefaultPollerConfigDao() {
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_configResource != null, "property configResource must be set to a non-null value");
        Assert.state(m_localServer != null, "property localServer must be set to a non-null value");
        Assert.state(m_verifyServer != null, "property verifyServer must be set to a non-null value");
        
        loadConfig();
    }

    private void loadConfig() throws Exception {
        Reader reader;
        long lastModified;
        
        File file = null;
        try {
            file = getConfigResource().getFile();
        } catch (IOException e) {
            log().info("Resource '" + getConfigResource() + "' does not seem to have an underlying File object; using ");
        }
        
        if (file != null) {
            lastModified = file.lastModified();
            reader = new FileReader(file);
        } else {
            lastModified = System.currentTimeMillis();
            reader = new InputStreamReader(getConfigResource().getInputStream());
        }

        setPollerConfig(new PollerConfigFactory(lastModified, reader, getLocalServer(), isVerifyServer()));
    }
    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    private void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    public Resource getConfigResource() {
        return m_configResource;
    }

    public void setConfigResource(Resource configResource) {
        m_configResource = configResource;
    }

    public String getLocalServer() {
        return m_localServer;
    }

    public void setLocalServer(String localServer) {
        m_localServer = localServer;
    }

    public Boolean isVerifyServer() {
        return m_verifyServer;
    }

    public void setVerifyServer(Boolean verifyServer) {
        m_verifyServer = verifyServer;
    }
    
    
}
