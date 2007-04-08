package org.opennms.netmgt.dao.castor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.support.FileReloadCallback;
import org.opennms.netmgt.dao.support.FileReloadContainer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public abstract class AbstractCastorConfigDao<K, V> implements InitializingBean{
    private static final CastorExceptionTranslator CASTOR_EXCEPTION_TRANSLATOR = new CastorExceptionTranslator();
    
    private Class<K> m_castorClass;
    private Resource m_configResource;
    private FileReloadContainer<V> m_container;
    private CastorReloadCallback m_callback = new CastorReloadCallback();

    public AbstractCastorConfigDao(Class<K> entityClass) {
        super();
        
        m_castorClass = entityClass;
    }

    public abstract V translateConfig(K castorConfig);

    protected Category log() {
        return ThreadCategory.getInstance();
    }

    protected V loadConfig(Resource resource) {
        Reader reader;
        try {
            reader = new InputStreamReader(resource.getInputStream());
        } catch (IOException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("opening XML configuration file for resource '" + resource + "'", e);
        }
    
        V config;
        try {
            log().debug("loading configuration");
            K castorConfig = CastorUtils.unmarshalWithTranslatedExceptions(m_castorClass, reader);
            config = translateConfig(castorConfig);
            log().debug("configuration loaded");
        } finally {
            IOUtils.closeQuietly(reader);
        }
        
        return config;
    }

    public void afterPropertiesSet() throws IOException {
        Assert.state(m_configResource != null, "property configResource must be set and be non-null");
    
        V config = loadConfig(m_configResource);
        m_container = new FileReloadContainer<V>(config, m_configResource, m_callback);
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
}