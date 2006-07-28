/*
 * Created on 11-gen-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import java.util.Set;
import java.util.TreeSet;


import org.apache.log4j.Category;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.asset.AssetModel;
import org.opennms.web.category.CategoryModel;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.event.EventUtil;
import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.MapsManagementException;
import org.opennms.web.map.config.Avail;
import org.opennms.web.map.config.MapPropertiesFactory;
import org.opennms.web.map.config.Severity;
import org.opennms.web.map.config.Status;
import org.opennms.web.map.db.*;



/**
 * @author maurizio
 *  
 */
public class Manager {

    org.opennms.web.map.db.Manager m_dbManager = null;
    
    MapPropertiesFactory mpf = null;
    
    private java.util.Map statuses;
    
    private java.util.Map severities;
    
    private java.util.Map avails;

    private int defaultStatusId;
    
    private int defaultSeverityId;
    
    private int unknownStatusId;
    
    private boolean availEnabled = true;
    
    private Avail defaultEnableFalseAvail;
    
    private Category log=null;
    
    public Manager() {

		String LOG4J_CATEGORY = "OpenNMS.Map";
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log= ThreadCategory.getInstance(this.getClass());

    	m_dbManager = new org.opennms.web.map.db.Manager();
        
        try {
        	MapPropertiesFactory.init();
        	mpf = MapPropertiesFactory.getInstance();
        	statuses = mpf.getStatusesMap();
        	severities = mpf.getSeveritiesMap();
        	avails = mpf.getAvailabilitiesMap();
        	defaultStatusId = mpf.getDefaultStatus().getId();
        	defaultSeverityId = mpf.getDefaultSeverity().getId();
        	unknownStatusId = mpf.getUnknownUeiStatus().getId();
        	availEnabled = mpf.enableAvail();
        	if (!availEnabled) defaultEnableFalseAvail = mpf.getDisabledAvail();
        } catch (Exception e) {
        	log.fatal("cannot use map.properties file " + e);
        }

    }
    
    class OutageInfo {
    	int nodeid;
    	int status;
    	float severity;
    	
    	private OutageInfo(int nodeid,int status,float severity) {
    		this.nodeid = nodeid;
    		this.severity = severity;
    		this.status = status;
    	}
    	
    	
		public float getSeverity() {
			return severity;
		}
		public void setSeverity(float severity) {
			this.severity = severity;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
    }
    

    /**
     * Start the session: only operations made in the block between start and
     * end session will be saved correctly.
     * 
     * @throws MapsException
     * @see endSession
     */
    public void startSession() throws MapsManagementException {
        try {
            m_dbManager.startSession();
        } catch (SQLException se) {
            throw new MapsManagementException(
                    "Manager: unable to start session." + se);
        }
    }

    /**
     * Close the block open by startSession() method.
     * 
     * @throws MapsException
     * @see startSession()
     */
    synchronized public void endSession() throws MapsManagementException {
        try {
            m_dbManager.endSession();
        } catch (SQLException se) {
            throw new MapsManagementException("Manager: unable to end session."
                    + se);
        }
    }

    /**
     * Create a new empty VMap and return it.
     * 
     * @return the new VMap created.
     */
    public VMap newMap() {
        VMap m = new VMap();
        return m;
    }

    /**
     * Create a new VMap and return it
     * 
     * @param name
     * @param accessMode
     * @param owner
     * @param userModifies
     * @param width 
     * @param height
     * @return the new VMap
     */
    public VMap newMap(String name, String accessMode, String owner,
            String userModifies, int width, int height) {
        VMap m = new VMap();
        m.setAccessMode(accessMode);
        m.setName(name);
        m.setOwner(owner);
        m.setUserLastModifies(userModifies);
        m.setWidth(width);
        m.setHeight(height);
        return m;
    }

