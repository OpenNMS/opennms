//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/*
 * Created on 11-gen-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.MapsManagementException;
import org.opennms.web.map.config.DataSource;
import org.opennms.web.map.config.MapPropertiesFactory;
import org.opennms.web.map.config.MapsFactory;

import org.opennms.web.map.db.*;



/**
 * @author maurizio
 *  
 */
public class Manager{

    org.opennms.web.map.db.Manager m_implManager = null;
    DataSource m_dataSource = null;
    MapPropertiesFactory mpf = null;
    MapsFactory m_mapsFactory = null;
 
    private Category log=null;
    
    /**
     * Manage Maps using default implementation of Factory and Manager
     */
    public Manager() throws MapsException{
    	
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log= ThreadCategory.getInstance(this.getClass());
        try {
        	MapPropertiesFactory.init();
        	mpf = MapPropertiesFactory.getInstance();
        	m_mapsFactory = mpf.getDefaultFactory();
        	
           	String dataSource = m_mapsFactory.getDataSource();
           	m_dataSource = null;
        	if(dataSource!=null){
        		m_dataSource = mpf.getDataSource(dataSource); 
        	}
 
        	Class[] parameterTypes = {DataSource.class,java.util.Map.class};
        	Object[] params = {m_dataSource,m_mapsFactory.getParam()};
        	Constructor managerConstr = Class.forName(m_mapsFactory.getManagerClass()).getConstructor(parameterTypes);
        	
        	m_implManager = (org.opennms.web.map.db.Manager)managerConstr.newInstance(params);
        } catch (Exception e) {
        	log.fatal("cannot use map.properties file " + e);
        	e.printStackTrace();
        	throw new MapsException(e);
        }
    }
    
    /**
     * Create a new Manager using the Maps Factory with label in input. If mapsFactoryLabel is null, default factory is used
     * @param mapsFactoryLabel
     */
    public Manager(String mapsFactoryLabel)throws MapsException{
    	this();
    	try {
    		if(mapsFactoryLabel!=null){
	        	m_mapsFactory = mpf.getMapsFactory(mapsFactoryLabel); 
	        	
	        	String dataSource = m_mapsFactory.getDataSource();
	        	m_dataSource = null;
	        	if(dataSource!=null){
	        		m_dataSource = mpf.getDataSource(dataSource); 
	        	}

	        	Class[] parameterTypes = {DataSource.class,java.util.Map.class};
	        	Object[] params = {m_dataSource,m_mapsFactory.getParam()};
	        	Constructor managerConstr = Class.forName(m_mapsFactory.getManagerClass()).getConstructor(parameterTypes);
	        	
	        	m_implManager = (org.opennms.web.map.db.Manager)managerConstr.newInstance(params);
        	}
    	} catch (Exception e) {
        	log.fatal("cannot use map.properties file " + e);
        	e.printStackTrace();
        	throw new MapsException(e);
        }
    }
    
    /**
     * Create a new Manager using the Maps Factory in input. If mFactory is null, default factory is used
     * 
     * @param mFactory
     */
    public Manager(MapsFactory mFactory)throws MapsException{
    	this();
    	try {
	    	if(mFactory!=null){	
	    		m_mapsFactory = mFactory;

	    		String dataSource = m_mapsFactory.getDataSource();
	    		m_dataSource = null;
	        	if(dataSource!=null){
	        		m_dataSource = mpf.getDataSource(dataSource); 
	        	}

	        	Class[] parameterTypes = {DataSource.class,java.util.Map.class};
	        	Object[] params = {m_dataSource,m_mapsFactory.getParam()};
	        	Constructor managerConstr = Class.forName(m_mapsFactory.getManagerClass()).getConstructor(parameterTypes);
	        	
	        	m_implManager = (org.opennms.web.map.db.Manager)managerConstr.newInstance(params);
	        	
	    	}
    	} catch (Exception e) {
        	log.fatal("cannot use map.properties file " + e);
        	e.printStackTrace();
        	throw new MapsException(e);
        }

    }
    
    /**
     * gets the using Maps Factory
     * @return
     */
    public MapsFactory getMapsFactory(){
    	return m_mapsFactory;
    }
    

    /**
     * Start the session: only operations made in the block between start and
     * end session will be saved correctly.
     * 
     * @throws MapsException
     * @see endSession
     */
    public void startSession() throws MapsException {
        	m_implManager.init();
            m_implManager.startSession();
    }

