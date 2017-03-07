package org.opennms.features.topology.plugins.topo.asset.test;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.opennms.features.topology.plugins.topo.asset.repo.NodeInfoRepository;
import org.opennms.features.topology.plugins.topo.asset.repo.NodeParamLabels;
import org.opennms.features.topology.plugins.topo.asset.repo.Utils;
import org.opennms.features.topology.plugins.topo.asset.repo.xml.NodeInfoRepositoryXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests that NodeInfoRepository can be marshalled and unmarshalled 
 * from xml for test data
 * @author admin
 *
 */
public class NodeInfoRepositoryXmlTest {
	private static final Logger LOG = LoggerFactory.getLogger(NodeInfoRepositoryXmlTest.class);
	
	public static final String TEST_TEMP_FOLDER="./target/tmptest";
	public static final String TEST_TEMP_FILE_NAME="nodeInfoMock.xml";
	
	public static final String TEST_RESOURCE_FOLDER="./src/test/resources";
	public static final String NODE_TEST_DATA_FILE_NAME="nodeInfoMockTestData2.xml";

	/**
	 * Test with mock data
	 */
	@Test
	public void nodeInfoRepositoryXmlTest() {
		LOG.debug("start of nodeInfoRepositoryXmlTest");
		
		// write MockNodeInfoRepository to xml file 
		NodeInfoRepository nodeInfoRep = NodeInfoRepositoryTest.getMockNodeInfoRepository(null);
		String nodeInfoxml = NodeInfoRepositoryXML.nodeInfoToXML(nodeInfoRep.getNodeInfo());
		Utils.writeFileToDisk(nodeInfoxml, TEST_TEMP_FILE_NAME, TEST_TEMP_FOLDER);
		
		//create and populate new NodeInfoRepository from xml file
		NodeInfoRepository nodeInfoRepository= new NodeInfoRepository();
		Map<String, Map<String, String>> nodeInfo = nodeInfoRepository.getNodeInfo();

		String nodeInfoXmlStr = Utils.readFileFromDisk(TEST_TEMP_FILE_NAME, TEST_TEMP_FOLDER);
		NodeInfoRepositoryXML.XMLtoNodeInfo(nodeInfo, nodeInfoXmlStr);
		
		//check sample data
		Map<String, String> n = nodeInfoRepository.getNodeInfo().get("5");
		assertNotNull(n);
		assertEquals("5", n.get(NodeParamLabels.NODE_NODEID));
		
		String returnedxml = NodeInfoRepositoryXML.nodeInfoToXML(nodeInfoRepository.getNodeInfo());
		
		assertEquals(returnedxml,nodeInfoxml);
		
		LOG.debug("end of nodeInfoRepositoryXmlTest");
	}
	

	/**
	 * Test with real inventory data file
	 */
	@Test
	public void nodeInfoRepositoryXmlDataTest() {
		LOG.debug("start of nodeInfoRepositoryXmlDataTest");
		//create and populate new NodeInfoRepository from xml file
		NodeInfoRepository nodeInfoRepository= new NodeInfoRepository();
		Map<String, Map<String, String>> nodeInfo = nodeInfoRepository.getNodeInfo();

		String nodeInfoXmlStr = Utils.readFileFromDisk(NODE_TEST_DATA_FILE_NAME, TEST_RESOURCE_FOLDER);
		NodeInfoRepositoryXML.XMLtoNodeInfo(nodeInfo, nodeInfoXmlStr);
		
		LOG.debug(nodeInfoRepository.nodeInfoToString());
		
		LOG.debug("end of nodeInfoRepositoryXmlTest");
	}
	
	
	

}
