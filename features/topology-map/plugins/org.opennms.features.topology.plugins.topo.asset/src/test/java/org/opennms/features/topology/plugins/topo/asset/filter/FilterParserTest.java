/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.asset.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphGenerator;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfig;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfigBuilder;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinitionRepository;
import org.opennms.features.topology.plugins.topo.asset.layers.NodeParamLabels;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterParserTest {
	private static final Logger LOG = LoggerFactory.getLogger(FilterParserTest.class);

	// Tests of simple filters

	@Test
	public void testMockNodes(){
		List<OnmsNode> nodelist = getMockNodeList();
		String s1 = nodelistToString(nodelist);
		LOG.debug("End testMockNodes():"+s1);
	}

	@Test
	public void testEmptyFilter(){
		List<OnmsNode> nodeList = getMockNodeList();
		String expected = nodelistToString(nodeList);
		LOG.debug("Start testEmptyFilter() before(and expected):"+expected);

		Map<String, Filter> filterMap=new HashMap<String, Filter>();
		List<OnmsNode> filteredNodeList = testFilterCode(nodeList, filterMap);
		String s2 = nodelistToString(filteredNodeList);
		LOG.debug("End testEmptyFilter()  after:"+s2);
		assertEquals(expected,s2);
	}

	@Test
	public void testRawFilter(){
		String expected="nodeList:{ [0] [5] }";

		List<OnmsNode> nodeList = getMockNodeList();
		String s1 = nodelistToString(nodeList);
		LOG.debug("Start testRawFilter() before:"+s1+ " expected:"+expected);

		Map<String, Filter> filterMap=new HashMap<String, Filter>();
		filterMap.put(NodeParamLabels.ASSET_DISPLAYCATEGORY, 
				new NotFilter<>(new OrFilter<>(new EqFilter<>("asset-displaycategory_0"), new EqFilter<>("asset-displaycategory_5"))));

		List<OnmsNode> filteredNodeList = testFilterCode(nodeList, filterMap);
		String s2 = nodelistToString(filteredNodeList);
		LOG.debug("End testRawFilter()  after:"+s2);

		assertEquals(expected,s2);
	}

	// tests of filter strings

	// test empty filter string
	@Test
	public void testEmptyFilterString(){
		String filter="";

		List<OnmsNode> nodeList = getMockNodeList();
		String expected = nodelistToString(nodeList);

		LOG.debug("Start testEmptyFilterString(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testEmptyFilterString()");
	}

	// test specific 'or' values
	// filter=asset-displaycategory=asset-displaycategory_0,asset-displaycategory_5
	@Test
	public void testFilterString1(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=asset-displaycategory_0,asset-displaycategory_5";
		String expected="nodeList:{ [0] [5] }";

		LOG.debug("Start testFilterString1(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString1()");
	}

	// test specific 'or' values separate entries
	// filter=asset-displaycategory=asset-displaycategory_0;asset-displaycategory=asset-displaycategory_5
	@Test
	public void testFilterString2(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=asset-displaycategory_0"
				+ ";"+NodeParamLabels.ASSET_DISPLAYCATEGORY+"=asset-displaycategory_5";
		String expected="nodeList:{ [0] [5] }";

		LOG.debug("Start testFilterString2(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString2()");
	}

	// test not value (all other values wild card)
	// filter=asset-displaycategory=!testDisplayCategory	
	@Test
	public void testFilterString3(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=!testDisplayCategory";
		String expected="nodeList:{ [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] }";

		LOG.debug("Start testFilterString3(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString3()");
	}

	// test multiple not values (all other values wild card)
	// filter=node-foreignsource=!testForeignSource1,!testForeignSource2,!testForeignSource3	
	@Test
	public void testFilterString4(){
		String filter=NodeParamLabels.NODE_FOREIGNSOURCE+"=!testForeignSource1,!testForeignSource2,!testForeignSource3";
		String expected="nodeList:{ [0] [1] [2] [3] [4] }";

		LOG.debug("Start testFilterString4(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString4()");
	}

	// test and separate parameters
	// filter=asset-pollercategory=testPollerCategory1;node-foreignsource=testForeignSource1,testForeignSource2
	@Test
	public void testFilterString5(){
		String filter=NodeParamLabels.ASSET_POLLERCATEGORY+"=testPollerCategory1"
				+ ";"+NodeParamLabels.NODE_FOREIGNSOURCE+"=testForeignSource1,testForeignSource2";

		String expected="nodeList:{ [5] [6] [7] [8] [9] [10] [11] [12] [13] [14] }";

		LOG.debug("Start testFilterString5(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString5()");
	}

	// test not value and or values
	// filter=asset-displaycategory=!testDisplayCategory;node-foreignsource=testForeignSource1,testForeignSource2
	@Test
	public void testFilterString6(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=!testDisplayCategory"
				+ ";"+NodeParamLabels.NODE_FOREIGNSOURCE+"=testForeignSource1,testForeignSource2";

		String expected="nodeList:{ [5] [6] [7] [8] [9] }";

		LOG.debug("Start testFilterString6(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString6()");
	}

	// test reversing order of not and or values
	// filter=node-foreignsource=testForeignSource1,testForeignSource2;asset-displaycategory=!testDisplayCategory
	@Test
	public void testFilterString7(){
		String filter=NodeParamLabels.NODE_FOREIGNSOURCE+"=testForeignSource1,testForeignSource2"
				+ ";"+NodeParamLabels.ASSET_DISPLAYCATEGORY+"=!testDisplayCategory";

		String expected="nodeList:{ [5] [6] [7] [8] [9] }";

		LOG.debug("Start testFilterString7(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString7()");
	}

	// test regex with not
	// filter=asset-displaycategory=~.*_.*,!asset-displaycategory_5
	@Test
	public void testFilterString8(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=~.*_.*,!asset-displaycategory_5";
		String expected="nodeList:{ [0] [1] [2] [3] [4] [6] [7] [8] [9] }";

		LOG.debug("Start testFilterString8(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString8()");
	}

	// test regex with or
	// filter=asset-displaycategory=testDisplayCategory,~.*_5
	@Test
	public void testFilterString9(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=testDisplayCategory,~.*_5";
		String expected="nodeList:{ [5] [10] [11] [12] [13] [14] [15] [16] [17] [18] [19] }";

		LOG.debug("Start testFilterString9(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString9()");
	}

	// test not regex
	// filter=asset-displaycategory=testDisplayCategory,~.*_5
	@Test
	public void testFilterString10(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=!~.*_5";
		String expected="nodeList:{ [0] [1] [2] [3] [4] [6] [7] [8] [9] [10] [11] [12] [13] [14] [15] [16] [17] [18] [19] }";

		LOG.debug("Start testFilterString10(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString10()");
	}

	// test invalid expressions
	// ------------------------

	// test invalid regex
	// filter=asset-displaycategory=testDisplayCategory,!~{.*_5
	@Test
	public void testFilterString11(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=!~{.*_5";

		LOG.debug("Start testFilterString11() filter="+filter);
		boolean expectedException=false;
		try {
			testFilterParser(filter);
		} catch ( IllegalArgumentException e){
			expectedException=true;
			LOG.debug("    expected IllegalArgumentException thrown="+e.getMessage());
		}
		assertEquals(true,expectedException);

		LOG.debug("End testFilterString11()");
	}
	
	// test too many '='
	// filter=asset-displaycategory=!testDisplayCategory;node-foreignsource=testForeignSource1,testForeign=Source2
	@Test
	public void testFilterString12(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=!testDisplayCategory"
				+ ";"+NodeParamLabels.NODE_FOREIGNSOURCE+"=testForeignSource1,testForeign=Source2";

		LOG.debug("Start testFilterString12() filter="+filter);
		boolean expectedException=false;
		try {
			testFilterParser(filter);
		} catch ( IllegalArgumentException e){
			expectedException=true;
			LOG.debug("    expected IllegalArgumentException thrown="+e.getMessage());
		}
		assertEquals(true,expectedException);

		LOG.debug("End testFilterString12()");
	}
	
	// test too many ','
	// filter=asset-displaycategory=!testDisplayCategory;node-foreignsource=testForeignSource1,,testForeignSource2
	@Test
	public void testFilterString13(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=!testDisplayCategory"
				+ ";"+NodeParamLabels.NODE_FOREIGNSOURCE+"=testForeignSource1,,testForeignSource2";

		LOG.debug("Start testFilterString13() filter="+filter);
		boolean expectedException=false;
		try {
			testFilterParser(filter);
		} catch ( IllegalArgumentException e){
			expectedException=true;
			LOG.debug("    expected IllegalArgumentException thrown="+e.getMessage());
		}
		assertEquals(true,expectedException);

		LOG.debug("End testFilterString13()");
	}
	
	// test empty not value "=!"
	// filter=asset-displaycategory=!;node-foreignsource=testForeignSource1,,testForeignSource2
	@Test
	public void testFilterString14(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=!"
				+ ";"+NodeParamLabels.NODE_FOREIGNSOURCE+"=testForeignSource1,testForeignSource2";

		LOG.debug("Start testFilterString14() filter="+filter);
		boolean expectedException=false;
		try {
			testFilterParser(filter);
		} catch ( IllegalArgumentException e){
			expectedException=true;
			LOG.debug("    expected IllegalArgumentException thrown="+e.getMessage());
		}
		assertEquals(true,expectedException);

		LOG.debug("End testFilterString14()");
	}

	// test empty regex value "=~"
	// filter=asset-displaycategory=~;node-foreignsource=testForeignSource1,,testForeignSource2
	@Test
	public void testFilterString15(){
		String filter=NodeParamLabels.ASSET_DISPLAYCATEGORY+"=~"
				+ ";"+NodeParamLabels.NODE_FOREIGNSOURCE+"=testForeignSource1,testForeignSource2";

		LOG.debug("Start testFilterString15() filter="+filter);
		boolean expectedException=false;
		try {
			testFilterParser(filter);
		} catch ( IllegalArgumentException e){
			expectedException=true;
			LOG.debug("    expected IllegalArgumentException thrown="+e.getMessage());
		}
		assertEquals(true,expectedException);

		LOG.debug("End testFilterString15()");
	}
	
	// test multiple values for categories
	// ***********************************
	
	// test simple node category selection
	// filter=filter=node-categories=category3,category4
	@Test
	public void testFilterString16(){
		String filter=NodeParamLabels.NODE_CATEGORIES+"=category3,category4";

		String expected="nodeList:{ [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] }";

		LOG.debug("Start testFilterString16(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString16()");
	}
	
	// test simple node category exclusion
	// filter=filter=node-categories=category1,!category4
	@Test
	public void testFilterString17(){
		String filter=NodeParamLabels.NODE_CATEGORIES+"=category1,!category4";

		String expected="nodeList:{ [0] [1] [2] [3] [4] [10] [11] [12] [13] [14] [15] [16] [17] [18] [19] }";

		LOG.debug("Start testFilterString17(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString17()");
	}
	
	// test regex on categories
	// filter=filter=node-categories=~.*4,category3
	@Test
	public void testFilterString18(){
		String filter=NodeParamLabels.NODE_CATEGORIES+"=~.*4,category3";

		String expected="nodeList:{ [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] }";

		LOG.debug("Start testFilterString18(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString18()");
	}
	
	// test not regex on categories
	// filter=node-categories=category1,!~.*4
	@Test
	public void testFilterString19(){
		String filter=NodeParamLabels.NODE_CATEGORIES+"=category1,!~.*4";
		String expected="nodeList:{ [0] [1] [2] [3] [4] [10] [11] [12] [13] [14] [15] [16] [17] [18] [19] }";

		LOG.debug("Start testFilterString19(): filter="+filter+" expected="+expected);
		String s2 = testFilterParser(filter);
		assertEquals(expected,s2);
		LOG.debug("End testFilterString19()");
	}

	// Utility methods for tests
	// -------------------------

	/**
	 * boiler plate test method to test filter strings
	 * @param filter
	 * @return
	 */
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
		
		AssetGraphGenerator.applyFilters(nodes, filterMap,layerDefinitionRepository);

		return nodes;
	}

	/**
	 * Utility to create a mock list of opennms nodes
	 * @return
	 */
	public static List<OnmsNode> getMockNodeList(){
		List<OnmsNode> nodeList= new ArrayList<>();

		for( int id = 0;id<5;id++){
			OnmsNode n =createNode(id);
			
			OnmsCategory onmsCategory3 = new OnmsCategory();
			onmsCategory3.setName("category3");
			n.getCategories().add(onmsCategory3);
			
			nodeList.add(n);
		}

		for( int id = 5;id<10;id++){
			OnmsNode n =createNode(id);
			n.setForeignSource("testForeignSource1");
			n.getAssetRecord().setPollerCategory("testPollerCategory1");
			
			OnmsCategory onmsCategory4 = new OnmsCategory();
			onmsCategory4.setName("category4");
			n.getCategories().add(onmsCategory4);
			
			nodeList.add(n);
		}

		for( int id = 10;id<15;id++){
			OnmsNode n =createNode(id);
			n.setForeignSource("testForeignSource2");
			n.getAssetRecord().setPollerCategory("testPollerCategory1");
			n.getAssetRecord().setDisplayCategory("testDisplayCategory");
			nodeList.add(n);
		}

		for( int id = 15;id<20;id++){
			OnmsNode n =createNode(id);
			n.setForeignSource("testForeignSource3");
			n.getAssetRecord().setPollerCategory("testPollerCategory2");
			n.getAssetRecord().setDisplayCategory("testDisplayCategory");
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

		Set<OnmsCategory> categories=new LinkedHashSet<>();
		node.setCategories(categories);

		// categories used for multi value tests
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
		gl.setLatitude(0d);
		gl.setLongitude(0d);

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
		final StringBuilder sb= new StringBuilder("nodeList:{ ");
		for (OnmsNode node:nodelist){
			sb.append("["+node.getId()+"] ");
		}
		sb.append("}");
		return sb.toString();
	}




}



