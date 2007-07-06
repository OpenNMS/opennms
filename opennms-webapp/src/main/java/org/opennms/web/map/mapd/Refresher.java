package org.opennms.web.map.mapd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;


import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.MapsException;
import org.opennms.netmgt.xml.map.NodeChange;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;



public class Refresher extends Thread{

	//the key is the session id, the value is the Snapshot of the map server-side 
	static Map<String, Snapshot> snapshots;
	static Category log = null;
	static SharedChanges m_sharedChanges;
	static ServletContext m_context; 
	private static Refresher m_singleton; 
	
	private List<NodeChange> nodesNotSynchronized=new ArrayList<NodeChange>(); 
	
	public static void init(ServletContext context ){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(Refresher.class);
		log.info("Initializing Maps Refresher");
		if(m_singleton!=null)
			return;

		m_context=context;
		m_sharedChanges=(SharedChanges)context.getAttribute("MapSharedChanges");
		if(m_sharedChanges==null){
			log.error("MapSharedChanges is null!");
			//TODO return;
			//throw new IllegalStateException("MapSharedChanges is null!");
		}
		m_singleton=new Refresher(m_sharedChanges);
	}
	
	public static Refresher getInstance()throws IllegalStateException{
		log.debug("Getting Instance.");
		if(m_sharedChanges==null){
			log.error("Call Init() before getInstance().");
			// TODO throw new IllegalStateException("Call Init() before getInstance().");
		}
		return m_singleton;
	}
	
	private Refresher(SharedChanges sc) {
		m_sharedChanges=sc;
		snapshots=new HashMap<String, Snapshot>();
	}
	
	private Refresher() {
	}
	
	public void doStart(){
		m_singleton.start();
	}
	public void run() {
		log.info("Running...");
		while(true){
			synchronized (m_sharedChanges) {
				try {
					log.info("waiting on MapSharedChanges...");
					m_sharedChanges.wait();
					log.info("waking up!");
					
					log.info("getting changed nodes from MapSharedChanges");
					HashMap<Integer, NodeChange> nodes = m_sharedChanges.popChangedNodes();
					
					try {
						synchronizeSnapshots(nodes);
					} catch (MapsException e) {
						log.error(e,e);
						//if errors occour, re-set nodes and links 
						nodesNotSynchronized.addAll(nodes.values());
					}
					log.info("Setting MapSharedChanges in application context");
					m_context.setAttribute("MapSharedChanges", m_sharedChanges);
				} catch (InterruptedException e) {
					log.error(e,e);
					//return;
				}
			}
		}
	}
	
	
	