    /**
     * Take the map with id in input and return it in VMap form.
     * 
     * @param id
     * @return the VMap with identifier id
     * @throws MapsException
     */
    public VMap getMap(int id) throws MapsManagementException,
            MapNotFoundException, MapsException {
    	
        VMap retVMap = null;
        try {
        	log.debug("Starting getMap with id "+id);
            Map m = Factory.getMap(id);
            log.debug("Ending getMap with id "+id);
            if (m == null) {
                throw new MapNotFoundException("Map with id " + id
                        + " doesn't exist.");
            }
            log.debug("Creating VMap with id "+id);
            retVMap = new VMap(id, m.getName(), m.getBackground(),
                    m.getOwner(), m.getAccessMode(), m.getUserLastModifies(), m
                            .getScale(), m.getOffsetX(), m.getOffsetY(), m
                            .getType(),m.getWidth(),m.getHeight());
            log.debug("VMap with id "+id+" created.");
            retVMap.setCreateTime(m.getCreateTime());
            retVMap.setLastModifiedTime(m.getLastModifiedTime());
            log.debug("Starting getting elems for map with id "+id);
            Element[] mapElems = Factory.getElementsOfMap(id);
            log.debug("Ending getting elems for map with id "+id);
            VElement elem =null;
            if(mapElems!=null){
	            for (int i = 0; i < mapElems.length; i++) {
            		elem = new VElement(mapElems[i]);
            		// here we must add all the stuff required
            		retVMap.addElement(elem);
	            }
            }
            log.debug("Starting refreshing elems for map with id "+id);
            List els = refreshElements(retVMap.getAllElements(),false);
            log.debug("Ending refreshing elems for map with id "+id);
            log.debug("els.toArray()");
            VElement[] ves = (VElement[]) els.toArray(new VElement[0]);
			log.debug("addElements(els)");
			retVMap.addElements(ves);
            
            log.debug("Starting adding links for map with id "+id);
            log.debug("Starting getLinks");
            VLink[] vls = (VLink[]) getLinks(retVMap.getAllElements()).toArray(new VLink[0]);
            log.debug("Ending getLinks");
            log.debug("Starting addLinks");
            retVMap.addLinks(vls);
            log.debug("Ending addLinks");
            log.debug("Ending adding links for map with id "+id);
        } catch (SQLException se) {
            throw new MapsManagementException(
                    "Factory: unable to get map with id=" + id + "." + se);
        }
        return retVMap;
    }

    /**
     * Take the map label and type in input and return it in VMap form.
     * 
     * @param mapname
     * @param maptype
     * @return the VMap[] with corresponding mapname and maptype
     * @throws MapsException
     */

    public VMap[] getMap(String mapname, String maptype) throws MapsManagementException,
            MapNotFoundException, MapsException {
    	
        VMap retVMap = null;
        if (!maptype.equals(VMap.AUTOMATICALLY_GENERATED_MAP) && !maptype.equals(VMap.USER_GENERATED_MAP)) {
            throw new MapNotFoundException("Map with uncorrect maptype " + maptype);
        }
        Vector ve = new Vector();

        try {
            Map[] maps = Factory.getMaps(mapname,maptype);
            if (maps == null) {
                throw new MapNotFoundException("Map with mapname " + mapname
                        + "and maptype " + maptype + " doesn't exist.");
            }
            for (int i = 0; i<maps.length;i++) {
            	Map m = maps[i];
            	retVMap = new VMap(m.getId(), m.getName(), m.getBackground(),
                    m.getOwner(), m.getAccessMode(), m.getUserLastModifies(), m
                            .getScale(), m.getOffsetX(), m.getOffsetY(), m
                            .getType(),m.getWidth(),m.getHeight());
            	retVMap.setCreateTime(m.getCreateTime());
            	retVMap.setLastModifiedTime(m.getLastModifiedTime());
            	Element[] mapElems = Factory.getElementsOfMap(m.getId());
            	VElement elem =null;
            	if(mapElems!=null){
            		for (int j = 0; j < mapElems.length; j++) {
            			elem = new VElement(mapElems[j]);
            			// here we must add all the stuff required
            			retVMap.addElement(elem);
            		}
            	}
            	retVMap.addElements((VElement[]) refreshElements(retVMap.getAllElements(),false).toArray(new VElement[0]));
            	retVMap.addLinks((VLink[]) getLinks(retVMap.getAllElements()).toArray(new VLink[0]));
            	ve.add(retVMap);
            }
        } catch (SQLException se) {
            throw new MapsManagementException(
                    "Factory: unable to get map with mapname and maptype " + mapname + maptype + "." + se);
        }
        VMap[] vmaps = new VMap[ve.size()];
        vmaps=(VMap[])ve.toArray(vmaps);
        return vmaps;
     }

    public MapMenu getMapMenu(int mapId) throws MapsManagementException,
    MapNotFoundException, MapsException {

		MapMenu m = null;
		try {
			m = Factory.getMapMenu(mapId);
		    if (m == null) {
		        throw new MapNotFoundException("No Maps found.");
		    }
		} catch (SQLException se) {
			throw new MapsManagementException("Factory: unable to get map (menu) with id "
		            +mapId +"." + se);
		}
		
		return m;
		
	}
    
    

