package org.opennms.netmgt.provision.persist;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class ProvisionNamespacePrefixMapper extends NamespacePrefixMapper {
    private String m_defaultNamespace;

    public ProvisionNamespacePrefixMapper() {
    }
    
    public ProvisionNamespacePrefixMapper(String defaultNamespace) {
        m_defaultNamespace = defaultNamespace;
    }

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
