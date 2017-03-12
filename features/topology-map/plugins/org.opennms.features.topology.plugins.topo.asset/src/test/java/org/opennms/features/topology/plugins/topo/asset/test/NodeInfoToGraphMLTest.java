package org.opennms.features.topology.plugins.topo.asset.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLWriter;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphGenerator;
import org.opennms.features.topology.plugins.topo.asset.repo.NodeInfoRepository;
import org.opennms.features.topology.plugins.topo.asset.repo.NodeParamLabels;
import org.opennms.features.topology.plugins.topo.asset.repo.Utils;
import org.opennms.features.topology.plugins.topo.asset.repo.xml.NodeInfoRepositoryXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeInfoToGraphMLTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(NodeInfoToGraphMLTest.class);
	
	public static final String TEST_RESOURCE_FOLDER="./src/test/resources";
	public static final String NODE_TEST_DATA_FILE_NAME="nodeInfoMockTestData2.xml";
	public static final String GRAPHML_TEST_TOPOLOGY_FILE_NAME="graphmlTestTopology2.xml";

//	@Test
//	public void test() throws InvalidGraphException {
//		GraphML graphML = new GraphML();
//		GraphMLWriter.write(graphML , new File("target/output.graphml"));
//	}

	//layerHierarch empty
	@Test
	public void assetGraphGeneratorTest1() throws InvalidGraphException {
		LOG.debug("start of assetGraphGeneratorTest1");
		//create and populate new NodeInfoRepository from xml file
		NodeInfoRepository nodeInfoRepository= new NodeInfoRepository();
		Map<String, Map<String, String>> nodeInfo = nodeInfoRepository.getNodeInfo();

		String nodeInfoXmlStr = Utils.readFileFromDisk(TEST_RESOURCE_FOLDER, NODE_TEST_DATA_FILE_NAME);
		NodeInfoRepositoryXML.XMLtoNodeInfo(nodeInfo, nodeInfoXmlStr);

		AssetGraphGenerator assetGraphGenerator=new AssetGraphGenerator();

		String menuLabelStr="testgraph";
		List<String> layerHierarchy= new ArrayList<String>(); //Arrays.asList(a);
		String preferredLayout="Grid Layout";
		GraphML graphML = assetGraphGenerator.nodeInfoToTopology(nodeInfoRepository.getNodeInfo(), menuLabelStr,layerHierarchy, preferredLayout);
		
		GraphMLWriter.write(graphML , new File("target/output.graphml"));
		
		LOG.debug("end of assetGraphGeneratorTest1");
	}
	
	// layerhierarchy populated
	@Test
	public void assetGraphGeneratorTest2() throws InvalidGraphException {
		LOG.debug("start of assetGraphGeneratorTest2");
		//create and populate new NodeInfoRepository from xml file
		NodeInfoRepository nodeInfoRepository= new NodeInfoRepository();
		Map<String, Map<String, String>> nodeInfo = nodeInfoRepository.getNodeInfo();

		String nodeInfoXmlStr = Utils.readFileFromDisk(TEST_RESOURCE_FOLDER, NODE_TEST_DATA_FILE_NAME);
		NodeInfoRepositoryXML.XMLtoNodeInfo(nodeInfo, nodeInfoXmlStr);

		AssetGraphGenerator assetGraphGenerator=new AssetGraphGenerator();

		String menuLabelStr="testgraph";
		List<String> layerHierarchy= Arrays.asList(NodeParamLabels.ASSET_REGION, 
				NodeParamLabels.ASSET_BUILDING, 
				NodeParamLabels.ASSET_RACK);
		String preferredLayout="Grid Layout";
		GraphML graphML = assetGraphGenerator.nodeInfoToTopology(nodeInfoRepository.getNodeInfo(), menuLabelStr,layerHierarchy, preferredLayout);
		
		GraphMLWriter.write(graphML , new File("target/output2.graphml"));
		
		LOG.debug("end of assetGraphGeneratorTest2");
	}


}
