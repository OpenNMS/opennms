/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: December 16, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.provision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Category;
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
 *
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

    public OnmsMapDao getOnmsMapDao() {
        return m_onmsMapDao;
    }

    public void setOnmsMapDao(OnmsMapDao onmsMapDao) {
        m_onmsMapDao = onmsMapDao;
    }

    public OnmsMapElementDao getOnmsMapElementDao() {
        return m_onmsMapElementDao;
    }

    public void setOnmsMapElementDao(OnmsMapElementDao onmsMapElementDao) {
        m_onmsMapElementDao = onmsMapElementDao;
    }

    public MapsAdapterConfig getMapsAdapterConfig() {
        return m_mapsAdapterConfig;
    }

    public void setMapsAdapterConfig(MapsAdapterConfig mapsAdapterConfig) {
        m_mapsAdapterConfig = mapsAdapterConfig;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    public NodeDao getOnmsNodeDao() {
        return m_onmsNodeDao;
    }

    public void setOnmsNodeDao(NodeDao onmsNodeDao) {
        m_onmsNodeDao = onmsNodeDao;
    }

    public TransactionTemplate getTemplate() {
        return m_template;
    }


    public void setTemplate(TransactionTemplate template) {
        m_template = template;
    }

    private static ThreadCategory log() {
        return ThreadCategory.getInstance(MapProvisioningAdapter.class);
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event event) {
        if (isReloadConfigEventTarget(event)) {
            LogUtils.debugf(this, "reloading the maps adapter configuration");
            try {
                MapsAdapterConfigFactory.reload();
                syncMaps();
            } catch (Exception e) {
                LogUtils.infof(this, e, "unable to reload maps adapter configuration");
            }
        }
    }

    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;
        
        List<Parm> parmCollection = event.getParms().getParmCollection();

        for (Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Provisiond.MapProvisioningAdapter".equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        
        log().debug("isReloadConfigEventTarget: Provisiond.MapProvisioningAdapter was target of reload event: " + isTarget);
        return isTarget;
    }

    public String getName() {
        return ADAPTER_NAME;
    }

    @Override
    public boolean isNodeReady(AdapterOperation op) {
        return true;
    }

    class MapSyncExecutor implements Runnable {

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
                            
                        }
                        
                    }
                    int i = m_onmsMapDao.updateAllAutomatedMap(new Date());
                    log().debug("reSyncMap: updated last modified time for automated map: row#: " + i);
                } catch (Exception e) {
                    log().error(e.getMessage());
                    sendAndThrow(e);
                }
                return null;
            }
        });
    }


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
    
    @Override
    public void init() throws ProvisioningAdapterException {
        MapSyncExecutor e = new MapSyncExecutor();
        new Thread(e).start();        
    }
    

    private void syncMaps() throws ProvisioningAdapterException {

        try {
            m_template.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(TransactionStatus arg0) {

                    log().info("syncMaps: acquiring lock...");
                    synchronized (m_lock) {
                        log().debug("syncMaps: lock acquired.  syncing maps...");

                        List<Cmap> cmaps = m_mapsAdapterConfig.getAllMaps();
                        m_mapNameMapSizeListMap = new ConcurrentHashMap<String, Integer>(cmaps.size());
                        Map<String,OnmsMap> mapNames= new ConcurrentHashMap<String,OnmsMap>(cmaps.size());
                        for (Cmap cmap: cmaps) {
                            OnmsMap onmsMap = new OnmsMap();
                            onmsMap.setName(cmap.getMapName());
                            onmsMap.setType(OnmsMap.AUTOMATICALLY_GENERATED_MAP);
                            mapNames.put(cmap.getMapName(),onmsMap);
                        }

                        Date now = new Date();
                        log().debug("syncMaps: sync automated and static maps in database with configuration");
                        
                        log().debug("syncMaps: deleting elements from automated existing map: ");
                        m_onmsMapElementDao.deleteElementsByMapType(OnmsMap.AUTOMATICALLY_GENERATED_MAP);
                        m_onmsMapElementDao.deleteElementsByType(OnmsMapElement.MAP_HIDE_TYPE);
                        m_onmsMapElementDao.deleteElementsByType(OnmsMapElement.NODE_HIDE_TYPE);                        
                        
                        for (OnmsMap onmsMap : m_onmsMapDao.findAutoAndSaveMaps()) {  
                            if ( mapNames.containsKey(onmsMap.getName()) || onmsMap.getType().equals(OnmsMap.AUTOMATIC_SAVED_MAP)) {
                                log().debug("syncMaps: fetching map from db: " +onmsMap.getName() + " type: " + onmsMap.getType());
                                mapNames.put(onmsMap.getName(), onmsMap);
                            } else {
                                log().debug("syncMaps: deleting old automated map: " + onmsMap.getName());
                                log().debug("syncMaps: removing as map Element from all maps.");
                                m_onmsMapElementDao.deleteElementsByElementIdAndType(onmsMap.getId(), OnmsMapElement.MAP_TYPE);
                                log().debug("syncMaps: removing from map table.");
                                m_onmsMapDao.delete(onmsMap);
                            }
                        }
                        
                        for (Cmap cmap: cmaps) {
                            OnmsMap onmsMap = mapNames.get(cmap.getMapName());

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
                        }
                        // adding nodes to auto maps
                        for(OnmsNode node: m_onmsNodeDao.findAllProvisionedNodes()) {
                            log().debug("syncMaps: try to sync automated maps for node element: '" + node.getLabel() +"'");
                            Map<String, Celement> mapNameCelements = m_mapsAdapterConfig.getElementByAddress(getSuitableIp(node));
                            for (String mapName: mapNameCelements.keySet()) {
                                Celement celement = mapNameCelements.get(mapName);
                                OnmsMap onmsMap = mapNames.get(mapName);
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

                        Map<String,List<Csubmap>> submaps = m_mapsAdapterConfig.getsubMaps();
                        
                        for (String mapName : submaps.keySet()) {
                            OnmsMap onmsMap = mapNames.get(mapName);
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
SUBMAP:                     for (Csubmap csubmap : submaps.get(mapName)) {
                                OnmsMap onmsSubMap = mapNames.get(csubmap.getName());
                                log().debug("syncMaps: add submap: " + csubmap.getName());
                                if ( (!csubmap.getAddwithoutelements()) &&
                                    m_mapNameMapSizeListMap.get(csubmap.getName())==0)
                                    continue;
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
                                    for (OnmsMapElement element: elements) {
                                        if (element.getElementId() == onmsSubMap.getId()) continue SUBMAP;
                                    }
                                    m_onmsMapElementDao.save(new OnmsMapElement(onmsMap,onmsSubMap.getId(),OnmsMapElement.MAP_HIDE_TYPE,csubmap.getLabel(),csubmap.getIcon(),0,0));                                                                        
                                }
                                
                            }
                            
                        }
                        log().debug("syncMaps: maps synchronized.  releasing lock...");
                    }
                    log().info("syncMaps: lock released.");
                    return null;
                }

            });
        } catch (Exception e) {
            log().error("syncMaps: Caught exception synchronizing maps: "+e, e);
            throw new ProvisioningAdapterException("syncMaps exception",e);
        }
    }    

    private void sendAndThrow(Exception e) {
        Event event = buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent();
        m_eventForwarder.sendNow(event);
        log().error(e.getMessage());
        throw new ProvisioningAdapterException(MESSAGE_PREFIX, e);
    }

    private EventBuilder buildEvent(String uei) {
        return new EventBuilder(uei, "Provisioner", new Date());
    }
    
    public String getSuitableIp(OnmsNode node){
        OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        
        if (primaryInterface == null) {
            Set<OnmsIpInterface> ipInterfaces = node.getIpInterfaces();
            for (OnmsIpInterface onmsIpInterface : ipInterfaces) {
                    return onmsIpInterface.getIpAddress();
            }
            return "0.0.0.0";
        }
        return primaryInterface.getIpAddress();
    }
    
    private String getLabel(String FQDN) {
        if (FQDN.indexOf(".")>0)
       return FQDN.substring(0, FQDN.indexOf(".")); 
        return FQDN;
    }

}