    public List getLinks(VElement[] elems) throws SQLException, MapsException {

    	List links = new ArrayList();
        
    	// this is the list of nodes set related to Element
    	java.util.List elemNodes = new java.util.ArrayList();
    	try{
	    	if (elems != null) {
	        	for (int i = 0; i < elems.length; i++) {
	                Set nodeids = getNodeidsOnElement(elems[i]);
	                elemNodes.add(nodeids);
	         	}
	        }
	        
	        Iterator ite = elemNodes.iterator();
	        int firstelemcount = 0;
	        Set linkid = new TreeSet();
	        Connection conn = Vault.getDbConnection();
	        while (ite.hasNext()) {
	        	Set firstelemnodes = (TreeSet) ite.next();
	        	Set firstlinkednodes = getLinkedNodeidsOnNodes(firstelemnodes, conn);
	            int secondelemcount = firstelemcount +1;
	            Iterator sub_ite = elemNodes.subList(secondelemcount,elemNodes.size()).iterator(); 
	        	while (sub_ite.hasNext()) {
	        		Iterator node_ite = ((TreeSet) sub_ite.next()).iterator();
	        		while (node_ite.hasNext()) {
	        			Integer curNodeId = (Integer) node_ite.next();
	        			if (firstlinkednodes.contains(curNodeId)) {
	        				VLink vlink = new VLink(elems[firstelemcount],elems[secondelemcount]);
	        				if(!links.contains(vlink)){
	        					log.debug("adding link "+vlink.getFirst().getId()+vlink.getFirst().getType()+"-"+vlink.getSecond().getId()+vlink.getSecond().getType());
	        					links.add(vlink);
	        				}
	        			}
	        		}
	        		secondelemcount++;
				}
				firstelemcount++;
			}
	        Vault.releaseDbConnection(conn);
    	}catch(SQLException s){
    		throw s;
    	}catch(Exception e){
    		throw new MapsException(e);
    	}
        return links;
    }

    public List getLinksOnElem(VElement[] elems,VElement elem, Connection conn) throws SQLException, MapsException {

    	List links = new java.util.ArrayList();
        
    	// this is the list of nodes set related to Element
    	Set linkednodeids = null;
		if (elem != null) {
			//log.debug("Before getNodeidsOnElement");
			Set nodes = getNodeidsOnElement(elem);
			//log.debug("After getNodeidsOnElement");
			
            linkednodeids = getLinkedNodeidsOnNodes(nodes, conn);
            
        } else {
        	return links;
        }
		log.debug("Before creating Links");
        if (elems != null && linkednodeids != null) {
    		
        	for (int i = 0; i < elems.length; i++) {
	    		Iterator node_ite = getNodeidsOnElement(elems[i]).iterator();
	    		while (node_ite.hasNext()) {
	    			Integer elemNodeId = (Integer) node_ite.next();
		        	if (linkednodeids.contains(elemNodeId)) {
		        		VLink vlink = new VLink(elems[i],elem);
		       			links.add(vlink);
		       		}
	    		}
	    	}
        }
        log.debug("After creating Links");
        return links;
    }
    
 	public List getLinksOnElem(VElement[] elems,VElement elem) throws SQLException, MapsException {

    	List links = new java.util.ArrayList();
        
    	// this is the list of nodes set related to Element
    	Set linkednodeids = null;
		if (elem != null) {
            linkednodeids = getLinkedNodeidsOnNodes(getNodeidsOnElement(elem));
        } else {
        	return links;
        }
        
        if (elems != null && linkednodeids != null) {
    		
        	for (int i = 0; i < elems.length; i++) {
	    		Iterator node_ite = getNodeidsOnElement(elems[i]).iterator();
	    		while (node_ite.hasNext()) {
	    			Integer elemNodeId = (Integer) node_ite.next();
		        	if (linkednodeids.contains(elemNodeId)) {
		        		VLink vlink = new VLink(elems[i],elem);
		       			links.add(vlink);
		       		}
	    		}
	    	}
        }
        return links;
    }    

    private Set getLinkedNodeidsOnNodes(Set nodes, Connection conn) throws SQLException {
    	Set linkedNodeIds = new TreeSet();
        if (nodes != null) {
        	Iterator ite = nodes.iterator();
        	while (ite.hasNext()) {
        		Integer curnodeid = (Integer) ite.next();
                linkedNodeIds.addAll(NetworkElementFactory.getLinkedNodeIdOnNode(curnodeid.intValue(),conn));
        	}
        }
        return linkedNodeIds;
    }
    
