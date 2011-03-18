package org.opennms.netmgt.provision.persist;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * <p>ProvisionNamespacePrefixMapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ProvisionNamespacePrefixMapper extends NamespacePrefixMapper {
    private String m_defaultNamespace;

    /**
     * <p>Constructor for ProvisionNamespacePrefixMapper.</p>
     */
    public ProvisionNamespacePrefixMapper() {
    }
    
    /**
     * <p>Constructor for ProvisionNamespacePrefixMapper.</p>
     *
     * @param defaultNamespace a {@link java.lang.String} object.
     */
    public ProvisionNamespacePrefixMapper(String defaultNamespace) {
        m_defaultNamespace = defaultNamespace;
    }

    /** {@inheritDoc} */
    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri)) {
            return "xsi";
        }

        if(m_defaultNamespace != null && m_defaultNamespace.equals(namespaceUri)) {
            return "";
        }

        return suggestion;
    }

}
