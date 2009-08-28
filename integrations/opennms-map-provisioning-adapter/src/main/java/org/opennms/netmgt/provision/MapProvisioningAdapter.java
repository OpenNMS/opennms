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
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.Set;
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
   
    private Object m_lock = new Object();
    private NodeDao m_onmsNodeDao;
    private OnmsMapDao m_onmsMapDao;
    private OnmsMapElementDao m_onmsMapElementDao;
    private EventForwarder m_eventForwarder;
    private MapsAdapterConfig m_mapsAdapterConfig;
    
    private TransactionTemplate m_template;
    
    private int m_nodeId;

    private static final String MESSAGE_PREFIX = "Dynamic Map provisioning failed: ";
    private static final String ADAPTER_NAME="MAP Provisioning Adapter";

    private volatile static ConcurrentMap<Integer, List<OnmsMapElement>> m_onmsNodeMapElementListMap;
    
    private volatile static ConcurrentMap<String,Integer> m_mapNameMapSizeListMap;
    
    private List<OnmsMapElement> m_onmsNodeMapElementToDelete;

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

            m_mapsAdapterConfig.rebuildPackageIpListMap();


            if (op.getType() == AdapterOperationType.ADD) {
                m_onmsNodeMapElementListMap.put(op.getNodeId(), new ArrayList<OnmsMapElement>());
                doAddOrUpdate(op.getNodeId());
            } else if (op.getType() == AdapterOperationType.UPDATE) {
                doAddOrUpdate(op.getNodeId());
            } else if (op.getType() == AdapterOperationType.DELETE) {
                doDelete(op.getNodeId());
            }

        }
        log().info("processPendingOperationsForNode: lock released.");
    }    
    
    public void afterPropertiesSet() throws Exception {
        
        Assert.notNull(m_onmsNodeDao, "Map Provisioning Adapter requires nodeDao property to be set.");
        Assert.notNull(m_onmsMapDao, "Map Provisioning Adapter requires OnmsMapDao property to be set.");
        Assert.notNull(m_onmsMapElementDao, "Map Provisioning Adapter requires OnmsMapElementDao property to be set.");
        Assert.notNull(m_mapsAdapterConfig, "Map Provisioning Adapter requires MapasAdapterConfig property to be set.");
        Assert.notNull(m_eventForwarder, "Map Provisioning Adapter requires EventForwarder property to be set.");


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
                        log().debug("syncMaps: lock aqcuired.  syncing maps...");

                        List<Cmap> cmaps = m_mapsAdapterConfig.getAllMaps();
                        m_mapNameMapSizeListMap = new ConcurrentHashMap<String, Integer>(cmaps.size());

                        List<OnmsNode> nodes = m_onmsNodeDao.findAllProvisionedNodes();
                        m_onmsNodeMapElementListMap = new ConcurrentHashMap<Integer, List<OnmsMapElement>>(nodes.size());

                        Date now = new Date();
                        log().debug("syncMaps: sync automated maps in database with configuration");

                        for (OnmsMap onmsMap : m_onmsMapDao.findAutoMaps()) {
                            log().debug("syncMaps: deleting old automated map: " + onmsMap.getName());
                            m_onmsMapDao.delete(onmsMap);
                            m_onmsMapDao.flush();
                        }

                        for (Cmap cmap: cmaps) {
                            OnmsMap onmsMap = getSuitableMap(cmap.getMapName());

                            log().debug("syncMaps: adding new automated map: " + onmsMap.getName());

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

                        }
                        m_onmsMapDao.flush();
                        m_onmsMapDao.clear();

                        for(OnmsNode node: nodes) {
                            log().debug("syncMaps: try to add to automated maps node element: '" + node.getLabel() +"'");
                            m_onmsNodeMapElementListMap.put(node.getId(), new ArrayList<OnmsMapElement>());
                            doAddOrUpdate(node.getId());
                        }

                        Map<String,List<Csubmap>> mapnameSubmapMap = m_mapsAdapterConfig.getsubMaps();
                        for (String mapName : mapnameSubmapMap.keySet()) {
                            log().debug("syncMaps: adding automated submap: " + mapName);
                            OnmsMap onmsMap = getSuitableMap(mapName);
                            for (Csubmap csubmap : mapnameSubmapMap.get(mapName)) {
                                OnmsMap onmsSubMap = getSuitableMap(csubmap.getName());
                                if (onmsSubMap.isNew()) {
                                    log().error("syncMap: add SubMaps: the submap does not exist: " + csubmap.getName());
                                    continue;
                                }
                                if (onmsSubMap.getMapElements().size() > 0 || csubmap.getAddwithoutelements()) {
                                    addSubMap(onmsMap,csubmap,onmsSubMap);
                                    onmsMap.setLastModifiedTime(new Date());
                                    m_onmsMapDao.update(onmsMap);
                                    m_onmsMapDao.flush();
                                }
                            }
                            m_onmsMapDao.clear();
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

    private void addSubMap(OnmsMap onmsMap, Csubmap csubmap, OnmsMap onmsSubMap) {
        log().debug("addSubMap: adding automated submap: " + onmsSubMap.getName() + " to map: " + onmsMap.getName());
        OnmsMapElement  mapElement = null;
        if (!onmsMap.getMapElements().isEmpty()) {
            log().debug("addSubMap: looping on elements of not empty map: " + onmsMap.getName());
            for (OnmsMapElement elem: onmsMap.getMapElements()) {
                log().debug("addSubMap: checking element with id: " + elem.getElementId() + " and type" + elem.getType());
                if (elem.getType().equals(OnmsMapElement.MAP_TYPE) && elem.getElementId() == onmsSubMap.getId()) {
                    log().debug("addSubMap: still exists in map updating");
                    mapElement = elem;
                    mapElement.setLabel(csubmap.getLabel());
                    mapElement.setIconName(csubmap.getIcon());
                    mapElement.setX(csubmap.getX());
                    mapElement.setY(csubmap.getY());
                    break;
                }
            }
        }
        if (mapElement == null) {
            mapElement = 
                new OnmsMapElement(onmsMap,onmsSubMap.getId(),OnmsMapElement.MAP_TYPE,csubmap.getLabel(),csubmap.getIcon(),csubmap.getX(),csubmap.getY());
            m_onmsMapElementDao.saveOrUpdate(mapElement);
        } else {
            m_onmsMapElementDao.update(mapElement);            
        }
        log().debug("added map element with id: " + mapElement.getId());
        log().debug("               with label: " + mapElement.getLabel());
        log().debug("                with icon: " + mapElement.getIconName());
        log().debug("                   with X: " + mapElement.getX());
        log().debug("                   with Y: " + mapElement.getY());
        m_onmsMapElementDao.flush();        
        m_onmsMapElementDao.clear();
    }

    private void doDelete(Integer nodeid) {
        m_nodeId = nodeid;
        log().debug("doDelete: deleting mapElements from the automated maps for the node with nodeid:" + m_nodeId);
        try {
            m_template.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus arg0) {
                    try {
                        for (OnmsMapElement elem: m_onmsNodeMapElementListMap.remove(Integer.valueOf(m_nodeId))) {
                            log().debug("doDelete: deleting element with label: '" + elem.getLabel() + "' from automated map: '" + elem.getMap().getName()+ "'");
                            Integer mapId = elem.getMap().getId();
                            m_onmsMapElementDao.delete(elem);
                            m_onmsMapElementDao.flush();
                            OnmsMap onmsMap = m_onmsMapDao.findMapById(mapId);
                            onmsMap.setLastModifiedTime(new Date());
                            m_onmsMapDao.update(onmsMap);
                            m_onmsMapDao.flush();
                            if (onmsMap.getMapElements().size() == 0)
                                removeEmptySubmap(onmsMap);

                        }
                        m_onmsMapDao.clear();
                        m_onmsMapElementDao.clear();
                    } catch (Exception e) {
                        log().error(e.getMessage());
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            sendAndThrow(m_nodeId, e);
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
                    Integer mapid = onmsMap.getId();
                    OnmsMapElement mapElement = m_onmsMapElementDao.findMapElement(submap.getId(), OnmsMapElement.MAP_TYPE, onmsMap);
                    if (mapElement != null) {
                        m_onmsMapElementDao.delete(mapElement);
                        m_onmsMapElementDao.flush();
                        onmsMap = m_onmsMapDao.findMapById(mapid);
                        onmsMap.setLastModifiedTime(new Date());
                        m_onmsMapDao.update(onmsMap);
                        m_onmsMapDao.flush();
                    }
                }
            }
        }
    }

    private void doAddOrUpdate(Integer nodeid) throws ProvisioningAdapterException {
        m_nodeId = nodeid;
        try {
            m_template.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus arg0) {
                    try {
                        addOrUpdate();
                    } catch (Exception e) {
                        log().error(e.getMessage());
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            sendAndThrow(m_nodeId, e);
        }
    }

    private void addOrUpdate() throws Exception {
        log().debug("addOrUpdate: adding or updating the automated maps for the node with nodeid:" + m_nodeId);

        OnmsNode node = m_onmsNodeDao.get(m_nodeId);
        if (node == null) throw new Exception("Error Adding element. Node does not exist: nodeid: " + m_nodeId);
        
        m_onmsNodeMapElementToDelete = m_onmsNodeMapElementListMap.get(m_nodeId);
        log().debug("addOrUpdate: found #" + m_onmsNodeMapElementToDelete.size() + " mapElements in automated maps for the nodeid: " +m_nodeId);
        // This is the array with the new elements
        List<OnmsMapElement> elems =  new ArrayList<OnmsMapElement>();
        m_onmsNodeMapElementListMap.replace(m_nodeId, elems);
        
        Map<String, Celement> celements = m_mapsAdapterConfig.getElementByAddress((getSuitableIp(node)));
        
        if (celements.isEmpty()) {
            log().info("addOrUpdate: Element is not managed in the adapter configuration file: no package match nodeid: "+m_nodeId);
        } else {
            log().debug("addOrUpdate: found #" + celements.size() + " container automated maps for the nodeid: " +m_nodeId);

            for (String mapName: celements.keySet()) {
                log().debug("addOrUpdate: found mapName: " + mapName + " container automated map for the nodeid: " +m_nodeId);
                Celement celement = celements.get(mapName);
                OnmsMap onmsMap = getSuitableMap(mapName);
                if (onmsMap.isNew()) {
                    throw new Exception("Error adding element. Automated map does not exist in database: " + mapName);
                } else {
                    log().debug("addOrUpdate: container automated map: " + mapName + " has mapId: " + onmsMap.getId() );
                    if (onmsMap.getMapElements().size() == 0) {
                        log().debug("addOrUpdate: automated map: " + mapName + " has no elements");
                        addAsSubMap(mapName);
                    }
                    OnmsMapElement mapElement = m_onmsMapElementDao.findMapElement(m_nodeId, OnmsMapElement.NODE_TYPE,onmsMap);
                    if (mapElement == null) {
                        int elementsize = m_mapNameMapSizeListMap.get(mapName);
                        log().debug("addOrUpdate: mapElement is new: found last mapElement at position #" + elementsize + " on map: " + mapName);
                        XY xy=getXY(onmsMap, elementsize);
                        mapElement = new OnmsMapElement(onmsMap,m_nodeId,OnmsMapElement.NODE_TYPE,node.getLabel(),celement.getIcon(),xy.getX(),xy.getY());
                        m_mapNameMapSizeListMap.replace(mapName, ++elementsize);
                        log().debug("doAddOrUpdate: adding node: " + node.getLabel() + " to map: " + mapName);
                    } else {
                        mapElement.setIconName(celement.getIcon());
                        mapElement.setLabel(node.getLabel());
                        log().debug("doAddOrUpdate: updating node: " + node.getLabel() + " to map: " + mapName);
                        List<OnmsMapElement> tempElems = new ArrayList<OnmsMapElement>();
                        for (OnmsMapElement oldElem : m_onmsNodeMapElementToDelete) {
                            log().debug("doAddOrUpdate: removing the old mapElement from the deleting list parsing element with mapId: " + oldElem.getMap().getId());
                            if ( oldElem.getMap().getId() != onmsMap.getId()) {
                                tempElems.add(oldElem);
                                log().debug("doAddOrUpdate: leaving the old mapElement in deleting list: ");
                            }
                        }
                        m_onmsNodeMapElementToDelete = tempElems;
                    }
                    m_onmsMapElementDao.saveOrUpdate(mapElement);
                    m_onmsMapElementDao.flush();
                    onmsMap.setLastModifiedTime(new Date());
                    m_onmsMapDao.update(onmsMap);
                    m_onmsMapDao.flush();
                    elems.add(mapElement);
                }
            }
            m_onmsMapElementDao.clear();
            m_onmsMapDao.clear();

            m_onmsNodeMapElementListMap.replace(m_nodeId, elems);
        }

        log().debug("doAddOrUpdate: deleting moved element from automated maps: size #" + m_onmsNodeMapElementToDelete.size());
        try {
            m_template.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus arg0) {
                    try {

                        for (OnmsMapElement elem : m_onmsNodeMapElementToDelete) {
                            log().debug("doAddOrUpdate: deleting element with label: '" + elem.getLabel() + "' from automated map: '" + elem.getMap().getName()+ "'");
                            Integer mapId = elem.getMap().getId();
                            m_onmsMapElementDao.delete(elem);
                            m_onmsMapElementDao.flush();
                            OnmsMap onmsMap = m_onmsMapDao.findMapById(mapId);
                            onmsMap.setLastModifiedTime(new Date());
                            m_onmsMapDao.update(onmsMap);
                            m_onmsMapDao.flush();
                            if (onmsMap.getMapElements().size() == 0)
                                removeEmptySubmap(onmsMap);
                            

                        }
                        m_onmsMapElementDao.clear();
                        m_onmsMapDao.clear();
                    } catch (Exception e) {
                        log().error(e.getMessage());
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            sendAndThrow(m_nodeId, e);
        }

        m_onmsNodeMapElementToDelete = null;

    }

    private void addAsSubMap(String submapName) {
        Map<String,Csubmap> csubmaps = m_mapsAdapterConfig.getContainerMaps(submapName);
        for(String mapName:csubmaps.keySet()) {
            OnmsMap onmsMap = getSuitableMap(mapName);
            Csubmap csubmap = csubmaps.get(mapName);
            OnmsMap onmsSubMap = getSuitableMap(csubmap.getName());
            if (onmsSubMap.isNew()) {
                log().error("add SubMaps: the submap doen not exists: " + csubmap.getName());
                continue;
            }
            if (!csubmap.getAddwithoutelements()) {
                addSubMap(onmsMap, csubmap, onmsSubMap);
                onmsMap.setLastModifiedTime(new Date());
                m_onmsMapDao.update(onmsMap);
                m_onmsMapDao.flush();

            }
        }
        m_onmsMapDao.clear();
    }
    

    private void sendAndThrow(int nodeId, Exception e) {
        Event event = buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED, nodeId).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent();
        m_eventForwarder.sendNow(event);
        log().error(e);
        throw new ProvisioningAdapterException(MESSAGE_PREFIX, e);
    }

    private EventBuilder buildEvent(String uei, int nodeId) {
        EventBuilder builder = new EventBuilder(uei, "Provisioner", new Date());
        builder.setNodeid(nodeId);
        return builder;
    }
    
    private String getSuitableIp(OnmsNode node){
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

        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByNameAndType(mapName, OnmsMap.AUTOMATICALLY_GENERATED_MAP);

        if (maps.size()>0) {
            onmsMap = maps.iterator().next();
            onmsMap.setNew(false);
            log().debug("getSuitableMap: found map with mapid #" + onmsMap.getMapId() + " for map name:" + mapName );
        } else {
            log().debug("getSuitableMap: no map found for name:" + mapName + ". Creating a new map.");
            onmsMap = new OnmsMap();
            onmsMap.setName(mapName);
            onmsMap.setType(OnmsMap.AUTOMATICALLY_GENERATED_MAP);
        }
        return onmsMap;

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
       
}
