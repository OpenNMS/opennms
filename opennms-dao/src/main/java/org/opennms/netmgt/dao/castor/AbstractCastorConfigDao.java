/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Jul 04: Move Castor unmarshalling code from a resource to
 *              CastorUtils. - dj@opennms.org
 * 2008 Feb 15: Add support for reloadCheckInterval, a description for
 *              log messages, time how long it takes to load the file and
 *              log this along with the description, and make the "loaded"
 *              log message overridable. - dj@opennms.org
 * 2008 Jan 06: Pass getClass() to ThreadInstance.getInstance(). - dj@opennms.org
 * 2007 Apr 09: This file was created. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.castor;


import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.support.FileReloadCallback;
import org.opennms.netmgt.dao.support.FileReloadContainer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:dj@gregor.com">DJ Gregor</a>
 *
 * @param <K> Castor class
 * @param <V> Configuration object that is stored in memory (might be the same
 *            as the Castor class or could be a different class)
 */
public abstract class AbstractCastorConfigDao<K, V> implements InitializingBean {
    private Class<K> m_castorClass;
    private String m_description;
    private Resource m_configResource;
    private FileReloadContainer<V> m_container;
    private CastorReloadCallback m_callback = new CastorReloadCallback();
    private Long m_reloadCheckInterval = null;

    public AbstractCastorConfigDao(Class<K> entityClass, String description) {
        super();
        
        m_castorClass = entityClass;
        m_description = description;
    }

    public abstract V translateConfig(K castorConfig);

    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    protected V loadConfig(Resource resource) {
        long startTime = System.currentTimeMillis();
        
        if (log().isDebugEnabled()) {
            log().debug("Loading " + m_description + " configuration from " + resource);
        }

        V config = translateConfig(CastorUtils.unmarshalWithTranslatedExceptions(m_castorClass, resource));
        
        long endTime = System.currentTimeMillis();
        log().info(createLoadedLogMessage(config, (endTime - startTime)));
        
        return config;
    }

    protected String createLoadedLogMessage(V translatedConfig, long diffTime) {
        return "Loaded " + getDescription() + " in " + diffTime + "ms";
    }

    public void afterPropertiesSet() {
        Assert.state(m_configResource != null, "property configResource must be set and be non-null");
    
        V config = loadConfig(m_configResource);
        m_container = new FileReloadContainer<V>(config, m_configResource, m_callback);

        if (m_reloadCheckInterval != null) {
            m_container.setReloadCheckInterval(m_reloadCheckInterval);
        }
    }

    public Resource getConfigResource() {
        return m_configResource;
    }

    public void setConfigResource(Resource configResource) {
        m_configResource = configResource;
    }
    
    protected FileReloadContainer<V> getContainer() {
        return m_container;
    }
    
    public class CastorReloadCallback implements FileReloadCallback<V> {
        public V reload(V object, Resource resource) {
            return loadConfig(resource);
        }
    }

    public Long getReloadCheckInterval() {
        return m_reloadCheckInterval;
    }

    public void setReloadCheckInterval(Long reloadCheckInterval) {
        m_reloadCheckInterval = reloadCheckInterval;
        if (m_reloadCheckInterval != null && m_container != null) {
            m_container.setReloadCheckInterval(m_reloadCheckInterval);
        }
    }
    
    public String getDescription() {
        return m_description;
    }
}