    /**
     * Close the block open by startSession() method.
     * 
     * @throws MapsException
     * @see startSession()
     */
    synchronized public void endSession() throws MapsException {
    		m_implManager.endSession();
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
     * @param refreshElems says if refresh the map's elements
     * @return the VMap with identifier id
     * @throws MapsException
     */
    public VMap getMap(int id, boolean refreshElems) throws MapsManagementException,
            MapNotFoundException, MapsException {
    	
        VMap retVMap = null;

        Map m = m_implManager.getMap(id);
        if (m == null) {
            throw new MapNotFoundException("Map with id " + id
                    + " doesn't exist.");
        }
        retVMap = new VMap(id, m.getName(), m.getBackground(),
                m.getOwner(), m.getAccessMode(), m.getUserLastModifies(), m
                        .getScale(), m.getOffsetX(), m.getOffsetY(), m
                        .getType(),m.getWidth(),m.getHeight());
        retVMap.setCreateTime(m.getCreateTime());
        retVMap.setLastModifiedTime(m.getLastModifiedTime());
        Element[] mapElems = m_implManager.getElementsOfMap(id);
        VElement elem =null;
        if(mapElems!=null){
            for (int i = 0; i < mapElems.length; i++) {
        		elem = new VElement(mapElems[i]);
        		// here we must add all the stuff required
        		retVMap.addElement(elem);
            }
        }

        if(refreshElems){
            log.debug("Starting refreshing elems for map with id "+id);
       		VElement[] changedElems = m_implManager.refreshElements(retVMap.getAllElements());
    		if(changedElems!=null){
	       		for(int i=0;i<changedElems.length;i++){
	    			retVMap.removeElement(changedElems[i].getId(), changedElems[i].getType());
	    			retVMap.addElement(changedElems[i]);
	    		}
    		}
        }
		
        
        log.debug("Starting adding links for map with id "+id);
        log.debug("Starting getLinks");
        //VLink[] vls = null;
        VLink[] vls = (VLink[]) getLinks(retVMap.getAllElements()).toArray(new VLink[0]);
        log.debug("Ending getLinks");
        log.debug("Starting addLinks");
        retVMap.addLinks(vls);
        log.debug("Ending addLinks");
        log.debug("Ending adding links for map with id "+id);
        return retVMap;
    }
    
    public void deleteElementsOfMap(int mapId)throws MapsException{
    	m_implManager.deleteElementsOfMap(mapId);
    }

    /**
     * Take the map label and type in input and return it in VMap form.
     * 
     * @param mapname
     * @param maptype
     * @param refreshElems says if refresh map's elements
     * @return the VMap[] with corresponding mapname and maptype
     * @throws MapsException
     */

    public VMap[] getMap(String mapname, String maptype, boolean refreshElems) throws MapsManagementException,
            MapNotFoundException, MapsException {
    	
        VMap retVMap = null;
        if (!maptype.equals(VMap.AUTOMATICALLY_GENERATED_MAP) && !maptype.equals(VMap.USER_GENERATED_MAP)) {
            throw new MapNotFoundException("Map with uncorrect maptype " + maptype);
        }
        Vector ve = new Vector();

        Map[] maps = m_implManager.getMaps(mapname,maptype);
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
        	Element[] mapElems = m_implManager.getElementsOfMap(m.getId());
        	VElement elem =null;
        	if(mapElems!=null){
        		for (int j = 0; j < mapElems.length; j++) {
        			elem = new VElement(mapElems[j]);
        			// here we must add all the stuff required
        			retVMap.addElement(elem);
        		}
        	}
            if(refreshElems){
                log.debug("Starting refreshing elems for map with name "+mapname+ " and id "+retVMap.getId());
        		VElement[] changedElems = m_implManager.refreshElements(retVMap.getAllElements());
        		for(int j=0;j<changedElems.length;j++){
        			retVMap.removeElement(changedElems[j].getId(), changedElems[j].getType());
        			retVMap.addElement(changedElems[j]);
        		}
                log.debug("Ending refreshing elems for map with name "+mapname+ " and id "+retVMap.getId());
            }
        	retVMap.addLinks((VLink[]) getLinks(retVMap.getAllElements()).toArray(new VLink[0]));
        	ve.add(retVMap);
        }
        VMap[] vmaps = new VMap[ve.size()];
        vmaps=(VMap[])ve.toArray(vmaps);
        return vmaps;
     }

