/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.plugins.topo.asset;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.GraphMLWriter;
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
		config.setLayerHierarchies(new ArrayList<>()); // empty layers
		config.setFilters(new ArrayList<>()); // empty filters

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
		config.setLayerHierarchies(Lists.newArrayList(
				NodeParamLabels.ASSET_REGION,
				NodeParamLabels.ASSET_BUILDING,
				NodeParamLabels.ASSET_RACK));
		config.setFilters(new ArrayList<>()); // empty filters

		final AssetGraphGenerator assetGraphGenerator = new AssetGraphGenerator(new TestNodeProvider());
		final GraphML generatedGraphML = assetGraphGenerator.generateGraphs(config);

		// Verify Region layer
		Assert.assertEquals(2, generatedGraphML.getGraph(config.getProviderId() + ":" + NodeParamLabels.ASSET_REGION).getNodes().size());
		Assert.assertEquals(9, generatedGraphML.getGraph(config.getProviderId() + ":" + NodeParamLabels.ASSET_BUILDING).getNodes().size());
		Assert.assertEquals(17, generatedGraphML.getGraph(config.getProviderId() + ":" + NodeParamLabels.ASSET_RACK).getNodes().size());

		// for debug generate file
		GraphMLWriter.write(generatedGraphML, new File("target/verifyGenerationWithLayersPopulatedGenerated.graphml"));
		
		// verify total graph
		GraphML expectedGraphML = GraphMLReader.read(getClass().getResourceAsStream(GRAPHML_TEST_TOPOLOGY_FILE_NAME));
		assertEquals(expectedGraphML, generatedGraphML);
	}

	@Test
	public void verifySimpleGraphGeneration() throws InvalidGraphException {
		final NodeProvider nodeProvider = new NodeProvider() {
			@Override
			public List<OnmsNode> getNodes(List<LayerDefinition> definitions) {
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
		config.setFilters(new ArrayList<>()); // empty filters
		final GraphML generatedGraphML = new AssetGraphGenerator(nodeProvider).generateGraphs(config);
		final GraphML expectedGraphML = GraphMLReader.read(getClass().getResourceAsStream("/test-graph-simple.xml"));
		
		// for debug generate file
		GraphMLWriter.write(generatedGraphML, new File("target/verifySimpleGraphGenerationGenerated.graphml"));
		
		Assert.assertEquals(generatedGraphML, expectedGraphML);
	}

	@Test
	public void verifyGraphGenerationWithCategories() throws InvalidGraphException {
		final NodeProvider nodeProvider = new NodeProvider() {
			@Override
			public List<OnmsNode> getNodes(List<LayerDefinition> definitions) {
				List<OnmsNode> nodes = new ArrayList<>();
				nodes.add(new NodeBuilder().withId(1).withLabel("Node 1").withCategories("Server").withAssets().withRegion("Stuttgart").withBuilding("S1").done().getNode());
				nodes.add(new NodeBuilder().withId(2).withLabel("Node 2").withCategories("Server").withAssets().withRegion("Stuttgart").withBuilding("S2").done().getNode());
				nodes.add(new NodeBuilder().withId(3).withLabel("Node 3").withCategories("Server,Router").withAssets().withRegion("Stuttgart").withBuilding("S2").done().getNode());
				return nodes;
			}
		};

		final GeneratorConfig config = new GeneratorConfig();
		config.setLayerHierarchies(Lists.newArrayList(NodeParamLabels.ASSET_REGION, NodeParamLabels.ASSET_BUILDING, NodeParamLabels.NODE_CATEGORIES));
		config.setFilters(new ArrayList<>()); // empty filters
		final GraphML generatedGraphML = new AssetGraphGenerator(nodeProvider).generateGraphs(config);
		
		// for debug generate file
		GraphMLWriter.write(generatedGraphML, new File("target/verifyGraphGenerationWithCategoriesGenerated.graphml"));
		
		final GraphML expectedGraphML = GraphMLReader.read(getClass().getResourceAsStream("/test-graph-categories.xml"));
		Assert.assertEquals(expectedGraphML, generatedGraphML);
	}

	@Test
	public void verifyFilter() throws InvalidGraphException {
		final NodeProvider nodeProvider = new NodeProvider() {
			@Override
			public List<OnmsNode> getNodes(List<LayerDefinition> definitions) {
				List<OnmsNode> nodes = new ArrayList<>();
				nodes.add(new NodeBuilder().withId(1).withLabel("Node 1").withAssets().withRegion("Stuttgart").withBuilding("S1").done().getNode());
				nodes.add(new NodeBuilder().withId(2).withLabel("Node 2").withAssets().withRegion("Fulda").withBuilding("F1").done().getNode());
				return nodes;
			}
		};

		final GeneratorConfig config = new GeneratorConfig();
		config.setLayerHierarchies(Lists.newArrayList(NodeParamLabels.ASSET_REGION));
		config.setFilters(Lists.newArrayList(String.format("%s=Stuttgart", NodeParamLabels.ASSET_REGION)));

		final GraphML generatedGraphML = new AssetGraphGenerator(nodeProvider).generateGraphs(config);
		Assert.assertEquals(2, generatedGraphML.getGraphs().size());
		Assert.assertEquals(1, generatedGraphML.getGraphs().get(0).getNodes().size());
		Assert.assertEquals(1, generatedGraphML.getGraphs().get(1).getNodes().size());
	}

}
