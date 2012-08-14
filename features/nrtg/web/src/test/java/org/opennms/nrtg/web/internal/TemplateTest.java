package org.opennms.nrtg.web.internal;

import static org.junit.Assert.*;

import org.junit.Test;

public class TemplateTest {

	@Test
	public void test() {
		
		String key = "monkey";
		String value = "island";
		String template = "XXX ${monkey} XXX";
		
		
		String val = template.replaceAll("\\$\\{"+key+"\\}", value);
		
		assertEquals("XXX island XXX", val);
	}

}
