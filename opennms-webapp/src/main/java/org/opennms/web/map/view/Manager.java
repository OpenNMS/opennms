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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.MapsManagementException;


/**
 * @author maurizio
 *  
 */
public interface Manager {

	// client/server configuration parameters
	
	public List<String> getCategories() throws MapsException;
	
	/**
     * Create a new empty VMap and return it.
     * 
     * @return the new VMap created.
     */
    public VMap newMap();

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
    public VMap newMap(String name, String accessMode, String owner,String userModifies, int width, int height) ;

    /**
     * Close a VMap previusly opened.
     * 
     */
    public void closeMap();

    /**
     * @return the default VMap, if exists
     * @throws MapsException
     */
    public VMap openMap() throws 
            MapNotFoundException;    


    /**
     * Take the map with id in input and return it in VMap form.
     * 
     * @param id
     * @param refreshElems says if refresh the map's elements
     * @return the VMap with identifier id
     * @throws MapsException
     */
    public VMap openMap(int id, boolean refreshElems) throws MapNotFoundException, MapsException;    

    /**
     * Take the map with id in input and return it in VMap form.
     * 
     * @param id
     * @param user
     * @param refreshElems says if refresh the map's elements
     * @return the VMap with identifier id
     * @throws MapsException
     */
    public VMap openMap(int id, String user, boolean refreshElems) throws MapNotFoundException, MapsException ;    

    public void clearMap()throws MapNotFoundException, MapsException ;
    
    public VMapInfo getMapMenu(int mapId) throws MapNotFoundException, MapsException ;
    
    public void deleteElementsOfMap(int mapId)throws MapsException;


    public List<VLink> getLinks(VElement[] elems) throws MapsException ;
    public List<VLink> getLinks(Collection<VElement> elems) throws MapsException ;

    public List<VLink> getLinksOnElem(VElement[] elems,VElement elem) throws MapsException ;
    

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
    public VMap[] getMapsLike(String likeLabel, boolean refreshElems) throws  MapsException ;

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
            MapNotFoundException, MapsException;

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
    public VMap[] getMapsByName(String mapName, boolean refreshElems) throws MapNotFoundException, MapsException ;
    
    /**
     * Get all defined maps.
     * @param refreshElems says if refresh maps' elements
     * @return the VMaps array containing all maps defined
     * @throws MapsException
     */
    public VMap[] getAllMaps(boolean refreshElems) throws MapNotFoundException, MapsException  ;

    /**
     * Get all defined maps.
     * 
     * @return the MapMenu array containing all maps defined
     * @throws MapsException
     */
    public VMapInfo[] getAllMapMenus() throws  MapsException;    

    /**
     * Take the maps with name in input and return them in
     * MapMenu[] form.
     * 
     * @param mapName
     * @return the MapMenu array if any map has name in input, null
     *         otherwise
     * @throws MapsException
     */
    public VMapInfo[] getMapsMenuByName(String mapName) throws MapNotFoundException, MapsException;
    
    /**
     * gets all visible maps for user in input
     * @param user
     * @return a List of MapMenu objects.
     * @throws MapsException
     */
    public List<VMapInfo> getVisibleMapsMenu(String user) throws MapsException;

    /**
     * get the default map for specified user in input 
     * if exists null otherwise 
     * @param user
     * @return a MapMenu object.
     * @throws MapsException
     */
    public VMapInfo getDefaultMapsMenu(String user) throws MapsException;


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
            MapNotFoundException, MapsException ;
    
    /**
     * Get a map element.
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @return the new VElement
     * @throws MapsException
     */
    public VElement getElement(int mapId, int elementId, String type) throws MapsException ;    
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
             MapsException;
   
	public VElement newElement(int elementId, String type, int x,int y) throws 
    MapsException;
	/**
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @return the new VElement
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type) throws MapsException ;	

	
	public VElement newElement(int elementId, String type) throws MapsException ;	
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
            MapsException ;
	
	public VElement newElement(int elementId, String type, String iconname, int x,int y) throws 
    		MapsException ;	
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
            MapsException;
	
	/**
     * delete the map in input
     * 
     * @param map
     *            to delete
     * @throws MapsException
     *             if an error occour deleting map, MapNotFoundException if the
     *             map to delete doesn't exist.
     */
    public void deleteMap(VMap map) throws MapsException,
            MapNotFoundException ;
 
    public void deleteMap() throws MapsException,
    MapNotFoundException ;
    /**
     * delete the map with identifier id
     * 
     * @param id
     *            of the map to delete
     * @throws MapsException
     */
    public void deleteMap(int mapId) throws MapsException ;

    /**
     * delete the maps in input
     * 
     * @param maps
     *            to delete
     * @throws MapsException
     */
    public void deleteMaps(VMap[] maps) throws MapsException ;

    /**
     * delete the maps with the identifiers in input
     * 
     * @param identifiers
     *            of the maps to delete
     * @throws MapsException
     */
    public void deleteMaps(int[] maps) throws MapsException ;

    /**
     * save the map in input
     * 
     * @param map
     *            to save
     * @throws MapsException
     */
    public void save(VMap map) throws MapsException ;

    /**
     * save the maps in input
     * 
     * @param maps
     *            to save
     * @throws MapsException
     */
    public void save(VMap[] maps) throws MapsException ;
    
    /**
     * delete all defined node elements in existent maps
     * @throws MapsException
     */
    public void deleteAllNodeElements()throws MapsException;
    
    /**
     * delete all defined sub maps in existent maps
     * @throws MapsException
     */
    
    public void deleteAllMapElements()throws MapsException;

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
    public List<VElement> refreshElements(VElement[] mapElements) throws MapsException;    

    public List<VElement> refreshMap() throws MapsException;
    
    public VElement refreshElement(VElement mapElement) throws MapsException;    
    
    /**
     * Refreshs avail,severity and status of the map in input and its elements
     * @param map 
     * @return the map refreshed
     */
    public VMap reloadMap(VMap map)throws MapsException;
    
    /**
     * TODO 
     * write this method simil way to refreshElement
     * Not Yet Implemented
     */
    public List<VLink> refreshLinks(VLink[] mapLinks) throws MapsException;
    
    public boolean foundLoopOnMaps(VMap parentMap,int mapId) throws MapsException ;

    /**
     * recursively gets all nodes contained by elem and its submaps (if elem is a map)
     */
    public Set<Integer> getNodeidsOnElement(VElement elem) throws MapsException;
    
	public VElementInfo[] getAllElementInfo() throws MapsException;
	
	public VElementInfo[] getElementInfoLike(String like) throws MapsException;
	
	/**
     * Gets all nodes on the passed map (and its submaps) with theirs occurrences
     * @param map
     * @return HashMap<Integer, Integer> (nodeid, occurrences) containing all nodes on the passed map (and its submaps) with theirs occourrences
     */
    public HashMap<Integer, Integer> getAllNodesOccursOnMap(VMap map)throws MapsException;
    

}
