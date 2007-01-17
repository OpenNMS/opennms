/*
 * Created on 4-gen-2005
 *
 */
package org.opennms.web.map.db;

import java.util.HashMap;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;


/**
 * @author maumig
 * 
 * 
 */
public abstract class Manager {

	
	protected boolean initialized=false;
	
	org.opennms.web.map.config.DataSource m_dataSource;
	
	java.util.Map m_params=new HashMap();

	protected Manager(org.opennms.web.map.config.DataSource dataSource) throws MapsException {
		m_dataSource=dataSource;
	}

	public Manager(org.opennms.web.map.config.DataSource dataSource, java.util.Map params)throws MapsException {
		m_dataSource=dataSource;
		m_params=params;
	}

	
	public abstract void init()throws MapsException ;

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
    
	public abstract java.util.Map getMapsStructure() throws MapsException ;

    public abstract int countMaps(int mapId) throws MapsException ;

	public abstract Map getMap(int id) throws MapsException ;

	public abstract Map[] getMaps(String mapname, String maptype) throws MapsException ;

	public abstract Map[] getAllMaps() throws MapsException ;

    public abstract Map[] getMapsLike(String mapLabel) throws MapsException ;
	  
    public abstract Map[] getMapsByName(String mapLabel) throws MapsException ;

	public abstract Map[] getContainerMaps(int id, String type) throws MapsException ;
	
	
	public abstract MapMenu[] getAllMapMenus() throws MapsException ;
	  
	public abstract MapMenu getMapMenu(int mapId) throws MapsException ;
	  
	public abstract MapMenu[] getMapsMenuByName(String mapLabel) throws MapsException ;

	public abstract MapMenu[] getMapsMenuByOwner(String owner) throws MapsException ;
	
	public abstract MapMenu[] getVisibleMapsMenu(String user, String userRole) throws MapsException;
	
	public abstract boolean isElementInMap(int elementId, int mapId, String type) throws MapsException ;
	  

	public abstract ElementInfo[] getAllElementInfo() throws MapsException ;

	
	
	public abstract VLink[] getLinks(VElement[] velems) throws MapsException;

	public abstract VLink[] getLinksOnElement(VElement[] velems,VElement elem) throws MapsException;

	
	public abstract VElement refreshElement(VElement velem) throws MapsException;
	
	public abstract VElement[] refreshElements(VElement[] velems) throws MapsException;
	
	public abstract VMap reloadMap(VMap vmap) throws MapsException;
	
	/**
	 * returns a java.util.Map <String, String> containing infos for the element of the map (key, value)
	 * @param elementId
	 * @param mapId
	 * @param type
	 * @return
	 * @throws MapsException
	 */
	public abstract java.util.Map getElementInfo(int elementId, int mapId, String type)throws MapsException;
}
