package org.opennms.features.topology.plugins.topo.asset.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphGenerator;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfig;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfigBuilder;
import org.opennms.features.topology.plugins.topo.asset.layers.ItemProvider;
import org.opennms.features.topology.plugins.topo.asset.layers.Layer;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinition;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinitionRepository;
import org.opennms.features.topology.plugins.topo.asset.layers.NodeParamLabels;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilteringTest {
	private static final Logger LOG = LoggerFactory.getLogger(FilteringTest.class);

	@Test
	public void testMockNodes(){
		List<OnmsNode> nodelist = getMockNodeList();
		String s1 = nodelistToString(nodelist);
		LOG.debug("testMockNodes():"+s1);
	}
	
	@Test
	public void testEmptyFilter(){
		List<OnmsNode> nodeList = getMockNodeList();
		String s1 = nodelistToString(nodeList);
		LOG.debug("testEmptyFilter() before:"+s1);
		
		Map<String, Filter> filterMap=new HashMap<String, Filter>();
		List<OnmsNode> filteredNodeList = testFilterCode(nodeList, filterMap);
		String s2 = nodelistToString(filteredNodeList);
		LOG.debug("testEmptyFilter()  after:"+s2);
		assertEquals(s1,s2);
	}
	
	@Test
	public void testRawFilter(){
		List<OnmsNode> nodeList = getMockNodeList();
		String s1 = nodelistToString(nodeList);
		LOG.debug("testFilter1() before:"+s1);
		
		Map<String, Filter> filterMap=new HashMap<String, Filter>();
		filterMap.put(NodeParamLabels.ASSET_DISPLAYCATEGORY, 
				new NotFilter<>(new OrFilter<>(new EqFilter<>("asset-displaycategory_0"), new EqFilter<>("asset-displaycategory_5"))));
		
		List<OnmsNode> filteredNodeList = testFilterCode(nodeList, filterMap);
		String s2 = nodelistToString(filteredNodeList);
		LOG.debug("testFilter1()  after:"+s2);
		
		String expected="nodeList:{ [0] [5] }";
		assertEquals(expected,s2);
	}
	
	@Test
	public void testFilterString1(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=asset-displaycategory_0,asset-displaycategory_5";
		String expected="nodeList:{ [0] [5] }";
		
		LOG.debug("Start testFilterString1(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString1()");
	}
	

	
	public String testFilterParser(String filter){
		
		List<OnmsNode> nodeList = getMockNodeList();
		String s1 = nodelistToString(nodeList);
		LOG.debug("  before:"+s1);

		final GeneratorConfig config = new GeneratorConfigBuilder()
		.withFilters(filter)
		.build();
		
		final Map<String, Filter> filterMap = new FilterParser().parse(config.getFilters());
		
		List<OnmsNode> filteredNodeList = testFilterCode(nodeList, filterMap);
		String s2 = nodelistToString(filteredNodeList);
		LOG.debug("   after:"+s2);
		return s2;
	}
	
	/**
	 * Excerpt from CreateAssetTopology - code which runs filter
	 * @param nodeList
	 * @param filterMap
	 * @return
	 */
	public List<OnmsNode> testFilterCode(List<OnmsNode> nodeList, Map<String, Filter> filterMap) {
		final LayerDefinitionRepository layerDefinitionRepository = new LayerDefinitionRepository();
		final List<OnmsNode> nodes = new ArrayList<OnmsNode>(nodeList);

		// Apply additional filters
		final List<LayerDefinition> layersToFilter = layerDefinitionRepository.getDefinitions(filterMap.keySet());
		layersToFilter.stream()
		.filter(layerToFilter -> filterMap.get(layerToFilter.getKey()) != null)
		.forEach(
				layerToFilter -> {
					final List<OnmsNode> filteredNodes = nodes.stream().filter(n -> {
						ItemProvider itemProvider = layerToFilter.getLayer().getItemProvider();
						Filter filter = filterMap.get(layerToFilter.getKey());
						return filter.apply(itemProvider.getItem(n));
					}).collect(Collectors.toList());
					if (!filteredNodes.isEmpty()) {
						final Layer layer = layerToFilter.getLayer();
						LOG.debug("Found nodes to remove due to filter settings. Removing nodes {}",
								filteredNodes.stream().map(n -> String.format("(id: %s, label: %s)", n.getId(), n.getLabel())).collect(Collectors.toList()));
						nodes.removeAll(filteredNodes);
					}
				});
		return nodes;
	}

	/**
	 * Utility to create a mock list of opennms nodes
	 * @return
	 */
	public static List<OnmsNode> getMockNodeList(){
		List<OnmsNode> nodeList= new ArrayList<OnmsNode>();

		for( int id = 0;id<5;id++){
			OnmsNode n =createNode(id);
			nodeList.add(n);
		}
		
		for( int id = 5;id<10;id++){
			OnmsNode n =createNode(id);
			nodeList.add(n);
		}
		
		for( int id = 10;id<15;id++){
			OnmsNode n =createNode(id);
			nodeList.add(n);
		}
		
		return nodeList;
	}


/**
 * Utility to create and populate a mock opennms node. 
 * @param id used to set unique id and values for node
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
	
	public static String nodelistToString(List<OnmsNode> nodelist){
		StringBuilder sb= new StringBuilder("nodeList:{ ");
		for (OnmsNode node:nodelist){
			sb.append("["+node.getId()+"] ");
		}
		sb.append("}");
		return sb.toString();
	}
	
	
	
	
}