    private Set getLinkedNodeidsOnNodes(Set nodes) throws SQLException {
   		Set linkedNodeIds = new TreeSet();
        if (nodes != null) {
        	Iterator ite = nodes.iterator();
        	while (ite.hasNext()) {
        		Integer curnodeid = (Integer) ite.next();
                linkedNodeIds.addAll(NetworkElementFactory.getLinkedNodeIdOnNode(curnodeid.intValue()));
        	}
        }
        return linkedNodeIds;
    }
    
    private Set getNodeidsOnElement(Element elem) throws SQLException, MapsException {
   		Set elementNodeIds = new TreeSet();
		if (elem.isNode()) {
			elementNodeIds.add(new Integer(elem.getId()));
		} else if (elem.isMap()) {
			int curMapId = elem.getId();
			Element[] elemNodeElems = Factory.getNodeElementsOfMap(curMapId);
			if (elemNodeElems != null && elemNodeElems.length >0 ) {
				for (int i=0; i<elemNodeElems.length;i++ ) {
					elementNodeIds.add(new Integer(elemNodeElems[i].getId()));
				}
			}
			Element[] elemMapElems = Factory.getMapElementsOfMap(curMapId);
			if (elemMapElems != null && elemMapElems.length >0 ) {
				for (int i=0; i<elemMapElems.length;i++ ) {
					elementNodeIds.addAll(getNodeidsOnElement(elemMapElems[i]));
				}
			}
		}
		return elementNodeIds;
   	
    }

    /**
     * This use DB table Asset to get default icons on Db 
     * other implementation are possble
     * geticon from Sysoid as an example
     */
    
    public String getIconName(int elementId,String type) throws SQLException {
    	return Factory.getIconName(elementId, type);
    }
    
    public String getIconName(int elementId,String type, Connection conn) throws SQLException {
    	return Factory.getIconName(elementId, type, conn);
    }    

    private String getLabel(VElement elem) throws SQLException {
    	if (elem.isMap()) return Factory.getMapName(elem.getId());
    	return NetworkElementFactory.getNodeLabel(elem.getId());
    }
    
    private String getLabel(VElement elem, Connection conn) throws SQLException {
    	if (elem.isMap()) return Factory.getMapName(elem.getId(), conn);
    	return NetworkElementFactory.getNodeLabel(elem.getId());
    }
    /**
     * Take the maps with label like the pattern in input and return them in
     * VMap[] form.
     * 
     * @param label
     * @return the VMaps array if any label matches the pattern in input, null
     *         otherwise
     * @throws MapsException
     */
    public VMap[] getMapsLike(String likeLabel) throws MapsManagementException,
            MapNotFoundException, MapsException {
        VMap[] retVMap = null;
        try {
            Map[] m = Factory.getMapsLike(likeLabel);
            if (m == null) {
                throw new MapNotFoundException("Maps with label like "
                        + likeLabel + " don't exist.");
            }
            retVMap = new VMap[m.length];
            for (int i = 0; i < m.length; i++) {
                retVMap[i] = getMap(m[i].getId());
            }

        } catch (SQLException se) {
            throw new MapsManagementException(
                    "Factory: unable to get maps with label like " + likeLabel
                            + "." + se);
        }
        return retVMap;
    }
    
    /**
     * Take the maps with name in input and return them in
     * VMap[] form.
     * 
     * @param mapName
     * @return the VMaps array if any map has name in input, null
     *         otherwise
     * @throws MapsException
     */
    public VMap[] getMapsByName(String mapName) throws MapsManagementException,
            MapNotFoundException, MapsException {
        VMap[] retVMap = null;
        try {
            Map[] m = Factory.getMapsByName(mapName);
            if (m == null) {
                throw new MapNotFoundException("Maps with name "
                        + mapName + " don't exist.");
            }
            retVMap = new VMap[m.length];
            for (int i = 0; i < m.length; i++) {
                retVMap[i] = getMap(m[i].getId());
            }

        } catch (SQLException se) {
            throw new MapsManagementException(
                    "Factory: unable to get maps with name " + mapName
                            + "." + se);
        }
        return retVMap;
    }

