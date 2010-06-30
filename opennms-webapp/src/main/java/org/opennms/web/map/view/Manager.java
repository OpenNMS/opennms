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

import org.opennms.web.map.config.MapStartUpConfig;


/**
 * <p>Manager interface.</p>
 *
 * @author maurizio
 * @version $Id: $
 * @since 1.6.12
 */
public interface Manager {

	//to use when modifying maps
	//public void startSession() throws MapsException;

	//public void endSession() throws MapsException;

	//the mapStartUpConfig mantains dynamic infos from client 
	/**
	 * <p>getMapStartUpConfig</p>
	 *
	 * @return a {@link org.opennms.web.map.config.MapStartUpConfig} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public MapStartUpConfig getMapStartUpConfig() throws MapsException;
	
	/**
	 * <p>setMapStartUpConfig</p>
	 *
	 * @param config a {@link org.opennms.web.map.config.MapStartUpConfig} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void setMapStartUpConfig(MapStartUpConfig config) throws MapsException;
	
	// client/server configuration parameters
	
	/**
	 * <p>getCategories</p>
	 *
	 * @return a {@link java.util.List} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public List<String> getCategories() throws MapsException;
	
    // Useful to manage nodes
    
    /**
     * <p>isUserAdmin</p>
     *
     * @return a boolean.
     */
    public boolean isUserAdmin();
    
    /**
     * <p>isAdminMode</p>
     *
     * @return a boolean.
     */
    public boolean isAdminMode();
    
    /**
     * <p>setAdminMode</p>
     *
     * @param mode a boolean.
     */
    public void setAdminMode(boolean mode);
    
    /**
     * Create a new empty VMap and return it.
     *
     * @return the new VMap created.
     */
    public VMap newMap();

    /**
     * Create a new VMap and return it
     *
     * @param name a {@link java.lang.String} object.
     * @param accessMode a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     * @param userModifies a {@link java.lang.String} object.
     * @param width a int.
     * @param height a int.
     * @return the new VMap
     */
    public VMap newMap(String name, String accessMode, String owner,String userModifies, int width, int height) ;

    /**
     * Close a VMap previusly opened.
     */
    public void closeMap();

    /**
     * <p>openMap</p>
     *
     * @return the default VMap, if exists
     * @throws MapsException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    public VMap openMap() throws 
            MapNotFoundException;    


    /**
     * Take the map with id in input and return it in VMap form.
     *
     * @param id a int.
     * @param refreshElems says if refresh the map's elements
     * @return the VMap with identifier id
     * @throws org.opennms.web.map.MapsException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    public VMap openMap(int id, boolean refreshElems) throws MapNotFoundException, MapsException;    

    /**
     * Take the map with id in input and return it in VMap form.
     *
     * @param id a int.
     * @param user a {@link java.lang.String} object.
     * @param refreshElems says if refresh the map's elements
     * @return the VMap with identifier id
     * @throws org.opennms.web.map.MapsException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    public VMap openMap(int id, String user, boolean refreshElems) throws MapNotFoundException, MapsException ;    

    /**
     * <p>clearMap</p>
     *
     * @throws org.opennms.web.map.MapNotFoundException if any.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public void clearMap()throws MapNotFoundException, MapsException ;
    
    /**
     * <p>getMapMenu</p>
     *
     * @param mapId a int.
     * @return a {@link org.opennms.web.map.view.VMapInfo} object.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public VMapInfo getMapMenu(int mapId) throws MapNotFoundException, MapsException ;
    
    /**
     * <p>deleteElementsOfMap</p>
     *
     * @param mapId a int.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public void deleteElementsOfMap(int mapId)throws MapsException;


    /**
     * <p>getLinks</p>
     *
     * @param elems an array of {@link org.opennms.web.map.view.VElement} objects.
     * @return a {@link java.util.List} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<VLink> getLinks(VElement[] elems) throws MapsException ;
    /**
     * <p>getLinks</p>
     *
     * @param elems a {@link java.util.Collection} object.
     * @return a {@link java.util.List} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<VLink> getLinks(Collection<VElement> elems) throws MapsException ;

    /**
     * <p>getLinksOnElem</p>
     *
     * @param elems an array of {@link org.opennms.web.map.view.VElement} objects.
     * @param elem a {@link org.opennms.web.map.view.VElement} object.
     * @return a {@link java.util.List} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<VLink> getLinksOnElem(VElement[] elems,VElement elem) throws MapsException ;
    

    /**
     * Take the maps with label like the pattern in input and return them in
     * VMap[] form.
     *
     * @param refreshElems says if refresh map's elements
     * @return the VMaps array if any label matches the pattern in input, null
     *         otherwise
     * @throws org.opennms.web.map.MapsException if any.
     * @param likeLabel a {@link java.lang.String} object.
     */
    public VMap[] getMapsLike(String likeLabel, boolean refreshElems) throws  MapsException ;

