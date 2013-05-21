/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.MapsAdapterConfig;
import org.opennms.netmgt.config.MapsAdapterConfigFactory;
import org.opennms.netmgt.config.map.adapter.Celement;
import org.opennms.netmgt.config.map.adapter.Cmap;
import org.opennms.netmgt.config.map.adapter.Csubmap;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.dao.OnmsMapElementDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapElement;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;


/**
 * A Dynamic Map provisioning adapter for integration with OpenNMS Provisioning daemon API.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
@EventListener(name="MapsProvisioningAdapter")
public class MapProvisioningAdapter extends SimpleQueuedProvisioningAdapter implements InitializingBean {
    
    private class XY {
        int x;
        int y;
        
        protected XY(){
            
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
        
        
    }
   
    private XY getXY(OnmsMap map, int mapElementSize) {
        int deltaX = m_mapsAdapterConfig.getMapElementDimension();
        int deltaY = deltaX/2;
        int maxNumberofelementsonX=map.getWidth()/(2*deltaX);
        log().debug("getXY: max number of elements on a row: " +maxNumberofelementsonX);
        int numberofexistingelement = mapElementSize;
        log().debug("getXY: number of existing elements on map: " + mapElementSize);
        int positiononX = 1;
        int positiononY = 1;
        boolean addoffset = true;
        while (maxNumberofelementsonX <= numberofexistingelement){
            numberofexistingelement = numberofexistingelement - maxNumberofelementsonX;
            log().debug("getXY: entering the loop: element found on the row: " + numberofexistingelement);
            positiononY++;
            if (addoffset) {
                maxNumberofelementsonX--;
            } else {
                maxNumberofelementsonX++;
            }
            addoffset = !addoffset;
        }
        positiononX = positiononX + numberofexistingelement;
        XY xy = new XY();
        if (addoffset) {
            xy.setX(2*deltaX*positiononX-deltaX);
        } else {
            xy.setX(2*deltaX*positiononX);
        }
        xy.setY(deltaY*positiononY);
        return xy;
    }

    private Object m_lock = new Object();
    private NodeDao m_onmsNodeDao;
    private OnmsMapDao m_onmsMapDao;
    private OnmsMapElementDao m_onmsMapElementDao;
    private EventForwarder m_eventForwarder;
    private MapsAdapterConfig m_mapsAdapterConfig;
    
    private TransactionTemplate m_template;
    
    private volatile static ConcurrentMap<String,Integer> m_mapNameMapSizeListMap;
    
    private static final String MESSAGE_PREFIX = "Dynamic Map provisioning failed: ";
    private static final String ADAPTER_NAME="MAP Provisioning Adapter";
    
    private static final long RESYNC_TIMEOUT=300000;
    
    private Set<Integer> m_deletes;
    private Set<Integer> m_adds;
    private Set<Integer> m_updates;
    
    private boolean doSync = false;

    /**
     * <p>getOnmsMapDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.OnmsMapDao} object.
     */
    public OnmsMapDao getOnmsMapDao() {
        return m_onmsMapDao;
    }

    /**
     * <p>setOnmsMapDao</p>
     *
     * @param onmsMapDao a {@link org.opennms.netmgt.dao.OnmsMapDao} object.
     */
    public void setOnmsMapDao(OnmsMapDao onmsMapDao) {
        m_onmsMapDao = onmsMapDao;
    }

    /**
     * <p>getOnmsMapElementDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.OnmsMapElementDao} object.
     */
    public OnmsMapElementDao getOnmsMapElementDao() {
        return m_onmsMapElementDao;
    }

    /**
     * <p>setOnmsMapElementDao</p>
     *
     * @param onmsMapElementDao a {@link org.opennms.netmgt.dao.OnmsMapElementDao} object.
     */
    public void setOnmsMapElementDao(OnmsMapElementDao onmsMapElementDao) {
        m_onmsMapElementDao = onmsMapElementDao;
    }

    /**
     * <p>getMapsAdapterConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.MapsAdapterConfig} object.
     */
    public MapsAdapterConfig getMapsAdapterConfig() {
        return m_mapsAdapterConfig;
    }

    /**
     * <p>setMapsAdapterConfig</p>
     *
     * @param mapsAdapterConfig a {@link org.opennms.netmgt.config.MapsAdapterConfig} object.
     */
    public void setMapsAdapterConfig(MapsAdapterConfig mapsAdapterConfig) {
        m_mapsAdapterConfig = mapsAdapterConfig;
    }

    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * <p>getOnmsNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public NodeDao getOnmsNodeDao() {
        return m_onmsNodeDao;
    }

    /**
     * <p>setOnmsNodeDao</p>
     *
     * @param onmsNodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setOnmsNodeDao(NodeDao onmsNodeDao) {
        m_onmsNodeDao = onmsNodeDao;
    }

    /**
     * <p>getTemplate</p>
     *
     * @return a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public TransactionTemplate getTemplate() {
        return m_template;
    }


    /**
     * <p>setTemplate</p>
     *
     * @param template a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public void setTemplate(TransactionTemplate template) {
        m_template = template;
    }

    private static ThreadCategory log() {
        return ThreadCategory.getInstance(MapProvisioningAdapter.class);
    }

    /**
     * <p>handleReloadConfigEvent</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event event) {
        if (isReloadConfigEventTarget(event)) {
            LogUtils.debugf(this, "reloading the maps adapter configuration");
            try {
                MapsAdapterConfigFactory.reload();
                syncMaps();
            } catch (Throwable e) {
                LogUtils.infof(this, e, "unable to reload maps adapter configuration");
            }
        }
    }

    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;
        
        List<Parm> parmCollection = event.getParmCollection();

        for (Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Provisiond.MapProvisioningAdapter".equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        
        log().debug("isReloadConfigEventTarget: Provisiond.MapProvisioningAdapter was target of reload event: " + isTarget);
        return isTarget;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return ADAPTER_NAME;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNodeReady(AdapterOperation op) {
        return true;
    }

    class MapSyncExecutor implements Runnable {

        @Override
        public void run() {
            syncMaps();
            while (true) {
                try {
                    log().debug("Sleeping: " + RESYNC_TIMEOUT);
                    Thread.sleep(RESYNC_TIMEOUT);
                } catch (InterruptedException e) {
                    log().error("Cannot sleep:" + e.getLocalizedMessage());
                }
                
                if (doSync) {
                    log().debug("Synchronization started");
                    Set<Integer> deletes = new TreeSet<Integer>();
                    Set<Integer> adds = new TreeSet<Integer>();
                    Set<Integer> updates = new TreeSet<Integer>();
                    log().info("acquiring lock...");
                    synchronized (m_lock) {                            
                        for (Integer i: m_deletes) {
                            deletes.add(i);
                        }                        
                        for (Integer i: m_adds) {
                            adds.add(i);
                        }
                        for (Integer i: m_updates) {
                            updates.add(i);
                        }
                        m_deletes = new TreeSet<Integer>();
                        m_updates = new TreeSet<Integer>();
                        m_adds = new TreeSet<Integer>();                            
                        doSync = false;
                    }
                    log().info("lock released.");
                    reSyncMap(deletes,adds,updates);
                } else {
                    log().debug("No Synchronization required");
                    
                }
            }
        }        
    }

    /** {@inheritDoc} */
    @Override
    public void processPendingOperationForNode(AdapterOperation op)
            throws ProvisioningAdapterException {
        
        log().info("processPendingOperationsForNode: acquiring lock...");
        synchronized (m_lock) {
            log().debug("processPendingOperationForNode: processing operation: " + op.getType().name() + " for node with Id: #" + op.getNodeId());

            if (op.getType() == AdapterOperationType.ADD) {
                m_adds.add(op.getNodeId());
            } else if (op.getType() == AdapterOperationType.UPDATE) {
                m_updates.add(op.getNodeId());
            } else if (op.getType() == AdapterOperationType.DELETE) {
                m_deletes.add(op.getNodeId());
            }
            doSync=true;
        }
        log().info("processPendingOperationsForNode: lock released.");

    }    
    
    private void reSyncMap(final Set<Integer> deletes,final Set<Integer> adds,final Set<Integer> updates) throws ProvisioningAdapterException {
        m_mapsAdapterConfig.rebuildPackageIpListMap();
        
        m_template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    // first of all delete the element with nodeid ind deletes
                    for (Integer nodeid: deletes) {
                        log().debug("reSyncMap: deleting map element with nodeid: " + nodeid);
                        m_onmsMapElementDao.deleteElementsByNodeid(nodeid);
                    }

                    // skip operation if there are only deletes
                    if (adds.isEmpty() && updates.isEmpty())
                        return null;

                    Map<String,OnmsMap> mapNames= new ConcurrentHashMap<String,OnmsMap>(m_mapNameMapSizeListMap.size());
                    
                    for (OnmsMap onmsMap : m_onmsMapDao.findAutoAndSaveMaps()) {  
                        if ( m_mapNameMapSizeListMap.containsKey(onmsMap.getName()) || onmsMap.getType().equals(OnmsMap.AUTOMATIC_SAVED_MAP)) {
                            log().debug("reSyncMaps: fetching map from db: " +onmsMap.getName() + " type: " + onmsMap.getType());
                            mapNames.put(onmsMap.getName(), onmsMap);
                        }
                    }
                    
                    for(Integer nodeid: adds) {
                        log().debug("reSyncMap: adding map elements with nodeid: " + nodeid);
                        if (deletes.contains(nodeid)) {
                            log().debug("reSyncMap: skipping because was deleted");
                            continue;
                        }
                        if (updates.contains(nodeid)) {
                            log().debug("reSyncMap: skipping because was updated");
                            continue;
                        }
                        OnmsNode node = m_onmsNodeDao.get(nodeid);
                        Map<String, Celement> mapNameCelements = m_mapsAdapterConfig.getElementByAddress(getSuitableIp(node));
                        for (String mapName: mapNameCelements.keySet()) {
                            log().debug("reSyncMap: add: found container map: " + mapName);
                            if (!mapNames.containsKey(mapName)) {
                                log().debug("reSyncMap: map: " + mapName + " not in database. skipping....");
                                continue;
                            }
                            Celement celement = mapNameCelements.get(mapName);
                            OnmsMap onmsMap = mapNames.get(mapName);
                            if (onmsMap.getType().equals(OnmsMap.AUTOMATICALLY_GENERATED_MAP)) {
                                log().debug("reSyncMap: adding node: " + node.getLabel() + " to map: " + mapName);
                                int elementsize = m_mapNameMapSizeListMap.get(mapName);
                                log().debug("reSyncMap: mapElement is new: found last mapElement at position #" + elementsize + " on map: " + mapName);                    
                                XY xy=getXY(onmsMap, elementsize);
                                log().debug("reSyncMaps: mapElement is new: saved last mapElement at X position: " +  xy.getX());
                                log().debug("reSyncMap: mapElement is new: saved last mapElement at Y position: " +  xy.getY());
                                m_onmsMapElementDao.save(
                                   new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_TYPE,getLabel(node.getLabel()),celement.getIcon(),xy.getX(),xy.getY())
                                );
                                m_mapNameMapSizeListMap.replace(mapName, ++elementsize);
                            } else {
                                m_onmsMapElementDao.save(
                                   new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_HIDE_TYPE,getLabel(node.getLabel()),celement.getIcon(),0,0)
                                );   
                            }
                            m_onmsMapElementDao.flush();
                        }
                    } // end add nodes loop
                    
                    for(Integer nodeid: updates) {
                        log().debug("reSyncMap: updating map elements with nodeid: " + nodeid);
                        if (deletes.contains(nodeid)) {
                            log().debug("reSyncMap: skipping because was deleted");
                            continue;
                        }                        
                        OnmsNode node = m_onmsNodeDao.get(nodeid);
                        Collection<OnmsMapElement> elements = m_onmsMapElementDao.findElementsByNodeId(nodeid);
                        Map<String, Celement> mapNameCelements = m_mapsAdapterConfig.getElementByAddress(getSuitableIp(node));
                        for (String mapName: mapNameCelements.keySet()) {
                            log().debug("reSyncMap: update: found container map: " + mapName);
                            if (!mapNames.containsKey(mapName)) {
                                log().debug("reSyncMap: map: " + mapName + " not in database. skipping....");
                                continue;
                            }
                            Celement celement = mapNameCelements.get(mapName);
                            OnmsMap onmsMap = mapNames.get(mapName);
                            Collection<OnmsMapElement> tempElem = new ArrayList<OnmsMapElement>();
                            boolean elementExist = false;
                            for (OnmsMapElement elem: elements) {
                                if (elem.getMap().getId() == onmsMap.getId() ) {
                                    elementExist = true;
                                    String label = getLabel(node.getLabel());
                                    if (elem.getLabel().equals(label)) { 
                                       log().debug("reSyncMap: nodeid: " + nodeid + " is in map:" + mapName + " and has the same label. skipping...");
                                    } else {
                                       log().debug("reSyncMap: nodeid: " + nodeid + " is in map:" + mapName + " and has not the same label. updating...");
                                       elem.setLabel(label);
                                       m_onmsMapElementDao.update(elem);
                                    }
                                    continue;
                                }
                                tempElem.add(elem);
                            }
                            elements = tempElem;
                            if (elementExist)
                                continue;
                            if (onmsMap.getType().equals(OnmsMap.AUTOMATICALLY_GENERATED_MAP)) {
                                log().debug("reSyncMap: adding node: " + node.getLabel() + " to map: " + mapName);
                                int elementsize = m_mapNameMapSizeListMap.get(mapName);
                                log().debug("reSyncMap: mapElement is new: found last mapElement at position #" + elementsize + " on map: " + mapName);                    
                                XY xy=getXY(onmsMap, elementsize);
                                log().debug("reSyncMaps: mapElement is new: saved last mapElement at X position: " +  xy.getX());
                                log().debug("reSyncMap: mapElement is new: saved last mapElement at Y position: " +  xy.getY());
                                m_onmsMapElementDao.save(
                                   new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_TYPE,getLabel(node.getLabel()),celement.getIcon(),xy.getX(),xy.getY())
                                );
                                m_mapNameMapSizeListMap.replace(mapName, ++elementsize);
                            } else {
                                m_onmsMapElementDao.save(
                                   new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_HIDE_TYPE,getLabel(node.getLabel()),celement.getIcon(),0,0)
                                );   
                            }
                            m_onmsMapElementDao.flush();
                        }
                        // delete elements from automated map
                        for(OnmsMapElement element: elements) {
                            if (element.getMap().getType().equals(OnmsMap.AUTOMATICALLY_GENERATED_MAP)) 
                                m_onmsMapElementDao.delete(element);                                
                        }
                    } // end updates loop

                    // add not empty map and remove empty submap
                    Map<String,List<Csubmap>> mapnameSubmapMap = m_mapsAdapterConfig.getsubMaps();
                    Map<String,Integer> mapNameSizeMap = new ConcurrentHashMap<String, Integer>();
                    
                    for (String mapName : mapnameSubmapMap.keySet()) {
                        log().debug("reSyncMap: update sub maps: found container map: " + mapName);
                        if (!mapNames.containsKey(mapName)) {
                            log().debug("reSyncMap: map: " + mapName + " not in database. skipping....");
                            continue;
                        }
                        OnmsMap onmsMap = mapNames.get(mapName);
                        log().debug("reSyncMaps: map type: " + onmsMap.getType());
                        
                        boolean auto;
                        Collection<OnmsMapElement> elements = m_onmsMapElementDao.findElementsByMapIdAndType(onmsMap.getId(), OnmsMapElement.MAP_TYPE);
                        
                        if (onmsMap.getType().equals(OnmsMap.AUTOMATICALLY_GENERATED_MAP)) {
                            auto = true;
                        } else if (onmsMap.getType().equals(OnmsMap.AUTOMATIC_SAVED_MAP)) {
                            auto = false;
                            elements.addAll(m_onmsMapElementDao.findElementsByMapIdAndType(onmsMap.getId(), OnmsMapElement.MAP_HIDE_TYPE));
                        } else {
                            log().debug("reSyncMaps: cannot add submaps to map: " + mapName);
                            continue;
                        }
                        // loop on submaps
                        for (Csubmap csubmap : mapnameSubmapMap.get(mapName)) {
                            log().debug("reSyncMaps: submap: " + csubmap.getName());

                            if (! mapNames.containsKey(csubmap.getName())) {
                                log().debug("reSyncMap: map: " + csubmap.getName() + " not in database. skipping....");
                                continue;
                            }
                            OnmsMap onmsSubMap = mapNames.get(csubmap.getName());
                            if (!mapNameSizeMap.containsKey(onmsSubMap.getName())) {
                                mapNameSizeMap.put(csubmap.getName(), m_onmsMapElementDao.countElementsOnMap(onmsSubMap.getId()));
                            }
                            
                            //Loop to verify if the map exists
                            Collection<OnmsMapElement> tempelems = new ArrayList<OnmsMapElement>();
                            OnmsMapElement foundelement= null;
                            for (OnmsMapElement element: elements) {
                                if (element.getElementId() == onmsSubMap.getId()){
                                    foundelement=element;
                                    log().debug("reSyncMap: map with id: " + onmsSubMap.getId() + " is in map:" + mapName + ".");
                                    continue;
                                }
                                tempelems.add(element);
                            }
                            elements = tempelems;
                            
                            if ( (!csubmap.getAddwithoutelements()) &&
                                    mapNameSizeMap.get(csubmap.getName())==0) {
                                if (foundelement != null && (auto|| foundelement.getType().equals(OnmsMapElement.MAP_HIDE_TYPE)))
                                    m_onmsMapElementDao.delete(foundelement);
                                continue;
                            }
                            
                            if (foundelement != null)
                                continue;
                            
                            log().debug("ReSyncMaps: add submap: " + csubmap.getName() + "to map: " + mapName);
                            if (auto) {
                                XY xy = new XY();
                                if (csubmap.hasX() && csubmap.hasY()) {
                                    xy.setX(csubmap.getX());
                                    xy.setY(csubmap.getY());
                                } else {
                                    int elementsize = m_mapNameMapSizeListMap.get(mapName);
                                    xy=getXY(onmsMap, elementsize);
                                    m_mapNameMapSizeListMap.replace(mapName, ++elementsize);
                                }                                    
                                m_onmsMapElementDao.save(new OnmsMapElement(onmsMap,onmsSubMap.getId(),OnmsMapElement.MAP_TYPE,csubmap.getLabel(),csubmap.getIcon(),xy.getX(),xy.getY()));                                    
                            } else {
                                m_onmsMapElementDao.save(new OnmsMapElement(onmsMap,onmsSubMap.getId(),OnmsMapElement.MAP_HIDE_TYPE,csubmap.getLabel(),csubmap.getIcon(),0,0));                                                                        
                            }
                            m_onmsMapElementDao.flush();
                            
                        }
                        
                    }
                    int i = m_onmsMapDao.updateAllAutomatedMap(new Date());
                    log().debug("reSyncMap: updated last modified time for automated map: row#: " + i);
                } catch (Throwable e) {
                    log().error(e.getMessage());
                    sendAndThrow(e);
                }
                return null;
            }
        });
    }


    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        
        Assert.notNull(m_onmsNodeDao, "Map Provisioning Adapter requires nodeDao property to be set.");
        Assert.notNull(m_onmsMapDao, "Map Provisioning Adapter requires OnmsMapDao property to be set.");
        Assert.notNull(m_onmsMapElementDao, "Map Provisioning Adapter requires OnmsMapElementDao property to be set.");
        Assert.notNull(m_mapsAdapterConfig, "Map Provisioning Adapter requires MapasAdapterConfig property to be set.");
        Assert.notNull(m_eventForwarder, "Map Provisioning Adapter requires EventForwarder property to be set.");

        m_deletes = new TreeSet<Integer>();
        m_updates = new TreeSet<Integer>();
        m_adds = new TreeSet<Integer>();
    }
    
    /** {@inheritDoc} */
    @Override
    public void init() throws ProvisioningAdapterException {
        MapSyncExecutor e = new MapSyncExecutor();
        new Thread(e, MapSyncExecutor.class.getSimpleName()).start();
    }
    

    private void syncMaps() throws ProvisioningAdapterException {

        try {
            m_template.execute(new TransactionCallback<Object>() {
                @Override
                public Object doInTransaction(TransactionStatus arg0) {

                    log().info("syncMaps: acquiring lock...");
                    synchronized (m_lock) {
                        log().debug("syncMaps: lock acquired.  syncing maps...");

                        m_mapsAdapterConfig.getReadLock().lock();
                        
                        try {
                            final List<Cmap> cmaps = m_mapsAdapterConfig.getAllMaps();
                            m_mapNameMapSizeListMap = new ConcurrentHashMap<String, Integer>(cmaps.size());
                            final Map<String,OnmsMap> mapNames= new ConcurrentHashMap<String,OnmsMap>(cmaps.size());
                            for (Cmap cmap: cmaps) {
                                final OnmsMap onmsMap = new OnmsMap();
                                onmsMap.setName(cmap.getMapName());
                                onmsMap.setType(OnmsMap.AUTOMATICALLY_GENERATED_MAP);
                                mapNames.put(cmap.getMapName(),onmsMap);
                            }
    
                            final Date now = new Date();
                            log().debug("syncMaps: sync automated and static maps in database with configuration");
                            
                            log().debug("syncMaps: deleting elements from automated existing map: ");
                            m_onmsMapElementDao.deleteElementsByMapType(OnmsMap.AUTOMATICALLY_GENERATED_MAP);
                            m_onmsMapElementDao.deleteElementsByType(OnmsMapElement.MAP_HIDE_TYPE);
                            m_onmsMapElementDao.deleteElementsByType(OnmsMapElement.NODE_HIDE_TYPE);                        
                            
                            for (final OnmsMap onmsMap : m_onmsMapDao.findAutoAndSaveMaps()) {  
                                if ( mapNames.containsKey(onmsMap.getName()) || onmsMap.getType().equals(OnmsMap.AUTOMATIC_SAVED_MAP)) {
                                    //Move a map from Static to User if its no longer in the mapsadapter-configuration.xml 
                                    if(onmsMap.getType().equals(OnmsMap.AUTOMATIC_SAVED_MAP) && !mapNames.containsKey(onmsMap.getName())) {
                                        onmsMap.setType(OnmsMap.USER_GENERATED_MAP);
                                    }
                                    log().debug("syncMaps: fetching map from db: " +onmsMap.getName() + " type: " + onmsMap.getType());
                                    mapNames.put(onmsMap.getName(), onmsMap);
                                } else {
                                    log().debug("syncMaps: deleting old automated map: " + onmsMap.getName());
                                    log().debug("syncMaps: removing as map Element from all maps.");
                                    m_onmsMapElementDao.deleteElementsByElementIdAndType(onmsMap.getId(), OnmsMapElement.MAP_TYPE);
                                    log().debug("syncMaps: removing from map table.");
                                    m_onmsMapDao.delete(onmsMap);
                                    m_onmsMapElementDao.flush();
                                    m_onmsMapDao.flush();
                                }
                            }
                            
                            for (final Cmap cmap: cmaps) {
                                final OnmsMap onmsMap = mapNames.get(cmap.getMapName());
    
                                if (onmsMap.getType().equals(OnmsMap.AUTOMATICALLY_GENERATED_MAP)) {
                                    log().debug("syncMaps: sync automated map: " + onmsMap.getName());
        
                                    onmsMap.setOwner(cmap.getMapOwner());
                                    onmsMap.setUserLastModifies(cmap.getMapOwner());
                                    onmsMap.setMapGroup(cmap.getMapGroup());
                                    onmsMap.setAccessMode(cmap.getMapAccess());
                                    onmsMap.setBackground(cmap.getMapBG());
                                    onmsMap.setHeight(cmap.getMapHeight());
                                    onmsMap.setWidth(cmap.getMapWidth());
                                    onmsMap.setLastModifiedTime(now);
        
                                    m_onmsMapDao.saveOrUpdate(onmsMap);
                                    m_mapNameMapSizeListMap.put(cmap.getMapName(),0);
                                } else {
                                    log().debug("syncMaps: skipping not automated map: " + onmsMap.getName());
                                    log().debug("syncMaps: map type: " + onmsMap.getType());
                                }
                                m_onmsMapElementDao.flush();
                            }
                            // adding nodes to auto maps
                            for(final OnmsNode node: m_onmsNodeDao.findAllProvisionedNodes()) {
                                log().debug("syncMaps: try to sync automated maps for node element: '" + node.getLabel() +"'");
                                final Map<String, Celement> mapNameCelements = m_mapsAdapterConfig.getElementByAddress(getSuitableIp(node));
                                for (final String mapName: mapNameCelements.keySet()) {
                                    final Celement celement = mapNameCelements.get(mapName);
                                    final OnmsMap onmsMap = mapNames.get(mapName);
                                    if (onmsMap.getType().equals(OnmsMap.AUTOMATICALLY_GENERATED_MAP)) {
                                        log().debug("syncMaps: adding node: " + node.getLabel() + " to map: " + mapName);
                                        int elementsize = m_mapNameMapSizeListMap.get(mapName);
                                        log().debug("syncMaps: mapElement is new: found last mapElement at position #" + elementsize + " on map: " + mapName);                    
                                        XY xy=getXY(onmsMap, elementsize);
                                        log().debug("syncMaps: mapElement is new: saved last mapElement at X position: " +  xy.getX());
                                        log().debug("syncMaps: mapElement is new: saved last mapElement at Y position: " +  xy.getY());
                                        m_onmsMapElementDao.save(
                                           new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_TYPE,getLabel(node.getLabel()),celement.getIcon(),xy.getX(),xy.getY())
                                        );
                                        m_mapNameMapSizeListMap.replace(mapName, ++elementsize);
                                    } else  if (m_onmsMapElementDao.findElement(node.getId(), OnmsMapElement.NODE_TYPE, onmsMap) == null &&
                                                m_onmsMapElementDao.findElement(node.getId(), OnmsMapElement.NODE_HIDE_TYPE, onmsMap) == null) {
                                        m_onmsMapElementDao.save(
                                           new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_HIDE_TYPE,getLabel(node.getLabel()),celement.getIcon(),0,0)
                                        );   
                                    }
                                }
                            }
    
                            final Map<String,List<Csubmap>> submaps = m_mapsAdapterConfig.getsubMaps();
                            
                            for (final String mapName : submaps.keySet()) {
                                final OnmsMap onmsMap = mapNames.get(mapName);
                                log().debug("syncMaps: found container map: " + mapName + " type: " + onmsMap.getType());
                                Collection<OnmsMapElement> elements = new ArrayList<OnmsMapElement>();
                                boolean auto;
                                if (onmsMap.getType().equals(OnmsMap.AUTOMATICALLY_GENERATED_MAP)) {
                                    auto = true;
                                } else if (onmsMap.getType().equals(OnmsMap.AUTOMATIC_SAVED_MAP)) {
                                    elements = m_onmsMapElementDao.findElementsByMapIdAndType(onmsMap.getId(), OnmsMapElement.MAP_TYPE);
                                    auto = false;
                                } else {
                                    log().debug("syncMaps: cannot add submaps to map: " + mapName);
                                    continue;
                                }
                                SUBMAP: for (final Csubmap csubmap : submaps.get(mapName)) {
                                    final OnmsMap onmsSubMap = mapNames.get(csubmap.getName());
                                    log().debug("syncMaps: add submap: " + csubmap.getName());
                                    if ( (!csubmap.getAddwithoutelements()) &&
                                        m_mapNameMapSizeListMap.get(csubmap.getName())==0) {
                                        continue;
                                    }
                                    log().debug("syncMaps: add submap: " + csubmap.getName() + "to map: " + mapName);
                                    if (auto) {
                                        XY xy = new XY();
                                        if (csubmap.hasX() && csubmap.hasY()) {
                                            xy.setX(csubmap.getX());
                                            xy.setY(csubmap.getY());
                                        } else {
                                            int elementsize = m_mapNameMapSizeListMap.get(mapName);
                                            xy=getXY(onmsMap, elementsize);
                                            m_mapNameMapSizeListMap.replace(mapName, ++elementsize);
                                        }                                    
                                        m_onmsMapElementDao.save(new OnmsMapElement(onmsMap,onmsSubMap.getId(),OnmsMapElement.MAP_TYPE,csubmap.getLabel(),csubmap.getIcon(),xy.getX(),xy.getY()));                                    
                                    } else {
                                        for (final OnmsMapElement element: elements) {
                                            if (element.getElementId() == onmsSubMap.getId()) continue SUBMAP;
                                        }
                                        m_onmsMapElementDao.save(new OnmsMapElement(onmsMap,onmsSubMap.getId(),OnmsMapElement.MAP_HIDE_TYPE,csubmap.getLabel(),csubmap.getIcon(),0,0));                                                                        
                                    }
                                    
                                }
                                m_onmsMapElementDao.flush();
                                
                            }
                            log().debug("syncMaps: maps synchronized.  releasing lock...");
                        } finally {
                            m_mapsAdapterConfig.getReadLock().unlock();
                        }
                    }
                    log().info("syncMaps: lock released.");
                    return null;
                }

            });
        } catch (final Exception e) {
            log().error("syncMaps: Caught exception synchronizing maps: "+e, e);
            throw new ProvisioningAdapterException("syncMaps exception",e);
        }
    }    

    private void sendAndThrow(final Throwable e) {
        final Event event = buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent();
        m_eventForwarder.sendNow(event);
        log().error(e.getMessage());
        throw new ProvisioningAdapterException(MESSAGE_PREFIX, e);
    }

    private EventBuilder buildEvent(final String uei) {
        return new EventBuilder(uei, "Provisioner", new Date());
    }
    
    /**
     * <p>getSuitableIp</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link java.lang.String} object.
     */
    public String getSuitableIp(final OnmsNode node){
        final OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        
        if (primaryInterface == null) {
            final Set<OnmsIpInterface> ipInterfaces = node.getIpInterfaces();
            for (final OnmsIpInterface onmsIpInterface : ipInterfaces) {
                    return str(onmsIpInterface.getIpAddress());
            }
            return "0.0.0.0";
        }
        return str(primaryInterface.getIpAddress());
    }
    
    private String getLabel(final String FQDN) {
    	if (FQDN.indexOf(".")>0 && !validate(FQDN))
            return FQDN.substring(0, FQDN.indexOf(".")); 			
        return FQDN;
    }
    
    private static final String IPADDRESS_PATTERN = 
		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"; 
 
    private static final Pattern m_pattern = Pattern.compile(IPADDRESS_PATTERN);

   /**
    * Validate ip address with regular expression
    * @param ip ip address for validation
    * @return true valid ip address, false invalid ip address
    */
    private boolean validate(final String ip) {	  
        final Matcher matcher = m_pattern.matcher(ip);
        return matcher.matches();
    }

}