    /**
     * Get all defined maps.
     * 
     * @return the VMaps array containing all maps defined
     * @throws MapsException
     */
    public VMap[] getAllMaps() throws MapsManagementException,
            MapNotFoundException, MapsException  {
        VMap[] retVMap = null;
        try {
            Map[] m = Factory.getAllMaps();
            if (m == null) {
                throw new MapNotFoundException("No Maps found.");
            }
            retVMap = new VMap[m.length];
            for (int i = 0; i < m.length; i++) {
                retVMap[i] = getMap(m[i].getId());
            }

        } catch (SQLException se) {
            throw new MapsManagementException("Factory: unable to get all maps"
                    + "." + se);
        }
        return retVMap;
    }

    /**
     * Get all defined maps.
     * 
     * @return the VMaps array containing all maps defined
     * @throws MapsException
     */
    
    public MapMenu[] getAllMapMenus() throws MapsManagementException,
            MapNotFoundException, MapsException {
    
    	MapMenu[] m = null;
		try {
			m = Factory.getAllMapsMenu();
		    if (m == null) {
			return new MapMenu[0];
		        //throw new MapNotFoundException("No Maps found.");
		    }
		} catch (SQLException se) {
			throw new MapsManagementException("Factory: unable to get all maps"
	                + "." + se);
		}
	
	    return m;

    }
    
    /**
     * Take the maps with name in input and return them in
     * MapMenu[] form.
     * 
     * @param mapName
     * @return the MapMenu array if any map has name in input, null
     *         otherwise
     * @throws MapsException
     */
    public MapMenu[] getMapsMenuByName(String mapName) throws MapsManagementException,
            MapNotFoundException, MapsException {
    	MapMenu[] retVMap = null;
        try {
        	retVMap = Factory.getMapsMenuByName(mapName);
            if (retVMap == null) {
                throw new MapNotFoundException("Maps with name "
                        + mapName + " don't exist.");
            }
            
        } catch (SQLException se) {
            throw new MapsManagementException(
                    "Factory: unable to get maps with name " + mapName
                            + "." + se);
        }
        return retVMap;
    }
    
    /**
     * Take all the maps in the tree of maps considering the with name in input
     * as the root of the tree. If there are more maps with <i>mapName</i> (case insensitive)
     * all trees with these maps as root are considered and returned. 
     * 
     * @param mapName
     * @return a List with the MapMenu objects.
     * @throws MapsException
     */
    public List getMapsMenuTreeByName(String mapName) throws MapsManagementException,
            MapNotFoundException, MapsException {
    	  List mapsInTreesList = new ArrayList();
	      //
	      MapMenu[] mapsMenu = null;
	      try{
	      	mapsMenu=getMapsMenuByName(mapName);
	      }catch(MapNotFoundException mnf){
	      	//do nothing...
	      }
	      if(mapsMenu!=null){
	      	  // find all accessible maps for the user,
	      	  // for all maps (and theirs tree of maps) with name like user's ADProfile. 
	      	  for(int k=0; k<mapsMenu.length;k++){
	      	  	  //build a map in wich each entry is [mapparentid, listofchildsids]
			      java.util.Map parent_child = new HashMap();
			      try{
			      	parent_child = Factory.getMapsStructure();
			      }catch(Exception e){
			      	throw new MapsException(e);
			      }
			      
			      
			      List childList = new ArrayList();
			      preorderVisit(new Integer(mapsMenu[k].getId()), childList, parent_child);
			      for(int i=0; i<childList.size(); i++){
			      	preorderVisit((Integer)childList.get(i), childList, parent_child);
			      }

			      //adds all sub-tree of maps to the visible map list
			      for(int i=0; i<childList.size(); i++){
			      	mapsInTreesList.add(getMapMenu(((Integer)childList.get(i)).intValue()));
			      }
	      	  }
	      }

        return mapsInTreesList;
    }
    
    private void preorderVisit(Integer rootElem, List treeElems, java.util.Map maps){
       	Set childs = (Set)maps.get(rootElem);
       	if(!treeElems.contains(rootElem)){
       		treeElems.add(rootElem);
       	}
       	if(childs!=null){
	    	Iterator it = childs.iterator();
	    	while(it.hasNext()){
	    		Integer child = (Integer)it.next();
	    		if(!treeElems.contains(child)){
	    			treeElems.add(child);
	    		}
	    		preorderVisit(child, treeElems, maps);
	    	}   	    	
       	}
    }


