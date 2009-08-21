package org.opennms.sms.monitor.internal.config;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class SmsSequenceNamespacePrefixMapper extends NamespacePrefixMapper {
    public SmsSequenceNamespacePrefixMapper() {
    }
    
    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri)) {
            return "xsi";
        }

        if("http://xmlns.opennms.org/xsd/config/sms-sequence".equals(namespaceUri)) {
            return "sms";
        }

        return suggestion;
    }
    
    public String[] getPreDeclaredNamespaceUris() {
    	return new String[] { "sms" };
    }
}
