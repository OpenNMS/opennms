package org.opennms.web.map.mapd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.map.MapsConstants;
import org.opennms.netmgt.xml.map.NodeChange;

public  class SharedChanges {
	Category log = null;
	public SharedChanges() {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		log.info("Creating SharedChanges");
		changedNodes = new HashMap<Integer,NodeChange>();
	}
	/**
	 * The HashMap <nodeid, nodeChange> contains changed nodes since last pop
	 */
	 HashMap<Integer,NodeChange> changedNodes;
	
	/**
	 * Pops all changed nodes since last pop
	 * @return HashMap<Integer, NodeChange> containing nodes by ids
	 */
	public synchronized HashMap<Integer, NodeChange> popChangedNodes(){
		HashMap<Integer, NodeChange> tmp = changedNodes;
		changedNodes.clear();
		return tmp;
	}
	
	
	/**
	 * Adds nodes to HashMap of changed nodes
	 * @param nodes
	 */
	public synchronized void addChangedNodes(Collection<NodeChange> nodes){
		Iterator<NodeChange> it  = nodes.iterator();
		while(it.hasNext()){
			NodeChange curr = it.next();
			changedNodes.put(curr.getId(),curr);
		}
	}
	
	/**
	 * Adds nodes to HashMap of changed nodes
	 * @param nodes
	 */
	public synchronized void addChangedNode(NodeChange node){
		changedNodes.put(node.getId(),node);
	}
	
	
}