    public MapMenu getMapMenu(int mapId) throws MapNotFoundException, MapsException {
		MapMenu m = null;
			m = m_implManager.getMapMenu(mapId);
		    if (m == null) {
		        throw new MapNotFoundException("No Maps found.");
		    }
		return m;
	}
    
    public List getLinks(VElement[] elems) throws MapsException {

    	List links = new ArrayList();
        
    	
    	VLink[] vlinks = m_implManager.getLinks(elems);
    	if (vlinks != null) for (int i=0;i<vlinks.length;i++) {
    		links.add(vlinks[i]);
    		
    	}
        return links;
    }    

 	public List getLinksOnElem(VElement[] elems,VElement elem) throws MapsException {

    	List links = new ArrayList();
        
    	
    	VLink[] vlinks = m_implManager.getLinksOnElement(elems, elem);
    	if (vlinks != null) for (int i=0;i<vlinks.length;i++) {
    		links.add(vlinks[i]);
    		
    	}
        return links;
    }    

    

    /**
     * Take the maps with label like the pattern in input and return them in
     * VMap[] form.
     * 
     * @param label
     * @param refreshElems says if refresh map's elements
     * @return the VMaps array if any label matches the pattern in input, null
     *         otherwise
     * @throws MapsException
     */
    public VMap[] getMapsLike(String likeLabel, boolean refreshElems) throws  MapsException {
        VMap[] retVMap = null;
        Map[] m = m_implManager.getMapsLike(likeLabel);
        if (m == null) {
            throw new MapNotFoundException("Maps with label like "
                    + likeLabel + " don't exist.");
        }
        retVMap = new VMap[m.length];
        for (int i = 0; i < m.length; i++) {
            retVMap[i] = getMap(m[i].getId(),refreshElems);
        }
        return retVMap;
    }
    
    /**
     * Take the maps with name in input and return them in
     * VMap[] form.
     * 
     * @param mapName
     * @param refhresElems says if refresh maps' elements
     * @return the VMaps array if any map has name in input, null
     *         otherwise
     * @throws MapsException
     */
    public VMap[] getMapsByName(String mapName, boolean refreshElems) throws MapNotFoundException, MapsException {
        VMap[] retVMap = null;
        Map[] m = m_implManager.getMapsByName(mapName);
        if (m == null) {
            throw new MapNotFoundException("Maps with name "
                    + mapName + " don't exist.");
        }
        retVMap = new VMap[m.length];
        for (int i = 0; i < m.length; i++) {
            retVMap[i] = getMap(m[i].getId(), refreshElems);
        }
        return retVMap;
    }

    /**
     * Get all defined maps.
     * @param refreshElems says if refresh maps' elements
     * @return the VMaps array containing all maps defined
     * @throws MapsException
     */
    public VMap[] getAllMaps(boolean refreshElems) throws MapNotFoundException, MapsException  {
        VMap[] retVMap = null;
        Map[] m = m_implManager.getAllMaps();
        if (m == null) {
            throw new MapNotFoundException("No Maps found.");
        }
        retVMap = new VMap[m.length];
        for (int i = 0; i < m.length; i++) {
            retVMap[i] = getMap(m[i].getId(), refreshElems);
        }
        return retVMap;
    }

    /**
     * Get all defined maps.
     * 
     * @return the MapMenu array containing all maps defined
     * @throws MapsException
     */
    
