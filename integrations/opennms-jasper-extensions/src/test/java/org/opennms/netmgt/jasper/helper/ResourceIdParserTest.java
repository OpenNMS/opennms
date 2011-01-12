package org.opennms.netmgt.jasper.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.netmgt.jasper.helper.ResourceIdParser;


public class ResourceIdParserTest {
	
	
	@Test
	public void testGetNodeId() {
		String resourceId = "node[7].responseTime[172.20.1.5]";
		
		ResourceIdParser parser = new ResourceIdParser();
		assertEquals("7", parser.getNodeId(resourceId));
	}
	
	@Test
	public void testGetResource() {
		String resourceId = "node[7].responseTime[172.20.1.5]";
		
		ResourceIdParser parser = new ResourceIdParser();
		assertEquals("172.20.1.5", parser.getResource(resourceId));
	}
	
}
