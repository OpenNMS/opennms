/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.asset.repo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Class used to generate a nested map of nodes and node parameters from the OpenNMS Node and Asset tables
 * 
 * The stored data takes the form of
 * nodeInfo  Map<nodeId, Map<nodeParamLabelKey, nodeParamValue>>
 * nodeParamLabelKey a node asset parameter key (from those defined in org.opennms.plugins.graphml.asset.NodeParamLabels)
 * nodeParamValue a node asset value ( e.g. key NodeParamLabels.ASSET_RACK ('asset-rack') value: rack1
 *
 */
public class NodeInfoRepository {
	private static final Logger LOG = LoggerFactory.getLogger(NodeInfoRepository.class);

	private volatile NodeDao nodeDao;
	private volatile TransactionOperations transactionOperations;

	/**
	 * Map of Maps of node parameters which is populated by populateBodyWithNodeInfo
	 * nodeKey is a unique identifier for the node from nodeid and/or node foreignsource or foreignid
	 * node_parameterKey is the parameter name e.g. foreignsource or foreignid
	 * node_parameterValue is the parameter value for the given key
	 * Map<nodeKey,Map<node_parameterKey,node_parameterValue>> nodeInfo
	 */
	private Map<String,Map<String,String>> nodeInfo = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, String>>());

	/* getters and setters */
	public Map<String, Map<String, String>> getNodeInfo() {
		return nodeInfo;
	}

	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public TransactionOperations getTransactionOperations() {
		return transactionOperations;
	}

	public void setTransactionOperations(TransactionOperations transactionOperations) {
		this.transactionOperations = transactionOperations;
	}

	/** 
	 * utility method to clear nodeInfo table
	 */
	private void clearNodeInfo(){
		// make sure nodeInfo is empty
		for(String nodeKey:nodeInfo.keySet()){
			Map<String, String> param = nodeInfo.get(nodeKey);
			if (param !=null) param.clear();
		};
		nodeInfo.clear();
	}

	/**
	 * initialises node info map from the opennms database node and asset tables using nodeDao
	 * @param requiredParameters list of parameters to populate (named from constants in NodeParamLabels) 
	 *        if requiredParameters is null return entire parameter list in node info
	 *        NODE_NODEID, NODE_NODELABEL,NODE_FOREIGNID and NODE_FOREIGNSOURCE are always added by default
	 */
	public synchronized void initialiseNodeInfo(List<String> requiredParameters) {
		if (nodeDao==null) throw new RuntimeException("nodeDao must be set before running initialiseNodeInfo");
		LOG.info("initialising node info");

		// make sure nodeInfo is empty
		clearNodeInfo();

		// populate nodeinfo from latest database provisioned nodes information
		// wrap in a transaction so that Hibernate session is bound and getCategories works
		transactionOperations.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				List<OnmsNode> nodeList = nodeDao.findAllProvisionedNodes();
				initialiseNodeInfoFromNodeList(nodeList,requiredParameters);
			}
		});       
	}

	/**
	 * Initialises node info map from supplied opennms node list 
	 * @param requiredParameeters list of parameters to populate (named from constants in NodeParamLabels) 
	 *        if requiredParameters is null return entire parameter list
	 *        NODE_NODEID, NODE_NODELABEL,NODE_FOREIGNID and NODE_FOREIGNSOURCE are always added by default
	 * @param nodeList
	 */
	public synchronized void initialiseNodeInfoFromNodeList(List<OnmsNode> nodeList, List<String> requiredParameters) {
		if (nodeList==null) throw new RuntimeException("nodeList must note be null");

		// make sure nodeInfo is empty
		clearNodeInfo();

		// populate nodeinfo from supplied nodeList information
		for (OnmsNode node:nodeList){
			Map<String, String> nodeParameters = new LinkedHashMap<String, String>();
			populateNodeParametersWithNodeInfo(nodeParameters , node, requiredParameters);
			String nodeId = node.getNodeId();
			nodeInfo.put(nodeId, nodeParameters);
			if (LOG.isDebugEnabled()){
				LOG.debug("\nNodeInfoRepository added nodeId:"+nodeId+" parameters:"+nodeParamatersToString(nodeParameters));
			}
		}
	}


	/**
	 * utility method to list contents of nodeParameters as String for debug
	 * @param nodeParameters
	 * @return single string of parameters for node
	 */
	public String nodeParamatersToString(Map<String, String> nodeParameters){
		if(nodeParameters==null) return null;
		StringBuffer sb=new StringBuffer();
		for( String parameterKey:nodeParameters.keySet()){
			sb.append("\n    nodeParamKey: '"+parameterKey+"' nodeParamValue: '"+nodeParameters.get(parameterKey)+"'");
		}
		return sb.toString();
	};

	/**
	 * utility method to list contents of nodeInfo as String for debug
	 * @return
	 */
	public String nodeInfoToString(){
		StringBuffer sb=new StringBuffer();

		for(String nodeId:nodeInfo.keySet()){
			Map<String, String> nodeParams = nodeInfo.get(nodeId);
			sb.append("\n nodeId: '"+nodeId+"' nodeParameters:"+nodeParamatersToString(nodeParams));
		}
		return sb.toString();
	}

	/**
	 * utility method to populate a given Map with the node and asset attributes listed in requiredParamaters
	 * The map keys are determined by keys in NodeParamLabels
	 * The map attributes are populated from the supplied OpenNMS node
	 * @param nodeParameters the supplied map to populate
	 * @param node the OpenNMS  node object to use
	 * @param requiredParameeters list of parameters to populate (named from constants in NodeParamLabels) 
	 *        if requiredParameters is null return entire parameter list
	 *        NODE_NODEID, NODE_NODELABEL,NODE_FOREIGNID and NODE_FOREIGNSOURCE are always added by default
	 */
	private void populateNodeParametersWithNodeInfo(Map<String,String> nodeParameters, OnmsNode node, List<String> requiredParameters){
		Map<String, String> newNodeParameters = new LinkedHashMap<String, String>();
		populateNodeParametersWithNodeInfo(newNodeParameters, node);
		if(requiredParameters!=null){
			for(String paramKey:requiredParameters){
				if(newNodeParameters.containsKey(paramKey)){
					nodeParameters.put(paramKey, newNodeParameters.get(paramKey));
				}
			}
		} else {
			nodeParameters.putAll(newNodeParameters);
		}
		// always add default values even if null
		nodeParameters.put(NodeParamLabels.NODE_NODEID, newNodeParameters.get(NodeParamLabels.NODE_NODEID));
		nodeParameters.put(NodeParamLabels.NODE_NODELABEL, newNodeParameters.get(NodeParamLabels.NODE_NODELABEL));
		nodeParameters.put(NodeParamLabels.NODE_FOREIGNID, newNodeParameters.get(NodeParamLabels.NODE_FOREIGNID));
		nodeParameters.put(NodeParamLabels.NODE_FOREIGNSOURCE, newNodeParameters.get(NodeParamLabels.NODE_FOREIGNSOURCE));
	}

	/**
	 * utility method to populate a given Map with the most important node and asset attributes
	 * The map keys are determined by keys in NodeParamLabels
	 * The map attributes are populated from the supplied OpenNMS node
	 *
	 * @param nodeParameters the supplied map to populate
	 * @param node the OpenNMS  node object to use
	 */
	private void populateNodeParametersWithNodeInfo(Map<String,String> nodeParameters, OnmsNode node) {
		if( node.getLabel()!=null && ! node.getLabel().isEmpty()) nodeParameters.put(NodeParamLabels.NODE_NODELABEL, node.getLabel());
		if( node.getNodeId()!=null && ! node.getNodeId().isEmpty()) nodeParameters.put(NodeParamLabels.NODE_NODEID, node.getNodeId());
		if( node.getForeignSource()!=null && ! node.getForeignSource().isEmpty()) nodeParameters.put(NodeParamLabels.NODE_FOREIGNSOURCE, node.getForeignSource());
		if( node.getForeignId()!=null && ! node.getForeignId().isEmpty()) nodeParameters.put(NodeParamLabels.NODE_FOREIGNID, node.getForeignId());
		if( node.getSysName()!=null && ! node.getSysName().isEmpty()) nodeParameters.put(NodeParamLabels.NODE_NODESYSNAME, node.getSysName());
		if( node.getSysLocation()!=null && ! node.getSysLocation().isEmpty()) nodeParameters.put(NodeParamLabels.NODE_NODESYSLOCATION, node.getSysLocation());
		if( node.getOperatingSystem()!=null && ! node.getOperatingSystem().isEmpty()) nodeParameters.put(NodeParamLabels.NODE_OPERATINGSYSTEM, node.getOperatingSystem());
		
		StringBuilder categories=new StringBuilder();
		for (Iterator<OnmsCategory> i=node.getCategories().iterator();i.hasNext();) {
			categories.append(((OnmsCategory)i.next()).getName());
			if(i.hasNext()) {
				categories.append(",");
			}
		}
		nodeParameters.put(NodeParamLabels.NODE_CATEGORIES, categories.toString());

		// parent information
		OnmsNode parent = node.getParent();
		if (parent!=null){
			if( parent.getLabel()!=null && ! parent.getLabel().isEmpty()) nodeParameters.put(NodeParamLabels.PARENT_NODELABEL, parent.getLabel());
			if( parent.getNodeId()!=null && ! parent.getNodeId().isEmpty()) nodeParameters.put(NodeParamLabels.PARENT_NODEID, parent.getNodeId());
			if( parent.getForeignSource()!=null && ! parent.getForeignSource().isEmpty()) nodeParameters.put(NodeParamLabels.PARENT_FOREIGNSOURCE, parent.getForeignSource());
			if( parent.getForeignId()!=null && ! parent.getForeignId().isEmpty()) nodeParameters.put(NodeParamLabels.PARENT_FOREIGNID, parent.getForeignId());
		}

		//assetRecord.
		OnmsAssetRecord assetRecord= node.getAssetRecord() ;
		if(assetRecord!=null){

			//geolocation
			OnmsGeolocation gl = assetRecord.getGeolocation();
			if (gl !=null){
				if( gl.getCountry()!=null && ! gl.getCountry().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_COUNTRY, gl.getCountry());
				if( gl.getAddress1()!=null && ! gl.getAddress1().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_ADDRESS1, gl.getAddress1());
				if( gl.getAddress2()!=null && ! gl.getAddress2().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_ADDRESS2, gl.getAddress2());
				if( gl.getCity()!=null && ! gl.getCity().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_CITY, gl.getCity());
				if( gl.getZip()!=null && ! gl.getZip().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_ZIP, gl.getZip());
				if( gl.getState()!=null && ! gl.getState().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_STATE, gl.getState());
				if( gl.getLatitude()!=null ) nodeParameters.put(NodeParamLabels.ASSET_LATITUDE, gl.getLatitude().toString());
				if( gl.getLongitude()!=null ) nodeParameters.put(NodeParamLabels.ASSET_LONGITUDE, gl.getLongitude().toString());
			}

			//assetRecord
			if( assetRecord.getRegion()!=null && ! assetRecord.getRegion().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_REGION, assetRecord.getRegion());
			if( assetRecord.getDivision()!=null && ! assetRecord.getDivision().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_DIVISION, assetRecord.getDivision());
			if( assetRecord.getDepartment()!=null && !assetRecord.getDepartment().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_DEPARTMENT, assetRecord.getDepartment());
			if( assetRecord.getBuilding()!=null && ! assetRecord.getBuilding().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_BUILDING, assetRecord.getBuilding());
			if( assetRecord.getFloor()!=null && ! assetRecord.getFloor().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_FLOOR, assetRecord.getFloor());
			if( assetRecord.getRoom()!=null && ! assetRecord.getRoom().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_ROOM, assetRecord.getRoom());
			if( assetRecord.getRack()!=null && ! assetRecord.getRack().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_RACK, assetRecord.getRack());
			if( assetRecord.getSlot()!=null && ! assetRecord.getSlot().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_SLOT, assetRecord.getSlot());
			if( assetRecord.getPort()!=null && ! assetRecord.getPort().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_PORT, assetRecord.getPort());
			if( assetRecord.getCircuitId()!=null && ! assetRecord.getCircuitId().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_CIRCUITID, assetRecord.getCircuitId());

			if( assetRecord.getCategory()!=null && ! assetRecord.getCategory().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_CATEGORY, assetRecord.getCategory());
			if( assetRecord.getDisplayCategory()!=null && ! assetRecord.getDisplayCategory().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_DISPLAYCATEGORY, assetRecord.getDisplayCategory());
			if( assetRecord.getNotifyCategory()!=null && ! assetRecord.getNotifyCategory().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_NOTIFYCATEGORY, assetRecord.getNotifyCategory());
			if( assetRecord.getPollerCategory()!=null && ! assetRecord.getPollerCategory().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_POLLERCATEGORY, assetRecord.getPollerCategory());
			if( assetRecord.getThresholdCategory()!=null && ! assetRecord.getThresholdCategory().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_THRESHOLDCATEGORY, assetRecord.getThresholdCategory());
			if( assetRecord.getManagedObjectType()!=null && ! assetRecord.getManagedObjectType().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_MANAGEDOBJECTTYPE, assetRecord.getManagedObjectType());
			if( assetRecord.getManagedObjectInstance()!=null && ! assetRecord.getManagedObjectInstance().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_MANAGEDOBJECTINSTANCE, assetRecord.getManagedObjectInstance());

			if( assetRecord.getManufacturer()!=null && ! assetRecord.getManufacturer().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_MANUFACTURER, assetRecord.getManufacturer());
			if( assetRecord.getVendor()!=null && ! assetRecord.getVendor().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_VENDOR, assetRecord.getVendor());
			if( assetRecord.getModelNumber()!=null && ! assetRecord.getModelNumber().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_MODELNUMBER, assetRecord.getModelNumber());
			if( assetRecord.getDescription()!=null && ! assetRecord.getDescription().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_DESCRIPTION, assetRecord.getDescription());
			if( assetRecord.getOperatingSystem()!=null && ! assetRecord.getOperatingSystem().isEmpty()) nodeParameters.put(NodeParamLabels.ASSET_OPERATINGSYSTEM, assetRecord.getOperatingSystem()); 


			/*not used or depreciated*/
			/*
                assetRecord.getComment();
                assetRecord.getPassword();
                assetRecord.getConnection();
                //assetRecord.getCountry(); // depreciated
                assetRecord.getUsername();
                assetRecord.getEnable();
                assetRecord.getAutoenable();
                assetRecord.getCpu();
                assetRecord.getRam();
                assetRecord.getSnmpcommunity();
                assetRecord.getRackunitheight();
                assetRecord.getAdmin();
                assetRecord.getAdditionalhardware();
                assetRecord.getInputpower();
                assetRecord.getNumpowersupplies();
                assetRecord.getHdd6();
                assetRecord.getHdd5();
                assetRecord.getHdd4();
                assetRecord.getHdd3();
                assetRecord.getHdd2();
                assetRecord.getHdd1();
                assetRecord.getStoragectrl();
                //assetRecord.getAddress1();// depreciated
                //assetRecord.getAddress2();// depreciated
                //assetRecord.getCity();// depreciated
                //assetRecord.getZip();// depreciated
                assetRecord.getVmwareManagedEntityType();
                assetRecord.getVmwareManagedObjectId();
                assetRecord.getVmwareManagementServer();
                assetRecord.getVmwareState();
                assetRecord.getVmwareTopologyInfo();
                assetRecord.getSerialNumber();
                assetRecord.getAssetNumber();
                assetRecord.getVendorPhone();
                assetRecord.getVendorFax();
                assetRecord.getVendorAssetNumber();
                assetRecord.getLastModifiedBy();
                assetRecord.getDateInstalled();
                assetRecord.getLease();
                assetRecord.getLeaseExpires();
                assetRecord.getSupportPhone();
                assetRecord.getMaintcontract();
                //assetRecord.getMaintContractNumber();// depreciated
                assetRecord.getMaintContractExpiration();
                //assetRecord.getState();// depreciated
			 */
		}

	}

}
