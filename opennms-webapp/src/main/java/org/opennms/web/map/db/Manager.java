/*
 * Created on 4-gen-2005
 *
 */
package org.opennms.web.map.db;

import java.sql.SQLException;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.opennms.web.map.MapsException;

import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VMapInfo;


/**
 * @author maumig
 * 
 * 
 */
public abstract class Manager {

	
	protected boolean initialized=false;
	
	public Manager(){
		
	}
	
	public abstract void finalize() throws MapsException;

	/**
	 * All your operations on Maps must be preceeded from a startSession() call 
	 * and termined by an endSession() call. 
	 * @throws MapsException
	 */
	public abstract void startSession() throws MapsException;

	public abstract void endSession() throws MapsException;

	public boolean isInitialized() {
		return initialized;
	}

	public abstract boolean isStartedSession() throws MapsException ;

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
	
	public abstract VMapInfo[] getVisibleMapsMenu(String user) throws MapsException;
	
	public abstract boolean isElementInMap(int elementId, int mapId, String type) throws MapsException ;
	  

	public abstract VElementInfo[] getAllElementInfo() throws MapsException ;
	
	public abstract VElementInfo[] getElementInfoLike(String like) throws MapsException;

	public abstract List<VElementInfo> getOutagedElements() throws MapsException;

	public abstract Vector<Integer> getDeletedNodes() throws MapsException;

	public abstract java.util.Map<Integer,Double> getAvails(Element[] mapElements)throws MapsException;
	
	public abstract String getNodeLabel(int id) throws MapsException;	
	
	public abstract Set<Integer> getNodeidsOnElement(Element elem) throws MapsException;
	
	public abstract Set getNodeIdsBySource(String query)throws MapsException;
	
    public abstract Set<LinkInfo> getLinksOnElements(Set<Integer> allnodes) throws SQLException, ClassNotFoundException;
}
