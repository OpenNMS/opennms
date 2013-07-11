/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadOnlyRtConfigDao implements RtConfigDao {
    private static final Logger LOG = LoggerFactory.getLogger(ReadOnlyRtConfigDao.class);
    private Configuration m_config = null;
    private long m_lastUpdated = 0L;
    private static final long TIMEOUT = 1000 * 60 * 5; // 5 minutes

    /**
	 * Retrieves the properties defined in the rt.properties file.
	 * 
	 * @return a <code>java.util.Properties</code> object containing rt plugin defined properties
	 * 
	 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
	 * 
	 */
	protected Configuration getProperties() {
	    final long now = System.currentTimeMillis();
	    if (m_config == null || now > (m_lastUpdated + TIMEOUT)) {
	        String propsFile = getFile();
	        
	        LOG.debug("loading properties from: {}", propsFile);
	        
	        try {
	            m_config = new PropertiesConfiguration(propsFile);
	            m_lastUpdated = now;
	        } catch (final ConfigurationException e) {
	            LOG.error("Unable to load RT properties", e);
	        }
	    }
	
	    return m_config;
	
	}

    protected String getFile() {
        return System.getProperty("opennms.home") + File.separatorChar + "etc" + File.separatorChar + getPrefix() + ".properties";
    }
	
	protected String getPrefix() {
	    return "rt";
	}

    @Override
	public String getUsername() {
		return getProperties().getString(getPrefix() + ".username");
	}

    @Override
	public String getPassword() {
		return getProperties().getString(getPrefix() + ".password");
	}
	
    @Override
	public String getQueue() {
		return getProperties().getString(getPrefix() + ".queue", "General");
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public List<String> getValidClosedStatus() {
		return getProperties().getList(getPrefix() + ".validclosedstatus");
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public List<Integer> getValidOpenStatus() {
		return getProperties().getList(getPrefix() + ".validopenstatus");
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public List<String> getValidCancelledStatus() {
		return getProperties().getList(getPrefix() + ".validcancelledstatus");
	}
	
    @Override
	public String getOpenStatus() {
		return getProperties().getString(getPrefix() + ".openstatus", "open");
	}
	
    @Override
	public String getClosedStatus() { 
		return getProperties().getString(getPrefix() + ".closedstatus", "closed");
	}
	
    @Override
	public String getCancelledStatus() {
		return getProperties().getString(getPrefix() + ".cancelledstatus", "cancelled");
	}

    @Override
	public String getRequestor() {
        return getProperties().getString(getPrefix() + ".requestor");
    }
	
    @Override
	public String getBaseURL() {
	    return getProperties().getString(getPrefix() + ".baseURL");
	}
	
    @Override
	public int getTimeout() {
	    return getProperties().getInt(getPrefix() + ".timeout", 3000);
	}
	
    @Override
	public int getRetry() {
	    return getProperties().getInt(getPrefix() + ".retry", 0);
	}
	
    @Override
	public void save() throws IOException {
	    LOG.warn("ReadOnlyRtConfigDao cannot save.");
	}

    protected void clearCache() {
        m_config = null;
    }

    protected String getStringProperty(final String propertyName, final String defaultValue) {
        if (getProperties() != null) {
            return getProperties().getString(propertyName, defaultValue);
        }
        LOG.warn("getProperties() is null, returning the default value instead");
        return defaultValue;
    }

    protected void setProperty(final String propertyName, final Object propertyValue) {
        if (getProperties() == null) {
            LOG.warn("Unable to set the {} property, getProperties() is null!", propertyName);
            return;
        }
        getProperties().setProperty(propertyName, propertyValue);
    }
	
}
