package org.opennms.features.topology.plugins.topo.asset.test;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLWriter;
import org.opennms.features.graphml.model.InvalidGraphException;

public class NodeInfoToGraphMLTest {

	@Test
	public void test() throws InvalidGraphException {
		GraphML graphML = new GraphML();
		GraphMLWriter.write(graphML , new File("target/output.graphml"));
	}

}
