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
 * @author maumig
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public abstract class Manager {

	
	protected boolean initialized=false;
	
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

	public boolean isInitialized() {
		return initialized;
	}

	//public abstract boolean isStartedSession() throws MapsException ;

	public abstract void saveMaps(Map[] m) throws MapsException;
	
	public abstract void saveMap(Map m) throws MapsException ;
	
	public abstract void saveElements(Element[] e) throws MapsException;

	public abstract void saveElement(Element e) throws MapsException;

	public abstract void deleteElements(Element[] elems) throws MapsException;

	public abstract void deleteElement(Element e) throws MapsException;

	public abstract void deleteElement(int id, int mapid, String type)throws MapsException;

	public abstract void deleteElementsOfMap(int id) throws MapsException;

	public abstract int deleteMap(Map m) throws MapsException;

	/**
	 * delete the map with id in input
	 * @param id
	 * @return number of maps deleted
	 * @throws MapsException
	 */
	public abstract int deleteMap(int id) throws MapsException;

	public abstract void deleteNodeTypeElementsFromAllMaps() throws MapsException ;
	
	public abstract void deleteMapTypeElementsFromAllMaps() throws MapsException ;
	

	public abstract Element getElement(int id, int mapId, String type) throws MapsException ;

	public abstract Element newElement(int id, int mapId, String type) throws MapsException ;

    public abstract Element[] getAllElements() throws MapsException ;

	public abstract Element[] getElementsOfMap(int mapid) throws MapsException ;
	  
	public abstract Element[] getNodeElementsOfMap(int mapid) throws MapsException ;

	public abstract Element[] getMapElementsOfMap(int mapid) throws MapsException ;

	public abstract Element[] getElementsLike(String elementLabel) throws MapsException ;


	/**
	 * get a java.util.Map<Integer,TreeSet> (key=parentMapId, value=set of child maps)
	 * @return java.util.Map<Integer,TreeSet> (key=parentMapId, value=set of child maps)
	*/
    
	public abstract java.util.Map<Integer,Set<Integer>> getMapsStructure() throws MapsException ;

    public abstract int countMaps(int mapId) throws MapsException ;

	public abstract Map getMap(int id) throws MapsException ;

	public abstract Map[] getMaps(String mapname, String maptype) throws MapsException ;

	public abstract Map[] getAllMaps() throws MapsException ;

    public abstract Map[] getMapsLike(String mapLabel) throws MapsException ;
	  
    public abstract Map[] getMapsByName(String mapLabel) throws MapsException ;

	public abstract Map[] getContainerMaps(int id, String type) throws MapsException ;
	
	public abstract VMapInfo[] getAllMapMenus() throws MapsException ;
	  
	public abstract VMapInfo getMapMenu(int mapId) throws MapsException ;
	  
	public abstract VMapInfo[] getMapsMenuByName(String mapLabel) throws MapsException ;

	public abstract VMapInfo[] getMapsMenuByOwner(String owner) throws MapsException ;

	public abstract VMapInfo[] getMapsMenuByGroup(String group) throws MapsException ;
	
    public abstract VMapInfo[] getMapsMenuByOther() throws MapsException ;	

	public abstract boolean isElementInMap(int elementId, int mapId, String type) throws MapsException ;
	
	public abstract boolean isElementNotDeleted(int elementId, String type) throws MapsException;
	  

	public abstract VElementInfo[] getAllElementInfo() throws MapsException ;
	
	public abstract VElementInfo[] getElementInfoLike(String like) throws MapsException;

	public abstract List<VElementInfo> getAlarmedElements() throws MapsException;

	public abstract Vector<Integer> getDeletedNodes() throws MapsException;

	public abstract java.util.Map<Integer,Double> getAvails(Element[] mapElements)throws MapsException;
	
	//public abstract String getNodeLabel(int id) throws MapsException;	
	
	public abstract Set<Integer> getNodeidsOnElement(Element elem) throws MapsException;
	
	public abstract Set<Integer> getNodeIdsBySource(String query)throws MapsException;
	
    public abstract Set<LinkInfo> getLinksOnElements(Set<Integer> allnodes) throws MapsException;
}
