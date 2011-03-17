package org.opennms.netmgt.xml;

import org.opennms.core.utils.LogUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class SimpleNamespaceFilter extends XMLFilterImpl {
    private String m_usedNamespaceUri;
    private boolean m_addNamespace = false;
    private boolean m_addedNamespace = false;

    public SimpleNamespaceFilter(final String namespaceUri, final boolean addNamespace) {
        super();

        if (addNamespace) {
            this.m_usedNamespaceUri = namespaceUri;
        } else { 
            this.m_usedNamespaceUri = "";
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
    	LogUtils.debugf(this, "start: uri = %s, localName = %s, qName = %s, attributes = %s", uri, localName, qName, attributes);
    	if (m_addNamespace) {
    		super.startElement(m_usedNamespaceUri, localName, qName, attributes);
    	}  else {
    		super.startElement(uri, localName, qName, attributes);
    	}
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
    	LogUtils.debugf(this, "end:   uri = %s, localName = %s, qName = %s", uri, localName, qName);
    	if(m_addNamespace) {
    		super.endElement(m_usedNamespaceUri, localName, qName);
    	} else {
    		super.endElement(uri, localName, qName);
    	}
    }

    @Override
    public void startPrefixMapping(final String prefix, final String url) throws SAXException {
        if (m_addNamespace) {
            this.startControlledPrefixMapping();
        } else {
        	super.startPrefixMapping(prefix, url);
        }

    }

    private void startControlledPrefixMapping() throws SAXException {
        if (m_addNamespace && !m_addedNamespace) {
            //We should add namespace since it is set and has not yet been done.
            super.startPrefixMapping("", m_usedNamespaceUri);

            //Make sure we don't do it twice
            m_addedNamespace = true;
        }
    }
}
