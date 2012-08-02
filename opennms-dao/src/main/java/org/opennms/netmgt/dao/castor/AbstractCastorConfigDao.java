/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.castor;


import org.opennms.core.utils.FileReloadCallback;
import org.opennms.core.utils.FileReloadContainer;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.CastorUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * <p>Abstract AbstractCastorConfigDao class.</p>
 *
 * @author <a href="mailto:dj@gregor.com">DJ Gregor</a>
 * @param <K> Castor class
 * @param <V> Configuration object that is stored in memory (might be the same
 *            as the Castor class or could be a different class)
 * @version $Id: $
 */
public abstract class AbstractCastorConfigDao<K, V> implements InitializingBean {
    private Class<K> m_castorClass;
    private String m_description;
    private Resource m_configResource;
    private FileReloadContainer<V> m_container;
    private CastorReloadCallback m_callback = new CastorReloadCallback();
    private Long m_reloadCheckInterval = null;

    /**
     * <p>Constructor for AbstractCastorConfigDao.</p>
     *
     * @param entityClass a {@link java.lang.Class} object.
     * @param description a {@link java.lang.String} object.
     * @param <K> a K object.
     * @param <V> a V object.
     */
    public AbstractCastorConfigDao(final Class<K> entityClass, final String description) {
        super();
        
        m_castorClass = entityClass;
        m_description = description;
    }

    /**
     * <p>translateConfig</p>
     *
     * @param castorConfig a K object.
     * @return a V object.
     */
    public abstract V translateConfig(K castorConfig);

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>loadConfig</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     * @return a V object.
     */
    protected V loadConfig(final Resource resource) {
        long startTime = System.currentTimeMillis();
        
        if (log().isDebugEnabled()) {
            log().debug("Loading " + m_description + " configuration from " + resource);
        }

        V config = translateConfig(CastorUtils.unmarshalWithTranslatedExceptions(m_castorClass, resource));
        
        long endTime = System.currentTimeMillis();
        log().info(createLoadedLogMessage(config, (endTime - startTime)));
        
        return config;
    }

    /**
     * <p>createLoadedLogMessage</p>
     *
     * @param translatedConfig a V object.
     * @param diffTime a long.
     * @return a {@link java.lang.String} object.
     */
    protected String createLoadedLogMessage(final V translatedConfig, final long diffTime) {
        return "Loaded " + getDescription() + " in " + diffTime + "ms";
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_configResource != null, "property configResource must be set and be non-null");
    
        final V config = loadConfig(m_configResource);
        m_container = new FileReloadContainer<V>(config, m_configResource, m_callback);

        if (m_reloadCheckInterval != null) {
            m_container.setReloadCheckInterval(m_reloadCheckInterval);
        }
    }

    /**
     * <p>getConfigResource</p>
     *
     * @return a {@link org.springframework.core.io.Resource} object.
     */
    public Resource getConfigResource() {
        return m_configResource;
    }

    /**
     * <p>setConfigResource</p>
     *
     * @param configResource a {@link org.springframework.core.io.Resource} object.
     */
    public void setConfigResource(final Resource configResource) {
        m_configResource = configResource;
    }
    
    /**
     * <p>getContainer</p>
     *
     * @return a {@link org.opennms.core.utils.FileReloadContainer} object.
     */
    protected FileReloadContainer<V> getContainer() {
        return m_container;
    }
    
    public class CastorReloadCallback implements FileReloadCallback<V> {
        public V reload(final V object, final Resource resource) {
            return loadConfig(resource);
        }
    }

    /**
     * <p>getReloadCheckInterval</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getReloadCheckInterval() {
        return m_reloadCheckInterval;
    }

    /**
     * <p>setReloadCheckInterval</p>
     *
     * @param reloadCheckInterval a {@link java.lang.Long} object.
     */
    public void setReloadCheckInterval(final Long reloadCheckInterval) {
        m_reloadCheckInterval = reloadCheckInterval;
        if (m_reloadCheckInterval != null && m_container != null) {
            m_container.setReloadCheckInterval(m_reloadCheckInterval);
        }
    }
    
    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_description;
    }
}
