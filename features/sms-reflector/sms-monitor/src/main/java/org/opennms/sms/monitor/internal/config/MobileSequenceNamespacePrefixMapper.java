package org.opennms.sms.monitor.internal.config;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class MobileSequenceNamespacePrefixMapper extends NamespacePrefixMapper {
    public MobileSequenceNamespacePrefixMapper() {
    }
    
    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri)) {
            return "xsi";
        }

        if("http://xmlns.opennms.org/xsd/config/mobile-sequence".equals(namespaceUri)) {
            return "";
        }

        return suggestion;
    }

    public String[] getPreDeclaredNamespaceUris() {
    	return new String[] { };
    }
}
