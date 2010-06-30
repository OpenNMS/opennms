package org.opennms.netmgt.provision.persist;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

@Provider
/**
 * <p>ProvisionPrefixContextResolver class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ProvisionPrefixContextResolver implements ContextResolver<JAXBContext> {
    private final Map<Class<?>, String> m_urls = new HashMap<Class<?>, String>();
    
    /**
     * <p>Constructor for ProvisionPrefixContextResolver.</p>
     *
     * @throws javax.xml.bind.JAXBException if any.
     */
    public ProvisionPrefixContextResolver() throws JAXBException {
        m_urls.put(Requisition.class, "http://xmlns.opennms.org/xsd/config/model-import");
        m_urls.put(ForeignSource.class, "http://xmlns.opennms.org/xsd/config/foreign-source");
    }

    /** {@inheritDoc} */
    public JAXBContext getContext(Class<?> objectType) {
        try {
            return new ProvisionJAXBContext(JAXBContext.newInstance(objectType), m_urls.get(objectType));
        } catch (JAXBException e) {
            log().warn("unable to get context for class " + objectType, e);
            return null;
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(ProvisionPrefixContextResolver.class);
    }

}
