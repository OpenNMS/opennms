package org.opennms.features.topology.plugins.topo.simple.internal;

import org.junit.Test;
import org.opennms.features.topology.plugins.devutils.internal.SaveToXmlOperation;

public class SaveToXmlTest {

	
	@Test
	public void testSave() {
		
		SimpleTopologyProvider simpleTopo = new SimpleTopologyProvider();
		simpleTopo.load("test-graph.xml");
		
		SaveToXmlOperation saver = new SaveToXmlOperation(simpleTopo);
		
		saver.execute(null, null);
		
		
		
	}
}
