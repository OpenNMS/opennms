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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class JaxbClassObjectAdapter extends XmlAdapter<Object, Object> {
    private static final Logger LOG = LoggerFactory.getLogger(JaxbClassObjectAdapter.class);
    private final Map<String,Class<?>> m_knownElementClasses = new HashMap<>();

    public JaxbClassObjectAdapter() {
        LOG.debug("Initializing JaxbClassObjectAdapter.");
    }

    public JaxbClassObjectAdapter(Class<?>... clazzes) {
        LOG.debug("Initializing JaxbClassObjectAdapter with {} classes.", clazzes.length);
        for (Class<?> clazz : clazzes) {
            final XmlRootElement annotation = clazz.getAnnotation(XmlRootElement.class);
            if (annotation != null) {
                m_knownElementClasses.put(annotation.name().toLowerCase(), clazz);
            }
        }
    }

    @Override
    public Object unmarshal(final Object from) throws Exception {
        LOG.trace("unmarshal: from = ({}){}", (from == null? null : from.getClass()), from);
        if (from == null) return null;

        if (from instanceof Node) {
            final Node e = (Node)from;
            e.normalize();
            final String nodeName = e.getNodeName();
            final Class<?> clazz = getClassForElement(nodeName);
            LOG.trace("class type = {} (node name = {})", clazz, nodeName);
            // JAXB has already turned this into an element, but we need to re-parse the XML.

            if (clazz == null) {
                LOG.warn("Unable to determine object type for node name {}. Known elements include: {}", nodeName, m_knownElementClasses);
                return from;
            }

            final DOMImplementationLS lsImpl = (DOMImplementationLS)e.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
            LSSerializer serializer = lsImpl.createLSSerializer();
            serializer.getDomConfig().setParameter("xml-declaration", false); //by default its true, so set it to false to get String without xml-declaration
            final String str = serializer.writeToString(e);

            return JaxbUtils.unmarshal(clazz, str);
        } else {
            LOG.error("Unsure how to determine which class to use for unmarshaling object type {}", from.getClass());
            throw new IllegalArgumentException("Unsure how to determine which class to use for unmarshaling object type " + from.getClass());
        }
    }

    @Override
    public Object marshal(final Object from) throws Exception {
        LOG.trace("marshal: from = ({}){}", (from == null? null : from.getClass()), from);
        if (from == null) return null;

        try {
            final String s = JaxbUtils.marshal(from);
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document doc = builder.parse(new ByteArrayInputStream(s.getBytes()));
            final Node node = doc.getDocumentElement();
            LOG.trace("marshal: node = {}", node);
            return node;
        } catch (final Exception e) {
            final IllegalArgumentException ex = new IllegalArgumentException("Unable to marshal object " + from, e);
            LOG.error("Unable to marshal object {}", from, ex);
            throw ex;
        }
    }

    public Class<?> getClassForElement(String nodeName) {
        final Class<?> clazz = m_knownElementClasses.get(nodeName.toLowerCase());
        if (clazz != null) {
            return clazz;
        }
        return JaxbUtils.getClassForElement(nodeName);
    }
}
