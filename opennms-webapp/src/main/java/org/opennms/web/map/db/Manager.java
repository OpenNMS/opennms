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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.opennms.web.map.MapsException;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VMapInfo;


/**
 * @author maumig
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public abstract class Manager {

	
	protected boolean initialized=false;
	
	public Manager(){
		
	}
	
	public abstract int saveMap(DbMap m, Collection<DbElement> e) throws MapsException ;
	
	/**
	 * delete the map with id in input
	 * @param id
	 * @return number of maps deleted
	 * @throws MapsException
	 */
	public abstract int deleteMap(int id) throws MapsException;

	public abstract void deleteNodeTypeElementsFromAllMaps() throws MapsException ;
	
	public abstract void deleteMapTypeElementsFromAllMaps() throws MapsException ;
	

	public abstract DbElement getElement(int id, int mapId, String type) throws MapsException ;

	public abstract DbElement newElement(int id, int mapId, String type) throws MapsException ;

    public abstract DbElement[] getAllElements() throws MapsException ;

	public abstract DbElement[] getElementsOfMap(int mapid) throws MapsException ;
	  
	public abstract DbElement[] getNodeElementsOfMap(int mapid) throws MapsException ;

	public abstract DbElement[] getMapElementsOfMap(int mapid) throws MapsException ;

	public abstract DbElement[] getElementsLike(String elementLabel) throws MapsException ;


	/**
	 * get a java.util.Map<Integer,TreeSet> (key=parentMapId, value=set of child maps)
	 * @return java.util.Map<Integer,TreeSet> (key=parentMapId, value=set of child maps)
	*/
    
	public abstract java.util.Map<Integer,Set<Integer>> getMapsStructure() throws MapsException ;

    public abstract int countMaps(int mapId) throws MapsException ;

	public abstract DbMap getMap(int id) throws MapsException ;

	public abstract DbMap[] getMaps(String mapname, String maptype) throws MapsException ;

	public abstract DbMap[] getAllMaps() throws MapsException ;

    public abstract DbMap[] getMapsLike(String mapLabel) throws MapsException ;
	  
    public abstract DbMap[] getMapsByName(String mapLabel) throws MapsException ;

	public abstract DbMap[] getContainerMaps(int id, String type) throws MapsException ;
	
	public abstract VMapInfo[] getAllMapMenus() throws MapsException ;
	  
	public abstract VMapInfo getMapMenu(int mapId) throws MapsException ;
	  
	public abstract VMapInfo[] getMapsMenuByName(String mapLabel) throws MapsException ;

	public abstract VMapInfo[] getMapsMenuByOwner(String owner) throws MapsException ;

	public abstract VMapInfo[] getMapsMenuByGroup(String group) throws MapsException ;
	
    public abstract VMapInfo[] getMapsMenuByOther() throws MapsException ;	

	public abstract boolean isElementInMap(int elementId, int mapId, String type) throws MapsException ;
	
	public abstract boolean isElementNotDeleted(int elementId, String type) throws MapsException;
	  

	public abstract Vector<VElementInfo> getAllElementInfo() throws MapsException ;
	
	public abstract List<VElementInfo> getAlarmedElements() throws MapsException;

	public abstract Vector<Integer> getDeletedNodes() throws MapsException;

	public abstract java.util.Map<Integer,Double> getAvails(DbElement[] mapElements)throws MapsException;
		
	public abstract Set<Integer> getNodeidsOnElement(DbElement elem) throws MapsException;
	
	public abstract Set<Integer> getNodeIdsBySource(String query)throws MapsException;
	
    public abstract Set<LinkInfo> getLinksOnElements(Set<Integer> allnodes) throws MapsException;
}