	private void synchronizeSnapshots(HashMap<Integer, NodeChange> changedNodes)throws MapsException{
		Iterator<Snapshot> iter = snapshots.values().iterator();
		while(iter.hasNext()){
			synchronizeSnapshot(iter.next(),changedNodes);
		}
	}

	
	private void synchronizeSnapshot(Snapshot ss, HashMap<Integer, NodeChange> changedNodes)throws MapsException{
	
		Iterator<NodeChange> elems = changedNodes.values().iterator();
		ArrayList<VElement> chNodes = new ArrayList<VElement>();
		ArrayList<VLink> addLinks = new ArrayList<VLink>();
		ArrayList<VLink> delLinks = new ArrayList<VLink>();
		log.info("Synchronizing snapshot (map/manager) "+ss.map.getId()+ "/"+ss.m_manager);
		while(elems.hasNext()){
			NodeChange n = elems.next();
			
			if(ss.containsElement(n.getId(),VElement.NODE_TYPE)){
				log.debug("Snapshot contains "+n.getId()+","+VElement.NODE_TYPE+" upgrading status and adding to changedNodes List");
				VElement node = ss.getElement(n.getId(), VElement.NODE_TYPE);
				node.setStatus(n.getStatus());
				//TODO manage availability and severity also.
				chNodes.add(node);
			}
			//TODO fix following: elements returned has not realtime values (status, avail, sev.)  updated, but their values are obsolete
			Set<VElement> parentsOfNode = ss.getElementsForNode(n.getId());
			chNodes.addAll(parentsOfNode);
			
			List<VLink> linksOnNode = new ArrayList<VLink>();
			if(n.getAddlinks() || n.getDeletelinks()){
				VElement nodeElement=ss.getElement(n.getId(), VElement.NODE_TYPE);
				VElement[] elemes = ss.getAllElements();
				linksOnNode = ss.getLinksOnElem(elemes, nodeElement);
			}
			if(n.getAddlinks()){
				log.info("adding links on node "+linksOnNode.toString());
				addLinks.addAll(linksOnNode);
			}
			if(n.getDeletelinks()){
				log.info("deleting links on node "+linksOnNode.toString());
				delLinks.addAll(linksOnNode);
			}
		}
		
		//add found changes to the snapshot
		if(chNodes.size()>0)
			ss.addChangedElements(chNodes);
		if(addLinks.size()>0)
			ss.addChangedLinks(addLinks);		
		if(delLinks.size()>0)
			ss.addDeletedLinks(delLinks);
	}
/*
	*//**
	 * The method finds (and returns) the links between elements (maps and nodes) in the map of passed snapshot. 
	 * @param link
	 * @param ss
	 * @return the links between elements (maps and nodes) in the map of passed snapshot.
	 *//*
	private List<VLink> getMatchingLinks(VLink link, Snapshot ss)throws MapsException{
		List<VLink> result = new ArrayList<VLink>();
		if(ss.containsElement(link.getFirst().getId(),link.getFirst().getType()) && ss.containsElement(link.getSecond().getId(),link.getSecond().getType())){
			result.add(link);
		}
		VElement[] elems = ss.getAllElements();
		if(elems!=null){
			boolean hasFirst=false, hasSecond=false;
			//find first elem of the link
			if(ss.containsElement(link.getFirst().getId(),link.getFirst().getType())){
				hasFirst=true;
			}
			//find second elem of the link
			if(ss.containsElement(link.getSecond().getId(),link.getSecond().getType())){
				hasSecond=true;
			}
			
			//add links node-submap (and submap-node)
			List<VElement> elemsContainingFirst = new ArrayList<VElement>();
			List<VElement> elemsContainingSecond = new ArrayList<VElement>();
			for(int i=0; i<elems.length;i++){
				if(ss.getNodeidsOnElement(elems[i]).contains(link.getFirst().getId()))
					elemsContainingFirst.add(elems[i]);
				if(ss.getNodeidsOnElement(elems[i]).contains(link.getSecond().getId()))
					elemsContainingSecond.add(elems[i]);
			}
			
			Iterator<VElement> iter = elemsContainingFirst.iterator();
			while(iter.hasNext()){
				VElement currEl = iter.next();
				if(hasSecond){
					VLink newLink = new VLink(currEl,link.getSecond());
					newLink.setLinkOperStatus(link.getLinkOperStatus());
					newLink.setLinkTypeId(link.getLinkTypeId());
					result.add(newLink);
				}
			}
			
			iter = elemsContainingSecond.iterator();
			while(iter.hasNext()){
				VElement currEl = iter.next();
				if(hasFirst){
					VLink newLink = new VLink(link.getFirst(),currEl);
					newLink.setLinkOperStatus(link.getLinkOperStatus());
					newLink.setLinkTypeId(link.getLinkTypeId());
					result.add(newLink);
				}
			}
			
			// add links submap-submap 
			iter = elemsContainingFirst.iterator();
			while(iter.hasNext()){
				VElement firstEl = iter.next();
				Iterator<VElement> ite2 = elemsContainingSecond.iterator();
				while(ite2.hasNext()){
					VElement secondEl = ite2.next();
					VLink newLink = new VLink(firstEl,secondEl);
					newLink.setLinkOperStatus(link.getLinkOperStatus());
					newLink.setLinkTypeId(link.getLinkTypeId());
					result.add(newLink);
				}
			}
			
		}
		return result;
	}*/
	
	/**
	 * gets last changed elements
	 * @param sessionId
	 * @param map
	 * @param manager
	 * @return List<VElement> containing the last changed elements
	 */
	List<VElement> getLastChangedElements(String sessionId, VMap map, Manager manager) throws MapsException{
		log.debug("getting last changed elements... (sessionId="+sessionId+"), (mapId="+map.getId()+")  (manager="+manager.toString()+")");
		Snapshot mapSS = snapshots.get(sessionId);
		if(mapSS==null || (mapSS!=null && mapSS.map.getId()!=map.getId())){
			log.debug("map's snapshot not found or containing old map; creating new snapshot and starting it.");
			try {
				mapSS = new Snapshot(map, manager);
			} catch (MapsException e) {
				log.error("Error while creating snapshot for map/manager "+map.getId()+"/"+manager,e);
				throw e;
			}
			snapshots.put(sessionId, mapSS);
		}
		return mapSS.getLastchangedElements();
	}
	
