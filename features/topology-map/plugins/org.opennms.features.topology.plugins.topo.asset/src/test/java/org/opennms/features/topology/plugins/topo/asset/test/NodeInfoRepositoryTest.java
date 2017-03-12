package org.opennms.features.topology.plugins.topo.asset.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.features.topology.plugins.topo.asset.repo.NodeInfoRepository;
import org.opennms.features.topology.plugins.topo.asset.repo.NodeParamLabels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * populates a list of opennms nodes with random data and populates a NodeInfoRepository
 * @author admin
 *
 */
public class NodeInfoRepositoryTest {
	private static final Logger LOG = LoggerFactory.getLogger(NodeInfoRepositoryTest.class);

	@Test
	public void getFullRepositoryTest() {
		LOG.debug("start of getFullRepositoryTest");
		NodeInfoRepository nir = getMockNodeInfoRepository(null);
		assertNotNull(nir);
		
		// sample test correct values are present
		Map<String, String> n = nir.getNodeInfo().get("5");
		assertNotNull(n);
		assertEquals("5", n.get(NodeParamLabels.NODE_NODEID));
		assertEquals("asset-region_5", n.get(NodeParamLabels.ASSET_REGION));
		assertEquals("asset-city_5", n.get(NodeParamLabels.ASSET_CITY));

		LOG.debug(nir.nodeInfoToString());
		LOG.debug("end of getFullRepositoryTest");

	}
	
	@Test
	public void getPartialRepositoryTest() {
		LOG.debug("start of getPartialRepositoryTest");
		List<String> requiredParameters = Arrays.asList(
				NodeParamLabels.ASSET_REGION,
				NodeParamLabels.ASSET_BUILDING,
				NodeParamLabels.ASSET_RACK);
		NodeInfoRepository nir = getMockNodeInfoRepository(requiredParameters);
		assertNotNull(nir);
		
		// sample test correct values are present
		Map<String, String> n = nir.getNodeInfo().get("5");
		assertNotNull(n);
		assertEquals("5",n.get(NodeParamLabels.NODE_NODEID));
		assertEquals("asset-region_5", n.get(NodeParamLabels.ASSET_REGION));
		assertNull(n.get(NodeParamLabels.ASSET_CITY));
		assertFalse(n.containsKey(NodeParamLabels.ASSET_CITY));

		LOG.debug(nir.nodeInfoToString());
		LOG.debug("end of getPartialRepositoryTest");

	}
	
	@Test
	public void getFilteredRepositoryTest() {
		LOG.debug("start of getFilteredRepositoryTest");
		NodeInfoRepository nir = getMockNodeInfoRepository(null);
		assertNotNull(nir);
		
		List<String> filter=Arrays.asList(NodeParamLabels.ASSET_REGION+"=asset-region_1,asset-region_2");
		
		// sample test correct values are present
	    Map<String, Map<String, String>> nodeInfo = nir.getFilteredNodeInfo(filter);
	    assertNotNull(nodeInfo);
	    assertEquals(2,nodeInfo.size());
	    assertTrue(nodeInfo.containsKey("1"));
	    assertTrue(nodeInfo.containsKey("2"));
	    assertTrue(!nodeInfo.containsKey("3"));

		LOG.debug("end of getFilteredRepositoryTest");

	}


	/* *************************
	 * UTILITY METHODS FOR TESTS
	 * *************************
	 */
	
	/**
	 * utility to create mock node info repository
	 * @param id
	 * @return
	 */
	public static NodeInfoRepository getMockNodeInfoRepository(List<String> requiredParameters){
		List<OnmsNode> nodeList= new ArrayList<OnmsNode>();

		for( int id = 0;id<10;id++){
			OnmsNode n =createNode(id);
			nodeList.add(n);
		}

		NodeInfoRepository nir= new NodeInfoRepository();

		nir.initialiseNodeInfoFromNodeList(nodeList, requiredParameters);
		return nir;
	}

	/**
	 * utility to create unique node with id
	 * @param id
	 * @return
	 */
	public static OnmsNode createNode(int id){

		OnmsNode node = new OnmsNode();

		node.setLabel("node-nodelabel_"+id);
		node.setId(id);
		node.setForeignSource("node-foreignsource_"+id);
		node.setForeignId("node-foreignid_"+id);
		node.setSysName("node-nodesysname_"+id);
		node.setSysLocation("node-nodeLocation_"+id);
		node.setOperatingSystem("node-operatingsystem_"+id);

		Set<OnmsCategory> categories=new LinkedHashSet<OnmsCategory>();
		node.setCategories(categories);

		OnmsCategory onmsCategory = new OnmsCategory();
		onmsCategory.setName("category1");
		categories.add(onmsCategory);
		OnmsCategory onmsCategory2 = new OnmsCategory();
		onmsCategory2.setName("category2");
		categories.add(onmsCategory2);

		// parent information
		OnmsNode parent = new OnmsNode();
		node.setParent(parent);
		parent.setLabel("parent-nodelabel");
		parent.setNodeId("1");
		parent.setForeignSource("parent-foreignsource_"+id);
		parent.setForeignId("parent-foreignid_"+id);

		OnmsAssetRecord assetRecord = new OnmsAssetRecord();
		node.setAssetRecord(assetRecord) ;

		OnmsGeolocation gl = new OnmsGeolocation();
		assetRecord.setGeolocation(gl);

		//geolocation
		gl.setCountry("asset-country_"+id);
		gl.setAddress1("asset-address1_"+id);
		gl.setAddress2("asset-address2_"+id);
		gl.setCity("asset-city_"+id);
		gl.setZip	("asset-zip_"+id);
		gl.setState("asset-state_"+id); 
		Float lat= Float.valueOf("0");
		gl.setLatitude(lat);
		Float lng= Float.valueOf("0");
		gl.setLongitude(lng);

		//assetRecord
		assetRecord.setRegion("asset-region_"+id);
		assetRecord.setDivision("asset-division_"+id);
		assetRecord.setDepartment("asset-department_"+id); 
		assetRecord.setBuilding("asset-building_"+id); 
		assetRecord.setFloor("asset-floor_"+id); 
		assetRecord.setRoom("asset-room_"+id);
		assetRecord.setRack("asset-rack_"+id); 
		assetRecord.setSlot("asset-slot_"+id);
		assetRecord.setPort("asset-port_"+id);
		assetRecord.setCircuitId("asset-circuitid_"+id); 

		assetRecord.setCategory("asset-category_"+id); 
		assetRecord.setDisplayCategory("asset-displaycategory_"+id);
		assetRecord.setNotifyCategory("asset-notifycategory_"+id);
		assetRecord.setPollerCategory("asset-pollercategory_"+id);
		assetRecord.setThresholdCategory("asset-thresholdcategory_"+id);
		assetRecord.setManagedObjectType("asset-managedobjecttype_"+id);
		assetRecord.setManagedObjectInstance("asset-managedobjectinstance_"+id); 

		assetRecord.setManufacturer("asset-manufacturer_"+id);
		assetRecord.setVendor("asset-vendor_"+id);
		assetRecord.setModelNumber("asset-modelnumber_"+id); 
		assetRecord.setDescription("asset-description_"+id);
		assetRecord.setOperatingSystem("asset-operatingsystem_"+id); 

		return node;
	}

}
