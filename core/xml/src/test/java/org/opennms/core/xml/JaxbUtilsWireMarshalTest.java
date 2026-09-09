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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils.MarshallerProfile;
import org.w3c.dom.Element;

public class JaxbUtilsWireMarshalTest {

    @XmlRootElement(name = "wire-test")
    public static class WireTestBean {
        private String value;
        private int count;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    private static WireTestBean sampleBean() {
        final WireTestBean bean = new WireTestBean();
        bean.setValue("hello");
        bean.setCount(42);
        return bean;
    }

    @Test
    public void wireMarshallerIsMoreCompactThanConfig() throws Exception {
        final WireTestBean bean = sampleBean();
        final JAXBContext context = JaxbUtils.getContextFor(WireTestBean.class);

        final StringWriter wireWriter = new StringWriter();
        JaxbUtils.createWireMarshaller(context).marshal(bean, wireWriter);
        final String wireXml = wireWriter.toString();

        final StringWriter configWriter = new StringWriter();
        JaxbUtils.getMarshallerFor(WireTestBean.class, context, MarshallerProfile.CONFIG).marshal(bean, configWriter);
        final String configXml = configWriter.toString();

        assertFalse(wireXml.contains("\n  "));
        assertTrue(configXml.length() >= wireXml.length());
    }

    @Test
    public void marshalToDomElementProducesRootElement() {
        final Element element = JaxbUtils.marshalToDomElement(sampleBean(), MarshallerProfile.WIRE);
        assertNotNull(element);
        assertEquals("wire-test", element.getLocalName());
    }

    @Test
    public void marshalToDomElementWithConfigProducesRootElement() {
        final Element element = JaxbUtils.marshalToDomElement(sampleBean(), MarshallerProfile.CONFIG);
        assertNotNull(element);
        assertEquals("wire-test", element.getLocalName());
    }

    @Test
    public void wireAndConfigMarshalProduceSemanticallyEqualObjects() throws Exception {
        final WireTestBean bean = sampleBean();
        final JAXBContext context = JaxbUtils.getContextFor(WireTestBean.class);

        final StringWriter wireWriter = new StringWriter();
        JaxbUtils.getMarshallerFor(WireTestBean.class, context, MarshallerProfile.WIRE).marshal(bean, wireWriter);

        final StringWriter configWriter = new StringWriter();
        JaxbUtils.getMarshallerFor(WireTestBean.class, context, MarshallerProfile.CONFIG).marshal(bean, configWriter);

        final WireTestBean fromWire = JaxbUtils.unmarshal(WireTestBean.class, wireWriter.toString(), false);
        final WireTestBean fromConfig = JaxbUtils.unmarshal(WireTestBean.class, configWriter.toString(), false);
        assertEquals(fromWire.getValue(), fromConfig.getValue());
        assertEquals(fromWire.getCount(), fromConfig.getCount());
    }

    @Test
    public void jaxbClassObjectAdapterRoundTrip() throws Exception {
        final WireTestBean original = sampleBean();
        final JaxbClassObjectAdapter adapter = new JaxbClassObjectAdapter(WireTestBean.class);

        final Object marshalled = adapter.marshal(original);
        assertTrue(marshalled instanceof Element);

        final Object unmarshalled = adapter.unmarshal(marshalled);
        assertTrue(unmarshalled instanceof WireTestBean);
        final WireTestBean restored = (WireTestBean) unmarshalled;
        assertEquals(original.getValue(), restored.getValue());
        assertEquals(original.getCount(), restored.getCount());
    }

    @Test
    public void xmlHandlerForWireRoundTrip() {
        final WireTestBean original = sampleBean();
        final XmlHandler<WireTestBean> handler = XmlHandler.forWire(WireTestBean.class);

        final String xml = handler.marshal(original);
        assertNotNull(xml);
        assertFalse(xml.isEmpty());

        final WireTestBean restored = handler.unmarshal(xml);
        assertEquals(original.getValue(), restored.getValue());
        assertEquals(original.getCount(), restored.getCount());
    }

    @Test
    public void wireMarshallerIsCachedPerThread() throws Exception {
        final Marshaller first = JaxbUtils.getMarshallerFor(WireTestBean.class, null, MarshallerProfile.WIRE);
        final Marshaller second = JaxbUtils.getMarshallerFor(WireTestBean.class, null, MarshallerProfile.WIRE);
        assertEquals(first, second);
    }
}
