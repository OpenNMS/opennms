package org.opennms.web.rest;

import java.util.Date;
import java.util.TimeZone;

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
