/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { };
    }
}
