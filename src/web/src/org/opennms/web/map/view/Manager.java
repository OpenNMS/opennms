/*
 * Created on 11-gen-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

import java.sql.SQLException;

import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.MapsManagementException;
import org.opennms.web.map.db.*;

/**
 * @author maurizio
 *  
 */
public class Manager {

    org.opennms.web.map.db.Manager m_dbManager = null;

    public Manager() {
        m_dbManager = new org.opennms.web.map.db.Manager();
    }

    /**
     * Start the session: only operations made in the block between start and
     * end session will be saved correctly.
     * 
     * @throws MapsException
     * @see endSession
     */
    public void startSession() throws MapsManagementException {
        try {
            m_dbManager.startSession();
        } catch (SQLException se) {
            throw new MapsManagementException(
                    "Manager: unable to start session." + se);
        }
    }

    /**
     * Close the block open by startSession() method.
     * 
     * @throws MapsException
     * @see startSession()
     */
    synchronized public void endSession() throws MapsManagementException {
        try {
            m_dbManager.endSession();
        } catch (SQLException se) {
            throw new MapsManagementException("Manager: unable to end session."
                    + se);
        }
    }

    /**
     * Create a new empty VMap and return it.
     * 
     * @return the new VMap created.
     */
    public VMap newMap() {
        VMap m = new VMap();
        return m;
    }

    /**
     * Create a new VMap and return it
     * 
     * @param name
     * @param accessMode
     * @param owner
     * @param userModifies
     * @return the new VMap
     */
    public VMap newMap(String name, String accessMode, String owner,
            String userModifies) {
        VMap m = new VMap();
        m.setAccessMode(accessMode);
        m.setName(name);
        m.setOwner(owner);
        m.setUserLastModifies(userModifies);
        return m;
    }

    /**
     * Take the map with id in input and return it in VMap form.
     * 
     * @param id
     * @return the VMap with identifier id
     * @throws MapsException
     */
    public VMap getMap(int id) throws MapsManagementException,
            MapNotFoundException {
        VMap retVMap = null;
        try {
            Map m = Factory.getMap(id);
            if (m == null) {
                throw new MapNotFoundException("Map with id " + id
                        + " doesn't exist.");
            }
            retVMap = new VMap(id, m.getName(), m.getBackground(),
                    m.getOwner(), m.getAccessMode(), m.getUserLastModifies(), m
                            .getScale(), m.getOffsetX(), m.getOffsetY(), m
                            .getType());
            retVMap.setCreateTime(m.getCreateTime());
            retVMap.setLastModifiedTime(m.getLastModifiedTime());
            Element[] mapElems = Factory.getElementsOfMap(id);
            VElement elem;
            if(mapElems!=null){
	            for (int i = 0; i < mapElems.length; i++) {
	                elem = null;
	                if (mapElems[i].getType().equals(VElement.MAP_TYPE)) {
	                    elem = new VSubmap(mapElems[i]);
	                } else if (mapElems[i].getType().equals(VElement.NODE_TYPE)) {
	                    elem = new VNode(mapElems[i]);
	                } else {
	                    throw new RuntimeException(
	                            "Element type must be 'M' or 'N'.");
	                }
	                retVMap.addElement(elem);
	            }
            }
        } catch (SQLException se) {
            throw new MapsManagementException(
                    "Factory: unable to get map with id=" + id + "." + se);
        }
        return retVMap;
    }

    /**
     * Take the maps with label like the pattern in input and return them in
     * VMap[] form.
     * 
     * @param label
     * @return the VMaps array if any label matches the pattern in input, null
     *         otherwise
     * @throws MapsException
     */
    public VMap[] getMapsLike(String likeLabel) throws MapsManagementException,
            MapNotFoundException {
        VMap[] retVMap = null;
        try {
            Map[] m = Factory.getMapsLike(likeLabel);
            if (m == null) {
                throw new MapNotFoundException("Maps with label like "
                        + likeLabel + " don't exist.");
            }
            retVMap = new VMap[m.length];
            for (int i = 0; i < m.length; i++) {
                retVMap[i] = getMap(m[i].getId());
            }

        } catch (SQLException se) {
            throw new MapsManagementException(
                    "Factory: unable to get maps with label like " + likeLabel
                            + "." + se);
        }
        return retVMap;
    }

