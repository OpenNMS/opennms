package org.opennms.netmgt.provision.adapters.link.config;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * <p>DefaultNamespacePrefixMapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultNamespacePrefixMapper extends NamespacePrefixMapper {
    private String m_uri;

    /**
     * <p>Constructor for DefaultNamespacePrefixMapper.</p>
     */
    public DefaultNamespacePrefixMapper() {
    }
    
    /**
     * <p>Constructor for DefaultNamespacePrefixMapper.</p>
     *
     * @param uri a {@link java.lang.String} object.
     */
    public DefaultNamespacePrefixMapper(String uri) {
        m_uri = uri;
    }

    /** {@inheritDoc} */
    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri)) {
            return "xsi";
        }

        if(m_uri != null && m_uri.equals(namespaceUri)) {
            return "";
        }

        return suggestion;
    }

    /**
     * <p>getPreDeclaredNamespaceUris</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { };
    }
}
