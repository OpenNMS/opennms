/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.map.db;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VMapInfo;


/**
 * <p>Abstract Manager class.</p>
 *
 * @author maumig
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class Manager {

	
	protected boolean initialized=false;
	
	/**
	 * <p>Constructor for Manager.</p>
	 */
	public Manager(){
		
	}
	
	/**
	 * <p>saveMap</p>
	 *
	 * @param m a {@link org.opennms.web.map.db.DbMap} object.
	 * @param e a {@link java.util.Collection} object.
	 * @return a int.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract int saveMap(DbMap m, Collection<DbElement> e) throws MapsException ;
	
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
	 * @return a {@link org.opennms.web.map.db.DbElement} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract DbElement getElement(int id, int mapId, String type) throws MapsException ;

	/**
	 * <p>newElement</p>
	 *
	 * @param id a int.
	 * @param mapId a int.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link org.opennms.web.map.db.DbElement} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract DbElement newElement(int id, int mapId, String type) throws MapsException ;

    /**
     * <p>getAllElements</p>
     *
     * @return an array of {@link org.opennms.web.map.db.DbElement} objects.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public abstract DbElement[] getAllElements() throws MapsException ;

	/**
	 * <p>getElementsOfMap</p>
	 *
	 * @param mapid a int.
	 * @return an array of {@link org.opennms.web.map.db.DbElement} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract DbElement[] getElementsOfMap(int mapid) throws MapsException ;
	  
	/**
	 * <p>getNodeElementsOfMap</p>
	 *
	 * @param mapid a int.
	 * @return an array of {@link org.opennms.web.map.db.DbElement} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract DbElement[] getNodeElementsOfMap(int mapid) throws MapsException ;

	/**
	 * <p>getMapElementsOfMap</p>
	 *
	 * @param mapid a int.
	 * @return an array of {@link org.opennms.web.map.db.DbElement} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract DbElement[] getMapElementsOfMap(int mapid) throws MapsException ;

	/**
	 * <p>getElementsLike</p>
	 *
	 * @param elementLabel a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.db.DbElement} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract DbElement[] getElementsLike(String elementLabel) throws MapsException ;


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
	 * @return a {@link org.opennms.web.map.db.DbMap} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract DbMap getMap(int id) throws MapsException ;

	/**
	 * <p>getMaps</p>
	 *
	 * @param mapname a {@link java.lang.String} object.
	 * @param maptype a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.db.DbMap} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract DbMap[] getMaps(String mapname, String maptype) throws MapsException ;

	/**
	 * <p>getAllMaps</p>
	 *
	 * @return an array of {@link org.opennms.web.map.db.DbMap} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract DbMap[] getAllMaps() throws MapsException ;

    /**
     * <p>getMapsLike</p>
     *
     * @param mapLabel a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.map.db.DbMap} objects.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public abstract DbMap[] getMapsLike(String mapLabel) throws MapsException ;
	  
    /**
     * <p>getMapsByName</p>
     *
     * @param mapLabel a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.map.db.DbMap} objects.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public abstract DbMap[] getMapsByName(String mapLabel) throws MapsException ;

	/**
	 * <p>getContainerMaps</p>
	 *
	 * @param id a int.
	 * @param type a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.db.DbMap} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract DbMap[] getContainerMaps(int id, String type) throws MapsException ;
	
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
	 * <p>getMapsMenuByGroup</p>
	 *
	 * @param group a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.map.view.VMapInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract VMapInfo[] getMapsMenuByGroup(String group) throws MapsException ;
	
    /**
     * <p>getMapsMenuByOther</p>
     *
     * @return an array of {@link org.opennms.web.map.view.VMapInfo} objects.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public abstract VMapInfo[] getMapsMenuByOther() throws MapsException ;	

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
	 * <p>isElementDeleted</p>
	 *
	 * @param elementId a int.
	 * @param type a {@link java.lang.String} object.
	 * @return a boolean.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract boolean isElementDeleted(int elementId, String type) throws MapsException;
	  

	/**
	 * <p>getAllElementInfo</p>
	 *
	 * @return a {@link java.util.Vector} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract List<VElementInfo> getAllElementInfo() throws MapsException ;
	
	/**
	 * <p>getAlarmedElements</p>
	 *
	 * @return a {@link java.util.List} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract List<VElementInfo> getAlarmedElements() throws MapsException;

	/**
	 * <p>getDeletedNodes</p>
	 *
	 * @return a {@link java.util.Vector} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract List<Integer> getDeletedNodes() throws MapsException;

	/**
	 * <p>getAvails</p>
	 *
	 * @param mapElements an array of {@link org.opennms.web.map.db.DbElement} objects.
	 * @return a {@link java.util.Map} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract java.util.Map<Integer,Double> getAvails(DbElement[] mapElements)throws MapsException;
		
	/**
	 * <p>getNodeidsOnElement</p>
	 *
	 * @param elem a {@link org.opennms.web.map.db.DbElement} object.
	 * @return a {@link java.util.Set} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Set<Integer> getNodeidsOnElement(DbElement elem) throws MapsException;
	
	/**
	 * <p>getNodeIdsBySource</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @return a {@link java.util.Set} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public abstract Set<Integer> getNodeIdsBySource(String query)throws MapsException;
	
    /**
     * <p>getLinksOnElements</p>
     *
     * @param allnodes a {@link java.util.Set} object.
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public abstract Set<LinkInfo> getLinksOnElements(Set<Integer> allnodes) throws MapsException;
}
