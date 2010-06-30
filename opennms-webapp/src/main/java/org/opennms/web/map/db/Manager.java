/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 17, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.map.db;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.opennms.web.map.MapsException;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VMapInfo;


/**
 * <p>Abstract Manager class.</p>
 *
 * @author maumig
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.6.12
 */
public abstract class Manager {

	
	protected boolean initialized=false;
	
	/**
	 * <p>Constructor for Manager.</p>
	 */
	public Manager(){
		
	}
	
	//public abstract void finalize() throws MapsException;

	/*
	 * All your operations on Maps must be preceeded from a startSession() call 
	 * and termined by an endSession() call. 
	 * @throws MapsException
	 */
	//public abstract void startSession() throws MapsException;

	//public abstract void endSession() throws MapsException;

	/**
	 * <p>isInitialized</p>
	 *
	 * @return a boolean.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	//public abstract boolean isStartedSession() throws MapsException ;

	/**
	 * <p>saveMaps</p>
	 *
	 * @param m an array of {@link org.opennms.web.map.db.Map} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract void saveMaps(Map[] m) throws MapsException;
	
	/**
	 * <p>saveMap</p>
	 *
	 * @param m a {@link org.opennms.web.map.db.Map} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract void saveMap(Map m) throws MapsException ;
	
	/**
	 * <p>saveElements</p>
	 *
	 * @param e an array of {@link org.opennms.web.map.db.Element} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract void saveElements(Element[] e) throws MapsException;

	/**
	 * <p>saveElement</p>
	 *
	 * @param e a {@link org.opennms.web.map.db.Element} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract void saveElement(Element e) throws MapsException;

	/**
	 * <p>deleteElements</p>
	 *
	 * @param elems an array of {@link org.opennms.web.map.db.Element} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract void deleteElements(Element[] elems) throws MapsException;

	/**
	 * <p>deleteElement</p>
	 *
	 * @param e a {@link org.opennms.web.map.db.Element} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract void deleteElement(Element e) throws MapsException;

	/**
	 * <p>deleteElement</p>
	 *
	 * @param id a int.
	 * @param mapid a int.
	 * @param type a {@link java.lang.String} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract void deleteElement(int id, int mapid, String type)throws MapsException;

	/**
	 * <p>deleteElementsOfMap</p>
	 *
	 * @param id a int.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract void deleteElementsOfMap(int id) throws MapsException;

	/**
	 * <p>deleteMap</p>
	 *
	 * @param m a {@link org.opennms.web.map.db.Map} object.
	 * @return a int.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract int deleteMap(Map m) throws MapsException;

	/**
	 * delete the map with id in input
	 *
	 * @param id a int.
	 * @return number of maps deleted
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract int deleteMap(int id) throws MapsException;

	/**
	 * <p>deleteNodeTypeElementsFromAllMaps</p>
	 *
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract void deleteNodeTypeElementsFromAllMaps() throws MapsException ;
	
	/**
	 * <p>deleteMapTypeElementsFromAllMaps</p>
	 *
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract void deleteMapTypeElementsFromAllMaps() throws MapsException ;
	

	/**
	 * <p>getElement</p>
	 *
	 * @param id a int.
	 * @param mapId a int.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link org.opennms.web.map.db.Element} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Element getElement(int id, int mapId, String type) throws MapsException ;

	/**
	 * <p>newElement</p>
	 *
	 * @param id a int.
	 * @param mapId a int.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link org.opennms.web.map.db.Element} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Element newElement(int id, int mapId, String type) throws MapsException ;

    /**
     * <p>getAllElements</p>
     *
     * @return an array of {@link org.opennms.web.map.db.Element} objects.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public abstract Element[] getAllElements() throws MapsException ;

	/**
	 * <p>getElementsOfMap</p>
	 *
	 * @param mapid a int.
	 * @return an array of {@link org.opennms.web.map.db.Element} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Element[] getElementsOfMap(int mapid) throws MapsException ;
	  
	/**
	 * <p>getNodeElementsOfMap</p>
	 *
	 * @param mapid a int.
	 * @return an array of {@link org.opennms.web.map.db.Element} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Element[] getNodeElementsOfMap(int mapid) throws MapsException ;

	/**
	 * <p>getMapElementsOfMap</p>
	 *
	 * @param mapid a int.
	 * @return an array of {@link org.opennms.web.map.db.Element} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Element[] getMapElementsOfMap(int mapid) throws MapsException ;

	/**
	 * <p>getElementsLike</p>
	 *
	 * @param elementLabel a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.db.Element} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Element[] getElementsLike(String elementLabel) throws MapsException ;


	/**
	 * get a java.util.Map<Integer,TreeSet> (key=parentMapId, value=set of child maps)
	 *
	 * @return java.util.Map<Integer,TreeSet> (key=parentMapId, value=set of child maps)
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract java.util.Map<Integer,Set<Integer>> getMapsStructure() throws MapsException ;

    /**
     * <p>countMaps</p>
     *
     * @param mapId a int.
     * @return a int.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public abstract int countMaps(int mapId) throws MapsException ;

	/**
	 * <p>getMap</p>
	 *
	 * @param id a int.
	 * @return a {@link org.opennms.web.map.db.Map} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Map getMap(int id) throws MapsException ;

	/**
	 * <p>getMaps</p>
	 *
	 * @param mapname a {@link java.lang.String} object.
	 * @param maptype a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.db.Map} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Map[] getMaps(String mapname, String maptype) throws MapsException ;

	/**
	 * <p>getAllMaps</p>
	 *
	 * @return an array of {@link org.opennms.web.map.db.Map} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Map[] getAllMaps() throws MapsException ;

    /**
     * <p>getMapsLike</p>
     *
     * @param mapLabel a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.map.db.Map} objects.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public abstract Map[] getMapsLike(String mapLabel) throws MapsException ;
	  
    /**
     * <p>getMapsByName</p>
     *
     * @param mapLabel a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.map.db.Map} objects.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public abstract Map[] getMapsByName(String mapLabel) throws MapsException ;

	/**
	 * <p>getContainerMaps</p>
	 *
	 * @param id a int.
	 * @param type a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.db.Map} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Map[] getContainerMaps(int id, String type) throws MapsException ;
	
	
	/**
	 * <p>getAllMapMenus</p>
	 *
	 * @return an array of {@link org.opennms.web.map.view.VMapInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract VMapInfo[] getAllMapMenus() throws MapsException ;
	  
	/**
	 * <p>getMapMenu</p>
	 *
	 * @param mapId a int.
	 * @return a {@link org.opennms.web.map.view.VMapInfo} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract VMapInfo getMapMenu(int mapId) throws MapsException ;
	  
	/**
	 * <p>getMapsMenuByName</p>
	 *
	 * @param mapLabel a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.view.VMapInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract VMapInfo[] getMapsMenuByName(String mapLabel) throws MapsException ;

	/**
	 * <p>getMapsMenuByOwner</p>
	 *
	 * @param owner a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.view.VMapInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract VMapInfo[] getMapsMenuByOwner(String owner) throws MapsException ;
	
	/**
	 * <p>getVisibleMapsMenu</p>
	 *
	 * @param user a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.view.VMapInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract VMapInfo[] getVisibleMapsMenu(String user) throws MapsException;
	
	/**
	 * <p>isElementInMap</p>
	 *
	 * @param elementId a int.
	 * @param mapId a int.
	 * @param type a {@link java.lang.String} object.
	 * @return a boolean.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract boolean isElementInMap(int elementId, int mapId, String type) throws MapsException ;
	  

	/**
	 * <p>getAllElementInfo</p>
	 *
	 * @return an array of {@link org.opennms.web.map.view.VElementInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract VElementInfo[] getAllElementInfo() throws MapsException ;
	
	/**
	 * <p>getElementInfoLike</p>
	 *
	 * @param like a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.view.VElementInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract VElementInfo[] getElementInfoLike(String like) throws MapsException;

	/**
	 * <p>getOutagedElements</p>
	 *
	 * @return a {@link java.util.List} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract List<VElementInfo> getOutagedElements() throws MapsException;

	/**
	 * <p>getDeletedNodes</p>
	 *
	 * @return a {@link java.util.Vector} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Vector<Integer> getDeletedNodes() throws MapsException;

	/**
	 * <p>getAvails</p>
	 *
	 * @param mapElements an array of {@link org.opennms.web.map.db.Element} objects.
	 * @return a java$util$Map object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract java.util.Map<Integer,Double> getAvails(Element[] mapElements)throws MapsException;
	
	//public abstract String getNodeLabel(int id) throws MapsException;	
	
	/**
	 * <p>getNodeidsOnElement</p>
	 *
	 * @param elem a {@link org.opennms.web.map.db.Element} object.
	 * @return a {@link java.util.Set} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Set<Integer> getNodeidsOnElement(Element elem) throws MapsException;
	
	/**
	 * <p>getNodeIdsBySource</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @return a {@link java.util.Set} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Set getNodeIdsBySource(String query)throws MapsException;
	
    /**
     * <p>getLinksOnElements</p>
     *
     * @param allnodes a {@link java.util.Set} object.
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public abstract Set<LinkInfo> getLinksOnElements(Set<Integer> allnodes) throws MapsException;
}
