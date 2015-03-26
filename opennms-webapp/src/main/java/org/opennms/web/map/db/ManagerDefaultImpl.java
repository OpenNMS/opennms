/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.MapsManagementException;
import org.opennms.web.map.config.MapPropertiesFactory;
import org.opennms.web.map.db.datasources.DataSourceInterface;
import org.opennms.web.map.view.Command;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;
import org.opennms.web.map.view.VMapInfo;
import org.opennms.web.map.view.VProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ManagerDefaultImpl class.</p>
 *
 * @author maurizio
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class ManagerDefaultImpl implements Manager {
	
	private static final Logger LOG = LoggerFactory.getLogger(ManagerDefaultImpl.class);


    private static class AlarmInfo {
        int status;
        int severity;

        public int getSeverity() {
            return severity;
        }

        public void setSeverity(int severity) {
            this.severity = severity;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        AlarmInfo(int status, int severity) {
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

    
    private List<VElementInfo> elemInfo = new ArrayList<VElementInfo>();

    private List<VMapInfo> mapInfo = new ArrayList<VMapInfo>();
    
    private Map<String, Command> commandmap = new HashMap<String, Command>();

    /**
     * <p>Getter for the field <code>filter</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * <p>Setter for the field <code>filter</code>.</p>
     *
     * @param filter a {@link java.lang.String} object.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * <p>Getter for the field <code>dataSource</code>.</p>
     *
     * @return a {@link org.opennms.web.map.db.datasources.DataSourceInterface} object.
     */
    public DataSourceInterface getDataSource() {
        return dataSource;
    }

    /**
     * <p>Setter for the field <code>dataSource</code>.</p>
     *
     * @param dataSource a {@link org.opennms.web.map.db.datasources.DataSourceInterface} object.
     */
    public void setDataSource(DataSourceInterface dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * <p>Getter for the field <code>dbManager</code>.</p>
     *
     * @return a {@link org.opennms.web.map.db.Manager} object.
     */
    public org.opennms.web.map.db.Manager getDbManager() {
        return dbManager;
    }

    /**
     * <p>Setter for the field <code>dbManager</code>.</p>
     *
     * @param dbManager a {@link org.opennms.web.map.db.Manager} object.
     */
    public void setDbManager(org.opennms.web.map.db.Manager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * <p>Getter for the field <code>mapsPropertiesFactory</code>.</p>
     *
     * @return a org$opennms$web$map$config$MapPropertiesFactory object.
     */
    public org.opennms.web.map.config.MapPropertiesFactory getMapsPropertiesFactory() {
        return mapsPropertiesFactory;
    }

    /**
     * <p>Setter for the field <code>mapsPropertiesFactory</code>.</p>
     *
     * @param mapsPropertiesFactory a org$opennms$web$map$config$MapPropertiesFactory object.
     */
    public void setMapsPropertiesFactory(
            org.opennms.web.map.config.MapPropertiesFactory mapsPropertiesFactory) {
        this.mapsPropertiesFactory = mapsPropertiesFactory;
    }

    private List<String> getCategories() throws MapsException {
        List<String> categories = new ArrayList<String>();
        try {
            CategoryFactory.init();
        } catch (Throwable e) {
            throw new MapsException("Error while getting categories.", e);
        }
        final CatFactory cf = CategoryFactory.getInstance();
        cf.getReadLock().lock();
        try {
            LOG.debug("Get categories:");
            for (final Categorygroup cg : cf.getConfig().getCategorygroupCollection()) {
                for (final org.opennms.netmgt.config.categories.Category category : cg.getCategories().getCategoryCollection()) {
                    final String categoryName = unescapeHtmlChars(category.getLabel());
                    LOG.debug(categoryName);
                    categories.add(categoryName);
                }
            }
        } finally {
            cf.getReadLock().unlock();
        }
        return categories;
    }

    /**
     * Manage Maps using default implementation of Factory and Manager
     *
     * @throws org.opennms.web.map.MapsException if any.
     */
    public ManagerDefaultImpl() throws MapsException {
        Logging.withPrefix(MapsConstants.LOG4J_CATEGORY, new Runnable() {

            @Override
            public void run() {
                LOG.debug("Instantiating ManagerDefaultImpl");
            }
            
        });
    }

    /**
     * <p>openMap</p>
     *
     * @return a {@link org.opennms.web.map.view.VMap} object.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    @Override
    public VMap openMap() throws MapNotFoundException {
        if (sessionMap != null) {
            return sessionMap;
        }
        throw new MapNotFoundException();
    }

    /**
     * <p>clearMap</p>
     *
     * @throws org.opennms.web.map.MapNotFoundException if any.
     * @throws org.opennms.web.map.MapsException if any.
     */
    @Override
    public void clearMap() throws MapNotFoundException, MapsException {
        if (sessionMap == null) {
            throw new MapNotFoundException();
        }
        sessionMap.removeAllLinks();
        sessionMap.removeAllElements();
    }

    /**
     * <p>deleteMap</p>
     *
     * @throws org.opennms.web.map.MapNotFoundException if any.
     * @throws org.opennms.web.map.MapsException if any.
     */
    @Override
    public void deleteMap() throws MapNotFoundException, MapsException {
        deleteMap(sessionMap.getId());
        
    }

    /**
     * <p>closeMap</p>
     */
    @Override
    public void closeMap() {
        sessionMap = null;
    }

    /** {@inheritDoc} */
    @Override
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
     * {@inheritDoc}
     *
     * Create a new VMap and return it
     */
    @Override
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
     * @param id a int.
     * @param refreshElems
     *            says if refresh the map's elements
     * @return the VMap with identifier id
     * @throws org.opennms.web.map.MapsException if any.
     * @throws org.opennms.web.map.MapsManagementException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
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
                LOG.debug("openMap: adding element to map with label: {}", elem.getLabel());
                retVMap.addElement(elem);
            }
        }

        if (refreshElems) {
            LOG.debug("Starting refreshing elems for map with id {}", id);
            for (VElement changedElem : localRefreshElements(retVMap.getElements().values())) {
                retVMap.removeElement(changedElem.getId(),
                                      changedElem.getType());
                retVMap.addElement(changedElem);
            }
        }

        LOG.debug("Starting adding links for map with id {}", id);
        retVMap.addLinks(getLinks(retVMap.getElements().values()));
        LOG.debug("Ending adding links for map with id {}", id);
        sessionMap = retVMap;
        return retVMap;
    }

    private List<VElement> localRefreshElements(
            Collection<VElement> mapElements) throws MapsException {
        List<VElement> elems = new ArrayList<VElement>();
        List<Integer> deletedNodeids = dbManager.getDeletedNodes();
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

    /**
     * <p>getMapMenu</p>
     *
     * @param mapId a int.
     * @return a {@link org.opennms.web.map.view.VMapInfo} object.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     * @throws org.opennms.web.map.MapsException if any.
     */
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
     * @param refreshElems
     *            says if refresh map's elements
     * @return the VMaps array if any label matches the pattern in input, null
     *         otherwise
     * @throws org.opennms.web.map.MapsException if any.
     * @param likeLabel a {@link java.lang.String} object.
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
     * @param mapName a {@link java.lang.String} object.
     * @return the VMaps array if any map has name in input, null otherwise
     * @throws org.opennms.web.map.MapsException if any.
     * @param refreshElems a boolean.
     * @throws org.opennms.web.map.MapNotFoundException if any.
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
     * @throws org.opennms.web.map.MapsException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
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
     * @throws org.opennms.web.map.MapsException if any.
     */
    public VMapInfo[] getAllMapMenus() throws MapsException {
        VMapInfo[] m = null;
        m = dbManager.getAllMapMenus();
        return m;

    }

    /**
     * Take the maps with name in input and return them in MapMenu[] form.
     *
     * @param mapName a {@link java.lang.String} object.
     * @return the MapMenu array if any map has name in input, null otherwise
     * @throws org.opennms.web.map.MapsException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
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

    /** {@inheritDoc} */
    @Override
    public VMapInfo getDefaultMapsMenu(String user) throws MapsException {

        Iterator<Group> ite = getGroupDao().findGroupsForUser(user).iterator();

        while (ite.hasNext()) {
            Group group = ite.next();
            LOG.debug("getDefaultMapsMenu: found group: {} for user:{}", group.getName(), user);
            if (group.getDefaultMap() != null) {
                LOG.debug("getDefaultMapsMenu: found default map: {} for group: {}", group.getDefaultMap(), group.getName());
                VMapInfo[] vmapsinfo = dbManager.getMapsMenuByName(group.getDefaultMap());
                if (vmapsinfo != null) {
                    LOG.debug("getDefaultMapsMenu: found {} maps. Verify access ", vmapsinfo.length);
                    for (int i = 0; i < vmapsinfo.length; i++) {
                        if (vmapsinfo[i].getOwner().equals(user)) {
                            LOG.info("getDefaultMapsMenu: found! user: {} owns the map", user);
                            return vmapsinfo[i];
                        } else {
                            DbMap map = dbManager.getMap(vmapsinfo[i].getId());
                            LOG.debug("getDefaultMapsMenu: map: {} mapName: {} Access: {} Group: {}", map.getId(), map.getName(), map.getAccessMode(), map.getGroup());
                            if (map.getAccessMode().trim().equalsIgnoreCase(
                                                                                MapsConstants.ACCESS_MODE_ADMIN)
                                    || map.getAccessMode().trim().equalsIgnoreCase(
                                                                                       MapsConstants.ACCESS_MODE_USER)
                                    || (map.getAccessMode().trim().equalsIgnoreCase(
                                                                                        MapsConstants.ACCESS_MODE_GROUP) && map.getGroup().equals(
                                                                                                                                                      group.getName()))) {
                                LOG.info("getDefaultMapsMenu: found! user: {} has access to map: {} with id: {}", user, map.getName(), map.getId());
                                return vmapsinfo[i];
                            } else {
                                LOG.info("getDefaultMapsMenu: access is denied for default map: {} to group: {}", group.getDefaultMap(), group.getName());
                            }
                        }
                    }
                } else {
                    LOG.info("getDefaultMapsMenu: no maps found for default map: {} for group: {}", group.getDefaultMap(), group.getName());
                }
            }
        }
        return new VMapInfo(MapsConstants.NEW_MAP, "no default map found",
                            user);
    }

    /**
     * {@inheritDoc}
     *
     * gets all visible maps for user and userRole in input
     */
    @Override
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
        mapInfo = maps;
        return mapInfo;
    }

    /**
     * Take all the maps in the tree of maps considering the with name in
     * input as the root of the tree. If there are more maps with
     * <i>mapName</i> (case insensitive) all trees with these maps as root are
     * considered and returned.
     *
     * @param mapName a {@link java.lang.String} object.
     * @return a List with the MapMenu objects.
     * @throws org.opennms.web.map.MapsException if any.
     * @throws org.opennms.web.map.MapNotFoundException if any.
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

                preorderVisit(Integer.valueOf(element.getId()), childList,
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
     * @param mapId a int.
     * @param elementId a int.
     * @param type
     *            the node type
     * @param x a int.
     * @param y a int.
     * @return the new VElement
     * @throws org.opennms.web.map.MapsException if any.
     */
    public VElement newElement(int mapId, int elementId, String type, int x,
            int y) throws MapsException {

        VElement velem = newElement(mapId, elementId, type);
        velem.setX(x);
        velem.setY(y);
        return velem;
    }

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
    public VElement newElement(int elementId, String type, int x, int y)
            throws MapsException {
        if (sessionMap == null) {
            throw new MapNotFoundException("session map in null");
        }
        return newElement(sessionMap.getId(), elementId, type, x, y);
    }

    /**
     * {@inheritDoc}
     *
     * Create a new element child of the map with mapId (this map must be the
     * sessionMap)
     */
    @Override
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
        LOG.debug("Adding velement to map {}", velem.toString());

        return velem;

    }

    private String getIconBySysoid(String sysoid) throws MapsException {
        try {
            java.util.Map<String, String> iconsBySysoid = mapsPropertiesFactory.getIconsBySysoid();
            if (iconsBySysoid != null) {
                LOG.debug("getIconBySysoid: sysoid = {}", sysoid);
                
                for (final Map.Entry<String,String> entry : iconsBySysoid.entrySet()) {
                    final String key = entry.getKey();
                    LOG.debug("getIconBySysoid: key = {}", key);
                    if (key.equals(sysoid)) {
                        final String value = entry.getValue();
                        LOG.debug("getIconBySysoid: iconBySysoid = {}", value);
                        return value;
                    }
                }
            }
        } catch (final Throwable e) {
            LOG.error("Exception while getting icons by sysoid");
            throw new MapsException(e);
        }
        return mapsPropertiesFactory.getDefaultNodeIcon();
    }

    /**
     * {@inheritDoc}
     *
     * Create a new element child of the map with mapId (this map must be the
     * sessionMap).
     */
    @Override
    public VElement newElement(int mapId, int elementId, String type,
            String iconname, int x, int y) throws MapsException {
        VElement velem = newElement(mapId, elementId, type);
        if (iconname == null ) {
            if (MapsConstants.MAP_TYPE.equals(type)) {
                iconname = mapsPropertiesFactory.getDefaultMapIcon();
            } else {
                iconname = mapsPropertiesFactory.getDefaultNodeIcon();
            }
        }
        velem.setIcon(iconname);
        velem.setX(x);
        velem.setY(y);
        return velem;

    }

    /**
     * Create a new (not child) empty Submap with the identifier setted to id.
     *
     * @param mapId a int.
     * @param elementId a int.
     * @param type
     *            the node type
     * @return the new VElement
     * @throws org.opennms.web.map.MapsException if any.
     * @param iconname a {@link java.lang.String} object.
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
     * @throws org.opennms.web.map.MapsException
     *             if an error occour deleting map, MapNotFoundException if
     *             the map to delete doesn't exist.
     * @throws org.opennms.web.map.MapNotFoundException if any.
     */
    public synchronized void deleteMap(VMap map) throws MapsException,
            MapNotFoundException {
        deleteMap(map.getId());
        deleteFromMapInfo(map.getId());
    }
    
    private void deleteFromMapInfo(int mapId) {
        List<VMapInfo> mapinfolist = new ArrayList<VMapInfo>();
        for (VMapInfo vmapinfo: mapInfo) {
            if (vmapinfo.getId() != mapId )
                mapinfolist.add(vmapinfo);
        }
        mapInfo.clear();
        mapInfo.addAll(mapinfolist);
    }

    /**
     * delete the map with identifier id
     *
     * @throws org.opennms.web.map.MapsException if any.
     * @param mapId a int.
     */
    public synchronized void deleteMap(int mapId) throws MapsException {
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
     * @throws org.opennms.web.map.MapsException if any.
     */
    public synchronized void deleteMaps(VMap[] maps) throws MapsException {
        for (VMap map : maps) {
            deleteMap(map);
        }
    }

    /**
     * delete the maps with the identifiers in input
     *
     * @throws org.opennms.web.map.MapsException if any.
     * @param maps an array of int.
     */
    public synchronized void deleteMaps(int[] maps) throws MapsException {
        for (int map : maps) {
            deleteMap(map);
        }
    }

    /**
     * {@inheritDoc}
     *
     * save the map in input
     */
    @Override
    public synchronized int save(VMap map) throws MapsException {
        Collection<DbElement> dbe = new ArrayList<DbElement>();
        for (VElement velem : map.getElements().values()) {
            dbe.add(new DbElement(velem));
        }
        return dbManager.saveMap(map, dbe);

    }

    /**
     * delete all defined node elements in existent maps
     *
     * @throws org.opennms.web.map.MapsException if any.
     */
    public synchronized void deleteAllNodeElements() throws MapsException {
        dbManager.deleteNodeTypeElementsFromAllMaps();
    }

    /**
     * delete all defined sub maps in existent maps
     *
     * @throws org.opennms.web.map.MapsException if any.
     */
    public synchronized void deleteAllMapElements() throws MapsException {
        dbManager.deleteMapTypeElementsFromAllMaps();
    }

    /**
     * {@inheritDoc}
     *
     * Reloads elements of map and theirs avail,severity and status
     */
    @Override
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

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public boolean foundLoopOnMaps(VMap parentMap, int mapId)
            throws MapsException {

        java.util.Map<Integer, Set<Integer>> maps = dbManager.getMapsStructure();
        Set<Integer> childSet = new TreeSet<Integer>();
        for (VElement elem : parentMap.getElements().values()) {
            if (elem.getType().equals(MapsConstants.MAP_TYPE)) {
                childSet.add(Integer.valueOf(elem.getId()));
            }
        }

        LOG.debug("List of sub-maps before preorder visit {}", childSet.toString());

        maps.put(Integer.valueOf(parentMap.getId()), childSet);

        while (childSet.size() > 0) {
            childSet = preorderVisit(childSet, maps);

            LOG.debug("List of sub-maps  {}", childSet.toString());

            if (childSet.contains(Integer.valueOf(mapId))) {
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
     *
     * @param velem a {@link org.opennms.web.map.view.VElement} object.
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public Set<Integer> getNodeidsOnElement(VElement velem)
            throws MapsException {
        DbElement elem = new DbElement(velem);
        return dbManager.getNodeidsOnElement(elem);
    }

    /**
     * <p>getElementInfo</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    @Override
    public List<VElementInfo> getElementInfo() throws MapsException {
        elemInfo=  dbManager.getAllElementInfo();
        return elemInfo;
    }

    /**
     * <p>getDataAccessManager</p>
     *
     * @return a {@link org.opennms.web.map.db.Manager} object.
     */
    public org.opennms.web.map.db.Manager getDataAccessManager() {
        return dbManager;
    }

    private String getSeverityLabel(int severity) throws MapsException {
        return OnmsSeverity.get(severity).getLabel();
    }

    private VElement refresh(VElement mapElement, Set<Integer> nodesBySource,
            List<Integer> deletedNodeids,
            java.util.Map<Integer, AlarmInfo> outagedNodes,
            java.util.Map<Integer, Double> avails) throws MapsException {
        
        VElement ve = mapElement.clone();
        
            LOG.debug("refresh: parsing VElement ID {}{}, label:{} with node by sources: {} deletedNodeids: {} outagedNode: {}", ve.getId(), ve.getType(), ve.getLabel(), nodesBySource.toString(), deletedNodeids.toString(), outagedNodes.keySet().toString());

        if (ve.isNode())
            return refreshNodeElement(ve, nodesBySource, deletedNodeids,outagedNodes,avails);
        else 
            return refreshMapElement(ve,nodesBySource, deletedNodeids,outagedNodes,avails);
    }
    
    private VElement refreshNodeElement(VElement ve, Set<Integer> nodesBySource,
            List<Integer> deletedNodeids,
            java.util.Map<Integer, AlarmInfo> outagedNodes,
            java.util.Map<Integer, Double> avails) throws MapsException {
    

        if (deletedNodeids.contains(Integer.valueOf(ve.getId())) ) {
            ve.setAvail(mapsPropertiesFactory.getUndefinedAvail().getMin());
            ve.setStatus(mapsPropertiesFactory.getUnknownStatus().getId());
            ve.setSeverity(mapsPropertiesFactory.getIndeterminateSeverity().getId());
            LOG.warn("refresh: The node with id={} has been deleted", ve.getId());
            return ve;
        }
        
        ve.setAvail(mapsPropertiesFactory.getDisabledAvail().getMin());
        ve.setStatus(mapsPropertiesFactory.getDefaultStatus().getId());
        ve.setSeverity(mapsPropertiesFactory.getDefaultSeverity().getId());

        if (nodesBySource.contains(Integer.valueOf(ve.getId()))) {
            Object id = Integer.valueOf(ve.getId());
            LOG.debug("refresh: getting status from alternative source {}", dataSource.getClass().getName());
            int status = mapsPropertiesFactory.getStatus(dataSource.getStatus(id));
            LOG.debug("refresh: got status from alternative source. Value is {}", status);
           if (status >= 0) {
                ve.setStatus(status);
            }

            int sev = mapsPropertiesFactory.getSeverity(dataSource.getSeverity(id));
            LOG.debug("refresh: got severity from alternative source. Value is {}", sev);
            if (sev >= 0) {
                ve.setSeverity(sev);
            }
            if (mapsPropertiesFactory.isAvailEnabled()) {
                double avail = dataSource.getAvailability(id);
                LOG.debug("refresh: got availability from alternative source. Value is {}", avail);
                if (avail >= 0) {
                    ve.setAvail(avail);
                }
            }
            return ve;
        } 
       
        AlarmInfo oi = outagedNodes.get(Integer.valueOf(ve.getId()));
        if (oi != null) {
            ve.setStatus(oi.getStatus());
            ve.setSeverity(oi.getSeverity());
        }
        if (mapsPropertiesFactory.isAvailEnabled()
                && (Integer.valueOf(ve.getId()) != null)
                && (avails.get(Integer.valueOf(ve.getId())) != null)) {
            ve.setAvail(avails.get(Integer.valueOf(ve.getId())).doubleValue());
        }
        return ve;
    } 


    private VElement refreshMapElement(VElement ve, Set<Integer> nodesBySource,
            List<Integer> deletedNodeids,
            java.util.Map<Integer, AlarmInfo> outagedNodes,
            java.util.Map<Integer, Double> avails) throws MapsException {

        if (dbManager.isElementDeleted(ve.getId(), ve.getType())) {
            ve.setAvail(mapsPropertiesFactory.getUndefinedAvail().getMin());
            ve.setStatus(mapsPropertiesFactory.getUnknownStatus().getId());
            ve.setSeverity(mapsPropertiesFactory.getIndeterminateSeverity().getId());
            LOG.warn("refresh: The map with id={} was deleted", ve.getId());
            return ve;
        } 
        //reset status
        ve.setStatus(-1);
        Set<Integer> nodesonve = getNodeidsOnElement(ve);
        LOG.debug("refresh: found nodes on Map element :{}", nodesonve.toString());
        if (nodesonve.size() == 0) return ve;
            
        for (Integer nextNodeId : nodesonve) {
            LOG.debug("refresh: Iterating on Map nodes with nodeid = {}", nextNodeId);
            double avail = 100/nodesonve.size();
            int status = mapsPropertiesFactory.getDefaultStatus().getId();
            int severity = mapsPropertiesFactory.getDefaultSeverity().getId();
            
            if (deletedNodeids.contains(nextNodeId)) {
                severity = mapsPropertiesFactory.getIndeterminateSeverity().getId(); 
                status = mapsPropertiesFactory.getUnknownStatus().getId();
            } else if (nodesBySource.contains(nextNodeId)) {
                int srcstatus = mapsPropertiesFactory.getStatus(dataSource.getStatus(nextNodeId));
                if (srcstatus >= 0)
                    status =srcstatus;
                LOG.debug("refresh: got status from alternative source. Value is {}", srcstatus);
                int srcseverity = mapsPropertiesFactory.getSeverity(dataSource.getSeverity(nextNodeId));
                if (srcseverity >= 0)
                    severity = srcseverity;
                LOG.debug("refresh: got severity from alternative source. Value is {}", srcseverity);
                if (mapsPropertiesFactory.isAvailEnabled()) {
                    double srcavail = dataSource.getAvailability(nextNodeId);
                    if (srcavail >= 0 )
                        avail = srcavail/nodesonve.size();
                    LOG.debug("refresh: got availability from alternative source. Value is {}", srcavail);
                }
            } else {
                AlarmInfo oi = outagedNodes.get(nextNodeId);
                if (oi != null) {
                    status = oi.getStatus();
                    severity = oi.getSeverity();
                }
                if (mapsPropertiesFactory.isAvailEnabled()
                            && (nextNodeId != null)
                            && (avails.get(nextNodeId) != null)) {
                    avail = avails.get(nextNodeId).doubleValue()/nodesonve.size();
                }
            }
            ve=recalculateMapElementStatus(ve, severity,status, avail);
        }
                
        
        return recalculateSeverity(ve, nodesonve.size());

    }

    private VElement recalculateSeverity(VElement ve, int size) {

        if (mapsPropertiesFactory.getSeverityMapAs().equalsIgnoreCase("avg")) 
           ve.setSeverity(new BigDecimal(ve.getSeverity()/size + 1 / 2).intValue());
        if (!mapsPropertiesFactory.isAvailEnabled())
            ve.setAvail(mapsPropertiesFactory.getDisabledAvail().getMin());            
        return ve;
    }

    private VElement recalculateMapElementStatus(VElement ve, int severity, int status, double avail) {
        LOG.debug("recalculateMapElementStatus: previuos severity =  {}", ve.getSeverity());
        LOG.debug("recalculateMapElementStatus: previous status = {}", ve.getStatus());
        LOG.debug("recalculateMapElementStatus: previuos avail = {}", ve.getAvail()); 
        
        LOG.debug("recalculateMapElementStatus: current node severity =  {}", severity);
        LOG.debug("recalculateMapElementStatus: current node status = {}", status);
        LOG.debug("recalculateMapElementStatus: current node avail = {}", avail);        
        
        if (ve.getStatus() == -1 ) {
            ve.setStatus(status);
            ve.setSeverity(severity);
            ve.setAvail(avail);
            LOG.debug("recalculateMapElementStatus: first iteration setting the upper map status to first node status");
        } else {
            String calculateSeverityAs = mapsPropertiesFactory.getSeverityMapAs();
            LOG.debug("recalculateMapElementStatus: calculate severity as: {}", calculateSeverityAs);
            if (calculateSeverityAs.equalsIgnoreCase("avg")) {
                ve.setSeverity(severity+ve.getSeverity());
                if (ve.getStatus() > status)
                    ve.setStatus(status);
            } else if (calculateSeverityAs.equalsIgnoreCase("worst")) {
                if (ve.getSeverity() > severity)
                    ve.setSeverity(severity);
                if (ve.getStatus() > status)
                    ve.setStatus(status);
            } else if (calculateSeverityAs.equalsIgnoreCase("best")) {
                if (ve.getSeverity() < severity) 
                    ve.setSeverity(severity);
                if (ve.getStatus() < status)
                    ve.setStatus(status);
            }
            ve.setAvail(ve.getAvail()+avail);
        }
        LOG.debug("recalculateMapElementStatus: updated severity =  {}", ve.getSeverity());
        LOG.debug("recalculateMapElementStatus: updated status = {}", ve.getStatus());
        LOG.debug("recalculateMapElementStatus: updated avail = {}", ve.getAvail());

        return ve;
    }
        
    private java.util.Map<Integer, AlarmInfo> getAlarmedNodes()
            throws MapsException {

        java.util.Map<Integer, AlarmInfo> alarmedNodes = new HashMap<Integer, AlarmInfo>();
        LOG.debug("Getting alarmed elems.");
        Iterator<VElementInfo> ite = dbManager.getAlarmedElements().iterator();
        LOG.debug("Alarmed elems obtained.");
        while (ite.hasNext()) {
            VElementInfo veleminfo = ite.next();
            int alarmStatus = mapsPropertiesFactory.getStatus(veleminfo.getUei());
            int alarmSeverity = mapsPropertiesFactory.getSeverity(getSeverityLabel(veleminfo.getSeverity()));

                LOG.info("parsing alarmed node with nodeid: {} severity: {} severity label: {}", veleminfo.getId(), veleminfo.getSeverity(), getSeverityLabel(veleminfo.getSeverity()));

                LOG.info("parsing alarmed node with nodeid: {} status: {} severity label: {}", veleminfo.getId(), veleminfo.getUei(), getSeverityLabel(veleminfo.getSeverity()));

                LOG.debug("local alarmed node status/severity {}/{}", alarmStatus, alarmSeverity);

            AlarmInfo alarminfo = alarmedNodes.get(Integer.valueOf(
                                                               veleminfo.getId()));

            if (alarminfo != null) {
                if (alarminfo.getStatus() > alarmStatus) {
                    alarminfo.setStatus(alarmStatus);
                    alarminfo.setSeverity(alarmSeverity);
                }
            } else {
                int curStatus = alarmStatus;
                int curSeverity = alarmSeverity;
                alarminfo = new AlarmInfo(curStatus, curSeverity);
            }
            alarmedNodes.put(Integer.valueOf(veleminfo.getId()), alarminfo);
                LOG.debug("global element node status/severity {}/{}", alarmStatus, alarmSeverity);
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
        Map<String,List<VLink>> singlevlinkmap = new HashMap<String,List<VLink>>();
        Map<String,VLink> multivlinkmap = new HashMap<String,VLink>();
        Map<String,Integer> numberofsinglelinksmap = new HashMap<String,Integer>();

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
            LOG.debug("Found link: node1:{} node2: {}", linfo.nodeid, linfo.nodeparentid);
            LOG.debug("Getting linkinfo for nodeid {}", linfo.nodeid);
            
            for (VElement first : node2Element.get(linfo.nodeid)) {
                LOG.debug("Getting linkinfo for nodeid {}", linfo.nodeparentid);
                for (VElement second : node2Element.get(linfo.nodeparentid)) {
                    if (first.hasSameIdentifier(second)) {
                        continue;
                    }
                    
                    int status=getLinkStatus(linfo);
                    String statusString = getLinkStatusString(status);
 
                    VLink vlink = new VLink(first.getId(), first.getType(),
                                            second.getId(), second.getType(), getLinkTypeId(linfo));
                    vlink.setLinkStatusString(statusString);
                    vlink.increaseStatusMapLinks(statusString);
                    Set<Integer> nodeids=vlink.getNodeids();
                    nodeids.add(linfo.nodeid);
                    nodeids.add(linfo.nodeparentid);
                    vlink.setNodeids(nodeids);
                    LOG.debug("adding new link as single link: {}", vlink.toString());
                    
                    List<VLink> templinks=null;
                    if (singlevlinkmap.containsKey(vlink.getId())) {
                        templinks=singlevlinkmap.get(vlink.getId());
                    } else {
                        templinks = new ArrayList<VLink>();
                    }
                    templinks.add(vlink);
                    singlevlinkmap.put(vlink.getId(), templinks);
                    
                    int numberofelement=1;
                    if (numberofsinglelinksmap.containsKey(vlink.getIdWithoutLinkType())) {
                        numberofelement = numberofsinglelinksmap.get(vlink.getIdWithoutLinkType());
                        numberofelement++;
                    }
                    numberofsinglelinksmap.put(vlink.getIdWithoutLinkType(), numberofelement);
                    LOG.debug("updated link counter between elements: {} Found #{}", vlink.getIdWithoutLinkType(), numberofelement);


                    VLink vmultilink = new VLink(first.getId(), first.getType(),
                                                 second.getId(), second.getType(), getLinkTypeId(linfo));
                    vmultilink.setLinkStatusString(statusString);
                    vmultilink.increaseStatusMapLinks(statusString);
                    if (multivlinkmap.containsKey(vmultilink.getId())) {
                        VLink alreadyIn = multivlinkmap.get(vmultilink.getId());
                        int numberOfLinks = alreadyIn.increaseLinks();
                        LOG.debug("Updated {} on Link: {}", numberOfLinks, alreadyIn.getId());
                        int numberOfLinkwithStatus = alreadyIn.increaseStatusMapLinks(statusString);
                        LOG.debug("Updated Status Map: found: {} links with Status: {}", numberOfLinkwithStatus, statusString );
                        if ( ( multilinkStatus.equals(MapPropertiesFactory.MULTILINK_BEST_STATUS) 
                               && status < getLinkStatusInt(alreadyIn.getLinkStatusString())
                             ) 
                          || ( multilinkStatus.equals(MapPropertiesFactory.MULTILINK_WORST_STATUS) 
                               && status > getLinkStatusInt(alreadyIn.getLinkStatusString())
                             )
                            ) {
                            LOG.debug("Upgrading with Link info becouse multilink.status={}", multilinkStatus);
                            LOG.debug("updating existing the link {} with status {}", alreadyIn.toString(), alreadyIn.getLinkStatusString());

                            LOG.debug("setting link properties: {} with new found status {}", vmultilink.toString(), vmultilink.getLinkStatusString());
                            alreadyIn.setLinkStatusString(statusString);
                        }
                        nodeids=alreadyIn.getNodeids();
                        nodeids.add(linfo.nodeid);
                        nodeids.add(linfo.nodeparentid);
                        alreadyIn.setNodeids(nodeids);
                        LOG.debug("updating multi link: {}", alreadyIn.toString());
                        multivlinkmap.put(alreadyIn.getId(),alreadyIn);
                    } else {
                        Set<Integer> vmnodeids=vmultilink.getNodeids();
                        vmnodeids.add(linfo.nodeid);
                        vmnodeids.add(linfo.nodeparentid);
                        vmultilink.setNodeids(vmnodeids);
                        LOG.debug("adding multi link: {}", vmultilink.toString());
                        multivlinkmap.put(vmultilink.getId(),vmultilink);
                    }
                } // end second element for
            } //end first element for
        } // end linkinfo for
        // Now add the VLink to links......
        int maxlinks=mapsPropertiesFactory.getMaxLinks();

        for (final Map.Entry<String,Integer> entry : numberofsinglelinksmap.entrySet()) {
            final String elid = entry.getKey();
            final Integer numLinks = entry.getValue();

            LOG.debug("parsing link between element: {} with #links {}", elid, numLinks);
            if (numLinks <= maxlinks) {
                for (final Map.Entry<String,List<VLink>> vlinkEntry : singlevlinkmap.entrySet()) {
                    final String linkid = vlinkEntry.getKey();
                    if (linkid.indexOf(elid) != -1) {
                        final List<VLink> vlinks = vlinkEntry.getValue();
                        LOG.debug("adding single links for {} Adding links # {}", linkid, vlinks.size());
                        links.addAll(vlinks);
                    }
                }
            } else {
                for (final Map.Entry<String,VLink> vlinkEntry : multivlinkmap.entrySet()) {
                    final String linkid = vlinkEntry.getKey();
                    if (linkid.indexOf(elid) != -1) { 
                        LOG.debug("adding multi link for : {}", linkid);
                        links.add(vlinkEntry.getValue());
                    }
                }
                
            }
        }
        LOG.debug("Found links #{}", links.size());
        for (VLink vlink : links) {
            LOG.debug(vlink.toString());
        }
        return links;
    }

    private String getLinkStatusString(int linkStatus) {
        if (linkStatus == 0 ) return "up";
        else if (linkStatus == 1 ) return "down";
        else if (linkStatus == 2 ) return "admindown";
        else if (linkStatus == 3 ) return "testing";
        else if (linkStatus == -100 ) return "good";
        else if (linkStatus == 1000 ) return "bad";
        else if (linkStatus == 1004 ) return "unknown";
        else return "unknown";
    }

    private int getLinkStatusInt(String linkStatus) {
        if (linkStatus.equals("up") ) return 0;
        else if (linkStatus.equals("down")) return 1;
        else if (linkStatus.equals("admindown")) return 2;
        else if (linkStatus.equals("testing") ) return 3;
        else if (linkStatus.equals("good") ) return -100;
        else if (linkStatus.equals("bad") ) return 1000;
        else return 1004;
    }

    private int getLinkStatus(LinkInfo linfo) {
        if (linfo.status.equalsIgnoreCase("G"))
            return -100;
        if (linfo.status.equalsIgnoreCase("B"))
            return 1000;
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

    /**
     * <p>getGroupDao</p>
     *
     * @return a {@link org.opennms.netmgt.config.GroupDao} object.
     */
    public GroupDao getGroupDao() {
        return m_groupDao;
    }

    /**
     * <p>setGroupDao</p>
     *
     * @param groupDao a {@link org.opennms.netmgt.config.GroupDao} object.
     */
    public void setGroupDao(GroupDao groupDao) {
        m_groupDao = groupDao;
    }

    private VElement getElement(int mapId, int elementId, String type)
            throws MapsException {
        return new VElement(dbManager.getElement(elementId, mapId, type));
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public java.util.Map<String, Set<Integer>> getNodeLabelToMaps(String user)
            throws MapsException {
        Map<Integer,String> maps = new HashMap<Integer,String>();
        for (VMapInfo mapinfo :mapInfo) {
            maps.put(Integer.valueOf(mapinfo.getId()),mapinfo.getName());
        }
        Map<Integer,String> elemInfoMap = new HashMap<Integer,String>();
        for (VElementInfo elem: elemInfo) {
            elemInfoMap.put(elem.getId(), elem.getLabel());
        }
        DbElement[] elems = dbManager.getAllElements();
        java.util.Map<String, Set<Integer>> nodelabelMap = new HashMap<String, Set<Integer>>();
        for (int i = 0; i < elems.length; i++) {
            DbElement elem = elems[i];
            Integer mapId = Integer.valueOf(elem.getMapId());
            if (!maps.containsKey(mapId))
                continue;

            String label = elem.getLabel();
            LOG.debug("getNodeLabelToMaps: found element with label: {}", label);
            Set<Integer> mapids = null;
            if (nodelabelMap.containsKey(label)) {
                mapids = nodelabelMap.get(label);
            } else {
                mapids = new TreeSet<Integer>();
            }
            mapids.add(mapId);
            nodelabelMap.put(label, mapids);
            // Adding the MapName if is a map
            if (elem.isMap()) {
                String mapName=maps.get(elem.getId());
                if (mapName.equals(label))
                    continue;
                else
                    label=mapName;
                LOG.debug("getNodeLabelToMaps: found map with name: {}", label);
            } else {
                String nodename=elemInfoMap.get(elem.getId());
                if (label.equals(nodename))
                    continue;
                else
                    label=nodename;
                LOG.debug("getNodeLabelToMaps: found node with name: {}", label);
            }
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

    /** {@inheritDoc} */
    @Override
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
        inObj.setSummaryLink(mapsPropertiesFactory.getSummaryLink());
        inObj.setSummaryLinkColor(mapsPropertiesFactory.getSummaryLinkColor());
        inObj.setUseSemaphore(mapsPropertiesFactory.isUseSemaphore());
        inObj.setMultilinkStatus(mapsPropertiesFactory.getMultilinkStatus());
        inObj.setMultilinkIgnoreColor(mapsPropertiesFactory.getMultilinkIgnoreColor());
        return inObj;
    }

    /** {@inheritDoc} */
    @Override
    public VMap addElements(VMap map, List<VElement> velems) throws MapsException {
        map.removeAllLinks();
        for (VElement ve: velems) {
            LOG.debug("adding map element to map with id: {}{}", ve.getId(), ve.getType());
            try {
                String type = MapsConstants.NODE_HIDE_TYPE;
                if (ve.isMap()) type =MapsConstants.MAP_HIDE_TYPE;
                VElement hve = getElement(map.getId(), ve.getId(), type);
                if (hve.getLabel() != null) {
                    ve.setLabel(hve.getLabel());
                    LOG.debug("preserving label map is hidden: label found: {}", hve.getLabel());
                }
            } catch (Throwable e) {
               LOG.debug("No Hidden Element found for id: {}{}", ve.getId(), ve.getType());
            }
            map.addElement(ve);
        }            
        map.addLinks(getLinks(map.getElements().values()));
        return map;
    }

    /**
     * <p>reloadConfig</p>
     *
     * @throws org.opennms.web.map.MapsException if any.
     */
    @Override
    public void reloadConfig() throws MapsException {
        try {
            mapsPropertiesFactory.reload(true);
        } catch (FileNotFoundException e) {
            throw new MapsException(e);
        } catch (IOException e) {
            throw new MapsException(e);
        }
    }

    @Override
    public String execCommand(final Command command) {
        String key= UUID.randomUUID().toString();
        commandmap.put(key, command);
        return key;
    }

    @Override
    public Command getCommand(String id) {
        return commandmap.get(id);
    }

    @Override
    public void removeCommand(String id) {
        commandmap.remove(id);
    }

    @Override
    public boolean checkCommandExecution() {
        final Iterator<String> commands = commandmap.keySet().iterator();
        while (commands.hasNext()) {
            final String key = commands.next();
            final Command c = commandmap.get(key);
            
            if (c.runned()) {
                if (c.scheduledToRemove()) {
                    commands.remove();
                } else {
                    c.scheduleToRemove();
                }
            }
        }

        if ( commandmap.size() > 5 ) return false;
        return true;
        
    }
    
    
}
