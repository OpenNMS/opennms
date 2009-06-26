//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Apr: refactoring to support ACL DAO work
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
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

package org.opennms.web.map.db;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CatFactory;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.MapsManagementException;
import org.opennms.web.map.config.MapPropertiesFactory;
import org.opennms.web.map.db.datasources.DataSourceInterface;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;
import org.opennms.web.map.view.VMapInfo;

/**
 * @author maurizio
 *  
 */ 
public class ManagerDefaultImpl implements Manager {
    
	private class AlarmInfo {
		int nodeid;
		int status;
		float severity;
		public int getNodeid() {
			return nodeid;
		}
		public void setNodeid(int nodeid) {
			this.nodeid = nodeid;
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
		AlarmInfo(int nodeid, int status, float severity) {
			super();
			this.nodeid = nodeid;
			this.status = status;
			this.severity = severity;
		}
		
	}

	org.opennms.web.map.db.Manager dbManager = null;
    
    MapPropertiesFactory mapsPropertiesFactory = null;
    
    DataSourceInterface dataSource = null;
    
    private GroupDao m_groupDao;
    
    String filter = null;
    
    VMap sessionMap = null;

    boolean adminMode=false;


    private Category log=null;
    
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public DataSourceInterface getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSourceInterface dataSource) {
		this.dataSource = dataSource;
	}

	public org.opennms.web.map.db.Manager getDbManager() {
		return dbManager;
	}

	public void setDbManager(org.opennms.web.map.db.Manager dbManager) {
		this.dbManager = dbManager;
	}

	public org.opennms.web.map.config.MapPropertiesFactory getMapsPropertiesFactory() {
		return mapsPropertiesFactory;
	}

	public void setMapsPropertiesFactory(
			org.opennms.web.map.config.MapPropertiesFactory mapsPropertiesFactory) {
		this.mapsPropertiesFactory = mapsPropertiesFactory;
	}

    public List<String> getCategories() throws MapsException {
    	List<String> categories=new ArrayList<String>();
    	try {
			CategoryFactory.init();
		} catch (Exception e) {
			throw new MapsException("Error while getting categories.",e);
		} 
    	CatFactory cf = CategoryFactory.getInstance();
    	Catinfo cinfo=cf.getConfig();
    	Enumeration<Categorygroup> catGroupEnum = cinfo.enumerateCategorygroup();
    	log.debug("Get categories:");
		while(catGroupEnum.hasMoreElements())
		{
			Categorygroup cg= catGroupEnum.nextElement();
			Enumeration<org.opennms.netmgt.config.categories.Category> catEnum = 	cg.getCategories().enumerateCategory();
			while(catEnum.hasMoreElements())
			{
				org.opennms.netmgt.config.categories.Category category = catEnum.nextElement();
				String categoryName = unescapeHtmlChars(category.getLabel());
				log.debug(categoryName);
				categories.add(categoryName);
			}
		}
    	return categories;
    }

    /**
     * Manage Maps using default implementation of Factory and Manager
     */
    public ManagerDefaultImpl() throws MapsException{
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log= ThreadCategory.getInstance(this.getClass());
		if(log.isDebugEnabled()) {
            log.debug("Instantiating ManagerDefaultImpl");
        }
    }
    

	public List<VLink> getLinks(Collection<VElement> elems) throws MapsException {
    	VElement[] arrayelems = elems.toArray(new VElement[0]);
    	return getLinks(arrayelems);
    }

    public VMap openMap() throws MapNotFoundException {
    	if (sessionMap != null) {
            return sessionMap;
        }
    	throw new MapNotFoundException();
    }
    
    public void clearMap() throws MapNotFoundException, MapsException {
    	if (sessionMap == null) {
            throw new MapNotFoundException();
        }
		sessionMap.removeAllLinks();
		sessionMap.removeAllElements();
    }
    
    public void deleteMap() throws MapNotFoundException,MapsException {
    	deleteMap(sessionMap.getId());
    }
    
    public void closeMap() {
    	sessionMap=null;
    }
    
    public VMap openMap(int id, String user, boolean refreshElems) throws MapsManagementException, MapNotFoundException, MapsException {

    	List<VMapInfo> visibleMaps = getVisibleMapsMenu(user);
		
		Iterator<VMapInfo> it = visibleMaps.iterator();
		while(it.hasNext()){
			VMapInfo mapMenu = it.next();
			if(mapMenu.getId()==id){
				sessionMap = openMap(id,refreshElems);
				return sessionMap;
			}
		}

    	throw new MapNotFoundException();
    }

    /*
     * Start the session: only operations made in the block between start and
     * end session will be saved correctly.
     * 
     * @throws MapsException
     * @see endSession
     
    public void startSession() throws MapsException {
            dbManager.startSession();
    }*/

    /*
     * Close the block open by startSession() method.
     * 
     * @throws MapsException
     * @see startSession()
     
    synchronized public void endSession() throws MapsException {
    		dbManager.endSession();
    }*/

