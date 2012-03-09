package org.opennms.netmgt.correlation.drools;

import java.io.IOException;

import org.opennms.core.utils.LogUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

public class ConfigFileApplicationContext extends AbstractXmlApplicationContext {
    
	private Resource m_resource;
    private String m_configFileLocation;
    
    public ConfigFileApplicationContext(Resource basePath, final String configFileLocation, final ApplicationContext parent) {
        super(parent);
        m_resource = basePath;
        m_configFileLocation = configFileLocation;
        refresh();
    }
    
    @Override
    protected String[] getConfigLocations() {
        if ( m_configFileLocation == null ) {
            return null;
        }
        return new String[] { m_configFileLocation };
    }

    @Override
    protected Resource getResourceByPath(final String path) {
    	try {
    		return m_resource.createRelative(path);
    	} catch(IOException e) {
    		LogUtils.errorf(this, e, "Unable to create resource for path %s relative the directory of %s", path, m_resource);
    		throw new IllegalArgumentException("Failed to create relative path for " + path);
    	}
    }
    
}