    /**
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @return the new VElement
     * @throws MapsManagementException
     * @throws MapNotFoundException
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type, int x,int y) throws MapsManagementException,
            MapNotFoundException, MapsException, SQLException {

		String label = null;
		if (type.equals(VElement.NODE_TYPE)) label = NetworkElementFactory.getNodeLabel(elementId);
		if (type.equals(VElement.MAP_TYPE)) label = Factory.getMapName(elementId);
		String iconname = getIconName(elementId,type);
		VElement elem = new VElement(mapId,elementId,type,iconname,label,x,y);
        return elem;
    }

    /**
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @return the new VElement
     * @throws MapsManagementException
     * @throws MapNotFoundException
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type) throws MapsManagementException,
            MapNotFoundException, MapsException, SQLException {

		String label = null;
		if (type.equals(VElement.NODE_TYPE)) label = NetworkElementFactory.getNodeLabel(elementId);
		if (type.equals(VElement.MAP_TYPE)) label = Factory.getMapName(elementId);
		String iconname = Factory.getIconName(elementId,type);
        VElement elem = new VElement(mapId,elementId,type,label,iconname);
        return elem;
    }
	
	public VElement newElement(int mapId, int elementId, String type, Connection conn) throws MapsManagementException,
    MapNotFoundException, MapsException, SQLException {

		String label = null;
		if (type.equals(VElement.NODE_TYPE)) label = NetworkElementFactory.getNodeLabel(elementId);
		if (type.equals(VElement.MAP_TYPE)) label = Factory.getMapName(elementId, conn);
		String iconname = Factory.getIconName(elementId,type, conn);
		VElement elem = new VElement(mapId,elementId,type,label,iconname);
		return elem;
	}

    /**
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @return the new VElement
     * @throws MapsManagementException
     * @throws MapNotFoundException
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type, String iconname, int x,int y) throws MapsManagementException,
            MapNotFoundException, MapsException, SQLException {

		String label = null;
		if (type.equals(VElement.NODE_TYPE)) label = NetworkElementFactory.getNodeLabel(elementId);
		if (type.equals(VElement.MAP_TYPE)) label = Factory.getMapName(elementId);
		VElement elem = new VElement(mapId,elementId,type,iconname,label,x,y);
        return elem;
    }

    /**
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @return the new VElement
     * @throws MapsManagementException
     * @throws MapNotFoundException
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type,String iconname) throws MapsManagementException,
            MapNotFoundException, MapsException, SQLException {

		String label = null;
		if (type.equals(VElement.NODE_TYPE)) label = NetworkElementFactory.getNodeLabel(elementId);
		if (type.equals(VElement.MAP_TYPE)) label = Factory.getMapName(elementId);
        VElement elem = new VElement(mapId,elementId,type,label,iconname);
        return elem;
    }

	/**
     * delete the map in input
     * 
     * @param map
     *            to delete
     * @throws MapsException
     *             if an error occour deleting map, MapNotFoundException if the
     *             map to delete doesn't exist.
     */
    synchronized public void deleteMap(VMap map) throws MapsException,
            MapNotFoundException {
        try {
            if (m_dbManager.deleteMap(map.getId()) == 0)
                throw new MapNotFoundException("The Map doesn't exist.");
        } catch (SQLException s) {
            throw new MapsException("Error while deleting map with id="
                    + map.getId() + "\n " + s);
        }
    }

    /**
     * delete the map with identifier id
     * 
     * @param id
     *            of the map to delete
     * @throws MapsException
     */
    synchronized public void deleteMap(int mapId) throws MapsException {
        try {
            m_dbManager.deleteMap(mapId);
        } catch (SQLException s) {
            throw new MapsException("Error while deleting map with id=" + mapId
                    + "\n " + s);
        }
    }

    /**
     * delete the maps in input
     * 
     * @param maps
     *            to delete
     * @throws MapsException
     */
    synchronized public void deleteMaps(VMap[] maps) throws MapsException {
        for (int i = 0; i < maps.length; i++) {
            deleteMap(maps[i]);
        }
    }

    /**
     * delete the maps with the identifiers in input
     * 
     * @param identifiers
     *            of the maps to delete
     * @throws MapsException
     */
    synchronized public void deleteMaps(int[] maps) throws MapsException {
        for (int i = 0; i < maps.length; i++) {
            deleteMap(maps[i]);
        }
    }

    /**
     * save the map in input
     * 
     * @param map
     *            to save
     * @throws MapsException
     */
    synchronized public void save(VMap map) throws MapsException {
        try {
            if (!map.isNew()) {
                m_dbManager.deleteElementsOfMap(map.getId());
            }
            m_dbManager.saveMap(map);
            m_dbManager.saveElements(map.getAllElements());
        } catch (SQLException se) {
            throw new MapsException("Error while saving map with id="
                    + map.getId() + "\n" + se);
        }
    }

