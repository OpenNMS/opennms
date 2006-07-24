package org.opennms.secret.model.test;

import junit.framework.TestCase;

import org.opennms.secret.model.DataSource;
import org.opennms.secret.model.GraphDataLine;
import org.opennms.secret.model.GraphDefinition;

public class GraphDefinitionTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGraphDataElement() throws Exception {
		DataSource ds = new DataSource();
		ds.setDataSource("test_graphDataLine");
		ds.setId("test_graphDataLine");
		ds.setName("test name");
		ds.setSource("/root/test/rrd");		
		GraphDataLine gdl_1 = new GraphDataLine(ds);	
		GraphDataLine gdl_2 = new GraphDataLine(ds);	
		assertFalse(gdl_1.getUniqueID().equals(gdl_2.getUniqueID()));
		
	}
		
		

	public void testCreate() throws Exception {
		// populate Graph Definition
		DataSource ds = new DataSource();
		ds.setDataSource("test");
		ds.setId("test");
		ds.setName("test name");
		ds.setSource("/root/test/rrd");		
		GraphDataLine gdl = new GraphDataLine(ds);	
		GraphDefinition gdef = new GraphDefinition();
		gdef.addGraphDataElement(gdl);
		
		
	}
}