	void resetLastChangedElements(String sessionId, VMap map, Manager manager){
		log.debug("resetting last changed elements... (sessionId="+sessionId+"), (mapId="+map.getId()+")  (manager="+manager.toString()+")");
		Snapshot mapSS = snapshots.get(sessionId);
		if(!(mapSS==null || (mapSS!=null && mapSS.map.getId()!=map.getId()))){
			mapSS.resetLastChangedElements();
			snapshots.put(sessionId, mapSS);
		}
	}

	
	
	void resetLastChangedLinks(String sessionId, VMap map, Manager manager){
		log.debug("resetting last changed links... (sessionId="+sessionId+"), (mapId="+map.getId()+")  (manager="+manager.toString()+")");
		Snapshot mapSS = snapshots.get(sessionId);
		if(!(mapSS==null || (mapSS!=null && mapSS.map.getId()!=map.getId()))){
			mapSS.resetLastChangedLinks();
			snapshots.put(sessionId, mapSS);
		}
	}
	
	void resetLastDeletedLinks(String sessionId, VMap map, Manager manager){
		log.debug("resetting last deleted links... (sessionId="+sessionId+"), (mapId="+map.getId()+")  (manager="+manager.toString()+")");
		Snapshot mapSS = snapshots.get(sessionId);
		if(!(mapSS==null || (mapSS!=null && mapSS.map.getId()!=map.getId()))){
			mapSS.resetLastDeletedLinks();
			snapshots.put(sessionId, mapSS);
		}
	}
	
	
	/**
	 * gets last changed links
	 * @param sessionId
	 * @param map
	 * @param manager
	 * @return List<VLink> containing the last changed links
	 */
	List<VLink> getLastChangedLinks(String sessionId, VMap map, Manager manager)throws MapsException{
		log.debug("getting last changed links... (sessionId="+sessionId+"), (mapId="+map.getId()+")  (manager="+manager.toString()+")");
		Snapshot mapSS = snapshots.get(sessionId);
		if(mapSS==null || (mapSS!=null && mapSS.map.getId()!=map.getId())){
			log.debug("map's snapshot not found or containing old map; creating new snapshot and starting it.");
			try {
				mapSS = new Snapshot(map, manager);
			} catch (MapsException e) {
				log.error("Error while creating snapshot for map/manager "+map.getId()+"/"+manager,e);
				throw e;
			}
			snapshots.put(sessionId, mapSS);
		}
		return mapSS.getLastchangedLinks();
	}

	/**
	 * gets last deleted links
	 * @param sessionId
	 * @param map
	 * @param manager
	 * @return List<VLink> containing the last deleted links
	 */
	List<VLink> getLastDeletedLinks(String sessionId, VMap map, Manager manager)throws MapsException{
		log.debug("getting deleted links... (sessionId="+sessionId+"), (mapId="+map.getId()+")  (manager="+manager.toString()+")");
		Snapshot mapSS = snapshots.get(sessionId);
		if(mapSS==null || (mapSS!=null && mapSS.map.getId()!=map.getId())){
			log.debug("map's snapshot not found or containing old map; creating new snapshot and starting it.");
			try {
				mapSS = new Snapshot(map, manager);
			} catch (MapsException e) {
				log.error("Error while creating snapshot for map/manager "+map.getId()+"/"+manager,e);
				throw e;
			}
			snapshots.put(sessionId, mapSS);
		}
		return mapSS.getLastdeletedLinks();
	}
	
	
	/**
	 * gets the changed elements
	 * @param sessionId
	 * @param map
	 * @param manager
	 * @return List<VElement> containing the changed elements
	 */
	List<VElement> getChangedElements(String sessionId, VMap map, Manager manager)throws MapsException{
		log.debug("getting changed elements... (sessionId="+sessionId+"), (mapId="+map.getId()+")  (manager="+manager.toString()+")");
		Snapshot mapSS = snapshots.get(sessionId);
		if(mapSS==null || (mapSS!=null && mapSS.map.getId()!=map.getId())){
			log.debug("map's snapshot not found or containing old map; creating new snapshot and starting it.");
			try {
				mapSS = new Snapshot(map, manager);
			} catch (MapsException e) {
				log.error("Error while creating snapshot for map/manager "+map.getId()+"/"+manager,e);
				throw e;
			}
			snapshots.put(sessionId, mapSS);
		}
		return mapSS.getChangedElements();
	}

	
	
