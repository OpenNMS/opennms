/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Test;
import org.opennms.netmgt.poller.PollerParameter;
import org.w3c.dom.Element;

import com.sun.org.apache.xerces.internal.dom.ElementImpl;

public class PollerParameterTest {

    @Test
    public void testSimple() throws Exception {
        final PollerParameter pp = PollerParameter.simple("test");
        assertTrue(pp.asSimple().isPresent());
        assertFalse(pp.asComplex().isPresent());

        assertEquals("test", pp.asSimple().get().getValue());
    }

    @Test
    public void testComplex() throws Exception {
        final PollerParameter pp = PollerParameter.complex(new ElementImpl(){});
        assertFalse(pp.asSimple().isPresent());
        assertTrue(pp.asComplex().isPresent());
    }

    @Test
    public void testComplexEquals() throws Exception {
        final PollerParameter pp1 = PollerParameter.marshall(new TestData("test", 42));
        final PollerParameter pp2 = PollerParameter.marshall(new TestData("test", 42));
        assertEquals(pp1, pp2);
    }

    @Test
    public void testMarshall() throws Exception {
        final PollerParameter pp = PollerParameter.marshall(new TestData("test", 42));
        assertFalse(pp.asSimple().isPresent());
        assertTrue(pp.asComplex().isPresent());

        final Element element = pp.asComplex().get().getElement();
        assertEquals("test-data", element.getTagName());
        assertEquals("test", element.getAttribute("field1"));
        assertEquals("42", element.getAttribute("field2"));
    }

    @Test
    public void testUnmarshall() throws Exception {
        final PollerParameter pp = PollerParameter.marshall(new TestData("test", 42));

        final TestData unmarshalled = pp.asComplex().get().getInstance(TestData.class);
        assertEquals("test", unmarshalled.getField1());
        assertEquals(42, unmarshalled.getField2());
    }

    @XmlRootElement(name="test-data")
    public static class TestData {
        private String field1;
        private int field2;

        public TestData() {
        }

        public TestData(final String field1, final int field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        @XmlAttribute(name="field1")
        public String getField1() {
            return this.field1;
        }

        public void setField1(final String field1) {
            this.field1 = field1;
        }

        @XmlAttribute(name="field2")
        public int getField2() {
            return this.field2;
        }

        public void setField2(final int field2) {
            this.field2 = field2;
        }
    }
}
