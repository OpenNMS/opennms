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

package org.opennms.features.topology.plugins.topo.asset;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLEdge;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLNode;
//import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLProperties;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.features.topology.plugins.topo.asset.repo.NodeInfoRepository;
import org.opennms.features.topology.plugins.topo.asset.repo.NodeParamLabels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

public class AssetGraphGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(AssetGraphGenerator.class);

	private NodeDao nodeDao;
	
	private TransactionOperations transactionOperations;
	
	public AssetGraphGenerator(){
		super();
	}
	
	public AssetGraphGenerator(NodeDao nodeDao, TransactionOperations transactionOperations) {
		this.nodeDao = Objects.requireNonNull(nodeDao);
		this.transactionOperations = Objects.requireNonNull(transactionOperations);
	}

	//    public GraphML generateGraphs(GeneratorConfig config) {
	//        final GraphML graphML = new GraphML();
	//        graphML.setProperty(GraphMLProperties.LABEL, config.getLabel());
	//        graphML.setProperty(GraphMLProperties.BREADCRUMB_STRATEGY, config.getBreadcrumbStrategy());
	//
	//        // Simulate asset layer generation
	//        final List<String> layerHierarchies = config.getLayerHierarchies();
	//        for (String eachLayer : layerHierarchies) {
	//            GraphMLGraph layerGraph = new GraphMLGraph();
	//            layerGraph.setId(eachLayer);
	//            layerGraph.setProperty(GraphMLProperties.NAMESPACE, "asset:" + eachLayer);
	//            applyDefaults(layerGraph, "Layer " + eachLayer, config);
	//
	//            // E.g. assetHelper.getAssetValues(eachLayer)
	//            for (int i=0; i<3; i++) {
	//                GraphMLNode assetNode = new GraphMLNode();
	//                assetNode.setId(eachLayer + "_" + i);
	//                assetNode.setProperty(GraphMLProperties.LABEL, "Simulated Asset value layer " + eachLayer + " item " + i);
	//
	//                layerGraph.addNode(assetNode);
	//            }
	//            graphML.addGraph(layerGraph);
	//        }
	//        // TODO implement linking to next layer, e.g.:
	////            for (String eachParent : assetHelper.getAssetValues(eachLayer)) {
	////                for (String eachChild : assetHelper.getAssetValues(eachParent)) {
	////
	////                }
	////            }
	//
	//
	//        // Finally simulating last layer
	//        GraphMLGraph nodeGraph = new GraphMLGraph();
	//        nodeGraph.setId("nodes");
	//        nodeGraph.setProperty(GraphMLProperties.NAMESPACE, "asset:nodes");
	//        applyDefaults(nodeGraph, "Nodes", config);
	//        for (OnmsNode node : nodeDao.findAllProvisionedNodes()) {
	//            GraphMLNode graphMLNode = new GraphMLNode();
	//            graphMLNode.setId("node: " + node.getNodeId());
	//
	//            graphMLNode.setProperty(GraphMLProperties.LABEL, node.getLabel());
	//            graphMLNode.setProperty(GraphMLProperties.NODE_ID, node.getId());
	//            graphMLNode.setProperty(GraphMLProperties.FOREIGN_ID, node.getForeignId());
	//            graphMLNode.setProperty(GraphMLProperties.FOREIGN_SOURCE, node.getForeignSource());
	//
	//            nodeGraph.addNode(graphMLNode);
	//        }
	//        graphML.addGraph(nodeGraph);
	//
	//        // TODO implement linking to last layer.:
	//        return graphML;
	//    }
	//    
	//    
	//
	//    private void applyDefaults(GraphMLGraph graph, String description, GeneratorConfig config) {
	//        graph.setProperty(GraphMLProperties.NAMESPACE, graph.getId());
	//        graph.setProperty(GraphMLProperties.FOCUS_STRATEGY, "ALL");
	//        if (config.getPreferredLayout() != null) {
	//            graph.setProperty(GraphMLProperties.PREFERRED_LAYOUT, config.getPreferredLayout());
	//        }
	//        if (description != null) {
	//            graph.setProperty(GraphMLProperties.DESCRIPTION, description);
	//        }
	//        graph.setProperty(GraphMLProperties.SEMANTIC_ZOOM_LEVEL, 0);
	//    }


	public GraphML generateGraphs(GeneratorConfig config) {

		String menuLabelStr= config.getLabel();
		List<String> layerHierarchy= config.getLayerHierarchies();
		String preferredLayout = config.getPreferredLayout();

		NodeInfoRepository nodeInfoRepository = new NodeInfoRepository();
		nodeInfoRepository.setNodeDao(nodeDao);
		nodeInfoRepository.setTransactionOperations(transactionOperations);
		nodeInfoRepository.initialiseNodeInfo(null);

		return nodeInfoToTopology(nodeInfoRepository, menuLabelStr,layerHierarchy, preferredLayout);

	}


	/**
	 * This method generates the layer hierarchy graphml file from the
	 * node asset information supplied in nodeInfoRepository
	 */
	public GraphML nodeInfoToTopology(NodeInfoRepository nodeInfoRepository, String menuLabelStr,List<String> layerHierarchy, String preferredLayout) {

		// print log info for graph definition
		StringBuffer msg = new StringBuffer("Creating topology "+menuLabelStr+" for layerHierarchy :");
		if(layerHierarchy.size()==0){
			msg.append("EMPTY");
		} else {
			for(String layer:layerHierarchy){
				msg.append(layer+",");
			}
		}
		LOG.info(msg.toString());

		GraphML graphmlType = createGraphML(menuLabelStr);

		Map<String, Map<String, String>> onmsNodeInfo = nodeInfoRepository.getNodeInfo();

		if (layerHierarchy.size()==0){
			//create simple graph with all nodes if layerHierarchy is empty
			if( LOG.isDebugEnabled()) LOG.debug("creating a simple graph containing all nodes as layerHierarchy is empty");

			Integer semanticZoomLevel=0;
			String graphId ="all nodes";
			String descriptionStr="A simple graph containing all nodes created because layerHierarchy property is empty";
			GraphMLGraph graph = createGraphInGraphmlType(graphmlType, graphId, descriptionStr, preferredLayout, semanticZoomLevel);

			Map<String, Map<String, String>> nodeInfo = nodeInfoRepository.getNodeInfo();
			addOpenNMSNodes(graph, nodeInfo);

		} else {
			// create graphs for all possible layers in hierarchy
			msg = new StringBuffer("create graphs from asset and layerHierarchy: ");

			List<GraphMLGraph> graphList = new ArrayList<GraphMLGraph>();
			Integer semanticZoomLevel=0;

			String descriptionStr="";
			// create graph for each layer in hierarchy
			for(String graphId:layerHierarchy){
				//(GraphmlType graphmlType, String graphId, String descriptionStr, String preferredLayout, Integer semanticZoomLevelInt)
				GraphMLGraph graph = createGraphInGraphmlType(graphmlType, graphId, descriptionStr, preferredLayout, semanticZoomLevel);
				graphList.add(graph);
				msg.append(graphId+",");
				semanticZoomLevel++;
			}

			//create graph for nodes layer (last layer in hierarchy)
			String graphId="nodes";
			GraphMLGraph nodegraph = createGraphInGraphmlType(graphmlType, graphId, descriptionStr, preferredLayout, semanticZoomLevel);
			graphList.add(nodegraph);
			msg.append(graphId);

			if( LOG.isDebugEnabled()) LOG.debug(msg.toString());

			// create graph hierarchy according to asset table contents

			// used to store all nodes which have been allocated to a graph layer
			Map<String, Map<String, String>> allocatedNodeInfo = new LinkedHashMap<String, Map<String, String>>();

			// add layer graphs for defined layerHierarchy
			String parentNodeId=null;
			recursivelyAddlayers(layerHierarchy, 0,   onmsNodeInfo,  allocatedNodeInfo,  graphmlType, graphList, parentNodeId);

			// add unallocated nodes into a default unallocated_Nodes graph
			Map<String, Map<String, String>> unAllocatedNodeInfo = new LinkedHashMap<String, Map<String, String>>();
			unAllocatedNodeInfo.putAll(onmsNodeInfo); //initialise with full list of nodes

			for (String allocatedNodeId:allocatedNodeInfo.keySet()){
				unAllocatedNodeInfo.remove(allocatedNodeId);
			}

			graphId="unallocated_Nodes";
			descriptionStr="A graph containing all nodes which cannot be placed in topology hierarchy";
			semanticZoomLevel=0;
			GraphMLGraph graph = createGraphInGraphmlType(graphmlType, graphId, descriptionStr, preferredLayout, semanticZoomLevel);
			addOpenNMSNodes(graph, unAllocatedNodeInfo);
		}

		return graphmlType;
	}



	/**
	 * Recursive function to add OpenNMS nodes defined in nodeInfo into hierarchy of graphs created in the given graphmlType
	 * returns list of nodes added in the next layer for use in edges in this layer
	 * @param layerHierarchy static list of layer names which correspond to asset table keys defined in NodeParamLabels
	 * @param layerHierarchyIndex the current layer for which this function is called (initialise to 0) subsequent recursive calls will increment the number until layerHierarchy.size()
	 * @param nodeInfo nodeInfo map with values Map<nodeId, Map<nodeParamLabelKey, nodeParamValue>>
	 *        nodeParamLabelKey a node asset parameter key (from those defined in org.opennms.plugins.graphml.asset.NodeParamLabels)
	 *        nodeParamValue a node asset value ( e.g. key NodeParamLabels.ASSET_RACK ('asset-rack') value: rack1
	 * @param allocatedNodeInfo this contains all of the nodes which a recursive call to this method has added. i.e. once the function is finished,
	 * all of the nodes placed in a graph are included in this list. The list can then be used to determine the unallocated nodes.
	 * @param graphmlType the parent graphmltype into which all the created graphe muse be placed
	 * @param graphList a list of pre created graphs which are in the same order and should  be pre-named with the names in the layerHierarchy
	 * @param parentNodeId the nodeId of the parent node which the edges generated for the next layer must reference
	 * @return addedNodes returns list of nodes which have been added by this recursive call. These nodes are used to create the edges in the previous layer
	 */
	private Map<String, Map<String, String>> recursivelyAddlayers(List<String> layerHierarchy, int layerHierarchyIndex,  
			Map<String, Map<String, String>> nodeInfo, Map<String, Map<String, String>> allocatedNodeInfo, 
			GraphML graphmlType, List<GraphMLGraph> graphList, String parentNodeId){
		if(layerHierarchy==null||layerHierarchy.size()==0 ) throw new RuntimeException("AssetTopologyMapperImpl layerHierarchy must not be null or empty");

		// returns list of nodes added - either OpenNMS nodes or higher level graphs
		Map<String, Map<String, String>> addedNodes=null;

		if( LOG.isDebugEnabled()) LOG.debug("recursivelyAddlayers called for layerHierarchyIndex:"+layerHierarchyIndex+" parentNodeId="+parentNodeId);

		// add nodes to graph
		if(layerHierarchyIndex>=layerHierarchy.size()){
			// we are at bottom of hierarchy so add real opennms nodes and edges

			//get hierarchy name for the previous layer
			String layerNodeParamLabel= layerHierarchy.get(layerHierarchyIndex-1);

			// get graph for this layer
			if( LOG.isDebugEnabled()) LOG.debug("populating graph with OpenNMS nodes for layer="+layerNodeParamLabel);

			// this will return the nodes graph - the last graph in graphList
			GraphMLGraph graph = graphList.get(layerHierarchyIndex); 

			//add real opennms nodes to graph
			addOpenNMSNodes(graph, nodeInfo);

			// add these nodes to the allocated node set
			allocatedNodeInfo.putAll(nodeInfo);

			if (LOG.isDebugEnabled()) {
				StringBuffer msg= new StringBuffer("adding opennms nodes to graphId="+layerNodeParamLabel+ " nodes:");

				for (String targetNodeId:nodeInfo.keySet()){
					msg.append(targetNodeId+",");
				}
			}

			addedNodes=nodeInfo;

		} else {
			// else create and add the parent nodes for the next layer
			if( LOG.isDebugEnabled()) LOG.debug("populating parent graph for index "+layerHierarchyIndex);

			//get hierarchy name for this layer
			String layerNodeParamLabelKey= layerHierarchy.get(layerHierarchyIndex);
			if( LOG.isDebugEnabled()) LOG.debug("parent graph name="+layerNodeParamLabelKey);

			// get graph for this layer
			GraphMLGraph graph = graphList.get(layerHierarchyIndex);
			// this will return the nodes graph - the last  graph in graphList
			GraphMLGraph nextgraph = graphList.get(layerHierarchyIndex+1); 

			// find all values corresponding to nodeParamLabelKey in this layer
			Set<String> layerNodeParamLabelValues = new TreeSet<String>();
			for (String nodeId: nodeInfo.keySet()){
				String nodeParamValue = nodeInfo.get(nodeId).get(layerNodeParamLabelKey);
				if(nodeParamValue!=null){
					layerNodeParamLabelValues.add(nodeParamValue);
				}
			}

			if (LOG.isDebugEnabled()){
				StringBuffer msg=new StringBuffer("values corresponding to layerNodeParamLabelKey="+layerNodeParamLabelKey+ " in this layer :");
				for (String nodeParamValue:layerNodeParamLabelValues){
					msg.append(nodeParamValue+",");
				}
				LOG.debug(msg.toString());
			}

			// create added nodes to return. These are the nodes which have been added in this layer
			// and are used to populate the edges in the previous layer
			addedNodes = new LinkedHashMap<String, Map<String, String>>();

			// iterate over values in this layer
			for (String nodeParamLabelValue:layerNodeParamLabelValues){

				// create new node for each value in this layer
				String graphmlNodeId= (parentNodeId==null) ? nodeParamLabelValue : parentNodeId+"."+nodeParamLabelValue;
				GraphMLNode node = createNodeType(graphmlNodeId,nodeParamLabelValue);
				graph.addNode(node);

				StringBuffer msg=new StringBuffer("created childNode graphmlNodeId="+graphmlNodeId+" nodeParamLabelValue="+nodeParamLabelValue+ " in  graphId="+layerNodeParamLabelKey);

				// create sub list of nodes corresponding to param label 
				Map<String, Map<String, String>> nodeInfoSubList =createNodeInfoSubList(layerNodeParamLabelKey, nodeParamLabelValue, nodeInfo);

				// recursively add graphs and nodes until complete
				int nextLayerHierarchyIndex=layerHierarchyIndex+1;
				Map<String, Map<String, String>> nextLayerNodesAdded = recursivelyAddlayers(layerHierarchy, nextLayerHierarchyIndex, nodeInfoSubList, allocatedNodeInfo, graphmlType, graphList, graphmlNodeId );

				// we are now using data returned from a recursive call to recursivelyAddlayers
				// nextLayerNodesAdded contains the nodes added in the lower layer
				// and these can be used to populates edges in this layer

				// create edge for each node in returned nextLayerNodesAdded
				if (nextLayerHierarchyIndex<layerHierarchy.size()){
					// if not lowest layer then add edges pointing next layers
					msg.append(" edges added for next graph layer: " );
					for (String targetNodeId:nextLayerNodesAdded.keySet()){
						Map<String, String> nodeParamaters = nextLayerNodesAdded.get(targetNodeId);
						String labelStr = nodeParamaters.get(layerHierarchy.get(nextLayerHierarchyIndex));
						String childNodeLabelStr= graphmlNodeId+"."+labelStr;

						GraphMLNode childNode = nextgraph.getNodeById(childNodeLabelStr);						
						GraphMLEdge edge = addEdgeToGraph(graph, node, childNode);

						if (edge!=null) msg.append(edge.getId()+",");

						addedNodes.put(targetNodeId, nodeParamaters);
					}
				} else {
					// if lowest layer then add node ids (i.e. opennms node labels)
					msg.append(" edges added for opennms nodes: " );
					for (String targetNodeId:nextLayerNodesAdded.keySet()){
						Map<String, String> nodeParamaters = nextLayerNodesAdded.get(targetNodeId);
						String nodeLabelStr = nodeParamaters.get(NodeParamLabels.NODE_NODELABEL);

						GraphMLNode childNode = nextgraph.getNodeById(nodeLabelStr);
						GraphMLEdge edge = addEdgeToGraph(graph, node, childNode);

						if (edge!=null) msg.append(edge.getId()+",");

						addedNodes.put(targetNodeId, nodeParamaters);
					}
				}

				if( LOG.isDebugEnabled()){
					LOG.debug(msg.toString());
				}

			}

		}
		if( LOG.isDebugEnabled()) LOG.debug("returning from recursivelyAddlayers called for layerHierarchyIndex:"+layerHierarchyIndex);
		return addedNodes;
	}


	/**
	 * searches the supplied nodeInfo for nodes with matching parameters for nodeParamLabelKey and nodeParamValue
	 * returns a sub list of nodeInfo only for the nodes with the matching parameter
	 * @param nodeParamLabelKey a node asset parameter key (from those defined in org.opennms.plugins.graphml.asset.NodeParamLabels)
	 * @param nodeParamValue a node asset value ( e.g. key NodeParamLabels.ASSET_RACK ('asset-rack') value: rack1
	 * @param nodeInfo Map<String, Map<String, String>> with values Map<nodeId, Map<nodeParamLabelKey, nodeParamValue>>
	 * @return
	 */
	private Map<String, Map<String, String>> createNodeInfoSubList(String nodeParamLabelKey, String nodeParamValue, Map<String, Map<String, String>> nodeInfo){
		if(nodeParamLabelKey==null) throw new RuntimeException("createNodeInfoSubList nodeParamLabel cannot be null");
		if(nodeParamValue==null) throw new RuntimeException("createNodeInfoSubList nodeParamValue cannot be null");

		Map<String,Map<String,String>> nodeInfoSubList = new LinkedHashMap<String, Map<String, String>>();

		StringBuffer msg = new StringBuffer("creating NodeInfoSubList for nodeParamLabelKey:"+nodeParamLabelKey+" nodeParamValue:"+nodeParamValue+ " sublist nodeIds:");
		for (String nodeId:nodeInfo.keySet()){
			Map<String, String> nodeParams = nodeInfo.get(nodeId);
			if(nodeParamValue.equals(nodeParams.get(nodeParamLabelKey))){
				nodeInfoSubList.put(nodeId, nodeParams );
				msg.append(nodeId+",");
			}
		}
		if( LOG.isDebugEnabled()) LOG.debug(msg.toString());
		return nodeInfoSubList;
	}


	/**
	 * Creates a new edge and adds it to a given graphml graph. The graph is first searched and the edge is only added if
	 * its id is not already defined. The id is concatenated from the sourceIdStr and the targetIdStr
	 * @param graph
	 * @param sourceIdStr source nodeId for this edge (nodeId represents the graphml nodeId unique in the graph namespace)
	 * @param targetIdStr target nodeId for this edge
	 * @return added edge or null if already in graph
	 */
	private GraphMLEdge addEdgeToGraph(GraphMLGraph graph, GraphMLNode sourceNode, GraphMLNode targetNode){

		GraphMLEdge edge= null;

		String sourceIdStr = sourceNode.getId();
		String targetIdStr = targetNode.getId();

		String id =sourceIdStr+"_"+targetIdStr;

		if (graph.getEdgeById(id)==null){
			if( LOG.isDebugEnabled()) LOG.debug("adding edge id="+id+ " to graph:"+graph.getId());
			edge= new GraphMLEdge();
			edge.setId(id);
			edge.setSource(sourceNode);
			edge.setTarget(targetNode);
			graph.addEdge(edge);
		} else {
			if( LOG.isDebugEnabled()) LOG.debug("not adding edge id="+id+ " as already in graph:"+graph.getId());
		}

		return edge;
	}

	/**
	 * Creates a new graphml node with a nodeId and nodeLabel
	 * note that the nodeId is the unique id in the graph name space. The nodelabel is the value which shows
	 * up on the rendered graph
	 * @param nodeId
	 * @param nodeLabel
	 * @return
	 */
	private GraphMLNode createNodeType(String nodeId, String nodeLabel){
		GraphMLNode graphMLNode = new GraphMLNode();
		graphMLNode.setId(nodeId);

		graphMLNode.setProperty(GraphMLProperties.LABEL, nodeLabel);
		return graphMLNode;
	}

	/**
	 * Creates a new empty graphml graph with a predefined breadcrumb strategy for the OpenNMS use case
	 * @param menuLabelStr
	 * @return
	 */
	private GraphML createGraphML(String menuLabelStr){
		final GraphML graphML = new GraphML();
		graphML.setProperty(GraphMLProperties.LABEL, menuLabelStr);
		//graphML.setProperty(GraphMLProperties.BREADCRUMB_STRATEGY, config.getBreadcrumbStrategy());
		//String bc = BreadcrumbStrategy.SHORTEST_PATH_TO_ROOT.name(); //TODO
		graphML.setProperty(GraphMLProperties.BREADCRUMB_STRATEGY, "SHORTEST_PATH_TO_ROOT");
		return graphML;
	}

	/**
	 * Creates a new graph in the graphml type with default data values for the opennms keys
	 * @param graphML
	 * @param graphId
	 * @param descriptionStr
	 * @param preferredLayout
	 * @param semanticZoomLevelInt
	 * @return
	 */
	private GraphMLGraph createGraphInGraphmlType(GraphML graphML, String graphId, String descriptionStr, String preferredLayout, Integer semanticZoomLevelInt) {
		GraphMLGraph graph = new GraphMLGraph();
		graph.setId(graphId);

		graph.setProperty(GraphMLProperties.NAMESPACE, graphId);
		graph.setProperty(GraphMLProperties.FOCUS_STRATEGY, "ALL");
		graph.setProperty(GraphMLProperties.PREFERRED_LAYOUT, preferredLayout);
		graph.setProperty(GraphMLProperties.DESCRIPTION, descriptionStr);
		graph.setProperty(GraphMLProperties.SEMANTIC_ZOOM_LEVEL, semanticZoomLevelInt);

		graphML.addGraph(graph);

		return graph;

	}

	/**
	 * Adds all of the OpenNMS nodes defined in the nodeInfo type to a graph. Adds nodeID and foreignsource / foreignid values if defined
	 * @param nodesGraph
	 * @param nodeInfo map with values Map<nodeId, Map<nodeParamLabelKey, nodeParamValue>>
	 *        nodeParamLabelKey a node asset parameter key (from those defined in org.opennms.plugins.graphml.asset.NodeParamLabels)
	 *        nodeParamValue a node asset value ( e.g. key NodeParamLabels.ASSET_RACK ('asset-rack') value: rack1
	 */
	private void addOpenNMSNodes(GraphMLGraph nodesGraph, Map<String, Map<String, String>> nodeInfo) {

		// set vertex-status-provider true for nodes graph		
		nodesGraph.setProperty(GraphMLProperties.VERTEX_STATUS_PROVIDER, true);

		for (String nodeId:nodeInfo.keySet()){

			GraphMLNode graphMLNode = new GraphMLNode();

			Map<String, String> nodeParamaters = nodeInfo.get(nodeId);
			String foreignSourceStr= nodeParamaters.get(NodeParamLabels.NODE_FOREIGNSOURCE);
			String foreignIdStr= nodeParamaters.get(NodeParamLabels.NODE_FOREIGNID);
			String nodeLabelStr = nodeParamaters.get(NodeParamLabels.NODE_NODELABEL);
			graphMLNode.setId(nodeLabelStr);
			graphMLNode.setProperty(GraphMLProperties.LABEL, nodeLabelStr);
			graphMLNode.setProperty(GraphMLProperties.NODE_ID, nodeId);
			graphMLNode.setProperty(GraphMLProperties.FOREIGN_ID, foreignIdStr);
			graphMLNode.setProperty(GraphMLProperties.FOREIGN_SOURCE, foreignSourceStr);

			nodesGraph.addNode(graphMLNode);
		}


	}







}
