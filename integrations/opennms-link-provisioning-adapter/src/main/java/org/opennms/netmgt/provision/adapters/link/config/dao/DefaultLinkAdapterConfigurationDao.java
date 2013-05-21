/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.adapters.link.config.dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.netmgt.dao.castor.AbstractCastorConfigDao;
import org.opennms.netmgt.provision.adapters.link.config.DefaultNamespacePrefixMapper;
import org.opennms.netmgt.provision.adapters.link.config.linkadapter.LinkAdapterConfiguration;
import org.opennms.netmgt.provision.adapters.link.config.linkadapter.LinkPattern;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>DefaultLinkAdapterConfigurationDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultLinkAdapterConfigurationDao extends AbstractCastorConfigDao<LinkAdapterConfiguration, LinkAdapterConfiguration> implements LinkAdapterConfigurationDao {
    private JAXBContext m_context;
    private Marshaller m_marshaller;
    private Unmarshaller m_unmarshaller;
    
    /**
     * <p>Constructor for DefaultLinkAdapterConfigurationDao.</p>
     */
    public DefaultLinkAdapterConfigurationDao() {
        super(LinkAdapterConfiguration.class, "Map Link Adapter Configuration");
    }
    
    /**
     * <p>Constructor for DefaultLinkAdapterConfigurationDao.</p>
     *
     * @param entityClass a {@link java.lang.Class} object.
     * @param description a {@link java.lang.String} object.
     */
    public DefaultLinkAdapterConfigurationDao(Class<LinkAdapterConfiguration> entityClass, String description) {
        super(entityClass, description);
    }

    /** {@inheritDoc} */
    @Override
    public LinkAdapterConfiguration translateConfig(LinkAdapterConfiguration config) {
        return config;
    }

    /**
     * <p>getPatterns</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @Override
    public Set<LinkPattern> getPatterns() {
        Assert.notNull(getContainer(), "LinkAdapterConfigDao has no container!");
        Assert.notNull(getContainer().getObject(), "LinkAdapterConfigDao has no configuration loaded!");
        return getContainer().getObject().getPatterns();
    }

    /** {@inheritDoc} */
    @Override
    public void setPatterns(Set<LinkPattern> patterns) {
        Assert.notNull(getContainer(), "LinkAdapterConfigDao has no container!");
        Assert.notNull(getContainer().getObject(), "LinkAdapterConfigDao has no configuration loaded!");
        getContainer().getObject().setPatterns(patterns);
    }

    /**
     * <p>saveCurrent</p>
     */
    @Override
    public synchronized void saveCurrent() {
        File file;
        try {
            file = getConfigResource().getFile();
        } catch (IOException e) {
            throw new DataAccessResourceFailureException("Unable to determine file for " + getConfigResource() + ": " + e, e);
        }
        if (file == null) {
            throw new DataAccessResourceFailureException("Unable to determine file for " + getConfigResource());
        }
        try {
            m_marshaller.marshal(getContainer().getObject(), file);
        } catch (Throwable e) {
            throw new DataAccessResourceFailureException("Could not marshal configuration file for " + getConfigResource() + ": " + e, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected LinkAdapterConfiguration loadConfig(Resource resource) {
        long startTime = System.currentTimeMillis();

        if (log().isDebugEnabled()) {
            log().debug("Loading " + getDescription() + " configuration from " + resource);
        }

        try {
            InputStream is = resource.getInputStream();
            LinkAdapterConfiguration config = (LinkAdapterConfiguration)m_unmarshaller.unmarshal(is);
            is.close();
            
            long endTime = System.currentTimeMillis();
            log().info(createLoadedLogMessage(config, (endTime - startTime)));

            return config;
        } catch (Throwable e) {
            throw new MarshallingResourceFailureException("Unable to unmarshal the link adapter configuration.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void afterPropertiesSet() {

        try {
            m_context = JAXBContext.newInstance(LinkAdapterConfiguration.class, LinkPattern.class);
    
            m_marshaller = m_context.createMarshaller();
            m_marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m_marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new DefaultNamespacePrefixMapper("http://xmlns.opennms.org/xsd/config/map-link-adapter"));
            
            m_unmarshaller = m_context.createUnmarshaller();
            m_unmarshaller.setSchema(null);
            
            ValidationEventHandler handler = new DefaultValidationEventHandler();
            m_unmarshaller.setEventHandler(handler);
        } catch (Throwable e) {
            throw new IllegalStateException("Unable to create JAXB context.", e);
        }

        super.afterPropertiesSet();
    }

}
