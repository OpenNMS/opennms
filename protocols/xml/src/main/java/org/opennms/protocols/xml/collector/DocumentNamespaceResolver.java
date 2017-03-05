/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.xml.collector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Simple namespace resolver that deflects namespaceURI 
 * resolution to the document. 
 *  
 * @author <a href="mailto:david.schlenk@spanlink.com">David Schlenk</a>
 *
 */
public class DocumentNamespaceResolver implements NamespaceContext {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DocumentNamespaceResolver.class);

    private Document m_document;
    private Map<String, String> m_prefixUriMap = new HashMap<String, String>();

    public DocumentNamespaceResolver(Document document) {
        super();
        m_document = document;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        String ret = null;
        if(XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)){
            LOG.debug("getNamespaceURI: no prefix");
            return m_document.lookupNamespaceURI(null);
        }else{
            if(m_prefixUriMap.containsKey(prefix)){
                ret = m_prefixUriMap.get(prefix);
                LOG.debug("getNamespaceURI: found cached namespace uri for prefix {}: {}", prefix, ret);
            }else{
                ret = m_document.lookupNamespaceURI(prefix);
                LOG.debug("getNamespaceURI: found namespace uri for prefix {}: {} in doc.", prefix, ret);
                m_prefixUriMap.put(prefix, ret);
            }
            return ret;
        }
    }

    // Think about doing some caching here too if this method ever gets used. 
    @Override
    public String getPrefix(String namespaceURI) {
        return m_document.lookupPrefix(namespaceURI);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }

}
