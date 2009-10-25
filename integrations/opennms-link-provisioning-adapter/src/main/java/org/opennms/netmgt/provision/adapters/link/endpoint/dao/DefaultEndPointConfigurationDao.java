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

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.dao.JAXBDataAccessFailureException;
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

public class DefaultEndPointConfigurationDao extends AbstractCastorConfigDao<EndPointTypeValidator, EndPointTypeValidator> implements EndPointConfigurationDao {
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
    
    public DefaultEndPointConfigurationDao() {
        super(EndPointTypeValidator.class, "End Point Type Configuration");
    }
    
    public DefaultEndPointConfigurationDao(Class<EndPointTypeValidator> entityClass, String description) {
        super(entityClass, description);
    }

    @Override
    public EndPointTypeValidator translateConfig(EndPointTypeValidator config) {
        return config;
    }

    public String getXsd() {
        StringResolver resolver = new StringResolver();
        try {
            m_context.generateSchema(resolver);
        } catch (IOException e) {
            LogUtils.debugf(this, e, "Unable to generate schema");
            return null;
        }
        return resolver.getXml();
    }

    public EndPointTypeValidator getValidator() {
        Assert.notNull(getContainer(), "no container found!");
        Assert.notNull(getContainer().getObject(), "no configuration loaded!");
        return getContainer().getObject();
    }

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
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Could not marshal configuration file for " + getConfigResource() + ": " + e, e);
        }
    }

    @Override
    protected EndPointTypeValidator loadConfig(Resource resource) {
        long startTime = System.currentTimeMillis();

        if (log().isDebugEnabled()) {
            log().debug("Loading " + getDescription() + " configuration from " + resource);
        }

        try {
            InputStream is = resource.getInputStream();
            EndPointTypeValidator config = (EndPointTypeValidator)m_unmarshaller.unmarshal(is);
            is.close();
            
            long endTime = System.currentTimeMillis();
            log().info(createLoadedLogMessage(config, (endTime - startTime)));

            return config;
        } catch (Exception e) {
            throw new JAXBDataAccessFailureException("Unable to unmarshal the endpoint configuration.", e);
        }
    }

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
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create JAXB context.", e);
        }

        super.afterPropertiesSet();
    }

}