    /**
     * Take the map label and type in input and return it in VMap form.
     *
     * @param mapname a {@link java.lang.String} object.
     * @param maptype a {@link java.lang.String} object.
     * @param refreshElems says if refresh map's elements
     * @return the VMap[] with corresponding mapname and maptype
     * @throws org.opennms.web.map.MapsException if any.
     * @throws org.opennms.web.map.MapsManagementException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    public VMap[] getMap(String mapname, String maptype, boolean refreshElems) throws MapsManagementException,
            MapNotFoundException, MapsException;

    /**
     * Take the maps with name in input and return them in
     * VMap[] form.
     *
     * @param mapName a {@link java.lang.String} object.
     * @return the VMaps array if any map has name in input, null
     *         otherwise
     * @throws org.opennms.web.map.MapsException if any.
     * @param refreshElems a boolean.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    public VMap[] getMapsByName(String mapName, boolean refreshElems) throws MapNotFoundException, MapsException ;
    
    /**
     * Get all defined maps.
     *
     * @param refreshElems says if refresh maps' elements
     * @return the VMaps array containing all maps defined
     * @throws org.opennms.web.map.MapsException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    public VMap[] getAllMaps(boolean refreshElems) throws MapNotFoundException, MapsException  ;

    /**
     * Get all defined maps.
     *
     * @return the MapMenu array containing all maps defined
     * @throws org.opennms.web.map.MapsException if any.
     */
    public VMapInfo[] getAllMapMenus() throws  MapsException;    

    /**
     * Take the maps with name in input and return them in
     * MapMenu[] form.
     *
     * @param mapName a {@link java.lang.String} object.
     * @return the MapMenu array if any map has name in input, null
     *         otherwise
     * @throws org.opennms.web.map.MapsException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    public VMapInfo[] getMapsMenuByName(String mapName) throws MapNotFoundException, MapsException;
    
    /**
     * gets all visible maps for user in input
     *
     * @param user a {@link java.lang.String} object.
     * @return a List of MapMenu objects.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<VMapInfo> getVisibleMapsMenu(String user)throws MapsException;

    /**
     * gets all visible maps for user setted previousely
     *
     * @return a List of MapMenu objects.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<VMapInfo> getVisibleMapsMenu()throws MapsException;

    /**
     * Take all the maps in the tree of maps considering the with name in input
     * as the root of the tree. If there are more maps with <i>mapName</i> (case insensitive)
     * all trees with these maps as root are considered and returned.
     *
     * @param mapName a {@link java.lang.String} object.
     * @return a List with the MapMenu objects.
     * @throws org.opennms.web.map.MapsException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    public List getMapsMenuTreeByName(String mapName) throws 
            MapNotFoundException, MapsException ;
    
	/**
	 * Create a new (not child) empty Submap with the identifier setted to id.
	 *
	 * @param mapId a int.
	 * @param elementId a int.
	 * @param type the node type
	 * @return the new VElement
	 * @throws org.opennms.web.map.MapsException if any.
	 * @param x a int.
	 * @param y a int.
	 */
	public VElement newElement(int mapId, int elementId, String type, int x,int y) throws 
             MapsException;
   
	/**
	 * <p>newElement</p>
	 *
	 * @param elementId a int.
	 * @param type a {@link java.lang.String} object.
	 * @param x a int.
	 * @param y a int.
	 * @return a {@link org.opennms.web.map.view.VElement} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public VElement newElement(int elementId, String type, int x,int y) throws 
    MapsException;
	/**
	 * Create a new (not child) empty Submap with the identifier setted to id.
	 *
	 * @param mapId a int.
	 * @param elementId a int.
	 * @param type the node type
	 * @return the new VElement
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public VElement newElement(int mapId, int elementId, String type) throws MapsException ;	

	
	/**
	 * <p>newElement</p>
	 *
	 * @param elementId a int.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link org.opennms.web.map.view.VElement} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public VElement newElement(int elementId, String type) throws MapsException ;	
	/**
	 * Create a new (not child) empty Submap with the identifier setted to id.
	 *
	 * @param mapId a int.
	 * @param elementId a int.
	 * @param type the node type
	 * @return the new VElement
	 * @throws org.opennms.web.map.MapsException if any.
	 * @param iconname a {@link java.lang.String} object.
	 * @param x a int.
	 * @param y a int.
	 */
	public VElement newElement(int mapId, int elementId, String type, String iconname, int x,int y) throws 
            MapsException ;
	
