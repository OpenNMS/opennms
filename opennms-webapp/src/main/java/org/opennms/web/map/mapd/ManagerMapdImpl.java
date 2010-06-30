/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 6, 2007
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


/**
 * <p>ManagerMapdImpl class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class ManagerMapdImpl implements Manager {

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
	public VElement newElement(int elementId, String type, int x, int y) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VElement newElement(int elementId, String type, String iconname, int x, int y) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VElement newElement(int elementId, String type) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>refreshMap</p>
	 *
	 * @return a {@link java.util.List} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public List<VElement> refreshMap() throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>clearMap</p>
	 *
	 * @throws org.opennms.web.map.MapNotFoundException if any.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void clearMap() throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>closeMap</p>
	 */
	public void closeMap() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>deleteAllMapElements</p>
	 *
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void deleteAllMapElements() throws MapsException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>deleteAllNodeElements</p>
	 *
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void deleteAllNodeElements() throws MapsException {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} */
	public void deleteElementsOfMap(int mapId) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>deleteMap</p>
	 *
	 * @throws org.opennms.web.map.MapsException if any.
	 * @throws org.opennms.web.map.MapNotFoundException if any.
	 */
	public void deleteMap() throws MapsException, MapNotFoundException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>deleteMap</p>
	 *
	 * @param mapId a int.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void deleteMap(int mapId) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} */
	public void deleteMap(VMap map) throws MapsException, MapNotFoundException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>deleteMaps</p>
	 *
	 * @param maps an array of int.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void deleteMaps(int[] maps) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>deleteMaps</p>
	 *
	 * @param maps an array of {@link org.opennms.web.map.view.VMap} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void deleteMaps(VMap[] maps) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>endSession</p>
	 *
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void endSession() throws MapsException {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} */
	public boolean foundLoopOnMaps(VMap parentMap, int mapId) throws MapsException {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * <p>getAllElementInfo</p>
	 *
	 * @return an array of {@link org.opennms.web.map.view.VElementInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public VElementInfo[] getAllElementInfo() throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>getAllMapMenus</p>
	 *
	 * @return an array of {@link org.opennms.web.map.view.VMapInfo} objects.
	 * @throws org.opennms.web.map.MapNotFoundException if any.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public VMapInfo[] getAllMapMenus() throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VMap[] getAllMaps(boolean refreshElems) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public HashMap<Integer, Integer> getAllNodesOccursOnMap(VMap map) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>getCategories</p>
	 *
	 * @return a {@link java.util.List} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public List<String> getCategories() throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VElementInfo[] getElementInfoLike(String like) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public List<VLink> getLinks(Collection<VElement> elems) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>getLinks</p>
	 *
	 * @param elems an array of {@link org.opennms.web.map.view.VElement} objects.
	 * @return a {@link java.util.List} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public List<VLink> getLinks(VElement[] elems) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public List<VLink> getLinksOnElem(VElement[] elems, VElement elem) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VMap[] getMap(String mapname, String maptype, boolean refreshElems) throws MapsManagementException, MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VMapInfo getMapMenu(int mapId) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VMap[] getMapsByName(String mapName, boolean refreshElems) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VMap[] getMapsLike(String likeLabel, boolean refreshElems) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VMapInfo[] getMapsMenuByName(String mapName) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public List getMapsMenuTreeByName(String mapName) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>getMapStartUpConfig</p>
	 *
	 * @return a {@link org.opennms.web.map.config.MapStartUpConfig} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public MapStartUpConfig getMapStartUpConfig() throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public Set<Integer> getNodeidsOnElement(VElement elem) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>getVisibleMapsMenu</p>
	 *
	 * @return a {@link java.util.List} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public List<VMapInfo> getVisibleMapsMenu() throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public List<VMapInfo> getVisibleMapsMenu(String user) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>isAdminMode</p>
	 *
	 * @return a boolean.
	 */
	public boolean isAdminMode() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * <p>isUserAdmin</p>
	 *
	 * @return a boolean.
	 */
	public boolean isUserAdmin() {
		// TODO Auto-generated method stub
		return false;
	}

	/** {@inheritDoc} */
	public VElement newElement(int mapId, int elementId, String type, int x, int y) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VElement newElement(int mapId, int elementId, String type, String iconname, int x, int y) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VElement newElement(int mapId, int elementId, String type, String iconname) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VElement newElement(int mapId, int elementId, String type) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>newMap</p>
	 *
	 * @return a {@link org.opennms.web.map.view.VMap} object.
	 */
	public VMap newMap() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VMap newMap(String name, String accessMode, String owner, String userModifies, int width, int height) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>openMap</p>
	 *
	 * @return a {@link org.opennms.web.map.view.VMap} object.
	 * @throws org.opennms.web.map.MapNotFoundException if any.
	 */
	public VMap openMap() throws MapNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VMap openMap(int id, boolean refreshElems) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VMap openMap(int id, String user, boolean refreshElems) throws MapNotFoundException, MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VElement refreshElement(VElement mapElement) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>refreshElements</p>
	 *
	 * @param mapElements an array of {@link org.opennms.web.map.view.VElement} objects.
	 * @return a {@link java.util.List} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public List<VElement> refreshElements(VElement[] mapElements) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>refreshLinks</p>
	 *
	 * @param mapLinks an array of {@link org.opennms.web.map.view.VLink} objects.
	 * @return a {@link java.util.List} object.
	 */
	public List refreshLinks(VLink[] mapLinks) {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public VMap reloadMap(VMap map) throws MapsException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>save</p>
	 *
	 * @param map a {@link org.opennms.web.map.view.VMap} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void save(VMap map) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>save</p>
	 *
	 * @param maps an array of {@link org.opennms.web.map.view.VMap} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void save(VMap[] maps) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} */
	public void setAdminMode(boolean mode) {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} */
	public void setMapStartUpConfig(MapStartUpConfig config) throws MapsException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>startSession</p>
	 *
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void startSession() throws MapsException {
		// TODO Auto-generated method stub
		
	}

	
}
