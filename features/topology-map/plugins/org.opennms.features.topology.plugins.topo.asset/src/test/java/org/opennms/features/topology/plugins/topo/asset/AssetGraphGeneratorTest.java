package org.opennms.features.topology.plugins.topo.asset;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinition;
import org.opennms.features.topology.plugins.topo.asset.layers.NodeParamLabels;
import org.opennms.features.topology.plugins.topo.asset.util.NodeBuilder;
import org.opennms.features.topology.plugins.topo.asset.util.TestNodeProvider;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.collect.Lists;

public class AssetGraphGeneratorTest {

	public static final String GRAPHML_TEST_TOPOLOGY_FILE_NAME="/test-graph-complex.xml";

	@Test
	public void verifyGenerationWithNoHierarchies() throws InvalidGraphException, FileNotFoundException {
		// Generate
		final GeneratorConfig config = new GeneratorConfig();
		config.setProviderId("asset");
		config.setLabel("testgraph");
		config.setPreferredLayout("Grid Layout");
		config.setGenerateUnallocated(true);
		config.setAssetLayers(""); // empty layers

		final AssetGraphGenerator assetGraphGenerator = new AssetGraphGenerator(new TestNodeProvider());

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

		final AssetGraphGenerator assetGraphGenerator = new AssetGraphGenerator(new TestNodeProvider());
		final GraphML generatedGraphML = assetGraphGenerator.generateGraphs(config);

		// Verify Region layer
		Assert.assertEquals(2, generatedGraphML.getGraph(config.getProviderId() + ":" + NodeParamLabels.ASSET_REGION).getNodes().size());
		Assert.assertEquals(9, generatedGraphML.getGraph(config.getProviderId() + ":" + NodeParamLabels.ASSET_BUILDING).getNodes().size());
		Assert.assertEquals(17, generatedGraphML.getGraph(config.getProviderId() + ":" + NodeParamLabels.ASSET_RACK).getNodes().size());

		// verify total graph
		GraphML expectedGraphML = GraphMLReader.read(getClass().getResourceAsStream(GRAPHML_TEST_TOPOLOGY_FILE_NAME));
		assertEquals(expectedGraphML, generatedGraphML);
	}

	@Test
	public void verifySimpleGraphGeneration() throws InvalidGraphException {
		final NodeProvider nodeProvider = new NodeProvider() {
			@Override
			public List<OnmsNode> getNodes(List<LayerDefinition.Mapping> mappings) {
				List<OnmsNode> nodes = new ArrayList<>();
				nodes.add(new NodeBuilder().withId(1).withLabel("Node 1").withAssets().withRegion("Stuttgart").withBuilding("S1").done().getNode());
				nodes.add(new NodeBuilder().withId(2).withLabel("Node 2").withAssets().withRegion("Stuttgart").withBuilding("S2").done().getNode());
				nodes.add(new NodeBuilder().withId(3).withLabel("Node 3").withAssets().withRegion("Fulda").withBuilding("F1").done().getNode());
				nodes.add(new NodeBuilder().withId(4).withLabel("Node 4").withAssets().withRegion("Frankfurt").withBuilding("F1").done().getNode());
				nodes.add(new NodeBuilder().withId(5).withLabel("Node 5").withAssets().withRegion("Frankfurt").withBuilding("F2").done().getNode());
				nodes.add(new NodeBuilder().withId(6).withLabel("Node 6").withAssets().withRegion("Frankfurt").done().getNode());
				nodes.add(new NodeBuilder().withId(7).withLabel("Node 7").withAssets().withBuilding("F2").done().getNode());
				return nodes;
			}
		};

		final GeneratorConfig config = new GeneratorConfig();
		config.setLayerHierarchies(Lists.newArrayList(NodeParamLabels.ASSET_REGION, NodeParamLabels.ASSET_BUILDING));
		final GraphML generatedGraphML = new AssetGraphGenerator(nodeProvider).generateGraphs(config);
		final GraphML expectedGraphML = GraphMLReader.read(getClass().getResourceAsStream("/test-graph-simple.xml"));
		Assert.assertEquals(generatedGraphML, expectedGraphML);
	}

	@Test
	public void verifyGraphGenerationWithCategories() throws InvalidGraphException {
		final NodeProvider nodeProvider = new NodeProvider() {
			@Override
			public List<OnmsNode> getNodes(List<LayerDefinition.Mapping> mappings) {
				List<OnmsNode> nodes = new ArrayList<>();
				nodes.add(new NodeBuilder().withId(1).withLabel("Node 1").withCategories("Server").withAssets().withRegion("Stuttgart").withBuilding("S1").done().getNode());
				nodes.add(new NodeBuilder().withId(2).withLabel("Node 2").withCategories("Server").withAssets().withRegion("Stuttgart").withBuilding("S2").done().getNode());
				nodes.add(new NodeBuilder().withId(3).withLabel("Node 3").withCategories("Server,Router").withAssets().withRegion("Stuttgart").withBuilding("S2").done().getNode());
				return nodes;
			}
		};

		final GeneratorConfig config = new GeneratorConfig();
		config.setLayerHierarchies(Lists.newArrayList(NodeParamLabels.ASSET_REGION, NodeParamLabels.ASSET_BUILDING, NodeParamLabels.NODE_CATEGORIES));
		final GraphML generatedGraphML = new AssetGraphGenerator(nodeProvider).generateGraphs(config);
		final GraphML expectedGraphML = GraphMLReader.read(getClass().getResourceAsStream("/test-graph-categories.xml"));
		Assert.assertEquals(expectedGraphML, generatedGraphML);
	}

}
