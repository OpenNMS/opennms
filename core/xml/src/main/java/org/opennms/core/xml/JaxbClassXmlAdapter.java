package org.opennms.core.xml;

import java.io.StringWriter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaxbClassXmlAdapter extends XmlAdapter<String, Object> {
    private static final Logger LOG = LoggerFactory.getLogger(JaxbClassXmlAdapter.class);

    public JaxbClassXmlAdapter() {
        super();
        LOG.info("Initializing JaxbClassXmlAdapter.");
    }

    @Override
    public Object unmarshal(final String xmlText) throws Exception {
        LOG.trace("unmarshal: xml = {}", xmlText);
        if (xmlText == null || xmlText.isEmpty()) {
            return null;
        }
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public String marshal(final Object obj) throws Exception {
        LOG.trace("marshal: object = {}", obj);
        if (obj == null) return "";
        try {
            final String text = JaxbUtils.marshal(obj);
            LOG.debug("marshal: text = {}", text);
            return text == null? "" : text;
        } catch (final Exception e) {
            final StringWriter sw = new StringWriter();
            CastorUtils.marshalWithTranslatedExceptions(obj, sw);
            final String text = sw.toString();
            LOG.debug("marshal: text = {}", text);
            return text == null? "" : text;
        }
    }

}
