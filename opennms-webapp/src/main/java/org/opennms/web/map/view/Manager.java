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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.MapsException;


/**
 * @author maurizio
 * @author antonio 
 */
public interface Manager {

    /**
     * Create a new VMap and return it
     * 
     * @param name
     * @param owner
     * @param userModifies
     * @param width 
     * @param height
     * @return the new VMap
     */
    public VMap newMap(String owner,String userModifies, int width, int height) ;

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
     * @param user
     * @param refreshElems says if refresh the map's elements
     * @return the VMap with identifier id
     * @throws MapsException
     */
    public VMap openMap(int id, String user, boolean refreshElems) throws MapNotFoundException, MapsException ;    

    public void clearMap()throws MapNotFoundException, MapsException ;

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
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param elementId
     * @param type the node type
     * @throws MapsException
     */	
	public VElement newElement(int mapid, int elementId, String type) throws MapsException ;	

	/**
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type the node type
     * @param iconnname the name of the icon
     * @param x position on the x axis
     * @param y position on the y axis
     * @return the new VElement
     * @throws MapsException
     */
	public VElement newElement(int mapId, int elementId, String type, String iconname, int x,int y) throws 
            MapsException ;
	
	/**
     * delete the map current map
     * @throws MapsException
     *             if an error occour deleting map, MapNotFoundException if the
     *             map to delete doesn't exist.
     */ 
    public void deleteMap() throws MapsException,
    MapNotFoundException ;

    /**
     * save the map in input
     * 
     * @param map
     *            to save
     * @throws MapsException
     */
    public int save(VMap map) throws MapsException ;
        
    public VMap refreshMap(VMap map) throws MapsException;
        
    /**
     * Refreshs avail,severity and status of the map in input and its elements
     * @param map 
     * @return the map refreshed
     */
    public VMap reloadMap(VMap map)throws MapsException;
        
    public boolean foundLoopOnMaps(VMap parentMap,int mapId) throws MapsException ;
    
	public List<VElementInfo> getElementInfo() throws MapsException;
	
    /**
     * Get a Map of nodelabel to the 
     * container mapids. 
     * @param user
     * @return
     */
    public Map<String,Set<Integer>> getNodeLabelToMaps(String user) throws MapsException;
    
    /**
     * 
     * @param name
     * @param owner
     * @param userModifies
     * @param width
     * @param height
     * @param velems
     * @return
     * @throws MapsException
     */
    public VMap searchMap(String owner,String userModifies, int width, int height,List<VElement> velems) throws MapsException ;
    
    public VProperties getProperties(boolean isUserAdmin) throws MapsException;
    
    public VMap addElements(VMap map, List<VElement> velems) throws MapsException;
    
    public void reloadConfig() throws MapsException;

}