    /**
     * Create a new empty VMap and return it.
     * 
     * @return the new VMap created.
     */
    public VMap newMap() {
        VMap m = new VMap();
        sessionMap=m;
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
		m.setId((MapsConstants.NEW_MAP));
		m.setBackground(MapsConstants.DEFAULT_BACKGROUND_COLOR);
		m.setAccessMode(MapsConstants.ROLE_ADMIN);        
        sessionMap=m;
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
    public VMap openMap(int id, boolean refreshElems) throws MapsManagementException,
            MapNotFoundException, MapsException {
    	
        VMap retVMap = null;

        Map m = dbManager.getMap(id);
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
        Element[] mapElems = dbManager.getElementsOfMap(id);
        VElement elem =null;
        if(mapElems!=null){
            for (Element mapElem : mapElems) {
        		elem = new VElement(mapElem);
        		elem.setSeverity(mapsPropertiesFactory.getIndeterminateSeverity().getId());
        		elem.setStatus(mapsPropertiesFactory.getUnknownStatus().getId());
        		elem.setRtc(mapsPropertiesFactory.getDisabledAvail().getMin());
        		// here we must add all the stuff required
        		retVMap.addElement(elem);
            }
        }

        if(refreshElems){
            log.debug("Starting refreshing elems for map with id "+id);
       		VElement[] changedElems = localRefreshElements(retVMap.getAllElements());
    		if(changedElems!=null){
	       		for (VElement changedElem : changedElems) {
	    			retVMap.removeElement(changedElem.getId(), changedElem.getType());
	    			retVMap.addElement(changedElem);
	    		}
    		}
        }
		
        
        log.debug("Starting adding links for map with id "+id);
        log.debug("Starting getLinks");
        //VLink[] vls = null;
        VLink[] vls = getLinks(retVMap.getAllElements()).toArray(new VLink[0]);
        log.debug("Ending getLinks");
        log.debug("Starting addLinks");
        retVMap.addLinks(vls);
        log.debug("Ending addLinks");
        log.debug("Ending adding links for map with id "+id);
        sessionMap = retVMap;
        return retVMap;
    }
    
    public void deleteElementsOfMap(int mapId)throws MapsException{
    	if(sessionMap==null) {
            throw new MapNotFoundException("session map is null");
        }
    	if(sessionMap.getId()!=mapId) {
            throw new MapsException("No current session map: cannot delete elements of map "+mapId);
        }
    	sessionMap.removeAllElements();
    	dbManager.deleteElementsOfMap(mapId);
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
        Vector<VMap> ve = new Vector<VMap>();

        Map[] maps = dbManager.getMaps(mapname,maptype);
        if (maps == null) {
            throw new MapNotFoundException("Map with mapname " + mapname
                    + "and maptype " + maptype + " doesn't exist.");
        }
        for (Map m : maps) {
        	retVMap = new VMap(m.getId(), m.getName(), m.getBackground(),
                m.getOwner(), m.getAccessMode(), m.getUserLastModifies(), m
                        .getScale(), m.getOffsetX(), m.getOffsetY(), m
                        .getType(),m.getWidth(),m.getHeight());
        	retVMap.setCreateTime(m.getCreateTime());
        	retVMap.setLastModifiedTime(m.getLastModifiedTime());
        	Element[] mapElems = dbManager.getElementsOfMap(m.getId());
        	VElement elem =null;
        	if(mapElems!=null){
        		for (Element mapElem : mapElems) {
        			elem = new VElement(mapElem);
            		elem.setSeverity(mapsPropertiesFactory.getIndeterminateSeverity().getId());
            		elem.setStatus(mapsPropertiesFactory.getUnknownStatus().getId());
            		elem.setRtc(mapsPropertiesFactory.getDisabledAvail().getId());
        			// here we must add all the stuff required
        			retVMap.addElement(elem);
        		}
        	}
            if(refreshElems){
                log.debug("Starting refreshing elems for map with name "+mapname+ " and id "+retVMap.getId());
        		VElement[] changedElems = localRefreshElements(retVMap.getAllElements());
        		for (VElement changedElem : changedElems) {
        			retVMap.removeElement(changedElem.getId(), changedElem.getType());
        			retVMap.addElement(changedElem);
        		}
                log.debug("Ending refreshing elems for map with name "+mapname+ " and id "+retVMap.getId());
            }
        	retVMap.addLinks(getLinks(retVMap.getAllElements()).toArray(new VLink[0]));
        	ve.add(retVMap);
        }
        VMap[] vmaps = new VMap[ve.size()];
        vmaps=ve.toArray(vmaps);
        return vmaps;
     }

    private VElement localRefreshElement(VElement mapElement) throws MapsException {
    	Vector<Integer> deletedNodeids= dbManager.getDeletedNodes();
    	
    	java.util.Map<Integer,AlarmInfo> outagedNodes=getAlarmedNodes();
    	VElement[] velems = {mapElement};
    	java.util.Map<Integer,Double> avails=dbManager.getAvails(velems);
    	Set<Integer> nodesBySource = new HashSet<Integer>();
    	if(dataSource!=null) {
            nodesBySource = dbManager.getNodeIdsBySource(filter);
        }

		VElement ve = refresh(mapElement,nodesBySource,deletedNodeids,outagedNodes,avails);
    	if (ve.equalsIgnorePosition(mapElement)) {
            return null;
        }
		if(sessionMap!=null && ve.getMapId()==sessionMap.getId()){
			sessionMap.removeElement(ve.getId(), ve.getType());
			sessionMap.addElement(ve);
		}
    	return ve;
    }
    
   	private VElement[] localRefreshElements(VElement[] mapElements) throws MapsException {
    		List<VElement> elems = new ArrayList<VElement>();
        	Vector<Integer> deletedNodeids= dbManager.getDeletedNodes();
        	java.util.Map<Integer,AlarmInfo> outagedNodes=getAlarmedNodes();
        	java.util.Map<Integer,Double> avails=dbManager.getAvails(mapElements);
        	Set<Integer> nodesBySource = new HashSet<Integer>();
        	if(dataSource!=null) {
                nodesBySource = dbManager.getNodeIdsBySource(filter);
            }

    		VElement ve = null;
    		if (mapElements != null) {
                for (VElement mapElement : mapElements) {
                	ve = refresh(mapElement,nodesBySource,deletedNodeids,outagedNodes,avails);
                	if (ve!=null) {
                		elems.add(ve);
                	}
                	
                	if(sessionMap!=null && mapElement.getMapId()==sessionMap.getId()){
                		sessionMap.removeElement(mapElement.getId(), mapElement.getType());
                		sessionMap.addElement(ve);
                	}
                }
            }
    		
        	return elems.toArray(new VElement[0]);
    }

   	public VMapInfo getMapMenu(int mapId) throws MapNotFoundException, MapsException {
		VMapInfo m = null;
			m = dbManager.getMapMenu(mapId);
		    if (m == null) {
		        throw new MapNotFoundException("No Maps found.");
		    }
		return m;
	}
    
    public List<VLink> getLinks(VElement[] elems) throws MapsException {

    	List<VLink> links = new ArrayList<VLink>();
        
    	
    	VLink[] vlinks = getLinkArray(elems);
    	if (vlinks != null) {
            for (VLink vlink : vlinks) {
            	links.add(vlink);
            	
            }
        }
        return links;
    }    

 	public List<VLink> getLinksOnElem(VElement[] elems,VElement elem) throws MapsException {

    	List<VLink> links = new ArrayList<VLink>();
        
    	
    	VLink[] vlinks = getLinksOnElement(elems, elem);
    	if (vlinks != null) {
            for (VLink vlink : vlinks) {
            	links.add(vlink);
            	
            }
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
        Map[] m = dbManager.getMapsLike(likeLabel);
        if (m == null) {
            throw new MapNotFoundException("Maps with label like "
                    + likeLabel + " don't exist.");
        }
        retVMap = new VMap[m.length];
        for (int i = 0; i < m.length; i++) {
            retVMap[i] = openMap(m[i].getId(),refreshElems);
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
        Map[] m = dbManager.getMapsByName(mapName);
        if (m == null) {
            throw new MapNotFoundException("Maps with name "
                    + mapName + " don't exist.");
        }
        retVMap = new VMap[m.length];
        for (int i = 0; i < m.length; i++) {
            retVMap[i] = openMap(m[i].getId(), refreshElems);
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
        Map[] m = dbManager.getAllMaps();
        if (m == null) {
            throw new MapNotFoundException("No Maps found.");
        }
        retVMap = new VMap[m.length];
        for (int i = 0; i < m.length; i++) {
            retVMap[i] = openMap(m[i].getId(), refreshElems);
        }
        return retVMap;
    }

    /**
     * Get all defined maps.
     * 
     * @return the MapMenu array containing all maps defined
     * @throws MapsException
     */
    
    public VMapInfo[] getAllMapMenus() throws  MapsException {
    	VMapInfo[] m = null;
		m = dbManager.getAllMapMenus();
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
    public VMapInfo[] getMapsMenuByName(String mapName) throws 
            MapNotFoundException, MapsException {
    	VMapInfo[] retVMap = null;
    	retVMap = dbManager.getMapsMenuByName(mapName);
        if (retVMap == null) {
            throw new MapNotFoundException("Maps with name "
                    + mapName + " don't exist.");
        }
        return retVMap;
    }
    
    public VMapInfo getDefaultMapsMenu(String user) throws MapsException {
        
        Iterator<Group> ite = getGroupDao().findGroupsForUser(user).iterator();

        while (ite.hasNext()) {
            Group group = ite.next();
            log.debug("getDefaultMapsMenu: found group: " + group.getName() + " for user:" + user);
            if (group.getDefaultMap() != null) {
                log.debug("getDefaultMapsMenu: found default map: " + group.getDefaultMap() + " for group: " + group.getName());
                VMapInfo[] vmapsinfo = dbManager.getMapsMenuByName(group.getDefaultMap());
                if (vmapsinfo != null ) {
                    log.debug("getDefaultMapsMenu: found " + vmapsinfo.length +" maps. Verify access ");                    
                    for (int i=0; i<vmapsinfo.length;i++) {
                        if (vmapsinfo[i].getOwner().equals(user)) {
                            log.info("getDefaultMapsMenu: found! user: " + user + " owns the map");
                            return vmapsinfo[i];
                        } else {
                            Map map = dbManager.getMap(vmapsinfo[i].getId());
                            log.debug("getDefaultMapsMenu: map: " + map.getId()+ " mapName: " +map.getName() + " Access: " + map.getAccessMode() + " Group: " + map.getGroup());
                            if (map.getAccessMode().trim().toUpperCase().equals(Map.ACCESS_MODE_ADMIN.toUpperCase()) 
                                    || map.getAccessMode().trim().toUpperCase().equals(Map.ACCESS_MODE_USER.toUpperCase()) 
                                    || (map.getAccessMode().trim().toUpperCase().equals(Map.ACCESS_MODE_GROUP.toUpperCase()) 
                                        && map.getGroup().equals(group.getName())) ) {
                                log.info("getDefaultMapsMenu: found! user: " + user + " has access to map: " + map.getName() + " with id: " + map.getId());
                                return vmapsinfo[i];
                            } else {
                                log.info("getDefaultMapsMenu: access is denied for default map: " + group.getDefaultMap() + " to group: " + group.getName());                                
                            }
                        }
                    }
                } else {
                    log.info("getDefaultMapsMenu: no maps found for default map: " + group.getDefaultMap() + " for group: " + group.getName());
                }
            }
        }
        return new VMapInfo(MapsConstants.NEW_MAP,"no default map found",user);
    }

    /**
     * gets all visible maps for user and userRole in input
     * @param user
     * @param userRole
     * @return a List of MapMenu objects.
     * @throws MapsException
     */
    public List<VMapInfo> getVisibleMapsMenu(String user)throws MapsException{
    	
        List<VMapInfo> maps = new ArrayList<VMapInfo>();
        List<Integer>  mapsIds = new ArrayList<Integer>();
        
        VMapInfo[] mapsbyother = dbManager.getMapsMenuByOther();
        if (mapsbyother != null) {
            for (VMapInfo element : mapsbyother) {
                maps.add(element);
                mapsIds.add(element.getId());
            }
        }
        
        VMapInfo[] mapsbyowner = dbManager.getMapsMenuByOwner(user);
        if (mapsbyowner != null) {
            for (int i=0;i<mapsbyowner.length; i++) {
                if (!mapsIds.contains(mapsbyowner[i].getId())) {
                    maps.add(mapsbyowner[i]);
                    mapsIds.add(mapsbyowner[i].getId());
                }
            }
        }

        Iterator<Group> ite = getGroupDao().findGroupsForUser(user).iterator();
        while (ite.hasNext()) {
            VMapInfo[] mapsbygroup = dbManager.getMapsMenuByGroup(ite.next().getName());
            if (mapsbygroup != null) {
                for (int i=0;i<mapsbygroup.length; i++) {
                    if (!mapsIds.contains(mapsbygroup[i].getId())) {
                        maps.add(mapsbygroup[i]);
                        mapsIds.add(mapsbygroup[i].getId());
                    }
                }
            }
        }
    	return maps;
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
    public List<VMapInfo> getMapsMenuTreeByName(String mapName) throws 
            MapNotFoundException, MapsException {
    	  List<VMapInfo> mapsInTreesList = new ArrayList<VMapInfo>();
	      //
	      VMapInfo[] mapsMenu = null;
	      try{
	      	mapsMenu=getMapsMenuByName(mapName);
	      }catch(MapNotFoundException mnf){
	      	//do nothing...
	      }
	      if (mapsMenu != null) {
	      	  // find all accessible maps for the user,
	      	  // for all maps (and theirs tree of maps) with name like mapName. 
	      	  for (VMapInfo element : mapsMenu) {
	      	  	  //build a map in wich each entry is [mapparentid, listofchildsids]
			      java.util.Map<Integer, Set<Integer>> parent_child = dbManager.getMapsStructure();
			      List<Integer> childList = new ArrayList<Integer>();
                  
			      preorderVisit(new Integer(element.getId()), childList, parent_child);
                  
			      for (int i = 0; i < childList.size(); i++) {
			          preorderVisit(childList.get(i), childList, parent_child);
			      }
                  
			      //adds all sub-tree of maps to the visible map list
			      for (int i = 0; i < childList.size(); i++) {
			          mapsInTreesList.add(getMapMenu(childList.get(i).intValue()));
			      }
	      	  }
	      }
        return mapsInTreesList;
    }
    
    private void preorderVisit(Integer rootElem, List<Integer> treeElems, java.util.Map<Integer, Set<Integer>> maps) {
       	Set<Integer> childs = maps.get(rootElem);
       	if (!treeElems.contains(rootElem)) {
       		treeElems.add(rootElem);
       	}
        
       	if (childs != null) {
	    	Iterator<Integer> it = childs.iterator();
	    	while (it.hasNext()) {
	    		Integer child = it.next();
	    		if (!treeElems.contains(child)) {
	    			treeElems.add(child);
	    		}
	    		preorderVisit(child, treeElems, maps);
	    	}   	    	
       	}
    }


    /**
     * Create a new VElement with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @param x 
     * @param y
     * @return the new VElement
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type, int x,int y) throws 
             MapsException{

		VElement velem = newElement(mapId,elementId,type);
		velem.setX(x);
		velem.setY(y);
        return velem; 
    }
	
	public VElement newElement(int elementId, String type, int x,int y) throws 
    MapsException{
		if(sessionMap==null) {
            throw new MapNotFoundException("session map in null");
        }
		return newElement(sessionMap.getId(),elementId, type, x, y);
	}

    /**
     * Create a new element child of the map with mapId (this map must be the sessionMap)
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @return the new VElement
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type) throws MapsException {
    	if(sessionMap==null) {
            throw new MapNotFoundException("session map is null");
        }
    	if(sessionMap.getId()!=mapId) {
            throw new MapsException("No current session map: cannot create new element of map "+mapId);
        }
		Element elem = dbManager.newElement(elementId, mapId, type);
		VElement velem= new VElement(elem);
		velem.setSeverity(mapsPropertiesFactory.getIndeterminateSeverity().getId());
		velem.setRtc(mapsPropertiesFactory.getUndefinedAvail().getMin());
		velem.setStatus(mapsPropertiesFactory.getUnknownStatus().getId());
		log.debug("Adding velement to map "+velem.toString());
        sessionMap.addElement(velem);
		return velem;

    }
	
	/**
	 * 
	 * Create a new element child of the map with mapId (this map must be the sessionMap)
	 */
	public VElement newElement(int elementId, String type) throws MapsException {
		if(sessionMap==null) {
            throw new MapNotFoundException("session map in null");
        }
		return newElement(sessionMap.getId(),elementId,type);

    }


    /**
     * Create a new element child of the map with mapId (this map must be the sessionMap).
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @return the new VElement
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type, String iconname, int x,int y) throws 
            MapsException {
		VElement velem = newElement(mapId, elementId, type);
		velem.setIcon(iconname);
		velem.setX(x);
		velem.setY(y);
        return velem;

    }
	
	public VElement newElement(int elementId, String type, String iconname, int x,int y) throws 
    MapsException {
		if(sessionMap==null) {
            throw new MapNotFoundException("session map in null");
        }
		return newElement(sessionMap.getId(),elementId, type, iconname, x,y); 
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
        VElement elem = newElement(mapId, elementId, type);
        elem.setIcon(iconname);
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
    		deleteMap(map.getId());
    }

    /**
     * delete the map with identifier id
     * 
     * @param id
     *            of the map to delete
     * @throws MapsException
     */
    synchronized public void deleteMap(int mapId) throws MapsException {
    	if(sessionMap==null) {
            throw new MapNotFoundException("session map in null");
        }
    	if(sessionMap.getId()!=mapId) {
            throw new MapsException("No current session map: cannot delete map with id "+mapId);
        }
    	if (dbManager.deleteMap(mapId) == 0) {
            throw new MapNotFoundException("Map with id "+mapId+" doesn't exist.");
        }
    	sessionMap=null;
    }

    
   
    
    /**
     * delete the maps in input
     * 
     * @param maps
     *            to delete
     * @throws MapsException
     */
    synchronized public void deleteMaps(VMap[] maps) throws MapsException {
        for (VMap map : maps) {
            deleteMap(map);
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
        for (int map : maps) {
            deleteMap(map);
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
            dbManager.deleteElementsOfMap(map.getId());
        }
        dbManager.saveMap(map);
        dbManager.saveElements(map.getAllElements());
    }

    /**
     * save the maps in input
     * 
     * @param maps
     *            to save
     * @throws MapsException
     */
    synchronized public void save(VMap[] maps) throws MapsException {
        for (VMap map : maps) {
            save(map);
        }
    }
    
    /**
     * delete all defined node elements in existent maps
     * @throws MapsException
     */
    synchronized public void deleteAllNodeElements()throws MapsException{
    	dbManager.deleteNodeTypeElementsFromAllMaps();
    }
    
    /**
     * delete all defined sub maps in existent maps
     * @throws MapsException
     */
    synchronized public void deleteAllMapElements()throws MapsException{
    	dbManager.deleteMapTypeElementsFromAllMaps();
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
    public List<VElement> refreshElements(VElement[] mapElements) throws MapsException{
    	
    	Category log;

		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		List<VElement> elems = new ArrayList<VElement>();

        if (mapElements == null || mapElements.length == 0){
			log.warn("refreshElements: method called with null or empty input");
			return elems;
        }

        return Arrays.asList(localRefreshElements(mapElements));
    }
    
    
    public VElement refreshElement(VElement mapElement) throws MapsException {
     	return localRefreshElement(mapElement);
    }    
    

    
    /**
     * Reloads elements of map and theirs avail,severity and status
     * @param map 
     * @return the map refreshed
     */
    public VMap reloadMap(VMap map)throws MapsException{
    	
		//VElement[] velems = localRefreshElements((map.getAllElements()));
    	Element[] elems = dbManager.getElementsOfMap(map.getId());
    	VElement[] velems = new VElement[elems.length];
    	for(int i=0; i<elems.length;i++) {
    		velems[i]=new VElement(elems[i]);
    	}
    	velems = localRefreshElements(velems);
    	map.removeAllLinks();
		map.removeAllElements();
    	map.addElements(velems);
		return map;
    }
    
    
    public List<VElement> refreshMap() throws MapsException{
    	if(sessionMap==null) {
            throw new MapNotFoundException("session map in null");
        }
    	return refreshElements(sessionMap.getAllElements());
    }

    public List<VLink> refreshLinks(VLink[] mapLinks) throws MapsException {
        throw new MapsException();
    }

    public boolean foundLoopOnMaps(VMap parentMap,int mapId) throws MapsException {
		
		
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		Category log= ThreadCategory.getInstance(this.getClass());

		java.util.Map<Integer,Set<Integer>> maps = dbManager.getMapsStructure();
		VElement[] elems = parentMap.getAllElements();
		if (elems == null) {
            return false;
        }
		Set<Integer> childSet = new TreeSet<Integer>();
		for (VElement elem : elems) {
			if (elem.getType().equals(VElement.MAP_TYPE)) {
				childSet.add(new Integer(elem.getId()));
            }
		}
	    
		log.debug("List of sub-maps before preorder visit "+childSet.toString());

	    maps.put(new Integer(parentMap.getId()),childSet);

	    while (childSet.size() > 0) {
		    childSet = preorderVisit(childSet, maps);
	
		    log.debug("List of sub-maps  "+childSet.toString());
	
		    if(childSet.contains(new Integer(mapId))){
				return true;
			}
	    }
	return false;
	
	}

    private Set<Integer> preorderVisit(Set<Integer> treeElems, java.util.Map<Integer, Set<Integer>> maps) {
    	Set<Integer> childset = new TreeSet<Integer>();
    	Iterator<Integer> it = treeElems.iterator();
    	while (it.hasNext()) {
    		Set<Integer> curset = maps.get(it.next());
    		if (curset != null) {
                childset.addAll(curset);
            }
    	}
    	return childset;
    }
    
    /**
     * recursively gets all nodes contained by elem and its submaps (if elem is a map)
     */
    public Set<Integer> getNodeidsOnElement(VElement velem) throws MapsException {
    	Element elem = new Element(velem);
    	return dbManager.getNodeidsOnElement(elem);
    }
    
	public VElementInfo[] getAllElementInfo() throws MapsException {
		return dbManager.getAllElementInfo();
	}
	
	public VElementInfo[] getElementInfoLike(String like) throws MapsException {
		return dbManager.getElementInfoLike(like);
	}
	
	public org.opennms.web.map.db.Manager getDataAccessManager(){
		return dbManager;
	}
	
	/**
     * Gets all nodes on the passed map (and its submaps) with theirs occurrences
     * @param map
     * @return HashMap<Integer, Integer> (nodeid, occurrences) containing all nodes on the passed map (and its submaps) with theirs occourrences
     */
    public HashMap<Integer, Integer> getAllNodesOccursOnMap(VMap map)throws MapsException{
    	HashMap<Integer, Integer> result=new HashMap<Integer, Integer>();
    	VElement[] elems = map.getAllElements();
    	
		for (VElement elem : elems) {
			if(elem.getType()==VElement.MAP_TYPE){
				Set<Integer> nodeids=getNodeidsOnElement(elem);
				Iterator<Integer> iter=nodeids.iterator();
				while(iter.hasNext()){
					Integer nid=iter.next();
					Integer occ=result.get(nid);
					if(occ==null) {
                        occ=0;
                    }
					occ++;
					result.put(nid, occ);
				}
			}else{
				Integer nid=elem.getId();
				Integer occ=result.get(nid);
				if(occ==null) {
                    occ=0;
                }
				occ++;
				result.put(nid, occ);
			}
		}
    	return result;
    }
    
	private String getSeverityLabel(int severity) throws MapsException {
	    return OnmsSeverity.get(severity).getLabel();
	}	
	
    private VElement refresh(VElement mapElement, Set<Integer> nodesBySource, Vector<Integer> deletedNodeids, java.util.Map<Integer,AlarmInfo> outagedNodes,java.util.Map<Integer,Double> avails) throws MapsException {
		VElement ve = mapElement.clone();
		if (log.isDebugEnabled()) {
            log.debug("refresh: parsing VElement ID " + ve.getId()
					+ ve.getType() + ", label:"+ve.getLabel()+" with node by sources: " +nodesBySource.toString() + " deletedNodeids: " + deletedNodeids.toString()
					+ " outagedNode: " +outagedNodes.keySet().toString());
        }

		double elementAvail = mapsPropertiesFactory.getDisabledAvail().getMin();
		int elementStatus = mapsPropertiesFactory.getDefaultStatus().getId();
		float elementSeverity = mapsPropertiesFactory.getDefaultSeverity().getId();
		String calculateSeverityAs = mapsPropertiesFactory.getSeverityMapAs();
		
		// get status, severity and availability: for each node, look for alternative data
		// sources; if no source is found or if the data is not retrieved, use opennms. 
		if (dbManager.isElementNotDeleted(ve.getId(), ve.getType())) {
            elementAvail = mapsPropertiesFactory.getUndefinedAvail().getMin();
            elementStatus=mapsPropertiesFactory.getUnknownStatus().getId();
            elementSeverity = mapsPropertiesFactory.getIndeterminateSeverity().getId();
            log.warn("The element type: " + ve.getType() +" with id=" + ve.getId() + " was deleted");
		} else if (ve.isNode()) {
			if(deletedNodeids.contains(new Integer(ve.getId()))){
	            elementAvail = mapsPropertiesFactory.getUndefinedAvail().getMin();
	            elementStatus=mapsPropertiesFactory.getUnknownStatus().getId();
	            elementSeverity = mapsPropertiesFactory.getIndeterminateSeverity().getId();
			} else{ //if the node isn't deleted
				if (nodesBySource.contains(new Integer(ve.getId()))) {
					Object id = new Integer(ve.getId());
					log.debug("getting status from alternative source " + dataSource.getClass().getName());
					int status = mapsPropertiesFactory.getStatus(dataSource.getStatus(id));
					if (status >= 0) {
						elementStatus = status;
						log.debug("got status from alternative source. Value is "+elementStatus);
					}
					
					int sev = mapsPropertiesFactory.getSeverity(dataSource.getSeverity(id));
					if (sev >= 0) {
						elementSeverity = sev;
						log.debug("got severity from alternative source. Value is "+sev);
					} 
					if (mapsPropertiesFactory.isAvailEnabled()) {
						double avail = dataSource.getAvailability(id);
						if (avail >= 0) {
							elementAvail = avail;
							log.debug("got availability from alternative source. Value is "+avail);
						} 
					}
				} else {
					AlarmInfo oi = outagedNodes.get(new Integer(ve.getId()));
					if (oi != null) {
						elementStatus = oi.getStatus();
						elementSeverity= oi.getSeverity();
					}
	  				if (mapsPropertiesFactory.isAvailEnabled()) {
	   					elementAvail =avails.get(new Integer(ve.getId())).doubleValue();
	   				}				
					
				}
			} // end of nodes deleted				
    	} else { // the element is a Map
			log.debug("Calculating severity for submap Element " + ve.getId()
					+ " using '" + calculateSeverityAs + "' mode.");
			Set<Integer> nodesonve = getNodeidsOnElement(ve);
			if (nodesonve != null && nodesonve.size() > 0) {
				log.debug("found nodes on Map element :" + nodesonve.toString());
				elementAvail = mapsPropertiesFactory.getDisabledAvail().getMin();
				float sev = 0;
				if (calculateSeverityAs.equalsIgnoreCase("worst")
						|| calculateSeverityAs.equalsIgnoreCase("best")) {
					sev = mapsPropertiesFactory.getDefaultSeverity().getId();
				}
				Iterator<Integer> ite = nodesonve.iterator();
				while (ite.hasNext()) {
					Integer nextNodeId = ite.next();
					if(deletedNodeids.contains(nextNodeId)){
						elementAvail += mapsPropertiesFactory.getUndefinedAvail().getMin();
						elementStatus=mapsPropertiesFactory.getUnknownStatus().getId();
						elementSeverity = mapsPropertiesFactory.getIndeterminateSeverity().getId();
					}else{ //if the node isn't deleted
						if (nodesBySource.contains(nextNodeId)) {
							int st = mapsPropertiesFactory.getStatus(dataSource.getStatus(nextNodeId));
							if (st >= 0) {
								if (st < elementStatus) {
									elementStatus = st;
								}
								log.debug("got status from alternative source. Value is "+st);
							}

							int tempSeverity = mapsPropertiesFactory.getSeverity(dataSource.getSeverity(nextNodeId));
							if (tempSeverity >= 0) {
								log.debug("got severity from alternative source. Value is "+tempSeverity);
								if (calculateSeverityAs.equalsIgnoreCase("avg")) {
									sev += tempSeverity;
								} else if (calculateSeverityAs
										.equalsIgnoreCase("worst")) {
									if (sev > tempSeverity) {
										sev = tempSeverity;
									}
								} else if (calculateSeverityAs
										.equalsIgnoreCase("best")) {
									if (sev < tempSeverity) {
										sev = tempSeverity;
									}
								}
							} 	
							if (mapsPropertiesFactory.isAvailEnabled()) {
								double avail = dataSource.getAvailability(nextNodeId);
								if (avail >= 0) {
									elementAvail += avail;
									log.debug("got availability from alternative source. Value is "+avail);
								} 
							}	
						} else {
							AlarmInfo oi = outagedNodes.get(nextNodeId);
							if (oi != null) {
								elementStatus = oi.getStatus();
								float tempSeverity= oi.getSeverity();
								if (tempSeverity >= 0) {
									if (calculateSeverityAs.equalsIgnoreCase("avg")) {
										sev += tempSeverity;
									} else if (calculateSeverityAs
											.equalsIgnoreCase("worst")) {
										if (sev > tempSeverity) {
											sev = tempSeverity;
										}
									} else if (calculateSeverityAs
											.equalsIgnoreCase("best")) {
										if (sev < tempSeverity) {
											sev = tempSeverity;
										}
									}
								} 	
							}
			  				if (mapsPropertiesFactory.isAvailEnabled()) {
			   					elementAvail +=avails.get(nextNodeId).doubleValue();
			   				}	
							
						}
					}
				}
				if (calculateSeverityAs.equalsIgnoreCase("avg")) {
					elementSeverity = sev / nodesonve.size();
				} else {
					elementSeverity = sev;
				}
				//calculate availability as average of all nodes on element
				if(elementAvail>0) {
                    elementAvail=elementAvail / nodesonve.size();
                }
				
			} else {
				log.debug("no nodes on Map element found");
			}
		}
		
		if (log.isDebugEnabled()) {
            log.debug("refreshElement: element avail/status/severity "
					+ elementAvail + "/" + elementStatus + "/"
					+ elementSeverity);
        }

		ve.setRtc(elementAvail);
		ve.setStatus(elementStatus);
		ve.setSeverity(new BigDecimal(elementSeverity + 1 / 2).intValue());
		return ve;
	}


    private java.util.Map<Integer,AlarmInfo> getAlarmedNodes() throws MapsException{
        
    	java.util.Map<Integer,AlarmInfo> alarmedNodes = new HashMap<Integer,AlarmInfo>();
        log.debug("Getting alarmed elems.");
        Iterator<VElementInfo> ite = dbManager.getAlarmedElements().iterator();
        log.debug("Alarmed elems obtained.");
		while (ite.hasNext()) {
			VElementInfo veleminfo = ite.next();
			int alarmStatus = mapsPropertiesFactory.getStatus(veleminfo.getUei());
			int alarmSeverity = mapsPropertiesFactory.getSeverity(getSeverityLabel(veleminfo.getSeverity()));

			if (log.isInfoEnabled()) {
                log.info("parsing alarmed node with nodeid: " + veleminfo.getId() + " severity: " + veleminfo.getSeverity() + " severity label: " +getSeverityLabel(veleminfo.getSeverity()));
            }

			if (log.isInfoEnabled()) {
                log.info("parsing alarmed node with nodeid: " + veleminfo.getId() + " status: " + veleminfo.getUei() + " severity label: " +getSeverityLabel(veleminfo.getSeverity()));
            }

			if (log.isDebugEnabled()) {
                log.debug("local alarmed node status/severity " + alarmStatus + "/" + alarmSeverity);
            }

			AlarmInfo alarminfo = alarmedNodes.get(new Integer(veleminfo.getId())); 

			if (alarminfo != null) {
				if (alarminfo.getStatus() > alarmStatus) {
					alarminfo.setStatus(alarmStatus);
					alarminfo.setSeverity(alarmSeverity);
				}	
			} else {
				int curStatus = alarmStatus;
				float curSeverity = alarmSeverity;
				alarminfo = new AlarmInfo(veleminfo.getId(),curStatus,curSeverity);
			}
			alarmedNodes.put(new Integer(veleminfo.getId()),alarminfo);
    		if (log.isDebugEnabled()) {
                log.debug("global element node status/severity " + alarmStatus + "/" + alarmSeverity);
            }
		}
        return alarmedNodes;
    }    
    
    /**
     * gets the id corresponding to the link defined in configuration file. The match is performed first by snmptype, 
     * then by speed (if more are defined). If there is no match, the default link id is returned. 
     * @param linkinfo
     * @return the id corresponding to the link defined in configuration file. If there is no match, the default link id is returned.
     */
    private int getLinkTypeId(LinkInfo linkinfo) {
    	return mapsPropertiesFactory.getLinkTypeId(linkinfo.snmpiftype, linkinfo.snmpifspeed);
    }
    
    private VLink[] getLinkArray(VElement[] elems) throws MapsException {
    	String multilinkStatus = mapsPropertiesFactory.getMultilinkStatus();
    	List<VLink> links = new ArrayList<VLink>();
        
    	// this is the list of nodes set related to Element
    	//java.util.List<Set<Integer>> elemNodes = new java.util.ArrayList<Set<Integer>>();
    	java.util.Map<Integer,Set<Integer>> node2Element = new HashMap<Integer,Set<Integer>>();
    	
    	HashSet<Integer> allNodes = new HashSet<Integer>();
		try {
	    	if (elems != null) {
	        	for (int i = 0; i < elems.length; i++) {
	        			        		
	                Set<Integer> nodeids = getNodeidsOnElement(elems[i]);
	                allNodes.addAll(nodeids);
	                //elemNodes.add(nodeids);
	                Iterator<Integer> ite = nodeids.iterator();
	    	    	while(ite.hasNext()) {
	    	    		Integer nodeid = ite.next();
	    	    		Set<Integer> elements = node2Element.get(nodeid);
	    	    		if (elements == null) {
                            elements = new java.util.HashSet<Integer>();
                        }
	    	    		elements.add(new Integer(i));
	    	    		node2Element.put(nodeid,elements);
	    	    	}
	    	    }
	        }else{
	        	return null;
	        }
    		
	    	log.debug("----------Node2Element----------");
	    	Iterator<Integer> it = node2Element.keySet().iterator();
	    	while(it.hasNext()){
	    		Integer nodeid=it.next();
	    		log.debug("Node "+nodeid+" contained in Elements "+node2Element.get(nodeid).toString());
	    	}
	    	log.debug("----------End of Node2Element----------");

	    	log.debug("----------Link on Elements ----------");
	    	Set<LinkInfo> linkinfo = dbManager.getLinksOnElements(allNodes);
	    	Iterator<LinkInfo> ite = linkinfo.iterator();
	    	while(ite.hasNext()) {
	    		LinkInfo linfo = ite.next();
	    		log.debug(""+linfo.nodeid+"-"+linfo.nodeparentid);
	    	}
	    	log.debug("----------End of Link on Elements ----------");
	    	ite = linkinfo.iterator();
	    	while(ite.hasNext()) {
	    		LinkInfo linfo = ite.next();
	    		log.debug("Getting linkinfo for nodeid "+linfo.nodeid);
	    		Set<Integer> fE = node2Element.get(linfo.nodeid);
	    		log.debug("Got "+fE);
	    		if(fE!=null){
		    		Iterator<Integer> firstElements = fE.iterator();
		    		while (firstElements.hasNext()) {
			    		log.debug("Getting linkinfo for nodeid "+linfo.nodeparentid);
		    			Set<Integer> sE=node2Element.get(linfo.nodeparentid);
		    			log.debug("Got "+sE);
		    			Integer firstNext = firstElements.next();
		    			if(sE!=null){
				    		Iterator<Integer> secondElements = sE.iterator();
				    		VElement first = elems[firstNext.intValue()]; 
				    		while (secondElements.hasNext()) {
				    			VElement second = elems[secondElements.next().intValue()];
				    			if (first.hasSameIdentifier(second)) {
                                    continue;
                                }
				    			VLink vlink = new VLink(first,second);
				    			vlink.setLinkOperStatus(linfo.snmpifoperstatus);
				    			vlink.setLinkTypeId(getLinkTypeId(linfo));
				    			int index = links.indexOf(vlink);
				    			if(index!=-1){
				    				VLink alreadyIn = links.get(index);
				    				if(alreadyIn.equals(vlink)){
				    					if(multilinkStatus.equals(MapPropertiesFactory.MULTILINK_BEST_STATUS)){
				    						if(vlink.getLinkOperStatus()<alreadyIn.getLinkOperStatus()){
				    							log.debug("removing to the array link "+alreadyIn.toString()+ " with status "+alreadyIn.getLinkOperStatus());
				    							links.remove(index);
				    							links.add(vlink);
				    							log.debug("adding to the array link "+vlink.toString()+ " with status "+vlink.getLinkOperStatus());
				    						}
				    					}else if(vlink.getLinkOperStatus()>alreadyIn.getLinkOperStatus()){
				    						log.debug("removing to the array link "+alreadyIn.toString()+ " with status "+alreadyIn.getLinkOperStatus());
			    							links.remove(index);
			    							links.add(vlink);
			    							log.debug("adding to the array link "+vlink.toString()+ " with status "+vlink.getLinkOperStatus());
			    						}
				    				}
				    			}else{
					    			log.debug("adding link ("+vlink.hashCode()+") "+vlink.getFirst().getId()+"-"+vlink.getSecond().getId());
					    			links.add(vlink);
				    			}
				    		}
		    			}
		    			
		    		}
	    		}else{ 
	    			log.debug("Getting linkinfo for nodeid "+linfo.nodeparentid);
		    		Set<Integer> ffE = node2Element.get(linfo.nodeparentid);
		    		log.debug("Got "+ffE);
	    			if(ffE!=null){
			    		Iterator<Integer> firstElements = ffE.iterator();
			    		while (firstElements.hasNext()) {
				    		log.debug("Getting linkinfo for nodeid "+linfo.nodeparentid);
			    			Set<Integer> sE=node2Element.get(linfo.nodeparentid);
			    			log.debug("Got "+sE);
			    			Integer firstNext = firstElements.next();
			    			if(sE!=null){
					    		Iterator<Integer> secondElements = sE.iterator();
					    		VElement first = elems[firstNext.intValue()]; 
					    		while (secondElements.hasNext()) {
					    			VElement second = elems[secondElements.next().intValue()];
					    			if (first.hasSameIdentifier(second)) {
                                        continue;
                                    }
					    			VLink vlink = new VLink(first,second);
					    			vlink.setLinkOperStatus(linfo.snmpifoperstatus);
					    			vlink.setLinkTypeId(getLinkTypeId(linfo));
					    			int index = links.indexOf(vlink);
					    			if(index!=-1){
					    				VLink alreadyIn = links.get(index);
					    				if(alreadyIn.equals(vlink)){
					    					if(multilinkStatus.equals(MapPropertiesFactory.MULTILINK_BEST_STATUS)){
					    						if(vlink.getLinkOperStatus()<alreadyIn.getLinkOperStatus()){
					    							log.debug("removing to the array link "+alreadyIn.toString()+ " with status "+alreadyIn.getLinkOperStatus());
					    							links.remove(index);
					    							links.add(vlink);
					    							log.debug("adding to the array link "+vlink.toString()+ " with status "+vlink.getLinkOperStatus());
					    						}
					    					}else if(vlink.getLinkOperStatus()>alreadyIn.getLinkOperStatus()){
					    						log.debug("removing to the array link "+alreadyIn.toString()+ " with status "+alreadyIn.getLinkOperStatus());
				    							links.remove(index);
				    							links.add(vlink);
				    							log.debug("adding to the array link "+vlink.toString()+ " with status "+vlink.getLinkOperStatus());
				    						}
					    				}
					    			}else{
						    			log.debug("adding link ("+vlink.hashCode()+") "+vlink.getFirst().getId()+"-"+vlink.getSecond().getId());
						    			links.add(vlink);
					    			}
					    		}
			    			}
			    			
			    		}
	    			}
	    		}
	    		
	    	}
	    	log.debug("Exit...");
	    	/* old method to restore if new is slower
	        Iterator ite = elemNodes.iterator();
	        int firstelemcount = 0;
	        while (ite.hasNext()) {
	        	Set firstelemnodes = (TreeSet) ite.next();
	        	Set<LinkInfo> firstlinkednodes = getLinkedNodeidInfosOnNodes(firstelemnodes);
	            int secondelemcount = firstelemcount +1;
	            Iterator sub_ite = elemNodes.subList(secondelemcount,elemNodes.size()).iterator(); 
	        	while (sub_ite.hasNext()) {
	        		Iterator node_ite = ((TreeSet) sub_ite.next()).iterator();
	        		while (node_ite.hasNext()) {
	        			Integer curNodeId = (Integer) node_ite.next();
	        			if (firstlinkednodes.contains(curNodeId)) {
	        				VLink vlink = new VLink(elems[firstelemcount],elems[secondelemcount]);
	        				vlink.setLinkOperStatus(getLinkOperStatus(vlink));
	        				vlink.setLinkTypeId(getLinkTypeId(vlink));
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
			*/
	    	
    	}catch(Exception e){
    		log.error(e,e);
    		throw new MapsException(e);
    	}
        return links.toArray(new VLink[0]);
    }
    
	private VLink[] getLinksOnElement(VElement[] elems,VElement elem) throws MapsException {
 		if(elems==null || elem==null) {
            return null;
        }
 		ArrayList<VElement> listOfElems = new ArrayList<VElement>();
 		for (VElement elem2 : elems) {
 			if(elem2!=null) {
                listOfElems.add(elem2);
            }
 		}
 		listOfElems.add(elem);
 		return getLinkArray(listOfElems.toArray(new VElement[0]));
 		/*
 		
    	HashSet<VLink> links = new HashSet<VLink>();
        
    	// this is the list of nodes set related to Element
    	Set<LinkInfo> linkinfo = null;
		if (elem != null) {
            linkinfo = getLinkedNodeidInfosOnNodes(getNodeidsOnElement(elem));
        } else {
        	return null;
        }
        
        if (elems != null && linkinfo != null) {
    		
        	for (int i = 0; i < elems.length; i++) {
	    		Iterator node_ite = getNodeidsOnElement(elems[i]).iterator();
	    		while (node_ite.hasNext()) {
	    			Integer elemNodeId = (Integer) node_ite.next();
	    	    	Iterator<LinkInfo> ite = linkinfo.iterator();
	    	    	while(ite.hasNext()) {
	    	    		LinkInfo linfo = ite.next();
	    	    		if (linfo.nodeid != elemNodeId && linfo.nodeparentid != elemNodeId) continue;
	    				VLink vlink = new VLink(elems[i],elem);
		    			vlink.setLinkOperStatus(linfo.snmpifoperstatus);
		    			vlink.setLinkTypeId(getLinkTypeId(linfo));
		    			log.debug("adding "+vlink.hashCode());
		    			log.debug(links.toString()+" "+links.add(vlink));
		       		}
	    		}
	    	}
        }
        return links.toArray(new VLink[0]);*/
    }    
	
	private   String unescapeHtmlChars(String input) {
        return (input == null ? null : input.replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">"));
    }

    public GroupDao getGroupDao() {
        return m_groupDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        m_groupDao = groupDao;
    }


}
