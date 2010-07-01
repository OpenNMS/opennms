package org.opennms.netmgt.provision.persist;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;


/**
 * <p>ProvisionJAXBContext class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@SuppressWarnings("deprecation")
public class ProvisionJAXBContext extends JAXBContext {
    private final JAXBContext m_context;
    private final String m_namespace;
    
    /**
     * <p>Constructor for ProvisionJAXBContext.</p>
     *
     * @param context a {@link javax.xml.bind.JAXBContext} object.
     * @param namespace a {@link java.lang.String} object.
     */
    public ProvisionJAXBContext(JAXBContext context, String namespace) {
        m_context = context;
        m_namespace = namespace;
    }

    /** {@inheritDoc} */
    @Override
    public Marshaller createMarshaller() throws JAXBException {
        Marshaller m = m_context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new ProvisionNamespacePrefixMapper(m_namespace));
        return m;
    }

    /** {@inheritDoc} */
    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        return m_context.createUnmarshaller();
    }

    /** {@inheritDoc} */
    @Override
    public Validator createValidator() throws JAXBException {
        return m_context.createValidator();
    }

}