    /**
     * Get all defined maps.
     * 
     * @return the VMaps array containing all maps defined
     * @throws MapsException
     */
    public VMap[] getAllMaps() throws MapsManagementException,
            MapNotFoundException {
        VMap[] retVMap = null;
        try {
            Map[] m = Factory.getAllMaps();
            if (m == null) {
                throw new MapNotFoundException("No Maps found.");
            }
            retVMap = new VMap[m.length];
            for (int i = 0; i < m.length; i++) {
                retVMap[i] = getMap(m[i].getId());
            }

        } catch (SQLException se) {
            throw new MapsManagementException("Factory: unable to get all maps"
                    + "." + se);
        }
        return retVMap;
    }

    /**
     * Create a new empty VNode and return it with only the id setted.
     * 
     * @param nodeId
     * @return the new VNode
     */
    public VNode newNode(int nodeId) throws MapsException {
        VNode retVNode = new VNode(nodeId);
        return retVNode;
    }

    /**
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param id
     * @return the new VSubmap
     * @throws MapsException
     */
    public VSubmap newSubmap(int id) throws MapsManagementException,
            MapNotFoundException {
        VSubmap retVSubmap = null;
        try {
            if (Factory.countMaps(id) > 0) {
                retVSubmap = new VSubmap(id);
            } else {
                throw new MapNotFoundException(
                        "Cannot create a Submap with id " + id
                                + ". A map with this id doesn't exist.");
            }

        } catch (SQLException se) {
            throw new MapsManagementException("Cannot count maps with id " + id
                    + " \n" + se);
        }
        return retVSubmap;
    }

    /**
     * delete the map in input
     * 
     * @param map
     *            to delete
     * @throws MapsException
     *             if an error occour deleting map, MapNotFoundException if the
     *             map to delete doesn't exist.
     */
    synchronized public void deleteMap(VMap map) throws MapsException,
            MapNotFoundException {
        try {
            if (m_dbManager.deleteMap(map.getId()) == 0)
                throw new MapNotFoundException("The Map doesn't exist.");
        } catch (SQLException s) {
            throw new MapsException("Error while deleting map with id="
                    + map.getId() + "\n " + s);
        }
    }

    /**
     * delete the map with identifier id
     * 
     * @param id
     *            of the map to delete
     * @throws MapsException
     */
    synchronized public void deleteMap(int mapId) throws MapsException {
        try {
            m_dbManager.deleteMap(mapId);
        } catch (SQLException s) {
            throw new MapsException("Error while deleting map with id=" + mapId
                    + "\n " + s);
        }
    }

    /**
     * delete the maps in input
     * 
     * @param maps
     *            to delete
     * @throws MapsException
     */
    synchronized public void deleteMaps(VMap[] maps) throws MapsException {
        for (int i = 0; i < maps.length; i++) {
            deleteMap(maps[i]);
        }
    }

    /**
     * delete the maps with the identifiers in input
     * 
     * @param identifiers
     *            of the maps to delete
     * @throws MapsException
     */
    synchronized public void deleteMaps(int[] maps) throws MapsException {
        for (int i = 0; i < maps.length; i++) {
            deleteMap(maps[i]);
        }
    }

    /**
     * save the map in input
     * 
     * @param map
     *            to save
     * @throws MapsException
     */
    synchronized public void save(VMap map) throws MapsException {
        try {
            if (!map.isNew()) {
                m_dbManager.deleteElementsOfMap(map.getId());
            }
            m_dbManager.saveMap(map);
            m_dbManager.saveElements(map.getAllElements());
        } catch (SQLException se) {
            throw new MapsException("Error while saving map with id="
                    + map.getId() + "\n" + se);
        }
    }

    /**
     * save the maps in input
     * 
     * @param maps
     *            to save
     * @throws MapsException
     */
    synchronized public void save(VMap[] maps) throws MapsException {
        for (int i = 0; i < maps.length; i++) {
            save(maps[i]);
        }
    }
}
