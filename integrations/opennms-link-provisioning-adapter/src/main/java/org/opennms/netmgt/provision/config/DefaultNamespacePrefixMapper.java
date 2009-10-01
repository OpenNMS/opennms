package org.opennms.netmgt.provision.config;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class DefaultNamespacePrefixMapper extends NamespacePrefixMapper {
    private String m_uri;

    public DefaultNamespacePrefixMapper() {
    }
    
    public DefaultNamespacePrefixMapper(String uri) {
        m_uri = uri;
    }

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

    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { };
    }
}
