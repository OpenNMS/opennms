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
package org.opennms.core.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class SimpleNamespaceFilter extends XMLFilterImpl {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleNamespaceFilter.class);

    private String m_namespaceUri;
    private boolean m_addNamespace = false;
    private boolean m_addedNamespace = false;

    public SimpleNamespaceFilter(final String namespaceUri, final boolean addNamespace) {
        super();

        LOG.trace("SimpleNamespaceFilter initalized with namespace {} ({})", namespaceUri, Boolean.valueOf(addNamespace));
        if (addNamespace) {
            this.m_namespaceUri = namespaceUri.intern();
        } else { 
            this.m_namespaceUri = "".intern();
        }
        this.m_addNamespace = addNamespace;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        if (m_addNamespace) {
            startControlledPrefixMapping();
        }
    }
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if (m_addNamespace) {
            LOG.trace("start: uri = {}, new uri = {}, localName = {}, qName = {}, attributes = {}", uri, m_namespaceUri, localName, qName, attributes);

            final String type = attributes.getValue("http://www.w3.org/2001/XMLSchema-instance", "type");

            // we found an xsi:type annotation, ignore to avoid, eg:
            // org.xml.sax.SAXParseException: cvc-elt.4.2: Cannot resolve 'events' to a type definition for element 'events'.
            if (type != null) {
                final AttributesImpl att = new AttributesImpl();
                for (int i = 0; i < attributes.getLength(); i++) {
                    if (!attributes.getLocalName(i).equals("type") || !attributes.getURI(i).equals("http://www.w3.org/2001/XMLSchema-instance")) {
                        att.addAttribute(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i), attributes.getType(i), attributes.getValue(i));
                    }
                }
                super.startElement(m_namespaceUri, localName, qName, att);
            } else {
                super.startElement(m_namespaceUri, localName, qName, attributes);
            }
        }  else {
            LOG.trace("start: uri = {}, new uri = {}, localName = {}, qName = {}, attributes = {}", uri, uri, localName, qName, attributes);
            super.startElement(uri, localName, qName, attributes);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if(m_addNamespace) {
            LOG.trace("end:   uri = {}, new uri = {}, localName = {}, qName = {}", uri, m_namespaceUri, localName, qName);
            super.endElement(m_namespaceUri, localName, qName);
        } else {
            LOG.trace("end:   uri = {}, new uri = {}, localName = {}, qName = {}", uri, uri, localName, qName);
            super.endElement(uri, localName, qName);
        }
    }

    @Override
    public void startPrefixMapping(final String prefix, final String url) throws SAXException {
        LOG.trace("startPrefixMapping: prefix = {}, url = {}", prefix, url);
        if (m_addNamespace) {
            this.startControlledPrefixMapping();
        } else {
            super.startPrefixMapping(prefix, url);
        }

    }

    private void startControlledPrefixMapping() throws SAXException {
        LOG.trace("startControlledPrefixMapping");
        if (m_addNamespace && !m_addedNamespace) {
            //We should add namespace since it is set and has not yet been done.
            super.startPrefixMapping("".intern(), m_namespaceUri);

            //Make sure we don't do it twice
            m_addedNamespace = true;
        }
    }
    
    @Override
    public String toString() {
        return "SimpleNamespaceFilter[namespaceUri=" + m_namespaceUri + ",addNamespace=" + m_addNamespace + "]";
    }
}