    /**
     * save the maps in input
     * 
     * @param maps
     *            to save
     * @throws MapsException
     */
    synchronized public void save(VMap[] maps) throws MapsException {
        for (int i = 0; i < maps.length; i++) {
            save(maps[i]);
        }
    }

    /**
     * 
     * @param mapElements the elements to refresh
     * @param returnChangedElem return only element changed
     * @return List of VElement
     * @throws MapsException
     * @throws SQLException
     */
    public List refreshElements(VElement[] mapElements, boolean returnChangedElem) throws MapsException, SQLException {
    	
    	String LOG4J_CATEGORY = "OpenNMS.Map";
    	Category log;

		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		List elems = new ArrayList();

        if (mapElements == null || mapElements.length == 0){
			log.warn("refreshElements: method called with null or empty input");
			return elems;
        }

        CategoryModel cModel;
        log.debug("Instantiating CModel");
        try {
            cModel = CategoryModel.getInstance();
        } catch (Exception e) {
        	throw new MapsException(e);
        }
        log.debug("CModel instantiated.");
        log.debug("Getting outaged elems.");
        Iterator ite = Factory.getOutagedVElems().iterator();
        log.debug("Outaged elems obtained.");
        java.util.Map outagedNodes = new HashMap();
		float severity = 0;
		while (ite.hasNext()) {
			ElementInfo outagelem = (ElementInfo) ite.next();
			int outageStatus = getStatus(outagelem.getUei());
			int outageSeverity = getSeverity(EventUtil.getSeverityLabel(outagelem.getSeverity()));

			if (log.isInfoEnabled())
				log.info("refreshElements: parsing outaged node with nodeid: " + outagelem.getId());
			OutageInfo oi = (OutageInfo)outagedNodes.get(new Integer(outagelem.getId())); 

			if (oi != null) {

				if (oi.getStatus() > outageStatus) oi.setStatus(outageStatus);
				oi.setSeverity((oi.getSeverity()+outageSeverity)/2);
			} else {
				int curStatus = outageStatus;
				float curSeverity = outageSeverity;
				oi = new OutageInfo(outagelem.getId(),curStatus,curSeverity);
			}
			outagedNodes.put(new Integer(outagelem.getId()),oi);
    		if (log.isDebugEnabled()) 
    			log.debug("refreshElements: node status/severity " + outageStatus + "/" + outageSeverity);
		}

		int status = defaultStatusId;
		VElement ve = null;
		Set nodeIds = new TreeSet();
		for(int i=0;i<mapElements.length;i++){
			if(mapElements[i].isNode()){
				nodeIds.add(new Integer(mapElements[i].getId()));
			}else{
				nodeIds.addAll(getNodeidsOnElement(mapElements[i]));
			}
		}
		
		java.util.Map availsMap = null;
		if (availEnabled) {
			log.debug("Getting avails for nodes of map ("+nodeIds.size()+" nodes)");
			availsMap = cModel.getNodeAvailability(nodeIds);
			log.debug("Avails obtained");
		}
		Connection conn = Vault.getDbConnection();
    	for(int i=0;i<mapElements.length;i++){
    		ve = (VElement) mapElements[i].clone();
    		if (log.isDebugEnabled()) 
    			log.debug("refreshElements: parsing VElement ID " + ve.getId()+ve.getType());

    		double elementAvail =100.0;
			int elementStatus = defaultStatusId;
   			float elementSeverity = defaultSeverityId;
 
    		// get status and severity
			// status is worse
			// severity is medium value among severities
   			if (ve.isNode()) {
   				if (availEnabled) {
   					elementAvail =((Double) availsMap.get(new Integer(ve.getId()))).doubleValue();
   				} else {
   					elementAvail = defaultEnableFalseAvail.getMin();
   				}
   				
    			OutageInfo oi = (OutageInfo) outagedNodes.get(new Integer(ve.getId()));
				if (oi != null) {
   					elementStatus = oi.getStatus();
   					elementSeverity= oi.getSeverity();
   	    			// get avalaibility
   				} 
     		} else {
   				Set nodesonve = getNodeidsOnElement(ve);
   	    		if (nodesonve != null && nodesonve.size()> 0) {
   	    			if (availEnabled) {
   	    				log.debug("Getting avails for Map-Node");
   	   	    			elementAvail = getNodeAvailability(nodesonve,availsMap);
   	   	    		log.debug("Avails for Map-Node obtained" );
   	    			} else {
   	   					elementAvail = defaultEnableFalseAvail.getMin();
   	   				}
   	    			
   	    			ite = nodesonve.iterator();
   	    			int sev = 0; 
   	    			while (ite.hasNext()) {
   	   					OutageInfo oi = (OutageInfo) outagedNodes.get((Integer)(ite.next()));
   	    				if (oi != null) {
   	    					if (oi.getStatus() < elementStatus) elementStatus= oi.getStatus();
	   	   					sev += oi.getSeverity();
   	    				} else {
   	    	    			sev += defaultSeverityId; 
   	    				}
   	    			}
   	    			elementSeverity = sev/nodesonve.size();
   	    		}
       		}

   			if (log.isDebugEnabled()) 
    			log.debug("refreshElements: element avail/status/severity " + elementAvail + "/" + elementStatus + "/" + elementSeverity);

    		ve.setRtc(elementAvail);
			ve.setStatus(elementStatus);
			ve.setSeverity(new BigDecimal(elementSeverity+1/2).intValue());
			//got the label
			ve.setLabel(getLabel(ve,conn));

			if (!returnChangedElem || (returnChangedElem && !ve.equalsIgnorePosition(mapElements[i]))){
				log.debug("Adding element "+ve.getId());
				elems.add(ve);
			}
    	}
    	Vault.releaseDbConnection(conn);
        return elems;
    }

