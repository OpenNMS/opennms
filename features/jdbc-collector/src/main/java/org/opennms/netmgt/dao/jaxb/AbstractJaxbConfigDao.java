/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.support.FileReloadCallback;
import org.opennms.netmgt.dao.support.FileReloadContainer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public abstract class AbstractJaxbConfigDao<K, V> implements InitializingBean {
    private Class<K> m_jaxbClass;
    private String m_description;
    private Resource m_configResource;
    private FileReloadContainer<V> m_container;
    private JaxbReloadCallback m_callback = new JaxbReloadCallback();
    private Long m_reloadCheckInterval = null;

    public AbstractJaxbConfigDao(Class<K> entityClass, String description) {
        super();
        
        m_jaxbClass = entityClass;
        m_description = description;
    }
    
    public abstract V translateConfig(K jaxbConfig);
    
    protected FileReloadContainer<V> getContainer() {
        return m_container;
    }
    
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    protected V loadConfig(Resource resource) {
        long startTime = System.currentTimeMillis();
        
        if (log().isDebugEnabled()) {
            log().debug("Loading " + m_description + " configuration from " + resource);
        }

        V config = translateConfig(JaxbUtils.unmarshal(m_jaxbClass, resource));
        
        long endTime = System.currentTimeMillis();
        log().info(createLoadedLogMessage(config, (endTime - startTime)));
        
        return config;
    }
    
    protected String createLoadedLogMessage(V translatedConfig, long diffTime) {
        return "Loaded " + getDescription() + " in " + diffTime + "ms";
    }
    
    public Resource getConfigResource() {
        return m_configResource;
    }
    
    public void setConfigResource(Resource configResource) {
        m_configResource = configResource;
    }
    
    public class JaxbReloadCallback implements FileReloadCallback<V> {
        public V reload(V object, Resource resource) {
            return loadConfig(resource);
        }
    }
    
    public String getDescription() {
        return m_description;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_configResource != null, "property configResource must be set and be non-null");
        
        V config = loadConfig(m_configResource);
        m_container = new FileReloadContainer<V>(config, m_configResource, m_callback);

        if (m_reloadCheckInterval != null) {
            m_container.setReloadCheckInterval(m_reloadCheckInterval);
        }

    }

}
