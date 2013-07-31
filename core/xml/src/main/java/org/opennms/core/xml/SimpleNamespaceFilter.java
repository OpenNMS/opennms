/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

        LOG.debug("SimpleNamespaceFilter initalized with namespace {} ({})", namespaceUri, Boolean.valueOf(addNamespace));
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
}
