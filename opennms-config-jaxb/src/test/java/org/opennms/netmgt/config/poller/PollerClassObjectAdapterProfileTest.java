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
package org.opennms.netmgt.config.poller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.JaxbUtils.MarshallerProfile;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.netmgt.config.pagesequence.Page;
import org.opennms.netmgt.config.pagesequence.PageSequence;
import org.w3c.dom.Element;

public class PollerClassObjectAdapterProfileTest {

    @Test
    public void configAdapterUsesConfigProfile() throws Exception {
        final PageSequence pageSequence = samplePageSequence();
        final PollerClassObjectAdapter adapter = new PollerClassObjectAdapter();

        final Element element = (Element) adapter.marshal(pageSequence);
        assertNotNull(element);
        assertEquals("page-sequence", element.getLocalName());

        final PageSequence restored = (PageSequence) adapter.unmarshal(element);
        assertEquals(pageSequence.getPages().size(), restored.getPages().size());
        assertEquals(pageSequence.getPages().get(0).getPath(), restored.getPages().get(0).getPath());
    }

    @Test
    public void wireAdapterUsesWireProfile() throws Exception {
        final PageSequence pageSequence = samplePageSequence();
        final PollerWireClassObjectAdapter adapter = new PollerWireClassObjectAdapter();

        final Element element = (Element) adapter.marshal(pageSequence);
        assertNotNull(element);
        assertEquals("page-sequence", element.getLocalName());

        final PageSequence restored = (PageSequence) adapter.unmarshal(element);
        assertEquals(pageSequence.getPages().size(), restored.getPages().size());
        assertEquals(pageSequence.getPages().get(0).getPath(), restored.getPages().get(0).getPath());
    }

    @Test
    public void invalidPageSequenceFailsConfigMarshalButSucceedsOnWire() {
        final PageSequence invalid = invalidPageSequence();

        try {
            JaxbUtils.marshalToDomElement(invalid, MarshallerProfile.CONFIG);
            fail("CONFIG marshal should reject XSD-invalid PageSequence");
        } catch (final MarshallingResourceFailureException e) {
            // expected
        }

        final Element wireElement = JaxbUtils.marshalToDomElement(invalid, MarshallerProfile.WIRE);
        assertNotNull(wireElement);
        assertEquals("page-sequence", wireElement.getLocalName());
    }

    @Test
    public void configAdapterRejectsInvalidPageSequence() throws Exception {
        final PollerClassObjectAdapter adapter = new PollerClassObjectAdapter();
        try {
            adapter.marshal(invalidPageSequence());
            fail("CONFIG adapter should reject XSD-invalid PageSequence");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof MarshallingResourceFailureException);
        }
    }

    @Test
    public void wireAdapterDomOutputIsMoreCompactThanConfigAdapter() throws Exception {
        final PageSequence pageSequence = samplePageSequence();
        final String wireXml = elementToString(
                (Element) new PollerWireClassObjectAdapter().marshal(pageSequence));
        final String configXml = elementToString(
                (Element) new PollerClassObjectAdapter().marshal(pageSequence));

        assertFalse(wireXml.contains("\n  "));
        assertTrue(configXml.length() >= wireXml.length());
    }

    @Test
    public void configAdapterDomMarshalMatchesJaxbUtilsConfigMarshal() throws Exception {
        final PageSequence pageSequence = samplePageSequence();
        final PollerClassObjectAdapter adapter = new PollerClassObjectAdapter();

        final Element element = (Element) adapter.marshal(pageSequence);
        final PageSequence fromElement = JaxbUtils.unmarshal(PageSequence.class, elementToString(element), false);
        final PageSequence fromConfig = JaxbUtils.unmarshal(PageSequence.class,
                JaxbUtils.marshal(pageSequence), false);

        assertEquals(fromConfig.getPages().get(0).getPath(), fromElement.getPages().get(0).getPath());
    }

    private static PageSequence samplePageSequence() {
        final PageSequence pageSequence = new PageSequence();
        final Page page = new Page();
        page.setPath("/Login.do");
        pageSequence.addPage(page);
        return pageSequence;
    }

    /** XSD requires at least one {@code page} child; an empty sequence is invalid. */
    private static PageSequence invalidPageSequence() {
        return new PageSequence();
    }

    private static String elementToString(final Element element) throws Exception {
        final javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        final java.io.StringWriter writer = new java.io.StringWriter();
        transformer.transform(new javax.xml.transform.dom.DOMSource(element),
                new javax.xml.transform.stream.StreamResult(writer));
        return writer.toString();
    }
}
