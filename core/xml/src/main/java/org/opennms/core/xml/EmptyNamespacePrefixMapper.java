package org.opennms.core.xml;

import org.eclipse.persistence.oxm.NamespacePrefixMapper;

public class EmptyNamespacePrefixMapper extends NamespacePrefixMapper {
    @Override
    public String getPreferredPrefix(final String namespaceUri, final String suggestion, final boolean requirePrefix) {
        return "";
    }
}
