/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
