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
package org.opennms.web.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

public class ISO8601DateEditorTest {
    private ISO8601DateEditor m_editor;

    @Before
    public void setUp() throws Exception {
        m_editor = new ISO8601DateEditor();
    }

    @Test
    public void testIsPaintable() {
        assertFalse("IsPaintable must be false", m_editor.isPaintable());
    }

    @Test
    public void testSetAsTextLong() {
        assertNull(m_editor.getValue());
        m_editor.setAsText("1");
        assertNotNull(m_editor.getValue());
        assertEquals(m_editor.getValue().getClass(), java.util.Date.class);
        Date dateValue=(Date)m_editor.getValue();
        assertEquals(1,dateValue.getTime());
    }

    @Test
    public void testSetAsTextFullDateString() {
        assertNull(m_editor.getValue());
        m_editor.setAsText("1970-01-01T00:00:00.000+00:00");
        assertNotNull(m_editor.getValue());
        assertEquals(m_editor.getValue().getClass(), java.util.Date.class);
        Date dateValue=(Date)m_editor.getValue();
        assertEquals(0, dateValue.getTime());
    }

    @Test
    public void testSetAsTextFullDateString2() {
        assertNull(m_editor.getValue());
        m_editor.setAsText("1970-01-02T00:00:00.000+00:00");
        assertNotNull(m_editor.getValue());
        assertEquals(m_editor.getValue().getClass(), java.util.Date.class);
        Date dateValue=(Date)m_editor.getValue();
        assertEquals(86400000, dateValue.getTime());
    }

    @Test
    public void testSetAsTextFullDateStringNoColon() {
        assertNull(m_editor.getValue());
        m_editor.setAsText("1970-01-02T00:00:00.000+0000");
        assertNotNull(m_editor.getValue());
        assertEquals(m_editor.getValue().getClass(), java.util.Date.class);
        Date dateValue=(Date)m_editor.getValue();
        assertEquals(86400000, dateValue.getTime());
    }

    @Test
    public void testSetAsTextFullDateStringNegativeOffset() {
        assertNull(m_editor.getValue());
        m_editor.setAsText("1970-01-02T00:00:00.000-0000");
        assertNotNull(m_editor.getValue());
        assertEquals(m_editor.getValue().getClass(), java.util.Date.class);
        Date dateValue=(Date)m_editor.getValue();
        assertEquals(86400000, dateValue.getTime());
    }

    @Test
    public void testGetAsText1() {
        m_editor.setValue(new Date(1));
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String formatted=m_editor.getAsText();
        assertEquals("1970-01-01T00:00:00.001Z", formatted);
    }

}
