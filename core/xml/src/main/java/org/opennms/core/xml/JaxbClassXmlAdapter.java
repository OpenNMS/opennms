/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
