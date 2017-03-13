package org.opennms.features.topology.plugins.topo.asset.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.GraphMLWriter;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphGenerator;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfig;
import org.opennms.features.topology.plugins.topo.asset.repo.NodeParamLabels;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class NodeInfoToGraphMLTest {

	private static final Logger LOG = LoggerFactory.getLogger(NodeInfoToGraphMLTest.class);


	public static final String GRAPHML_TEST_TOPOLOGY_FILE_NAME="/graphmlTestTopology2.xml";

	public static void main(String[] args) throws InvalidGraphException {
		GraphML graphML = GraphMLReader.read(NodeInfoToGraphMLTest.class.getResourceAsStream(GRAPHML_TEST_TOPOLOGY_FILE_NAME));
		graphML.getGraphs().forEach(g -> {
			if (!g.getId().startsWith("asset")) {
				g.setId("asset:" + g.getId());
			}
			g.setProperty(GraphMLProperties.NAMESPACE, g.getId());
		});
		graphML.getGraph("asset:nodes").getNodes().forEach(n -> {
			n.setId(AssetGraphGenerator.createIdForNode(String.valueOf((int) n.getProperty(GraphMLProperties.NODE_ID)), n.getProperty(GraphMLProperties.LABEL)));
		});
		graphML.getGraph("asset:asset-rack").getEdges().forEach(e -> {
			e.setId(e.getSource().getId() + "_" + e.getTarget().getId());
		});
		GraphMLWriter.write(graphML, new File("/Users/mvrueden/Desktop/graphml.xml"));
	}

	@Test
	public void verifyGenerationWithNoHierarchies() throws InvalidGraphException, FileNotFoundException {
		// Generate
		final GeneratorConfig config = new GeneratorConfig();
		config.setProviderId("asset");
		config.setLabel("testgraph");
		config.setPreferredLayout("Grid Layout");
		config.setGenerateUnallocated(true);
		config.setAssetLayers(""); // empty layers

		final AssetGraphGenerator assetGraphGenerator = new AssetGraphGenerator(new TestDataProvider());

		final GraphML graphML = assetGraphGenerator.generateGraphs(config);

		// Verify
		assertEquals(1,graphML.getGraphs().size());
	}

	@Test
	public void verifyGenerationWithLayersPopulated() throws InvalidGraphException, FileNotFoundException {
		// Generate
		final GeneratorConfig config = new GeneratorConfig();
		config.setProviderId("asset");
		config.setLabel("testgraph");
		config.setPreferredLayout("Grid Layout");
		config.setGenerateUnallocated(true);
		config.setLayerHierarchies(Lists.newArrayList(
				NodeParamLabels.ASSET_REGION,
				NodeParamLabels.ASSET_BUILDING,
				NodeParamLabels.ASSET_RACK));

		final AssetGraphGenerator assetGraphGenerator = new AssetGraphGenerator(new TestDataProvider());

		final GraphML graphML = assetGraphGenerator.generateGraphs(config);

		// verify
		GraphML expectedGraphML = GraphMLReader.read(getClass().getResourceAsStream(GRAPHML_TEST_TOPOLOGY_FILE_NAME));
		assertEquals(expectedGraphML, graphML);
	}


}