	/**
	 * <p>newElement</p>
	 *
	 * @param elementId a int.
	 * @param type a {@link java.lang.String} object.
	 * @param iconname a {@link java.lang.String} object.
	 * @param x a int.
	 * @param y a int.
	 * @return a {@link org.opennms.web.map.view.VElement} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public VElement newElement(int elementId, String type, String iconname, int x,int y) throws 
    		MapsException ;	
	/**
	 * Create a new (not child) empty Submap with the identifier setted to id.
	 *
	 * @param mapId a int.
	 * @param elementId a int.
	 * @param type the node type
	 * @return the new VElement
	 * @throws org.opennms.web.map.MapsException if any.
	 * @param iconname a {@link java.lang.String} object.
	 */
	public VElement newElement(int mapId, int elementId, String type,String iconname) throws 
            MapsException;
	
    /**
     * delete the map in input
     *
     * @param map
     *            to delete
     * @throws org.opennms.web.map.MapsException
     *             if an error occour deleting map, MapNotFoundException if the
     *             map to delete doesn't exist.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    public void deleteMap(VMap map) throws MapsException,
            MapNotFoundException ;
 
    /**
     * <p>deleteMap</p>
     *
     * @throws org.opennms.web.map.MapsException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    public void deleteMap() throws MapsException,
    MapNotFoundException ;
    /**
     * delete the map with identifier id
     *
     * @throws org.opennms.web.map.MapsException if any.
     * @param mapId a int.
     */
    public void deleteMap(int mapId) throws MapsException ;

    /**
     * delete the maps in input
     *
     * @param maps
     *            to delete
     * @throws org.opennms.web.map.MapsException if any.
     */
    public void deleteMaps(VMap[] maps) throws MapsException ;

    /**
     * delete the maps with the identifiers in input
     *
     * @throws org.opennms.web.map.MapsException if any.
     * @param maps an array of int.
     */
    public void deleteMaps(int[] maps) throws MapsException ;

    /**
     * save the map in input
     *
     * @param map
     *            to save
     * @throws org.opennms.web.map.MapsException if any.
     */
    public void save(VMap map) throws MapsException ;

    /**
     * save the maps in input
     *
     * @param maps
     *            to save
     * @throws org.opennms.web.map.MapsException if any.
     */
    public void save(VMap[] maps) throws MapsException ;
    
    /**
     * delete all defined node elements in existent maps
     *
     * @throws org.opennms.web.map.MapsException if any.
     */
    public void deleteAllNodeElements()throws MapsException;
    
    /**
     * delete all defined sub maps in existent maps
     *
     * @throws org.opennms.web.map.MapsException if any.
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
     * @return List of VElement
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<VElement> refreshElements(VElement[] mapElements) throws MapsException;    

    /**
     * <p>refreshMap</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<VElement> refreshMap()throws MapsException;
    
    /**
     * <p>refreshElement</p>
     *
     * @param mapElement a {@link org.opennms.web.map.view.VElement} object.
     * @return a {@link org.opennms.web.map.view.VElement} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public VElement refreshElement(VElement mapElement) throws MapsException;    
    
    /**
     * Refreshs avail,severity and status of the map in input and its elements
     *
     * @param map a {@link org.opennms.web.map.view.VMap} object.
     * @return the map refreshed
     * @throws org.opennms.web.map.MapsException if any.
     */
    public VMap reloadMap(VMap map)throws MapsException;
    
    /**
     * TODO
     * write this method simil way to refreshElement
     * Not Yet Implemented
     *
     * @param mapLinks an array of {@link org.opennms.web.map.view.VLink} objects.
     * @return a {@link java.util.List} object.
     */
    public List refreshLinks(VLink[] mapLinks);
    
    /**
     * <p>foundLoopOnMaps</p>
     *
     * @param parentMap a {@link org.opennms.web.map.view.VMap} object.
     * @param mapId a int.
     * @return a boolean.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public boolean foundLoopOnMaps(VMap parentMap,int mapId) throws MapsException ;

    /**
     * recursively gets all nodes contained by elem and its submaps (if elem is a map)
     *
     * @param elem a {@link org.opennms.web.map.view.VElement} object.
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public Set<Integer> getNodeidsOnElement(VElement elem) throws MapsException;
    
	/**
	 * <p>getAllElementInfo</p>
	 *
	 * @return an array of {@link org.opennms.web.map.view.VElementInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public VElementInfo[] getAllElementInfo() throws MapsException;
	
	/**
	 * <p>getElementInfoLike</p>
	 *
	 * @param like a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.view.VElementInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public VElementInfo[] getElementInfoLike(String like) throws MapsException;
    /**
     * Gets all nodes on the passed map (and its submaps) with theirs occurrences
     *
     * @param map a {@link org.opennms.web.map.view.VMap} object.
     * @return HashMap<Integer, Integer> (nodeid, occurrences) containing all nodes on the passed map (and its submaps) with theirs occourrences
     * @throws org.opennms.web.map.MapsException if any.
     */
    public HashMap<Integer, Integer> getAllNodesOccursOnMap(VMap map)throws MapsException;
    

}
