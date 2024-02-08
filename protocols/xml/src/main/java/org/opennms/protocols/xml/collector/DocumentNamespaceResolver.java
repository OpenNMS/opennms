/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
