package org.opennms.features.topology.api.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.AbstractVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;

public class SavedHistoryTest {
	@Test
	public void testMarshall() {
		Map<String,String> settings = new HashMap<String,String>();
		settings.put("hello", "world");

		VertexRef vert1 = new AbstractVertexRef("nodes", "1");
		VertexRef vert2 = new AbstractVertexRef("nodes", "2", "HasALabel");

		Map<VertexRef,Point> locations = new HashMap<VertexRef,Point>();
		locations.put(vert1, new Point(0, 0));
		locations.put(vert2, new Point(0, 0));

		SavedHistory savedHistory = new SavedHistory(
				0, 
				new BoundingBox(0,0,100,100), 
				locations,
				Collections.singleton(vert2),
				settings
		);
		JAXB.marshal(savedHistory, System.out);
	}
}
