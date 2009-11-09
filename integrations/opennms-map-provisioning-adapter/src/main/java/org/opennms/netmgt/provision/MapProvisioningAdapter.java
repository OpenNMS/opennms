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
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.MapsAdapterConfig;
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
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;


/**
 * A Dynamic Map provisioning adapter for integration with OpenNMS Provisoning daemon API.
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 *
 */
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
    
    private Set<Integer> deletes;
    private Set<Integer> adds;
    private Set<Integer> updates;

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

    private static Category log() {
        return ThreadCategory.getInstance(MapProvisioningAdapter.class);
    }


    public String getName() {
        return ADAPTER_NAME;
    }

    @Override
    public boolean isNodeReady(AdapterOperation op) {
        return true;
    }


    @Override
    public void processPendingOperationForNode(AdapterOperation op)
            throws ProvisioningAdapterException {
        
        log().info("processPendingOperationsForNode: acquiring lock...");
        synchronized (m_lock) {
            log().debug("processPendingOperationForNode: processing operation: " + op.getType().name() + " for node with Id: #" + op.getNodeId());

            if (op.getType() == AdapterOperationType.ADD) {
                adds.add(op.getNodeId());
            } else if (op.getType() == AdapterOperationType.UPDATE) {
                updates.add(op.getNodeId());
            } else if (op.getType() == AdapterOperationType.DELETE) {
                deletes.add(op.getNodeId());
            }
            if ((adds.size()+deletes.size()+updates.size()) > m_mapsAdapterConfig.getOperationNumberBeforeSync()) {
                reSyncMap();
            }
        log().info("processPendingOperationsForNode: lock released.");
        }

    }    
    
    private void reSyncMap() throws ProvisioningAdapterException {
        m_mapsAdapterConfig.rebuildPackageIpListMap();
        
        m_template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    // first of all delete the element with nodeid ind deletes
                    for (Integer nodeid:deletes) {
                        log().debug("reSyncMap: deleting map element with nodeid: " + nodeid);
                        m_onmsMapElementDao.deleteElementsByNodeid(nodeid);
                    }

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
                                   new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_TYPE,node.getLabel(),celement.getIcon(),xy.getX(),xy.getY())
                                );
                                m_mapNameMapSizeListMap.replace(mapName, ++elementsize);
                            } else {
                                m_onmsMapElementDao.save(
                                   new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_HIDE_TYPE,node.getLabel(),celement.getIcon(),0,0)
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
                                    log().debug("reSyncMap: nodeid: " + nodeid + " is in map:" + mapName + ". skipping...");
                                    elementExist = true;
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
                                   new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_TYPE,node.getLabel(),celement.getIcon(),xy.getX(),xy.getY())
                                );
                                m_mapNameMapSizeListMap.replace(mapName, ++elementsize);
                            } else {
                                m_onmsMapElementDao.save(
                                   new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_HIDE_TYPE,node.getLabel(),celement.getIcon(),0,0)
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
        deletes = new TreeSet<Integer>();
        updates = new TreeSet<Integer>();
        adds = new TreeSet<Integer>();
    }


    public void afterPropertiesSet() throws Exception {
        
        Assert.notNull(m_onmsNodeDao, "Map Provisioning Adapter requires nodeDao property to be set.");
        Assert.notNull(m_onmsMapDao, "Map Provisioning Adapter requires OnmsMapDao property to be set.");
        Assert.notNull(m_onmsMapElementDao, "Map Provisioning Adapter requires OnmsMapElementDao property to be set.");
        Assert.notNull(m_mapsAdapterConfig, "Map Provisioning Adapter requires MapasAdapterConfig property to be set.");
        Assert.notNull(m_eventForwarder, "Map Provisioning Adapter requires EventForwarder property to be set.");

        deletes = new TreeSet<Integer>();
        updates = new TreeSet<Integer>();
        adds = new TreeSet<Integer>();
    }
    
    @Override
    public void init() throws ProvisioningAdapterException {
        MapSyncExecutor e = new MapSyncExecutor();
        new Thread(e).start();        
    }
    
    class MapSyncExecutor implements Runnable {

        public void run() {
                syncMaps();
        }        
    }

    private void syncMaps() throws ProvisioningAdapterException {

        try {
            m_template.execute(new TransactionCallback() {
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
                                       new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_TYPE,node.getLabel(),celement.getIcon(),xy.getX(),xy.getY())
                                    );
                                    m_mapNameMapSizeListMap.replace(mapName, ++elementsize);
                                } else {
                                    m_onmsMapElementDao.save(
                                       new OnmsMapElement(onmsMap,node.getId(),OnmsMapElement.NODE_HIDE_TYPE,node.getLabel(),celement.getIcon(),0,0)
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

    private void addOrUpdate(Integer nodeId) throws Exception {
        log().debug("addOrUpdate: adding or updating the automated maps for the node with nodeid:" + nodeId);

        OnmsNode node = m_onmsNodeDao.get(nodeId);
        if (node == null) throw new Exception("Error Adding element. Node does not exist: nodeid: " + nodeId);
        
        // This is the array with the new elements
        
        Map<String, Celement> celements = m_mapsAdapterConfig.getElementByAddress((getSuitableIp(node)));
        
        Map<String,OnmsMapElement> elemsinmaps = new HashMap<String,OnmsMapElement>();

        // first of all delete all elements not matching the packages
        for (OnmsMapElement elem: m_onmsMapElementDao.findElementsByElementIdAndType(nodeId, OnmsMapElement.NODE_TYPE)) {
            
            if (celements.containsKey(elem.getMap().getName())) { 
                elemsinmaps.put(elem.getMap().getName(), elem);
                log().debug("addOrUpdate: element with label: '" + elem.getLabel() + "' is in map: '" + elem.getMap().getName()+ "'");
                continue;
            }
            
            if (elem.getMap().getType().equals(OnmsMap.AUTOMATICALLY_GENERATED_MAP)) {
                OnmsMap onmsMap = elem.getMap(); 
                log().debug("addOrUpdate: deleting element with label: '" + elem.getLabel() + "' from automated map: '" + elem.getMap().getName()+ "'");

                m_onmsMapElementDao.delete(elem);
                
                onmsMap.setLastModifiedTime(new Date());
                m_onmsMapDao.update(onmsMap);
                
                if (onmsMap.getMapElements().size() == 0)
                    removeEmptySubmap(onmsMap);
            }            
        }
        
        if (celements.isEmpty()) {
            log().info("addOrUpdate: Element is not managed in the adapter configuration file: no package match nodeid: "+nodeId);
            return;
        }
        log().debug("addOrUpdate: found #" + celements.size() + " container automated maps for the nodeid: " +nodeId);

        for (String mapName: celements.keySet()) {
            log().debug("addOrUpdate: found mapName: " + mapName + " container map for the nodeid: " +nodeId);
            Celement celement = celements.get(mapName);
            OnmsMap onmsMap = getSuitableMap(mapName);
            if (onmsMap.getType().equals(OnmsMap.AUTOMATICALLY_GENERATED_MAP)) {
                log().debug("addOrUpdate: found container automated map: " + mapName + " with mapId: " + onmsMap.getId() );
                if (m_mapNameMapSizeListMap.get(mapName) == 0) {
                    log().debug("addOrUpdate: automated map: " + mapName + " has at least 1 element add as submap to container maps");
                    addAsSubMap(mapName);
                }
                OnmsMapElement mapElement = elemsinmaps.get(mapName);
                if (mapElement == null) {
                    log().debug("doAddOrUpdate: adding node: " + node.getLabel() + " to map: " + mapName);
                    int elementsize = m_mapNameMapSizeListMap.get(mapName);
                    log().debug("addOrUpdate: mapElement is new: found last mapElement at position #" + elementsize + " on map: " + mapName);                    
                    XY xy=getXY(onmsMap, elementsize);
                    log().debug("addOrUpdate: mapElement is new: saved last mapElement at X position: " +  xy.getX());
                    log().debug("addOrUpdate: mapElement is new: saved last mapElement at Y position: " +  xy.getY());
                    mapElement = new OnmsMapElement(onmsMap,nodeId,OnmsMapElement.NODE_TYPE,node.getLabel(),celement.getIcon(),xy.getX(),xy.getY());
                    m_mapNameMapSizeListMap.replace(mapName, ++elementsize);
                } else {
                    mapElement.setIconName(celement.getIcon());
                    mapElement.setLabel(node.getLabel());
                    log().debug("doAddOrUpdate: updating node: " + node.getLabel() + " to map: " + mapName);
                }
                m_onmsMapElementDao.saveOrUpdate(mapElement);
                onmsMap.setLastModifiedTime(new Date());
                m_onmsMapDao.update(onmsMap);
            } else if(onmsMap.getType().equals(OnmsMap.AUTOMATIC_SAVED_MAP)) {
                if (elemsinmaps.containsKey(mapName)) {
                    log().debug("addOrUpdate: map element found in static map, doing nothink");
                    continue;
                }
                OnmsMapElement mapElement = new OnmsMapElement(onmsMap,nodeId,OnmsMapElement.NODE_HIDE_TYPE,node.getLabel(),celement.getIcon(),0,0);
                m_onmsMapElementDao.saveOrUpdate(mapElement);
                onmsMap.setLastModifiedTime(new Date());
                m_onmsMapDao.update(onmsMap);
            } else {
                log().warn("addOrUpdate: map: " + mapName + " has mapId: " + onmsMap.getId() + " Type: " + onmsMap.getType());                    
            }
        }
    }

    private void addAsSubMap(String submapName) {
        Map<String,Csubmap> csubmaps = m_mapsAdapterConfig.getContainerMaps(submapName);
        for(String mapName:csubmaps.keySet()) {
            OnmsMap onmsMap = getSuitableMap(mapName);
            String elementType=null;
            log().debug("add SubMaps: the container map is : " + onmsMap.getType());
                        
            if (onmsMap.getType().equals(OnmsMap.AUTOMATICALLY_GENERATED_MAP)) {
                elementType=OnmsMapElement.MAP_TYPE;
            } else if (onmsMap.getType().equals(OnmsMap.AUTOMATIC_SAVED_MAP)) {
                elementType=OnmsMapElement.MAP_HIDE_TYPE;                
            } else {
                log().debug("add SubMaps: skipping....");
                continue;                               
            }
            Csubmap csubmap = csubmaps.get(mapName);
            OnmsMap onmsSubMap = getSuitableMap(csubmap.getName());
            addSubMap(onmsMap, csubmap, onmsSubMap,elementType);
            onmsMap.setLastModifiedTime(new Date());
            m_onmsMapDao.update(onmsMap);
        }
    }
    

    private void sendAndThrow(Exception e) {
        Event event = buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent();
        m_eventForwarder.sendNow(event);
        log().error(e);
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
        }
        return primaryInterface.getIpAddress();
    }

    private OnmsMap getSuitableMap(String mapName){
        OnmsMap onmsMap = null;
        
        Collection<OnmsMap> automaps = m_onmsMapDao.findMapsByNameAndType(mapName, OnmsMap.AUTOMATICALLY_GENERATED_MAP);
       
        if (automaps.size()>0) {
            onmsMap = automaps.iterator().next();
            log().debug("getSuitableMap: found auto map with mapid #" + onmsMap.getMapId() + " for map name:" + mapName );
        } else {
            Collection<OnmsMap> savedmaps = m_onmsMapDao.findMapsByNameAndType(mapName, OnmsMap.AUTOMATIC_SAVED_MAP);
            if (savedmaps.size()>0) {
                onmsMap = savedmaps.iterator().next();
                log().debug("getSuitableMap: found auto saved map with mapid #" + onmsMap.getMapId() + " for map name:" + mapName );
            } else {
                log().debug("getSuitableMap: no map found for name:" + mapName + ". Creating a new map.");
                onmsMap = new OnmsMap();
                onmsMap.setName(mapName);
                onmsMap.setType(OnmsMap.AUTOMATICALLY_GENERATED_MAP);
            }
        }
        return onmsMap;
    }

    private void addSubMap(OnmsMap onmsMap, Csubmap csubmap, OnmsMap onmsSubMap, String type) {
        log().debug("addSubMap: adding automated submap: " + onmsSubMap.getName() + " to map: " + onmsMap.getName());
        if (!onmsMap.getMapElements().isEmpty()) {
            log().debug("addSubMap: looping on elements of not empty map: " + onmsMap.getName());
            for (OnmsMapElement elem: onmsMap.getMapElements()) {
                log().debug("addSubMap: checking element with id: " + elem.getElementId() + " and type" + elem.getType());
                if (elem.getElementId() == onmsSubMap.getId() && 
                        (elem.getType().equals(OnmsMapElement.MAP_TYPE) || elem.getType().equals(OnmsMapElement.MAP_HIDE_TYPE))) {
                    log().debug("addSubMap: still exists in map skipping");
                    return;
                }
            }
        }

        XY xy = new XY();
        if (csubmap.hasX() && csubmap.hasY()) {
            xy.setX(csubmap.getX());
            xy.setY(csubmap.getY());
        } else {
            int elementsize = m_mapNameMapSizeListMap.get(onmsMap.getName());
            xy=getXY(onmsMap, elementsize);
            m_mapNameMapSizeListMap.replace(onmsMap.getName(), ++elementsize);
        }
        
        OnmsMapElement mapElement = 
                new OnmsMapElement(onmsMap,onmsSubMap.getId(),type,csubmap.getLabel(),csubmap.getIcon(),xy.getX(),xy.getY());
        
        m_onmsMapElementDao.saveOrUpdate(mapElement);
 
        log().debug("added map element with id: " + mapElement.getId());
        log().debug("               with label: " + mapElement.getLabel());
        log().debug("                with icon: " + mapElement.getIconName());
        log().debug("                   with X: " + mapElement.getX());
        log().debug("                   with Y: " + mapElement.getY());
    }

    private void doDelete(final Integer nodeId) {
        log().debug("doDelete: deleting mapElements from the automated maps for the node with nodeid:" + nodeId);
        try {
            m_template.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus arg0) {
                    try {                        
                        Collection<OnmsMapElement> elems = m_onmsMapElementDao.findElementsByElementIdAndType(nodeId, OnmsMapElement.NODE_TYPE);
                        elems.addAll(m_onmsMapElementDao.findElementsByElementIdAndType(nodeId, OnmsMapElement.NODE_HIDE_TYPE));
                        for (OnmsMapElement elem: elems) {
                            log().debug("doDelete: deleting element with label: '" + elem.getLabel() + "' from automated map: '" + elem.getMap().getName()+ "'");
                            Integer mapId = elem.getMap().getId();
                            m_onmsMapElementDao.delete(elem);
                            
                            OnmsMap onmsMap = m_onmsMapDao.findMapById(mapId);
                            onmsMap.setLastModifiedTime(new Date());
                            m_onmsMapDao.update(onmsMap);
                            if (onmsMap.getMapElements().size() == 0)
                                removeEmptySubmap(onmsMap);

                        }
                    } catch (Exception e) {
                        log().error(e.getMessage());
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            sendAndThrow(e);
        }
    }

    protected void removeEmptySubmap(OnmsMap submap) {
        log().debug("removeEmptySubmap: verify delete map element which correspond to empty submap: " + submap.getName());
        Map<String,List<Csubmap>> submaps = m_mapsAdapterConfig.getsubMaps();
        for (String mapName : submaps.keySet()) {
            for (Csubmap csubmap : submaps.get(mapName)) {
                if (csubmap.getName().equals(submap.getName())) {
                    if (csubmap.getAddwithoutelements()) continue;
                    log().debug("removeEmptySubmap: delete from container map: '" + mapName +"' empty submap '" + submap.getName() );
                    OnmsMap onmsMap = getSuitableMap(mapName);
                    if (onmsMap.getType().equals(OnmsMap.AUTOMATICALLY_GENERATED_MAP)) {
                        log().debug("removeEmptySubmap: delete from automatic map: '" + mapName +"' empty submap '" + submap.getName() );
                    } else {
                        log().debug("removeEmptySubmap: do not delete from static map: '" + mapName +"' empty submap '" + submap.getName() );
                        continue;
                    }
                    Integer mapid = onmsMap.getId();
                    OnmsMapElement mapElement = m_onmsMapElementDao.findElement(submap.getId(), OnmsMapElement.MAP_TYPE, onmsMap);
                    if (mapElement != null) {
                        m_onmsMapElementDao.delete(mapElement);
                        onmsMap = m_onmsMapDao.findMapById(mapid);
                        onmsMap.setLastModifiedTime(new Date());
                        m_onmsMapDao.update(onmsMap);
                    }
                }
            }
        }
    }

}
