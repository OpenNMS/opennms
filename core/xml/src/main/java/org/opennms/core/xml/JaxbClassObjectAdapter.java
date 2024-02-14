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
