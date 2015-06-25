/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.api;

import java.util.Date;
import java.util.TimeZone;

import org.opennms.web.api.ISO8601DateEditor;

import junit.framework.TestCase;

public class ISO8601DateEditorTest extends TestCase {
	
	private ISO8601DateEditor editor;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		editor=new ISO8601DateEditor();
	}

	public void testIsPaintable() {
		assertFalse("IsPaintable must be false", editor.isPaintable());
	}
	
	public void testSetAsTextLong() {
		assertNull(editor.getValue());
		editor.setAsText("1");
		assertNotNull(editor.getValue());
		assertEquals(editor.getValue().getClass(), java.util.Date.class);
		Date dateValue=(Date)editor.getValue();
		assertEquals(1,dateValue.getTime());
	}
	
	public void testSetAsTextFullDateString() {
		assertNull(editor.getValue());
		editor.setAsText("1970-01-01T00:00:00.000+00:00");
		assertNotNull(editor.getValue());
		assertEquals(editor.getValue().getClass(), java.util.Date.class);
		Date dateValue=(Date)editor.getValue();
		assertEquals(0, dateValue.getTime());
	}
	
	public void testSetAsTextFullDateString2() {
		assertNull(editor.getValue());
		editor.setAsText("1970-01-02T00:00:00.000+00:00");
		assertNotNull(editor.getValue());
		assertEquals(editor.getValue().getClass(), java.util.Date.class);
		Date dateValue=(Date)editor.getValue();
		assertEquals(86400000, dateValue.getTime());
	}
	
	public void testGetAsText1() {
		editor.setValue(new Date(1));
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		String formatted=editor.getAsText();
		assertEquals("1970-01-01T00:00:00.001Z", formatted);
	}

}
