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

package org.opennms.netmgt.provision.adapters.link.endpoint.dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.netmgt.dao.castor.AbstractCastorConfigDao;
import org.opennms.netmgt.provision.adapters.link.config.DefaultNamespacePrefixMapper;
import org.opennms.netmgt.provision.adapters.link.endpoint.AndEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointType;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;
import org.opennms.netmgt.provision.adapters.link.endpoint.MatchingSnmpEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.OrEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.PingEndPointValidationExpression;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>DefaultEndPointConfigurationDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultEndPointConfigurationDao extends AbstractCastorConfigDao<EndPointTypeValidator, EndPointTypeValidator> implements EndPointConfigurationDao {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultEndPointConfigurationDao.class);
    private JAXBContext m_context;
    private Marshaller m_marshaller;
    private Unmarshaller m_unmarshaller;

    private static final class StringResolver extends SchemaOutputResolver {
        private StringWriter m_writer = new StringWriter();
        
        @Override
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            StreamResult sr = new StreamResult(m_writer);
            sr.setSystemId("unused-but-requred");
            return sr;
        }

        public String getXml() {
            return m_writer.toString();
        }
    }
    
    /**
     * <p>Constructor for DefaultEndPointConfigurationDao.</p>
     */
    public DefaultEndPointConfigurationDao() {
        super(EndPointTypeValidator.class, "End Point Type Configuration");
    }
    
    /**
     * <p>Constructor for DefaultEndPointConfigurationDao.</p>
     *
     * @param entityClass a {@link java.lang.Class} object.
     * @param description a {@link java.lang.String} object.
     */
    public DefaultEndPointConfigurationDao(Class<EndPointTypeValidator> entityClass, String description) {
        super(entityClass, description);
    }

    /** {@inheritDoc} */
    @Override
    public EndPointTypeValidator translateConfig(EndPointTypeValidator config) {
        return config;
    }

    /**
     * <p>getXsd</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getXsd() {
        StringResolver resolver = new StringResolver();
        try {
            m_context.generateSchema(resolver);
        } catch (IOException e) {
            LOG.debug("Unable to generate schema", e);
            return null;
        }
        return resolver.getXml();
    }

    /**
     * <p>getValidator</p>
     *
     * @return a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator} object.
     */
    @Override
    public EndPointTypeValidator getValidator() {
        Assert.notNull(getContainer(), "no container found!");
        Assert.notNull(getContainer().getObject(), "no configuration loaded!");
        return getContainer().getObject();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void save(EndPointTypeValidator validator) {
        Assert.notNull(getContainer(), "no container found!");
        Assert.notNull(getContainer().getObject(), "no configuration loaded!");

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
    protected EndPointTypeValidator loadConfig(Resource resource) {
        long startTime = System.currentTimeMillis();

        LOG.debug("Loading {} configuration from {}", resource, getDescription());

        try {
            InputStream is = resource.getInputStream();
            EndPointTypeValidator config = (EndPointTypeValidator)m_unmarshaller.unmarshal(is);
            is.close();
            
            long endTime = System.currentTimeMillis();
            LOG.info(createLoadedLogMessage(config, (endTime - startTime)));

            return config;
        } catch (Throwable e) {
            throw new MarshallingResourceFailureException("Unable to unmarshal the endpoint configuration.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void afterPropertiesSet() {

        try {
            m_context = JAXBContext.newInstance(
                EndPointTypeValidator.class,
                EndPointType.class,
                AndEndPointValidationExpression.class,
                OrEndPointValidationExpression.class,
                MatchingSnmpEndPointValidationExpression.class,
                PingEndPointValidationExpression.class
            );
    
            m_marshaller = m_context.createMarshaller();
            m_marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m_marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new DefaultNamespacePrefixMapper("http://xmlns.opennms.org/xsd/config/endpoint-types"));
            
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
