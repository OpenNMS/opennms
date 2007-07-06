package org.opennms.web.map.mapd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;

public class Snapshot{
	protected Manager m_manager;
	protected VMap map;
	
	private HashMap<Set<Integer>, VElement> nodeidsOnElements=null;
	
	private long refreshCounter=1;

	private boolean changed=false;
	
	List<VElement> lastchangedElements=new ArrayList<VElement>();
	List<VLink> lastchangedLinks=new ArrayList<VLink>();
	List<VLink> lastdeletedLinks=new ArrayList<VLink>();
	
	//These lists contains the last changes posted to the client. They may be used to 
	//synchronize the client and the server map by using the feedback of the refresh obtained from the client.
	List<VElement> changedElements;
	List<VLink> changedLinks;
	List<VLink> deletedLinks;
	Category log = null;
	

	

	public boolean isChanged() {
		return changed;
	}
	
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	
	public Snapshot(VMap map, Manager manager) throws MapsException{
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		log.debug("Creating the Snapshot for the map "+map.getId() +" and the manager "+manager);
		this.map=map;
		this.m_manager=manager;
		VElement[] elems=map.getAllElements();
		nodeidsOnElements=new HashMap<Set<Integer>, VElement>();
		if(elems!=null)
			for(int i=0;i<elems.length;i++){
				Set<Integer> nodeids=m_manager.getNodeidsOnElement(elems[i]);
				nodeidsOnElements.put(nodeids, elems[i]);
			}

	}
	
	/**
	 * gets the VElements in which the node with id (nodeid) stays. 
	 * @param nodeid
	 * @return the Set of VElement in which the node with id (nodeid) stays
	 */
	public Set<VElement> getElementsForNode(int nodeid) {
		Set<VElement> result=new HashSet<VElement>();
		Iterator<Set<Integer>> nodesSet = nodeidsOnElements.keySet().iterator();
		while(nodesSet.hasNext()){
			Set<Integer> set = nodesSet.next();
			if(set.contains(new Integer(nodeid))){
				result.add(nodeidsOnElements.get(set));
			}
		}
		return result;
	}
	
    public Set<Integer> getNodeidsOnElement(VElement elem) throws MapsException {
    	return m_manager.getNodeidsOnElement(elem);
    }


	
	public VElement getElement(int id, String type){
		return map.getElement(id, type);
	}
	
	public VElement[] getAllElements(){
		return map.getAllElements();
	}
	
	public boolean containsElement(int id, String type){
		return map.containsElement(id, type);
	}
	
	public void resetLastChangedElements(){
		lastchangedElements=new ArrayList<VElement>();
	}
	
	
	public void resetLastChangedLinks(){
		lastchangedLinks=new ArrayList<VLink>();
	}
	
	public void resetLastDeletedLinks(){
		lastdeletedLinks=new ArrayList<VLink>();
	}
	
	public List<VLink> getLinksOnElem(VElement[] elems,VElement elem) throws MapsException {
		return m_manager.getLinksOnElem(elems, elem);
	}

	
	/**
	 * get the elements added in last refresh call 
	 * @return List<VElement> containing the elements added in last refresh call 
	 */
	public List<VElement> getChangedElements() {
		log.debug("getChangedElements "+changedElements.toString());
		lastchangedElements.addAll(changedElements);
		return changedElements;
	}



	/**
	 * get the links added in last refresh call 
	 * @return List<VLink> containing the links added in last refresh call 
	 */
	public List<VLink> getChangedLinks() {
		log.debug("getChangedLinks "+changedLinks.toString());
		lastchangedLinks.addAll(changedLinks);
		return changedLinks;
	}


	/**
	 * get the links removed in last refresh call 
	 * @return List<VLink> containing the links removed in last refresh call 
	 */
	public List<VLink> getDeletedLinks() {
		log.debug("getDeletedLinks "+deletedLinks.toString());
		lastdeletedLinks.addAll(deletedLinks);
		return deletedLinks;
	}
	
	
	public long incrementCounter(){
		return ++refreshCounter;
	}
	
	public long getCounter(){
		return refreshCounter;
	}

	public void addChangedElements(List<VElement> changed) {
		this.changedElements.addAll(changed);
	}

	
	public void setChangedElements(List<VElement> changed) {
		this.changedElements = changed;
	}
	
	public void addChangedLinks(List<VLink> changed) {
		this.changedLinks.addAll(changed);
	}
	
	public void setChangedLinks(List<VLink> changed) {
		this.changedLinks = changed;
	}

	public void addDeletedLinks(List<VLink> deleted) {
		this.deletedLinks.addAll(deleted);
	}

	public void setDeletedLinks(List<VLink> deleted) {
		this.deletedLinks = deleted;
	}

	protected List<VElement> getLastchangedElements() {
		return lastchangedElements;
	}

	protected List<VLink> getLastchangedLinks() {
		return lastchangedLinks;
	}

	
	protected List<VLink> getLastdeletedLinks() {
		return lastdeletedLinks;
	}
	
	
	
}
