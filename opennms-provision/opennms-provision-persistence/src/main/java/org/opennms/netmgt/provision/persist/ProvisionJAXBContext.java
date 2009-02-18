package org.opennms.netmgt.provision.persist;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;


@SuppressWarnings("deprecation")
public class ProvisionJAXBContext extends JAXBContext {
    private final JAXBContext m_context;
    private final String m_namespace;
    
    public ProvisionJAXBContext(JAXBContext context, String namespace) {
        m_context = context;
        m_namespace = namespace;
    }

    @Override
    public Marshaller createMarshaller() throws JAXBException {
        Marshaller m = m_context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new ProvisionNamespacePrefixMapper(m_namespace));
        return m;
    }

    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        return m_context.createUnmarshaller();
    }

    @Override
    public Validator createValidator() throws JAXBException {
        return m_context.createValidator();
    }

}