    private int getSeverity(String severityLabel) {
    	Severity sev = ((Severity)severities.get(severityLabel));
    	if(sev==null){
    		throw new IllegalStateException("Severity with label "+severityLabel+" not found.");
    	}
    	return sev.getId();
    }

    private int getStatus(String uei) {
    	
    	Status status = (Status)statuses.get(uei);
    	if(status==null){
    		return unknownStatusId;
    	}
    	return status.getId();
    }

    private double getNodeAvailability(Set nodes,java.util.Map availsMap) throws SQLException{
    	Iterator ite = nodes.iterator();
    	double avail = 0.0;
    	while (ite.hasNext()) {
			avail+=((Double)availsMap.get((Integer)ite.next())).doubleValue();
		}
		avail = avail/nodes.size();
		return avail;
    }

    /**
     * TODO 
     * write this method simil way to refreshElement
     * Not Yet Implemented
     */
    public List refreshLinks(VLink[] mapLinks) {
    	List links = new ArrayList();
    	return links;
    }

    public boolean foundLoopOnMaps(VMap parentMap,int mapId) throws SQLException {
		
		String LOG4J_CATEGORY = "OpenNMS.Map";
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log= ThreadCategory.getInstance(this.getClass());

		java.util.Map maps = Factory.getMapsStructure();
		VElement[] elems = parentMap.getAllElements();
		if (elems == null) return false;
		Set childSet = new TreeSet();
		for (int i=0; i<elems.length;i++) {
			if (elems[i].getType().equals(VElement.MAP_TYPE))
				childSet.add(new Integer(elems[i].getId()));
		}
	    
		log.debug("List of sub-maps before preorder visit "+childSet.toString());

	    maps.put(new Integer(parentMap.getId()),childSet);

	    while (childSet.size() > 0) {
		    childSet = preorderVisit(childSet,maps);
	
		    log.debug("List of sub-maps  "+childSet.toString());
	
		    if(childSet.contains(new Integer(mapId))){
				return true;
			}
	    }
	return false;
	
	}

    private Set preorderVisit(Set treeElems,java.util.Map maps){
    	Set childset = new TreeSet();
    	Iterator it = treeElems.iterator();
    	while(it.hasNext()){
    		Set curset = (Set) maps.get((Integer) it.next());
    		if (curset != null) childset.addAll(curset);
    	}
    	return childset;
    }
    
    public boolean equalsLink(VLink linkA,VLink linkB) {
    	String idA = getLinkId(linkA.getFirst().getId(),linkA.getFirst().getType(),linkA.getSecond().getId(),linkA.getSecond().getType());
    	String idB = getLinkId(linkB.getFirst().getId(),linkB.getFirst().getType(),linkB.getSecond().getId(),linkB.getSecond().getType());
    	if (idA.equals(idB)) return true;
    	return false;
    }

    private String getLinkId(int id1, String type1, int id2, String type2) {
    	String linkId = (new Integer(id1).toString())+type1+"-"+(new Integer(id2).toString())+type2;
    	if (id1>id2 || (id1 == id2 && type2.equals(Element.MAP_TYPE)) ){
    		linkId = (new Integer(id2).toString())+type2+"-"+(new Integer(id1).toString())+type1;
    	}
    	return linkId;
    }
}
