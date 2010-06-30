package org.opennms.sms.monitor.internal.config;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * <p>MobileSequenceNamespacePrefixMapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MobileSequenceNamespacePrefixMapper extends NamespacePrefixMapper {
    /**
     * <p>Constructor for MobileSequenceNamespacePrefixMapper.</p>
     */
    public MobileSequenceNamespacePrefixMapper() {
    }
    
    /** {@inheritDoc} */
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

    /**
     * <p>getPreDeclaredNamespaceUris</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getPreDeclaredNamespaceUris() {
    	return new String[] { };
    }
}