    public MapMenu[] getAllMapMenus() throws MapNotFoundException, MapsException {
    	MapMenu[] m = null;
		m = m_implManager.getAllMapMenus();
	    if (m == null) {
	        throw new MapNotFoundException("No Maps found.");
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
    public MapMenu[] getMapsMenuByName(String mapName) throws 
            MapNotFoundException, MapsException {
    	MapMenu[] retVMap = null;
    	retVMap = m_implManager.getMapsMenuByName(mapName);
        if (retVMap == null) {
            throw new MapNotFoundException("Maps with name "
                    + mapName + " don't exist.");
        }
        return retVMap;
    }
    
    /**
     * gets all visible maps for user and userRole in input
     * @param user
     * @param userRole
     * @return a List of MapMenu objects.
     * @throws MapsException
     */
    public List<MapMenu> getVisibleMapsMenu(String user, String userRole)throws MapsException{
    	MapMenu[] maps = m_implManager.getVisibleMapsMenu(user, userRole);
    	if(maps==null)
    		return new ArrayList<MapMenu>();
    	return Arrays.asList(maps);
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
    public List getMapsMenuTreeByName(String mapName) throws 
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
	      	  // for all maps (and theirs tree of maps) with name like mapName. 
	      	  for(int k=0; k<mapsMenu.length;k++){
	      	  	  //build a map in wich each entry is [mapparentid, listofchildsids]
			      java.util.Map parent_child = new HashMap();
			      parent_child = m_implManager.getMapsStructure();
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
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type, int x,int y) throws 
             MapsException{

		Element elem = m_implManager.newElement(elementId, mapId, type);
        return new VElement(mapId,elementId,type,elem.getIcon(),elem.getLabel(),x,y);
    }

    /**
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @return the new VElement
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type) throws MapsException {

		Element elem = m_implManager.newElement(elementId, mapId, type);
        return new VElement(mapId,elementId,type,elem.getLabel(),elem.getIcon());

    }
	


    /**
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @return the new VElement
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type, String iconname, int x,int y) throws 
            MapsException {
		Element elem = m_implManager.newElement(elementId, mapId, type);
        return new VElement(mapId,elementId,type,iconname,elem.getLabel(),x,y);

    }

    /**
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @return the new VElement
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type,String iconname) throws 
            MapsException{
        Element elem = m_implManager.newElement(elementId, mapId, type);
        return new VElement(mapId,elementId,type,elem.getLabel(),iconname);
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
        if (m_implManager.deleteMap(map.getId()) == 0)
            throw new MapNotFoundException("The Map doesn't exist.");
    }

    /**
     * delete the map with identifier id
     * 
     * @param id
     *            of the map to delete
     * @throws MapsException
     */
    synchronized public void deleteMap(int mapId) throws MapsException {
         m_implManager.deleteMap(mapId);
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
        if (!map.isNew()) {
            m_implManager.deleteElementsOfMap(map.getId());
        }
        m_implManager.saveMap(map);
        m_implManager.saveElements(map.getAllElements());
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
     * delete all defined node elements in existent maps
     * @throws MapsException
     */
    synchronized public void deleteAllNodeElements()throws MapsException{
    	m_implManager.deleteNodeTypeElementsFromAllMaps();
    }
    
    /**
     * delete all defined sub maps in existent maps
     * @throws MapsException
     */
    synchronized public void deleteAllMapElements()throws MapsException{
    	m_implManager.deleteMapTypeElementsFromAllMaps();
    }

    /**
     * Refreshes of avail,severity and status of the elements in input.
     * The refresh is performed as follows:
     * 	- default factory is used if no others defined;
     *	- if in the using factory is defined a source, the system will use this one 
     *	  else, the system will use default source (OpenNMS)
     *	 
     * @param mapElements the elements to refresh
     * @param incremental return only changed elements, if the refresh implementation is capable of this operation
     * @return List of VElement
     * @throws MapsException
     */
    public List refreshElements(VElement[] mapElements) throws MapsException{
    	
    	String LOG4J_CATEGORY = "OpenNMS.Map";
    	Category log;

		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		List elems = new ArrayList();

        if (mapElements == null || mapElements.length == 0){
			log.warn("refreshElements: method called with null or empty input");
			return elems;
        }

        return Arrays.asList(m_implManager.refreshElements(mapElements));
    }
    

    
    /**
     * Refreshs avail,severity and status of the map in input and its elements
     * @param map 
     * @return the map refreshed
     */
    public VMap reloadMap(VMap map)throws MapsException{
    	return m_implManager.reloadMap(map);
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

    public boolean foundLoopOnMaps(VMap parentMap,int mapId) throws MapsException {
		
		String LOG4J_CATEGORY = "OpenNMS.Map";
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log= ThreadCategory.getInstance(this.getClass());

		java.util.Map maps = m_implManager.getMapsStructure();
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
    
	public ElementInfo[] getAllElementInfo() throws MapsException {
		return m_implManager.getAllElementInfo();
	}
	
	public org.opennms.web.map.db.Manager getDataAccessManager(){
		return m_implManager;
	}
	

}
