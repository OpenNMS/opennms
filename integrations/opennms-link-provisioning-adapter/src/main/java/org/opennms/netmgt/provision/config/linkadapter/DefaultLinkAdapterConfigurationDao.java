package org.opennms.netmgt.provision.config.linkadapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.opennms.netmgt.dao.JAXBDataAccessFailureException;
import org.opennms.netmgt.dao.castor.AbstractCastorConfigDao;
import org.opennms.netmgt.provision.config.DefaultNamespacePrefixMapper;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

public class DefaultLinkAdapterConfigurationDao extends AbstractCastorConfigDao<LinkAdapterConfiguration, LinkAdapterConfiguration> implements LinkAdapterConfigurationDao {
    private JAXBContext m_context;
    private Marshaller m_marshaller;
    private Unmarshaller m_unmarshaller;
    
    public DefaultLinkAdapterConfigurationDao() {
        super(LinkAdapterConfiguration.class, "Map Link Adapter Configuration");
    }
    
    public DefaultLinkAdapterConfigurationDao(Class<LinkAdapterConfiguration> entityClass, String description) {
        super(entityClass, description);
    }

    @Override
    public LinkAdapterConfiguration translateConfig(LinkAdapterConfiguration config) {
        return config;
    }

    public Set<LinkPattern> getPatterns() {
        Assert.notNull(getContainer(), "LinkAdapterConfigDao has no container!");
        Assert.notNull(getContainer().getObject(), "LinkAdapterConfigDao has no configuration loaded!");
        return getContainer().getObject().getPatterns();
    }

    public void setPatterns(Set<LinkPattern> patterns) {
        Assert.notNull(getContainer(), "LinkAdapterConfigDao has no container!");
        Assert.notNull(getContainer().getObject(), "LinkAdapterConfigDao has no configuration loaded!");
        getContainer().getObject().setPatterns(patterns);
    }

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
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Could not marshal configuration file for " + getConfigResource() + ": " + e, e);
        }
    }

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
        } catch (Exception e) {
            throw new JAXBDataAccessFailureException("Unable to unmarshal the link adapter configuration.", e);
        }
    }

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
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create JAXB context.", e);
        }

        super.afterPropertiesSet();
    }

}