	/**
	 * gets the changed links
	 * @param sessionId
	 * @param map
	 * @param manager
	 * @return List<VLink> containing the changed links
	 */
	List<VLink> getChangedLinks(String sessionId, VMap map, Manager manager)throws MapsException{
		log.debug("getting changed links... (sessionId="+sessionId+"), (mapId="+map.getId()+")  (manager="+manager.toString()+")");
		Snapshot mapSS = snapshots.get(sessionId);
		if(mapSS==null || (mapSS!=null && mapSS.map.getId()!=map.getId())){
			log.debug("map's snapshot not found or containing old map; creating new snapshot and starting it.");
			try {
				mapSS = new Snapshot(map, manager);
			} catch (MapsException e) {
				log.error("Error while creating snapshot for map/manager "+map.getId()+"/"+manager,e);
				throw e;
			}
			snapshots.put(sessionId, mapSS);
		}
		return mapSS.getChangedLinks();
	}

	/**
	 * gets the deleted links
	 * @param sessionId
	 * @param map
	 * @param manager
	 * @return List<VLink> containing the deleted links
	 */
	List<VLink> getDeletedLinks(String sessionId, VMap map, Manager manager)throws MapsException{
		log.debug("getting deleted links... (sessionId="+sessionId+"), (mapId="+map.getId()+")  (manager="+manager.toString()+")");
		Snapshot mapSS = snapshots.get(sessionId);
		if(mapSS==null || (mapSS!=null && mapSS.map.getId()!=map.getId())){
			log.debug("map's snapshot not found or containing old map; creating new snapshot and starting it.");
			try {
				mapSS = new Snapshot(map, manager);
			} catch (MapsException e) {
				log.error("Error while creating snapshot for map/manager "+map.getId()+"/"+manager,e);
				throw e;
			}
			snapshots.put(sessionId, mapSS);
		}
		return mapSS.getDeletedLinks();
	}
	
	/**
	 * increment the refresh counter for the snapshot of the session
	 * @param sessionId
	 * @param map
	 * @param manager
	 * @return the current value of the refresh counter
	 */	
	long incrementRefreshCounter(String sessionId, VMap map, Manager manager)throws MapsException{
		log.debug("incrementing refresh counter... (sessionId="+sessionId+"), (mapId="+map.getId()+")  (manager="+manager.toString()+")");
		Snapshot mapSS = snapshots.get(sessionId);
		if(mapSS==null || (mapSS!=null && mapSS.map.getId()!=map.getId())){
			log.debug("map's snapshot not found or containing old map; creating new snapshot and starting it.");
			try {
				mapSS = new Snapshot(map, manager);
			} catch (MapsException e) {
				log.error("Error while creating snapshot for map/manager "+map.getId()+"/"+manager,e);
				throw e;
			}
			snapshots.put(sessionId, mapSS);
		}
		return mapSS.incrementCounter();
	}

	/**
	 * get the refresh counter of the snapshot of the session
	 * @param sessionId
	 * @param map
	 * @param manager
	 * @return the current value of the refresh counter
	 */	
	long getRefreshCounter(String sessionId, VMap map, Manager manager)throws MapsException{
		log.debug("getting refresh counter... (sessionId="+sessionId+"), (mapId="+map.getId()+")  (manager="+manager.toString()+")");
		Snapshot mapSS = snapshots.get(sessionId);
		if(mapSS==null || (mapSS!=null && mapSS.map.getId()!=map.getId())){
			log.debug("map's snapshot not found or containing old map; creating new snapshot and starting it.");
			try {
				mapSS = new Snapshot(map, manager);
			} catch (MapsException e) {
				log.error("Error while creating snapshot for map/manager "+map.getId()+"/"+manager,e);
				throw e;
			}
			snapshots.put(sessionId, mapSS);
		}
		return mapSS.getCounter();
	}	

}
