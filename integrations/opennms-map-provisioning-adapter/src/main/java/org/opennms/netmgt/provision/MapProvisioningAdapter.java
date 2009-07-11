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
import java.util.Iterator;
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
            
        log().debug("processing operation: " + op.getType().name() + " for node with Id: #" + op.getNodeId());

        if (op.getType() == AdapterOperationType.ADD) {
            doAddOrUpdate(op.getNodeId());
        } else if (op.getType() == AdapterOperationType.UPDATE) {
            doAddOrUpdate(op.getNodeId());
        } else if (op.getType() == AdapterOperationType.DELETE) {
            doDelete(op.getNodeId());
        }
       
    }    
    
    public void afterPropertiesSet() throws Exception {
        
        Assert.notNull(m_onmsNodeDao, "Map Provisioning Adapter requires nodeDao property to be set.");
        Assert.notNull(m_onmsMapDao, "Map Provisioning Adapter requires OnmsMapDao property to be set.");
        Assert.notNull(m_onmsMapElementDao, "Map Provisioning Adapter requires OnmsMapElementDao property to be set.");
        Assert.notNull(m_mapsAdapterConfig, "Map Provisioning Adapter requires MapasAdapterConfig property to be set.");
        Assert.notNull(m_eventForwarder, "Map Provisioning Adapter requires EventForwarder property to be set.");

        m_template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                List<Cmap> cmaps = m_mapsAdapterConfig.getAllMaps();
                m_mapNameMapSizeListMap = new ConcurrentHashMap<String, Integer>(cmaps.size());
                
                List<OnmsNode> nodes = m_onmsNodeDao.findAllProvisionedNodes();
                m_onmsNodeMapElementListMap = new ConcurrentHashMap<Integer, List<OnmsMapElement>>(nodes.size());

                syncMaps(cmaps, nodes);
                return null;
           }

        });

    }

    private void syncMaps(List<Cmap> cmaps,List<OnmsNode> nodes) {

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
        
        log().debug("syncMaps: add node element of automated maps");
        
        for(OnmsNode node: nodes) {
            log().debug("syncMaps: parsing node element for adding to automated maps: " + node.getLabel());
            doAddOrUpdate(node.getId());
        }

        log().debug("syncMaps: adding automated submaps");
        Map<String,List<Csubmap>> mapnameSubmapMap = m_mapsAdapterConfig.getsubMaps();
        Iterator<String> ite = mapnameSubmapMap.keySet().iterator();
        while (ite.hasNext()) {
            String mapName = ite.next();
            log().debug("syncMaps: adding automated submap: " + mapName);
            OnmsMap onmsMap = getSuitableMap(mapName);
            Iterator<Csubmap> sub_ite = mapnameSubmapMap.get(mapName).iterator();
            while (sub_ite.hasNext()) {
                Csubmap csubmap = sub_ite.next();
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
    }

    private void addSubMap(OnmsMap onmsMap, Csubmap csubmap, OnmsMap onmsSubMap) {
        OnmsMapElement  mapElement = null;
        if (!onmsMap.getMapElements().isEmpty()) {
            for (OnmsMapElement elem: onmsMap.getMapElements()) {
                if (elem.getType().equals(OnmsMapElement.MAP_TYPE) && elem.getElementId() == onmsSubMap.getId()) {
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
        m_onmsMapElementDao.flush();        
        m_onmsMapElementDao.clear();
    }

    private void doDelete(Integer nodeid) {
        m_nodeId = nodeid;
        try {
            m_template.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus arg0) {
                    try {
                        for (OnmsMapElement elem: m_onmsNodeMapElementListMap.remove(Integer.valueOf(m_nodeId))) {
                            OnmsMap onmsMap = elem.getMap();
                            onmsMap.setLastModifiedTime(new Date());
                            m_onmsMapElementDao.delete(elem);
                            m_onmsMapDao.update(onmsMap);
                        }
                        m_onmsMapDao.flush();
                        m_onmsMapElementDao.flush();
                        m_onmsMapDao.clear();
                        m_onmsMapElementDao.clear();
                        removeEmptySubmaps();
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

    protected void removeEmptySubmaps() {
        Map<String,List<Csubmap>> submaps = m_mapsAdapterConfig.getsubMaps();
        Iterator<String> ite = submaps.keySet().iterator();
        while (ite.hasNext()) {
            String mapName = ite.next();
            Iterator<Csubmap> sub_ite = submaps.get(mapName).iterator();
            while (sub_ite.hasNext()) {
                Csubmap csubmap = sub_ite.next();
                if (csubmap.getAddwithoutelements()) continue;
                OnmsMap onmsSubMap = getSuitableMap(csubmap.getName());
                if (onmsSubMap.isNew()) continue;
                if (onmsSubMap.getMapElements().size() == 0) {
                    OnmsMap onmsMap = getSuitableMap(mapName);
                    OnmsMapElement mapElement = 
                        new OnmsMapElement(onmsMap,onmsSubMap.getId(),OnmsMapElement.MAP_TYPE,csubmap.getLabel(),csubmap.getIcon(),csubmap.getX(),csubmap.getY());
                    m_onmsMapElementDao.delete(mapElement);
                    m_onmsMapElementDao.flush();
                    onmsMap.setLastModifiedTime(new Date());
                    m_onmsMapDao.update(onmsMap);
                    m_onmsMapDao.flush();

                }
            }
        }
        m_onmsMapElementDao.clear();
        m_onmsMapDao.clear();
    }

    private void doAddOrUpdate(Integer nodeid) throws ProvisioningAdapterException {
        m_nodeId = nodeid;
        log().debug("Map PROVISIONING ADAPTER CALLED doAddOrUpdate on nodeid: " + m_nodeId);        
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
        OnmsNode node = m_onmsNodeDao.get(m_nodeId);
        if (node == null) throw new Exception("Error Adding element. Node does not exist: nodeid: " + m_nodeId);
        Map<String, Celement> celements = m_mapsAdapterConfig.getElementByAddress((getSuitableIpForMap(node)));
        if (celements.isEmpty()) {
            log().info("Element is not managed in the adapter: nodeid="+m_nodeId);
        } else {
            Iterator<String> ite = celements.keySet().iterator();
            while (ite.hasNext()) {
                String mapName = ite.next();
                Celement celement = celements.get(mapName);
                OnmsMap onmsMap = getSuitableMap(mapName);
                if (onmsMap.isNew()) {
                    throw new Exception("Error adding element. Map does not exist: " + mapName);
                } else {
                    if (onmsMap.getMapElements().size() == 0) {
                        addAsSubMap(mapName);
                    }
                    OnmsMapElement mapElement = m_onmsMapElementDao.findMapElement(m_nodeId, OnmsMapElement.NODE_TYPE,onmsMap);
                    if (mapElement == null) {
                        int elementsize = m_mapNameMapSizeListMap.get(mapName);
                        log().debug("Element is new: found #" + elementsize + " on map: " + mapName);
                        XY xy=getXY(onmsMap, elementsize);
                        mapElement = new OnmsMapElement(onmsMap,m_nodeId,OnmsMapElement.NODE_TYPE,node.getLabel(),celement.getIcon(),xy.getX(),xy.getY());
                        m_mapNameMapSizeListMap.replace(mapName, ++elementsize);
                        log().debug("doAddOrUpdate: adding node: " + node.getLabel() + " to map: " + mapName);
                    } else {
                        mapElement.setIconName(celement.getIcon());
                        mapElement.setLabel(node.getLabel());
                        log().debug("doAddOrUpdate: updating node: " + node.getLabel() + " to map: " + mapName);
                    }
                    m_onmsMapElementDao.saveOrUpdate(mapElement);
                    m_onmsMapElementDao.flush();
                    onmsMap.setLastModifiedTime(new Date());
                    m_onmsMapDao.update(onmsMap);
                    m_onmsMapDao.flush();
                    updateNodeMapElementList(Integer.valueOf(m_nodeId), mapElement);
                }
            }
            m_onmsMapElementDao.clear();
            m_onmsMapDao.clear();
        }

    }

    private void addAsSubMap(String submapName) {
        Map<String,Csubmap> csubmaps = m_mapsAdapterConfig.getContainerMaps(submapName);
        Iterator<String> ite = csubmaps.keySet().iterator();
        while (ite.hasNext()) {
            String mapName = ite.next();
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
    
    private void updateNodeMapElementList(Integer nodeId,OnmsMapElement elem) {
        List<OnmsMapElement> elems = new ArrayList<OnmsMapElement>();
        if (m_onmsNodeMapElementListMap.containsKey(nodeId)) {
            for (OnmsMapElement elemInList :m_onmsNodeMapElementListMap.get(nodeId)) {
                if (elemInList.getElementId() != elem.getElementId())
                    elems.add(elemInList);
            }
        }
        elems.add(elem);
        m_onmsNodeMapElementListMap.put(nodeId, elems);        
    }
    
    private String getSuitableIpForMap(OnmsNode node){
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
            log().debug("getSuitableMap maplist found map with mapid #" + onmsMap.getMapId() + " for map name:" + mapName );
        } else {
            log().debug("map not found for name creating:" + mapName );
            onmsMap = new OnmsMap();
            onmsMap.setName(mapName);
            onmsMap.setType(OnmsMap.AUTOMATICALLY_GENERATED_MAP);
            log().debug("is map new? " + onmsMap.isNew());
        }
        return onmsMap;

    }

     private XY getXY(OnmsMap map, int mapElementSize) {
        int delta = m_mapsAdapterConfig.getMapElementDimension();
        int maxNumberofelementsonX=map.getWidth()/(2*delta);
        int numberofexistingelement = mapElementSize;
        int positiononX = 1;
        int positiononY = 1;
        int numberofremelement = numberofexistingelement;
        boolean addoffset = false;
        while (maxNumberofelementsonX < numberofexistingelement){
            numberofremelement = numberofremelement - maxNumberofelementsonX;
            positiononY++;
            addoffset = !addoffset;
        }
        positiononX = positiononX + numberofremelement;
        XY xy = new XY();
        if (addoffset) {
            xy.setX(delta+2*delta*positiononX);
        } else {
            xy.setX(2*delta*positiononX);
        }
        xy.setY(delta*positiononY);
        return xy;
    }
       
}
