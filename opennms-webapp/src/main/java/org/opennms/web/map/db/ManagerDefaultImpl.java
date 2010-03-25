//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2009 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Apr: refactoring to support ACL DAO work
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.web.map.db;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Category;

import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.config.CatFactory;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.model.OnmsSeverity;

import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.MapsManagementException;
import org.opennms.web.map.config.MapPropertiesFactory;
import org.opennms.web.map.db.datasources.DataSourceInterface;

import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;
import org.opennms.web.map.view.VMapInfo;
import org.opennms.web.map.view.VProperties;

/**
 * @author maurizio
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public class ManagerDefaultImpl implements Manager {

    private class AlarmInfo {
        int status;
        float severity;

        public float getSeverity() {
            return severity;
        }

        public void setSeverity(float severity) {
            this.severity = severity;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        AlarmInfo(int status, float severity) {
            super();
            this.status = status;
            this.severity = severity;
        }

    }

    org.opennms.web.map.db.Manager dbManager = null;

    MapPropertiesFactory mapsPropertiesFactory = null;

    DataSourceInterface dataSource = null;

    private GroupDao m_groupDao;

    String filter = null;

    VMap sessionMap = null;

    VMap searchMap = null;

    boolean adminMode = false;

    private Category log = null;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public DataSourceInterface getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSourceInterface dataSource) {
        this.dataSource = dataSource;
    }

    public org.opennms.web.map.db.Manager getDbManager() {
        return dbManager;
    }

    public void setDbManager(org.opennms.web.map.db.Manager dbManager) {
        this.dbManager = dbManager;
    }

    public org.opennms.web.map.config.MapPropertiesFactory getMapsPropertiesFactory() {
        return mapsPropertiesFactory;
    }

    public void setMapsPropertiesFactory(
            org.opennms.web.map.config.MapPropertiesFactory mapsPropertiesFactory) {
        this.mapsPropertiesFactory = mapsPropertiesFactory;
    }

    private List<String> getCategories() throws MapsException {
        List<String> categories = new ArrayList<String>();
        try {
            CategoryFactory.init();
        } catch (Exception e) {
            throw new MapsException("Error while getting categories.", e);
        }
        CatFactory cf = CategoryFactory.getInstance();
        Catinfo cinfo = cf.getConfig();
        Enumeration<Categorygroup> catGroupEnum = cinfo.enumerateCategorygroup();
        log.debug("Get categories:");
        while (catGroupEnum.hasMoreElements()) {
            Categorygroup cg = catGroupEnum.nextElement();
            Enumeration<org.opennms.netmgt.config.categories.Category> catEnum = cg.getCategories().enumerateCategory();
            while (catEnum.hasMoreElements()) {
                org.opennms.netmgt.config.categories.Category category = catEnum.nextElement();
                String categoryName = unescapeHtmlChars(category.getLabel());
                log.debug(categoryName);
                categories.add(categoryName);
            }
        }
        return categories;
    }

    /**
     * Manage Maps using default implementation of Factory and Manager
     */
    public ManagerDefaultImpl() throws MapsException {
        ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(this.getClass());
        if (log.isDebugEnabled()) {
            log.debug("Instantiating ManagerDefaultImpl");
        }
    }

    public VMap openMap() throws MapNotFoundException {
        if (sessionMap != null) {
            return sessionMap;
        }
        throw new MapNotFoundException();
    }

    public void clearMap() throws MapNotFoundException, MapsException {
        if (sessionMap == null) {
            throw new MapNotFoundException();
        }
        sessionMap.removeAllLinks();
        sessionMap.removeAllElements();
    }

    public void deleteMap() throws MapNotFoundException, MapsException {
        deleteMap(sessionMap.getId());
    }

    public void closeMap() {
        sessionMap = null;
    }

    public VMap openMap(int id, String user, boolean refreshElems)
            throws MapsManagementException, MapNotFoundException,
            MapsException {

        if (id == MapsConstants.SEARCH_MAP) {
            sessionMap = searchMap;
            return sessionMap;
        }
        List<VMapInfo> visibleMaps = getMapsMenuByuser(user);

        Iterator<VMapInfo> it = visibleMaps.iterator();
        while (it.hasNext()) {
            VMapInfo mapMenu = it.next();
            if (mapMenu.getId() == id) {
                sessionMap = open(id, refreshElems);
                return sessionMap;
            }
        }

        throw new MapNotFoundException();
    }

    /**
     * Create a new VMap and return it
     * 
     * @param name
     * @param accessMode
     * @param owner
     * @param userModifies
     * @param width
     * @param height
     * @return the new VMap
     */
    public VMap newMap(String owner,
            String userModifies, int width, int height) {
        VMap m = new VMap(MapsConstants.NEW_MAP_NAME);
        m.setOwner(owner);
        m.setUserLastModifies(userModifies);
        m.setWidth(width);
        m.setHeight(height);
        m.setId((MapsConstants.NEW_MAP));
        m.setBackground(mapsPropertiesFactory.getDefaultBackgroundColor());
        m.setAccessMode(MapsConstants.ACCESS_MODE_ADMIN);
        m.setType(MapsConstants.USER_GENERATED_MAP);
        sessionMap = m;
        return m;
    }

    /**
     * Take the map with id in input and return it in VMap form.
     * 
     * @param id
     * @param refreshElems
     *            says if refresh the map's elements
     * @return the VMap with identifier id
     * @throws MapsException
     */
    public VMap openMap(int id, boolean refreshElems)
            throws MapsManagementException, MapNotFoundException,
            MapsException {
        return open(id, refreshElems);
    }

    private VMap open(int id, boolean refreshElems)
            throws MapsManagementException, MapNotFoundException,
            MapsException {
        VMap retVMap = null;

        DbMap m = dbManager.getMap(id);
        if (m == null) {
            throw new MapNotFoundException("Map with id " + id
                    + " doesn't exist.");
        }
        retVMap = new VMap(id, m.getName(), m.getBackground(), m.getOwner(),
                           m.getAccessMode(), m.getUserLastModifies(),
                           m.getScale(), m.getOffsetX(), m.getOffsetY(),
                           m.getType(), m.getWidth(), m.getHeight());
        retVMap.setCreateTime(m.getCreateTime());
        retVMap.setLastModifiedTime(m.getLastModifiedTime());
        DbElement[] mapElems = dbManager.getElementsOfMap(id);
        VElement elem = null;
        if (mapElems != null) {
            for (DbElement mapElem : mapElems) {
                elem = new VElement(mapElem);
                elem.setSeverity(mapsPropertiesFactory.getIndeterminateSeverity().getId());
                elem.setStatus(mapsPropertiesFactory.getUnknownStatus().getId());
                elem.setAvail(mapsPropertiesFactory.getDisabledAvail().getMin());
                // here we must add all the stuff required
                log.debug("openMap: adding element to map with label: "
                        + elem.getLabel());
                retVMap.addElement(elem);
            }
        }

        if (refreshElems) {
            log.debug("Starting refreshing elems for map with id " + id);
            for (VElement changedElem : localRefreshElements(retVMap.getElements().values())) {
                retVMap.removeElement(changedElem.getId(),
                                      changedElem.getType());
                retVMap.addElement(changedElem);
            }
        }

        log.debug("Starting adding links for map with id " + id);
        retVMap.addLinks(getLinks(retVMap.getElements().values()));
        log.debug("Ending adding links for map with id " + id);
        sessionMap = retVMap;
        return retVMap;
    }

    private List<VElement> localRefreshElements(
            Collection<VElement> mapElements) throws MapsException {
        List<VElement> elems = new ArrayList<VElement>();
        Vector<Integer> deletedNodeids = dbManager.getDeletedNodes();
        java.util.Map<Integer, AlarmInfo> outagedNodes = getAlarmedNodes();
        java.util.Map<Integer, Double> avails = dbManager.getAvails(mapElements.toArray(new VElement[0]));
        Set<Integer> nodesBySource = new HashSet<Integer>();
        if (dataSource != null) {
            nodesBySource = dbManager.getNodeIdsBySource(filter);
        }

        for (VElement mapElement : mapElements) {
            elems.add(refresh(mapElement, nodesBySource, deletedNodeids,
                         outagedNodes, avails));
        }

        return elems;
    }

    public VMapInfo getMapMenu(int mapId) throws MapNotFoundException,
            MapsException {
        VMapInfo m = null;
        m = dbManager.getMapMenu(mapId);
        if (m == null) {
            throw new MapNotFoundException("No Maps found.");
        }
        return m;
    }

    /**
     * Take the maps with label like the pattern in input and return them in
     * VMap[] form.
     * 
     * @param label
     * @param refreshElems
     *            says if refresh map's elements
     * @return the VMaps array if any label matches the pattern in input, null
     *         otherwise
     * @throws MapsException
     */
    public VMap[] getMapsLike(String likeLabel, boolean refreshElems)
            throws MapsException {
        VMap[] retVMap = null;
        DbMap[] m = dbManager.getMapsLike(likeLabel);
        if (m == null) {
            throw new MapNotFoundException("Maps with label like "
                    + likeLabel + " don't exist.");
        }
        retVMap = new VMap[m.length];
        for (int i = 0; i < m.length; i++) {
            retVMap[i] = openMap(m[i].getId(), refreshElems);
        }
        return retVMap;
    }

    /**
     * Take the maps with name in input and return them in VMap[] form.
     * 
     * @param mapName
     * @param refhresElems
     *            says if refresh maps' elements
     * @return the VMaps array if any map has name in input, null otherwise
     * @throws MapsException
     */
    public VMap[] getMapsByName(String mapName, boolean refreshElems)
            throws MapNotFoundException, MapsException {
        VMap[] retVMap = null;
        DbMap[] m = dbManager.getMapsByName(mapName);
        if (m == null) {
            throw new MapNotFoundException("Maps with name " + mapName
                    + " don't exist.");
        }
        retVMap = new VMap[m.length];
        for (int i = 0; i < m.length; i++) {
            retVMap[i] = openMap(m[i].getId(), refreshElems);
        }
        return retVMap;
    }

    /**
     * Get all defined maps.
     * 
     * @param refreshElems
     *            says if refresh maps' elements
     * @return the VMaps array containing all maps defined
     * @throws MapsException
     */
    public VMap[] getAllMaps(boolean refreshElems)
            throws MapNotFoundException, MapsException {
        VMap[] retVMap = null;
        DbMap[] m = dbManager.getAllMaps();
        if (m == null) {
            throw new MapNotFoundException("No Maps found.");
        }
        retVMap = new VMap[m.length];
        for (int i = 0; i < m.length; i++) {
            retVMap[i] = openMap(m[i].getId(), refreshElems);
        }
        return retVMap;
    }

    /**
     * Get all defined maps.
     * 
     * @return the MapMenu array containing all maps defined
     * @throws MapsException
     */

    public VMapInfo[] getAllMapMenus() throws MapsException {
        VMapInfo[] m = null;
        m = dbManager.getAllMapMenus();
        return m;

    }

    /**
     * Take the maps with name in input and return them in MapMenu[] form.
     * 
     * @param mapName
     * @return the MapMenu array if any map has name in input, null otherwise
     * @throws MapsException
     */
    public VMapInfo[] getMapsMenuByName(String mapName)
            throws MapNotFoundException, MapsException {
        VMapInfo[] retVMap = null;
        retVMap = dbManager.getMapsMenuByName(mapName);
        if (retVMap == null) {
            throw new MapNotFoundException("Maps with name " + mapName
                    + " don't exist.");
        }
        return retVMap;
    }

    public VMapInfo getDefaultMapsMenu(String user) throws MapsException {

        Iterator<Group> ite = getGroupDao().findGroupsForUser(user).iterator();

        while (ite.hasNext()) {
            Group group = ite.next();
            log.debug("getDefaultMapsMenu: found group: " + group.getName()
                    + " for user:" + user);
            if (group.getDefaultMap() != null) {
                log.debug("getDefaultMapsMenu: found default map: "
                        + group.getDefaultMap() + " for group: "
                        + group.getName());
                VMapInfo[] vmapsinfo = dbManager.getMapsMenuByName(group.getDefaultMap());
                if (vmapsinfo != null) {
                    log.debug("getDefaultMapsMenu: found " + vmapsinfo.length
                            + " maps. Verify access ");
                    for (int i = 0; i < vmapsinfo.length; i++) {
                        if (vmapsinfo[i].getOwner().equals(user)) {
                            log.info("getDefaultMapsMenu: found! user: "
                                    + user + " owns the map");
                            return vmapsinfo[i];
                        } else {
                            DbMap map = dbManager.getMap(vmapsinfo[i].getId());
                            log.debug("getDefaultMapsMenu: map: "
                                    + map.getId() + " mapName: "
                                    + map.getName() + " Access: "
                                    + map.getAccessMode() + " Group: "
                                    + map.getGroup());
                            if (map.getAccessMode().trim().toUpperCase().equals(
                                                                                MapsConstants.ACCESS_MODE_ADMIN.toUpperCase())
                                    || map.getAccessMode().trim().toUpperCase().equals(
                                                                                       MapsConstants.ACCESS_MODE_USER.toUpperCase())
                                    || (map.getAccessMode().trim().toUpperCase().equals(
                                                                                        MapsConstants.ACCESS_MODE_GROUP.toUpperCase()) && map.getGroup().equals(
                                                                                                                                                      group.getName()))) {
                                log.info("getDefaultMapsMenu: found! user: "
                                        + user + " has access to map: "
                                        + map.getName() + " with id: "
                                        + map.getId());
                                return vmapsinfo[i];
                            } else {
                                log.info("getDefaultMapsMenu: access is denied for default map: "
                                        + group.getDefaultMap()
                                        + " to group: " + group.getName());
                            }
                        }
                    }
                } else {
                    log.info("getDefaultMapsMenu: no maps found for default map: "
                            + group.getDefaultMap()
                            + " for group: "
                            + group.getName());
                }
            }
        }
        return new VMapInfo(MapsConstants.NEW_MAP, "no default map found",
                            user);
    }

    /**
     * gets all visible maps for user and userRole in input
     * 
     * @param user
     * @param userRole
     * @return a List of MapMenu objects.
     * @throws MapsException
     */
    public List<VMapInfo> getVisibleMapsMenu(String user)
            throws MapsException {
        return getMapsMenuByuser(user);
    }

    private List<VMapInfo> getMapsMenuByuser(String user)
            throws MapsException {

        List<VMapInfo> maps = new ArrayList<VMapInfo>();
        List<Integer> mapsIds = new ArrayList<Integer>();

        VMapInfo[] mapsbyother = dbManager.getMapsMenuByOther();
        if (mapsbyother != null) {
            for (VMapInfo element : mapsbyother) {
                maps.add(element);
                mapsIds.add(element.getId());
            }
        }

        VMapInfo[] mapsbyowner = dbManager.getMapsMenuByOwner(user);
        if (mapsbyowner != null) {
            for (int i = 0; i < mapsbyowner.length; i++) {
                if (!mapsIds.contains(mapsbyowner[i].getId())) {
                    maps.add(mapsbyowner[i]);
                    mapsIds.add(mapsbyowner[i].getId());
                }
            }
        }

        Iterator<Group> ite = getGroupDao().findGroupsForUser(user).iterator();
        while (ite.hasNext()) {
            VMapInfo[] mapsbygroup = dbManager.getMapsMenuByGroup(ite.next().getName());
            if (mapsbygroup != null) {
                for (int i = 0; i < mapsbygroup.length; i++) {
                    if (!mapsIds.contains(mapsbygroup[i].getId())) {
                        maps.add(mapsbygroup[i]);
                        mapsIds.add(mapsbygroup[i].getId());
                    }
                }
            }
        }
        return maps;
    }

    /**
     * Take all the maps in the tree of maps considering the with name in
     * input as the root of the tree. If there are more maps with
     * <i>mapName</i> (case insensitive) all trees with these maps as root are
     * considered and returned.
     * 
     * @param mapName
     * @return a List with the MapMenu objects.
     * @throws MapsException
     */
    public List<VMapInfo> getMapsMenuTreeByName(String mapName)
            throws MapNotFoundException, MapsException {
        List<VMapInfo> mapsInTreesList = new ArrayList<VMapInfo>();
        //
        VMapInfo[] mapsMenu = null;
        try {
            mapsMenu = getMapsMenuByName(mapName);
        } catch (MapNotFoundException mnf) {
            // do nothing...
        }
        if (mapsMenu != null) {
            // find all accessible maps for the user,
            // for all maps (and theirs tree of maps) with name like mapName.
            for (VMapInfo element : mapsMenu) {
                // build a map in wich each entry is [mapparentid,
                // listofchildsids]
                java.util.Map<Integer, Set<Integer>> parent_child = dbManager.getMapsStructure();
                List<Integer> childList = new ArrayList<Integer>();

                preorderVisit(new Integer(element.getId()), childList,
                              parent_child);

                for (int i = 0; i < childList.size(); i++) {
                    preorderVisit(childList.get(i), childList, parent_child);
                }

                // adds all sub-tree of maps to the visible map list
                for (int i = 0; i < childList.size(); i++) {
                    mapsInTreesList.add(getMapMenu(childList.get(i).intValue()));
                }
            }
        }
        return mapsInTreesList;
    }

    private void preorderVisit(Integer rootElem, List<Integer> treeElems,
            java.util.Map<Integer, Set<Integer>> maps) {
        Set<Integer> childs = maps.get(rootElem);
        if (!treeElems.contains(rootElem)) {
            treeElems.add(rootElem);
        }

        if (childs != null) {
            Iterator<Integer> it = childs.iterator();
            while (it.hasNext()) {
                Integer child = it.next();
                if (!treeElems.contains(child)) {
                    treeElems.add(child);
                }
                preorderVisit(child, treeElems, maps);
            }
        }
    }

    /**
     * Create a new VElement with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type
     *            the node type
     * @param x
     * @param y
     * @return the new VElement
     * @throws MapsException
     */
    public VElement newElement(int mapId, int elementId, String type, int x,
            int y) throws MapsException {

        VElement velem = newElement(mapId, elementId, type);
        velem.setX(x);
        velem.setY(y);
        return velem;
    }

    public VElement newElement(int elementId, String type, int x, int y)
            throws MapsException {
        if (sessionMap == null) {
            throw new MapNotFoundException("session map in null");
        }
        return newElement(sessionMap.getId(), elementId, type, x, y);
    }

    /**
     * Create a new element child of the map with mapId (this map must be the
     * sessionMap)
     * 
     * @param mapId
     * @param elementId
     * @param type
     *            the node type
     * @return the new VElement
     * @throws MapsException
     */
    public VElement newElement(int mapId, int elementId, String type)
            throws MapsException {
        VElement velem = new VElement(dbManager.newElement(elementId, mapId, type));
        
        if (velem.isNode())
            velem.setIcon(getIconBySysoid(velem.getSysoid()));
        else if (velem.isMap())
            velem.setIcon(mapsPropertiesFactory.getDefaultMapIcon());

        velem.setSeverity(mapsPropertiesFactory.getIndeterminateSeverity().getId());
        velem.setAvail(mapsPropertiesFactory.getUndefinedAvail().getMin());
        velem.setStatus(mapsPropertiesFactory.getUnknownStatus().getId());
        log.debug("Adding velement to map " + velem.toString());

        return velem;

    }

    private String getIconBySysoid(String sysoid) throws MapsException {
        try {
            java.util.Map<String, String> iconsBySysoid = mapsPropertiesFactory.getIconsBySysoid();
            if (iconsBySysoid != null) {
                log.debug("getIconBySysoid: sysoid = " + sysoid);
                for (String key : iconsBySysoid.keySet()) {
                    log.debug("getIconBySysoid: key = " + key);
                    if (key.equals(sysoid)) {
                        log.debug("getIconBySysoid: iconBySysoid = "
                                + iconsBySysoid.get(key));
                        return iconsBySysoid.get(key);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception while getting icons by sysoid");
            throw new MapsException(e);
        }
        return mapsPropertiesFactory.getDefaultNodeIcon();
    }

    /**
     * Create a new element child of the map with mapId (this map must be the
     * sessionMap).
     * 
     * @param mapId
     * @param elementId
     * @param type
     *            the node type
     * @return the new VElement
     * @throws MapsException
     */
    public VElement newElement(int mapId, int elementId, String type,
            String iconname, int x, int y) throws MapsException {
        VElement velem = newElement(mapId, elementId, type);
        if (iconname == null ) {
            if (type == MapsConstants.MAP_TYPE)
                iconname = mapsPropertiesFactory.getDefaultMapIcon();
            else 
                iconname = mapsPropertiesFactory.getDefaultNodeIcon();
        }
        velem.setIcon(iconname);
        velem.setX(x);
        velem.setY(y);
        return velem;

    }

    /**
     * Create a new (not child) empty Submap with the identifier setted to id.
     * 
     * @param mapId
     * @param elementId
     * @param type
     *            the node type
     * @return the new VElement
     * @throws MapsException
     */
    public VElement newElement(int mapId, int elementId, String type,
            String iconname) throws MapsException {
        VElement elem = newElement(mapId, elementId, type);
        elem.setIcon(iconname);
        return elem;
    }

    /**
     * delete the map in input
     * 
     * @param map
     *            to delete
     * @throws MapsException
     *             if an error occour deleting map, MapNotFoundException if
     *             the map to delete doesn't exist.
     */
    synchronized public void deleteMap(VMap map) throws MapsException,
            MapNotFoundException {
        deleteMap(map.getId());
    }

    /**
     * delete the map with identifier id
     * 
     * @param id
     *            of the map to delete
     * @throws MapsException
     */
    synchronized public void deleteMap(int mapId) throws MapsException {
        if (sessionMap == null) {
            throw new MapNotFoundException("session map in null");
        }
        if (sessionMap.getId() != mapId) {
            throw new MapsException(
                                    "No current session map: cannot delete map with id "
                                            + mapId);
        }
        if (dbManager.deleteMap(mapId) == 0) {
            throw new MapNotFoundException("Map with id " + mapId
                    + " doesn't exist or is automatic map");
        }
        sessionMap = null;
    }

    /**
     * delete the maps in input
     * 
     * @param maps
     *            to delete
     * @throws MapsException
     */
    synchronized public void deleteMaps(VMap[] maps) throws MapsException {
        for (VMap map : maps) {
            deleteMap(map);
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
        for (int map : maps) {
            deleteMap(map);
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
        Collection<DbElement> dbe = new ArrayList<DbElement>();
        for (VElement velem : map.getElements().values()) {
            dbe.add(new DbElement(velem));
        }
        dbManager.saveMap(map, dbe);

    }

    /**
     * delete all defined node elements in existent maps
     * 
     * @throws MapsException
     */
    synchronized public void deleteAllNodeElements() throws MapsException {
        dbManager.deleteNodeTypeElementsFromAllMaps();
    }

    /**
     * delete all defined sub maps in existent maps
     * 
     * @throws MapsException
     */
    synchronized public void deleteAllMapElements() throws MapsException {
        dbManager.deleteMapTypeElementsFromAllMaps();
    }

    /**
     * Reloads elements of map and theirs avail,severity and status
     * 
     * @param map
     * @return the map refreshed
     */
    public VMap reloadMap(VMap map) throws MapsException {

        DbElement[] elems = dbManager.getElementsOfMap(map.getId());
        List<VElement> velems = new ArrayList<VElement>(elems.length);
        for (int i = 0; i < elems.length; i++) {
            velems.add(new VElement(elems[i]));
        }
        velems = localRefreshElements(velems);
        map.removeAllLinks();
        map.removeAllElements();
        map.addElements(velems);
        map.addLinks(getLinks(map.getElements().values()));
        return map;
    }

    public VMap refreshMap(VMap map) throws MapsException {
        
        if (map == null) {
            throw new MapNotFoundException("map is null");
        }

        Collection<VElement> velems = map.getElements().values();
        velems = localRefreshElements(velems);
        for (VElement mapElement : velems) {
            map.addElement(mapElement);
        }
        
        map.removeAllLinks();
        map.addLinks(getLinks(map.getElements().values()));

        return map;
    }

    public boolean foundLoopOnMaps(VMap parentMap, int mapId)
            throws MapsException {

        ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(this.getClass());

        java.util.Map<Integer, Set<Integer>> maps = dbManager.getMapsStructure();
        Set<Integer> childSet = new TreeSet<Integer>();
        for (VElement elem : parentMap.getElements().values()) {
            if (elem.getType().equals(MapsConstants.MAP_TYPE)) {
                childSet.add(new Integer(elem.getId()));
            }
        }

        log.debug("List of sub-maps before preorder visit "
                + childSet.toString());

        maps.put(new Integer(parentMap.getId()), childSet);

        while (childSet.size() > 0) {
            childSet = preorderVisit(childSet, maps);

            log.debug("List of sub-maps  " + childSet.toString());

            if (childSet.contains(new Integer(mapId))) {
                return true;
            }
        }
        return false;

    }

    private Set<Integer> preorderVisit(Set<Integer> treeElems,
            java.util.Map<Integer, Set<Integer>> maps) {
        Set<Integer> childset = new TreeSet<Integer>();
        Iterator<Integer> it = treeElems.iterator();
        while (it.hasNext()) {
            Set<Integer> curset = maps.get(it.next());
            if (curset != null) {
                childset.addAll(curset);
            }
        }
        return childset;
    }

    /**
     * recursively gets all nodes contained by elem and its submaps (if elem
     * is a map)
     */
    public Set<Integer> getNodeidsOnElement(VElement velem)
            throws MapsException {
        DbElement elem = new DbElement(velem);
        return dbManager.getNodeidsOnElement(elem);
    }

    public List<VElementInfo> getElementInfo() throws MapsException {
        return dbManager.getAllElementInfo();
    }

    public org.opennms.web.map.db.Manager getDataAccessManager() {
        return dbManager;
    }

    private String getSeverityLabel(int severity) throws MapsException {
        return OnmsSeverity.get(severity).getLabel();
    }

    private VElement refresh(VElement mapElement, Set<Integer> nodesBySource,
            Vector<Integer> deletedNodeids,
            java.util.Map<Integer, AlarmInfo> outagedNodes,
            java.util.Map<Integer, Double> avails) throws MapsException {
        VElement ve = mapElement.clone();
        if (log.isDebugEnabled()) {
            log.debug("refresh: parsing VElement ID " + ve.getId()
                    + ve.getType() + ", label:" + ve.getLabel()
                    + " with node by sources: " + nodesBySource.toString()
                    + " deletedNodeids: " + deletedNodeids.toString()
                    + " outagedNode: " + outagedNodes.keySet().toString());
        }

        double elementAvail = mapsPropertiesFactory.getDisabledAvail().getMin();
        int elementStatus = mapsPropertiesFactory.getDefaultStatus().getId();
        float elementSeverity = mapsPropertiesFactory.getDefaultSeverity().getId();
        String calculateSeverityAs = mapsPropertiesFactory.getSeverityMapAs();

        // get status, severity and availability: for each node, look for
        // alternative data
        // sources; if no source is found or if the data is not retrieved, use
        // opennms.
        if (dbManager.isElementNotDeleted(ve.getId(), ve.getType())) {
            elementAvail = mapsPropertiesFactory.getUndefinedAvail().getMin();
            elementStatus = mapsPropertiesFactory.getUnknownStatus().getId();
            elementSeverity = mapsPropertiesFactory.getIndeterminateSeverity().getId();
            log.warn("The element type: " + ve.getType() + " with id="
                    + ve.getId() + " was deleted");
        } else if (ve.isNode()) {
            if (deletedNodeids.contains(new Integer(ve.getId()))) {
                elementAvail = mapsPropertiesFactory.getUndefinedAvail().getMin();
                elementStatus = mapsPropertiesFactory.getUnknownStatus().getId();
                elementSeverity = mapsPropertiesFactory.getIndeterminateSeverity().getId();
            } else { // if the node isn't deleted
                if (nodesBySource.contains(new Integer(ve.getId()))) {
                    Object id = new Integer(ve.getId());
                    log.debug("getting status from alternative source "
                            + dataSource.getClass().getName());
                    int status = mapsPropertiesFactory.getStatus(dataSource.getStatus(id));
                    if (status >= 0) {
                        elementStatus = status;
                        log.debug("got status from alternative source. Value is "
                                + elementStatus);
                    }

                    int sev = mapsPropertiesFactory.getSeverity(dataSource.getSeverity(id));
                    if (sev >= 0) {
                        elementSeverity = sev;
                        log.debug("got severity from alternative source. Value is "
                                + sev);
                    }
                    if (mapsPropertiesFactory.isAvailEnabled()) {
                        double avail = dataSource.getAvailability(id);
                        if (avail >= 0) {
                            elementAvail = avail;
                            log.debug("got availability from alternative source. Value is "
                                    + avail);
                        }
                    }
                } else {
                    AlarmInfo oi = outagedNodes.get(new Integer(ve.getId()));
                    if (oi != null) {
                        elementStatus = oi.getStatus();
                        elementSeverity = oi.getSeverity();
                    }
                    if (mapsPropertiesFactory.isAvailEnabled()
                            && (new Integer(ve.getId()) != null)
                            && (avails.get(new Integer(ve.getId())) != null)) {
                        elementAvail = avails.get(new Integer(ve.getId())).doubleValue();
                    }

                }
            } // end of nodes deleted
        } else { // the element is a Map
            log.debug("Calculating severity for submap Element " + ve.getId()
                    + " using '" + calculateSeverityAs + "' mode.");
            Set<Integer> nodesonve = getNodeidsOnElement(ve);
            if (nodesonve != null && nodesonve.size() > 0) {
                log.debug("found nodes on Map element :"
                        + nodesonve.toString());
                elementAvail = mapsPropertiesFactory.getDisabledAvail().getMin();
                float sev = 0;
                if (calculateSeverityAs.equalsIgnoreCase("worst")
                        || calculateSeverityAs.equalsIgnoreCase("best")) {
                    sev = mapsPropertiesFactory.getDefaultSeverity().getId();
                }
                Iterator<Integer> ite = nodesonve.iterator();
                while (ite.hasNext()) {
                    Integer nextNodeId = ite.next();
                    if (deletedNodeids.contains(nextNodeId)) {
                        elementAvail += mapsPropertiesFactory.getUndefinedAvail().getMin();
                        elementStatus = mapsPropertiesFactory.getUnknownStatus().getId();
                        elementSeverity = mapsPropertiesFactory.getIndeterminateSeverity().getId();
                    } else { // if the node isn't deleted
                        if (nodesBySource.contains(nextNodeId)) {
                            int st = mapsPropertiesFactory.getStatus(dataSource.getStatus(nextNodeId));
                            if (st >= 0) {
                                if (st < elementStatus) {
                                    elementStatus = st;
                                }
                                log.debug("got status from alternative source. Value is "
                                        + st);
                            }

                            int tempSeverity = mapsPropertiesFactory.getSeverity(dataSource.getSeverity(nextNodeId));
                            if (tempSeverity >= 0) {
                                log.debug("got severity from alternative source. Value is "
                                        + tempSeverity);
                                if (calculateSeverityAs.equalsIgnoreCase("avg")) {
                                    sev += tempSeverity;
                                } else if (calculateSeverityAs.equalsIgnoreCase("worst")) {
                                    if (sev > tempSeverity) {
                                        sev = tempSeverity;
                                    }
                                } else if (calculateSeverityAs.equalsIgnoreCase("best")) {
                                    if (sev < tempSeverity) {
                                        sev = tempSeverity;
                                    }
                                }
                            }
                            if (mapsPropertiesFactory.isAvailEnabled()) {
                                double avail = dataSource.getAvailability(nextNodeId);
                                if (avail >= 0) {
                                    elementAvail += avail;
                                    log.debug("got availability from alternative source. Value is "
                                            + avail);
                                }
                            }
                        } else {
                            AlarmInfo oi = outagedNodes.get(nextNodeId);
                            if (oi != null) {
                                elementStatus = oi.getStatus();
                                float tempSeverity = oi.getSeverity();
                                if (tempSeverity >= 0) {
                                    if (calculateSeverityAs.equalsIgnoreCase("avg")) {
                                        sev += tempSeverity;
                                    } else if (calculateSeverityAs.equalsIgnoreCase("worst")) {
                                        if (sev > tempSeverity) {
                                            sev = tempSeverity;
                                        }
                                    } else if (calculateSeverityAs.equalsIgnoreCase("best")) {
                                        if (sev < tempSeverity) {
                                            sev = tempSeverity;
                                        }
                                    }
                                }
                            }
                            if (mapsPropertiesFactory.isAvailEnabled()
                                    && (nextNodeId != null)
                                    && (avails.get(nextNodeId) != null)) {
                                elementAvail += avails.get(nextNodeId).doubleValue();
                            }

                        }
                    }
                }
                if (calculateSeverityAs.equalsIgnoreCase("avg")) {
                    elementSeverity = sev / nodesonve.size();
                } else {
                    elementSeverity = sev;
                }
                // calculate availability as average of all nodes on element
                if (elementAvail > 0) {
                    elementAvail = elementAvail / nodesonve.size();
                }

            } else {
                log.debug("no nodes on Map element found");
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("refreshElement: element avail/status/severity "
                    + elementAvail + "/" + elementStatus + "/"
                    + elementSeverity);
        }

        ve.setAvail(elementAvail);
        ve.setStatus(elementStatus);
        ve.setSeverity(new BigDecimal(elementSeverity + 1 / 2).intValue());
        return ve;
    }

    private java.util.Map<Integer, AlarmInfo> getAlarmedNodes()
            throws MapsException {

        java.util.Map<Integer, AlarmInfo> alarmedNodes = new HashMap<Integer, AlarmInfo>();
        log.debug("Getting alarmed elems.");
        Iterator<VElementInfo> ite = dbManager.getAlarmedElements().iterator();
        log.debug("Alarmed elems obtained.");
        while (ite.hasNext()) {
            VElementInfo veleminfo = ite.next();
            int alarmStatus = mapsPropertiesFactory.getStatus(veleminfo.getUei());
            int alarmSeverity = mapsPropertiesFactory.getSeverity(getSeverityLabel(veleminfo.getSeverity()));

            if (log.isInfoEnabled()) {
                log.info("parsing alarmed node with nodeid: "
                        + veleminfo.getId() + " severity: "
                        + veleminfo.getSeverity() + " severity label: "
                        + getSeverityLabel(veleminfo.getSeverity()));
            }

            if (log.isInfoEnabled()) {
                log.info("parsing alarmed node with nodeid: "
                        + veleminfo.getId() + " status: "
                        + veleminfo.getUei() + " severity label: "
                        + getSeverityLabel(veleminfo.getSeverity()));
            }

            if (log.isDebugEnabled()) {
                log.debug("local alarmed node status/severity " + alarmStatus
                        + "/" + alarmSeverity);
            }

            AlarmInfo alarminfo = alarmedNodes.get(new Integer(
                                                               veleminfo.getId()));

            if (alarminfo != null) {
                if (alarminfo.getStatus() > alarmStatus) {
                    alarminfo.setStatus(alarmStatus);
                    alarminfo.setSeverity(alarmSeverity);
                }
            } else {
                int curStatus = alarmStatus;
                float curSeverity = alarmSeverity;
                alarminfo = new AlarmInfo(curStatus, curSeverity);
            }
            alarmedNodes.put(new Integer(veleminfo.getId()), alarminfo);
            if (log.isDebugEnabled()) {
                log.debug("global element node status/severity "
                        + alarmStatus + "/" + alarmSeverity);
            }
        }
        return alarmedNodes;
    }

    /**
     * gets the id corresponding to the link defined in configuration file.
     * The match is performed first by snmptype, then by speed (if more are
     * defined). If there is no match, the default link id is returned.
     * 
     * @param linkinfo
     * @return the id corresponding to the link defined in configuration file.
     *         If there is no match, the default link id is returned.
     */
    private int getLinkTypeId(LinkInfo linkinfo) {
        if (linkinfo.linktypeid > 0)
            return linkinfo.linktypeid;
        else
            return mapsPropertiesFactory.getLinkTypeId(linkinfo.snmpiftype,
                                                       linkinfo.snmpifspeed);
    }

    private List<VLink> getLinks(Collection<VElement> elems) throws MapsException {
        if (elems == null)
            return null;
        String multilinkStatus = mapsPropertiesFactory.getMultilinkStatus();
        List<VLink> links = new ArrayList<VLink>();

        java.util.Map<Integer, Set<VElement>> node2Element = new HashMap<Integer, Set<VElement>>();

        HashSet<Integer> allNodes = new HashSet<Integer>();

        for (VElement ve : elems) {
            for (Integer nodeid : getNodeidsOnElement(ve)) {
                allNodes.add(nodeid);
                Set<VElement> elements = node2Element.get(nodeid);
                if (elements == null) {
                    elements = new java.util.HashSet<VElement>();
                }
                elements.add(ve);
                node2Element.put(nodeid, elements);
            }
        }

        for (LinkInfo linfo : dbManager.getLinksOnElements(allNodes)) {
            log.debug("Found link: node1:" + linfo.nodeid + " node2: "
                    + linfo.nodeparentid);
            log.debug("Getting linkinfo for nodeid " + linfo.nodeid);
            if (!node2Element.containsKey(linfo.nodeid))
                continue;
            if (!node2Element.containsKey(linfo.nodeparentid))
                continue;
            for (VElement first : node2Element.get(linfo.nodeid)) {
                log.debug("Getting linkinfo for nodeid " + linfo.nodeparentid);
                for (VElement second : node2Element.get(linfo.nodeparentid)) {
                    if (first.hasSameIdentifier(second)) {
                        continue;
                    }
                    VLink vlink = new VLink(first.getId(), first.getType(),
                                            second.getId(), second.getType());
                    int status=getLinkStatus(linfo);
                    vlink.setLinkStatusString(getLinkStatusString(status));
                    vlink.setLinkTypeId(getLinkTypeId(linfo));
                    vlink.setFirstNodeid(linfo.nodeid);
                    vlink.setSecondNodeid(linfo.nodeparentid);
                    int index = links.indexOf(vlink);
                    if (index != -1) {
                        VLink alreadyIn = links.get(index);
                        if (alreadyIn.equals(vlink)) {
                            if (multilinkStatus.equals(MapPropertiesFactory.MULTILINK_BEST_STATUS)) {
                                if (status < getLinkStatusInt(alreadyIn.getLinkStatusString())) {
                                    log.debug("removing to the array link "
                                            + alreadyIn.toString()
                                            + " with status "
                                            + alreadyIn.getLinkStatusString());
                                    links.remove(index);
                                    links.add(vlink);
                                    log.debug("adding to the array link "
                                            + vlink.toString()
                                            + " with status "
                                            + vlink.getLinkStatusString());
                                }
                            } else if (status > getLinkStatusInt(alreadyIn.getLinkStatusString())) {
                                log.debug("removing to the array link "
                                        + alreadyIn.toString()
                                        + " with status "
                                        + alreadyIn.getLinkStatusString());
                                links.remove(index);
                                links.add(vlink);
                                log.debug("adding to the array link "
                                        + vlink.toString() + " with status "
                                        + vlink.getLinkStatusString());
                            }
                        }
                    } else {
                        log.debug("adding link (" + vlink.hashCode() + ") "
                                + vlink.getFirst() + "-" + vlink.getSecond());
                        links.add(vlink);
                    }
                }
            }
        }
        return links;
    }

    private String getLinkStatusString(int linkStatus) {
        if (linkStatus == 0 ) return "up";
        else if (linkStatus == 1 ) return "down";
        else if (linkStatus == 2 ) return "admindown";
        else if (linkStatus == 3 ) return "testing";
        else if (linkStatus == -100 ) return "good";
        else if (linkStatus == -99 ) return "bad";
        else if (linkStatus == 1004 ) return "unknown";
        else return "unknown";
    }

    public int getLinkStatusInt(String linkStatus) {
        if (linkStatus.equals("up") ) return 0;
        else if (linkStatus.equals("down")) return 1;
        else if (linkStatus.equals("admindown")) return 2;
        else if (linkStatus.equals("testing") ) return 3;
        else if (linkStatus.equals("good") ) return 1001;
        else if (linkStatus.equals("bad") ) return 1002;
        else return 1004;
    }

    private int getLinkStatus(LinkInfo linfo) {
        if (linfo.status.equalsIgnoreCase("G"))
            return -100;
        if (linfo.status.equalsIgnoreCase("B"))
            return -99;
        if (linfo.status.equalsIgnoreCase("X"))
            return 2;
        if (linfo.status.equalsIgnoreCase("U"))
            return 1004;
        if (linfo.snmpifoperstatus == 1 && linfo.snmpifadminstatus == 1)
            return 0;
        if (linfo.snmpifoperstatus == 2 && linfo.snmpifadminstatus == 1)
            return 1;
        return linfo.snmpifadminstatus;
    }

    private String unescapeHtmlChars(String input) {
        return (input == null ? null
                             : input.replaceAll("&amp;", "&").replaceAll(
                                                                         "&lt;",
                                                                         "<").replaceAll(
                                                                                         "&gt;",
                                                                                         ">"));
    }

    public GroupDao getGroupDao() {
        return m_groupDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        m_groupDao = groupDao;
    }

    private VElement getElement(int mapId, int elementId, String type)
            throws MapsException {
        return new VElement(dbManager.getElement(elementId, mapId, type));
    }

    public VMap searchMap(String owner,String userModifies, int width, int height, List<VElement> elems) throws MapsException {
        VMap m = new VMap(MapsConstants.SEARCH_MAP_NAME);
        m.setOwner(owner);
        m.setUserLastModifies(userModifies);
        m.setWidth(width);
        m.setHeight(height);
        m.setId((MapsConstants.SEARCH_MAP));
        m.setBackground(mapsPropertiesFactory.getDefaultBackgroundColor());
        m.setAccessMode(MapsConstants.ACCESS_MODE_ADMIN);
        m.setType(MapsConstants.USER_GENERATED_MAP);
        
        m.addElements(elems);
        m.addLinks(getLinks(elems));
        
        sessionMap = m;
        searchMap = m;
        return m;    
    }

    public java.util.Map<String, Set<Integer>> getNodeLabelToMaps(String user)
            throws MapsException {
        List<Integer> maps = new ArrayList<Integer>();
        for (VMapInfo mapinfo : getMapsMenuByuser(user)) {
            maps.add(new Integer(mapinfo.getId()));
        }
        DbElement[] elems = dbManager.getAllElements();
        java.util.Map<String, Set<Integer>> nodelabelMap = new HashMap<String, Set<Integer>>();
        for (int i = 0; i < elems.length; i++) {
            DbElement elem = elems[i];
            String label = elem.getLabel();
            log.debug("getNodeLabelToMaps: found element with label: "
                    + label);
            Integer mapId = new Integer(elem.getMapId());
            if (!elem.isNode())
                continue;
            if (!maps.contains(mapId))
                continue;

            Set<Integer> mapids = null;
            if (nodelabelMap.containsKey(label)) {
                mapids = nodelabelMap.get(label);
            } else {
                mapids = new TreeSet<Integer>();
            }
            mapids.add(mapId);
            nodelabelMap.put(label, mapids);
        }
        return nodelabelMap;
    }

    public VProperties getProperties(boolean isUserAdmin)
            throws MapsException {
        VProperties inObj = new VProperties();
        inObj.setAvailEnabled(mapsPropertiesFactory.isAvailEnabled());
        inObj.setDoubleClickEnabled(mapsPropertiesFactory.isDoubleClickEnabled());
        inObj.setContextMenuEnabled(mapsPropertiesFactory.isContextMenuEnabled());
        inObj.setReload(mapsPropertiesFactory.isReload());
        inObj.setContextMenu(mapsPropertiesFactory.getContextMenu());
        inObj.setLinks(mapsPropertiesFactory.getLinks());
        inObj.setLinkStatuses(mapsPropertiesFactory.getLinkStatuses());
        inObj.setStatuses(mapsPropertiesFactory.getStatuses());
        inObj.setSeverities(mapsPropertiesFactory.getSeverities());
        inObj.setAvails(mapsPropertiesFactory.getAvails());
        inObj.setIcons(mapsPropertiesFactory.getIcons());
        inObj.setBackgroundImages(mapsPropertiesFactory.getBackgroundImages());
        inObj.setMapElementDimensions(mapsPropertiesFactory.getMapElementDimensions());
        inObj.setDefaultNodeIcon(mapsPropertiesFactory.getDefaultNodeIcon());
        inObj.setDefaultMapIcon(mapsPropertiesFactory.getDefaultMapIcon());
        inObj.setDefaultBackgroundColor(mapsPropertiesFactory.getDefaultBackgroundColor());
        inObj.setUserAdmin(isUserAdmin);
        inObj.setCategories(getCategories());
        inObj.setUnknownstatusid(mapsPropertiesFactory.getUnknownStatusId());
        inObj.setDefaultMapElementDimension(mapsPropertiesFactory.getDefaultMapElementDimension());
        inObj.setMaxLinks(mapsPropertiesFactory.getMaxLinks());
        return inObj;
    }

    public VMap addElements(VMap map, List<VElement> velems) throws MapsException {
        map.removeAllLinks();
        for (VElement ve: velems) {
            log.debug("adding map element to map with id: " +ve.getId()+ve.getType());
            try {
                String type = MapsConstants.NODE_HIDE_TYPE;
                if (ve.isMap()) type =MapsConstants.MAP_HIDE_TYPE;
                VElement hve = getElement(map.getId(), ve.getId(), type);
                if (hve.getLabel() != null) {
                    ve.setLabel(hve.getLabel());
                    log.debug("preserving label map is hidden: label found: " + hve.getLabel());
                }
            } catch (Exception e) {
               log.debug("No Hidden Element found for id: " +ve.getId()+ve.getType()); 
            }
            map.addElement(ve);
        }            
        map.addLinks(getLinks(map.getElements().values()));
        return map;
    }
}
