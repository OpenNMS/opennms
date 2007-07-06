package org.opennms.web.map.mapd;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.MapsManagementException;
import org.opennms.web.map.config.MapStartUpConfig;

import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;
import org.opennms.web.map.view.VMapInfo;

public class ManagerMapdImpl implements Manager {

	public VElement newElement(int elementId, String type, int x, int y) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VElement newElement(int elementId, String type, String iconname, int x, int y) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VElement newElement(int elementId, String type) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VElement> refreshMap() throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public void clearMap() throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		
	}

	public void closeMap() {
		// TODO Auto-generated method stub
		
	}

	public void deleteAllMapElements() throws MapsException {
		// TODO Auto-generated method stub
		
	}

	public void deleteAllNodeElements() throws MapsException {
		// TODO Auto-generated method stub
		
	}

	public void deleteElementsOfMap(int mapId) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	public void deleteMap() throws MapsException, MapNotFoundException {
		// TODO Auto-generated method stub
		
	}

	public void deleteMap(int mapId) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	public void deleteMap(VMap map) throws MapsException, MapNotFoundException {
		// TODO Auto-generated method stub
		
	}

	public void deleteMaps(int[] maps) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	public void deleteMaps(VMap[] maps) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	public void endSession() throws MapsException {
		// TODO Auto-generated method stub
		
	}

	public boolean foundLoopOnMaps(VMap parentMap, int mapId) throws MapsException {
		// TODO Auto-generated method stub
		return false;
	}

	public VElementInfo[] getAllElementInfo() throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VMapInfo[] getAllMapMenus() throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VMap[] getAllMaps(boolean refreshElems) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<Integer, Integer> getAllNodesOccursOnMap(VMap map) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getCategories() throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VElementInfo[] getElementInfoLike(String like) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VLink> getLinks(Collection<VElement> elems) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VLink> getLinks(VElement[] elems) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VLink> getLinksOnElem(VElement[] elems, VElement elem) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VMap[] getMap(String mapname, String maptype, boolean refreshElems) throws MapsManagementException, MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VMapInfo getMapMenu(int mapId) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VMap[] getMapsByName(String mapName, boolean refreshElems) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VMap[] getMapsLike(String likeLabel, boolean refreshElems) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VMapInfo[] getMapsMenuByName(String mapName) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public List getMapsMenuTreeByName(String mapName) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public MapStartUpConfig getMapStartUpConfig() throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Integer> getNodeidsOnElement(VElement elem) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VMapInfo> getVisibleMapsMenu() throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VMapInfo> getVisibleMapsMenu(String user) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAdminMode() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUserAdmin() {
		// TODO Auto-generated method stub
		return false;
	}

	public VElement newElement(int mapId, int elementId, String type, int x, int y) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VElement newElement(int mapId, int elementId, String type, String iconname, int x, int y) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VElement newElement(int mapId, int elementId, String type, String iconname) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VElement newElement(int mapId, int elementId, String type) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VMap newMap() {
		// TODO Auto-generated method stub
		return null;
	}

	public VMap newMap(String name, String accessMode, String owner, String userModifies, int width, int height) {
		// TODO Auto-generated method stub
		return null;
	}

	public VMap openMap() throws MapNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public VMap openMap(int id, boolean refreshElems) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VMap openMap(int id, String user, boolean refreshElems) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public VElement refreshElement(VElement mapElement) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VElement> refreshElements(VElement[] mapElements) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public List refreshLinks(VLink[] mapLinks) {
		// TODO Auto-generated method stub
		return null;
	}

	public VMap reloadMap(VMap map) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	public void save(VMap map) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	public void save(VMap[] maps) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	public void setAdminMode(boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void setMapStartUpConfig(MapStartUpConfig config) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	public void startSession() throws MapsException {
		// TODO Auto-generated method stub
		
	}

	
}
