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
package org.opennms.netmgt.xml.eventconf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;

/**
 * Whitespace around a UEI is never part of it (NMS-20608).
 */
public class EventUeiTrimTest {

    @Test
    public void setUeiTrimsSurroundingWhitespace() {
        final Event e = new Event();
        e.setUei("\t uei.opennms.org/x \r\n");
        assertEquals("uei.opennms.org/x", e.getUei());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUeiRejectsWhitespaceOnly() {
        new Event().setUei(" \n\t ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUeiRejectsUnicodeSpacesOnly() {
        // String.trim() keeps U+2003 (em space) and U+3000 (ideographic space)
        new Event().setUei("\u2003 \u3000");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUeiRejectsEmpty() {
        new Event().setUei("");
    }

    /** Pins the package-level StringTrimAdapter that every upload, edit and reload relies on. */
    @Test
    public void jaxbParseTrimsPaddedUei() {
        final Event e = JaxbUtils.unmarshal(Event.class,
                "<event xmlns=\"http://xmlns.opennms.org/xsd/eventconf\"><uei>\n   uei.opennms.org/x\n</uei>"
                + "<event-label>L</event-label><descr>d</descr><severity>Normal</severity></event>");
        assertEquals("uei.opennms.org/x", e.getUei());
        assertTrue(JaxbUtils.marshal(e).contains("<uei>uei.opennms.org/x</uei>"));
    }
}
