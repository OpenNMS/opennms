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
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PathElement;
import org.opennms.netmgt.model.events.AddEventVisitor;
import org.opennms.netmgt.model.events.DeleteEventVisitor;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.OnmsForeignSource;
import org.opennms.netmgt.provision.persist.OnmsNodeRequisition;
import org.opennms.netmgt.provision.persist.requisition.OnmsRequisition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * DefaultProvisionService
 *
 * @author brozow
 */
@Service
public class DefaultProvisionService implements ProvisionService {

    @Autowired
    private DistPollerDao m_distPollerDao;
    
    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;
    
    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;
    
    @Autowired
    private ServiceTypeDao m_serviceTypeDao;
    
    @Autowired
    private CategoryDao m_categoryDao;
    
    @Autowired
    @Qualifier("transactionAware")
    private EventForwarder m_eventForwarder;
    
    @Autowired
    private ForeignSourceRepository m_foreignSourceRepository;
    
    private final ThreadLocal<HashMap<String, OnmsServiceType>> m_typeCache = new ThreadLocal<HashMap<String, OnmsServiceType>>();
    private final ThreadLocal<HashMap<String, OnmsCategory>> m_categoryCache = new ThreadLocal<HashMap<String, OnmsCategory>>();

    @Transactional
    public void insertNode(OnmsNode node) {
        
        OnmsDistPoller distPoller = m_distPollerDao.get("localhost");

        node.setDistPoller(distPoller);
        m_nodeDao.save(node);

        EntityVisitor eventAccumlator = new AddEventVisitor(m_eventForwarder);

        node.visit(eventAccumlator);
        
        //TODO: Update node in schedule
    }
    
    @Transactional
    public void updateNode(OnmsNode scannedNode, boolean x, boolean xx) {
        
        OnmsNode dbNode = m_nodeDao.getHierarchy(scannedNode.getId());

        dbNode.mergeNode(scannedNode, m_eventForwarder);
    
        m_nodeDao.update(dbNode);
        
        //TODO: update the node in the scheduledList of Nodes
        
    }

    @Transactional
    public void deleteNode(Integer nodeId) {
        
        OnmsNode node = m_nodeDao.get(nodeId);
        if (node != null) {
    
            m_nodeDao.delete(node);
    
            node.visit(new DeleteEventVisitor(m_eventForwarder));
        }
        
        //TODO: Should remove the node from the scheduled list of nodes
    
    }
    
    private void assertNotNull(Object o, String format, Object... args) {
        if (o == null) {
            throw new IllegalArgumentException(String.format(format, args));
        }
    }
    
    public void updateIpInterfaceAttributes(String foreignSource, String foreignId, OnmsIpInterface scannedIface) {
        
        OnmsIpInterface dbIface = m_ipInterfaceDao.findByForeignKeyAndIpAddress(foreignSource, foreignId, scannedIface.getIpAddress());
        
        if (dbIface != null) {
            // update the interface that was found
            dbIface.mergeInterfaceAttributes(scannedIface);
            m_ipInterfaceDao.update(dbIface);
        } else {
            // add the interface to the node, if it wasn't found
            OnmsNode dbNode = m_nodeDao.findByForeignId(foreignSource, foreignId);
            assertNotNull(dbNode, "no node found with foreignKey %s : %s", foreignSource, foreignId);
            dbNode.addIpInterface(scannedIface);
            m_nodeDao.update(dbNode);
        }
        

    }

    public void updateSnmpInterfaceAttributes(String foreignSource, String foreignId, OnmsSnmpInterface snmpInterface) {
        OnmsSnmpInterface dbSnmpIface = m_snmpInterfaceDao.findByForeignKeyAndIfIndex(foreignSource, foreignId, snmpInterface.getIfIndex());
        if (dbSnmpIface != null) {
            // update the interface that was found
            dbSnmpIface.mergeSnmpInterfaceAttributes(snmpInterface);
            m_snmpInterfaceDao.update(dbSnmpIface);
        } else {
            // add the interface to the node, if it wasn't found
            OnmsNode dbNode = m_nodeDao.findByForeignId(foreignSource, foreignId);
            assertNotNull(dbNode, "no node found with foreignKey %s : %s", foreignSource, foreignId);
            dbNode.addSnmpInterface(snmpInterface);
            m_nodeDao.update(dbNode);
        }
    }

    public void addMonitoredService(String foreignSource, String foreignId, String ipAddress, String svcName) {
        OnmsIpInterface iface = m_ipInterfaceDao.findByForeignKeyAndIpAddress(foreignSource, foreignId, ipAddress);
        assertNotNull(iface, "could not find interface %s on node with foreignKey %s:%s", ipAddress, foreignSource, foreignId);
        OnmsServiceType svcType = m_serviceTypeDao.findByName(svcName);
        if (svcType == null) {
            svcType = new OnmsServiceType(svcName);
            m_serviceTypeDao.save(svcType);
        }
        
        OnmsMonitoredService svc =new OnmsMonitoredService(iface, svcType);
        
        m_ipInterfaceDao.save(iface);
    }

    public void clearCache() {
        m_nodeDao.clear();
    }
    
    @Transactional
    public OnmsDistPoller createDistPollerIfNecessary(String dpName, String dpAddr) {
        OnmsDistPoller distPoller = m_distPollerDao.get(dpName);
        if (distPoller == null) {
            
            distPoller = new OnmsDistPoller(dpName, dpAddr);
            m_distPollerDao.save(distPoller);
        }
        return distPoller;
    }
    

    @Transactional
    public OnmsNode getRequisitionedNode(String foreignSource, String foreignId) throws ForeignSourceRepositoryException {
        OnmsNodeRequisition nodeReq = m_foreignSourceRepository.getNodeRequisition(foreignSource, foreignId);
        Assert.notNull(nodeReq, "nodeReq cannot be null!");
        return nodeReq.constructOnmsNodeFromRequisition();
    }
    
    @Transactional
    public OnmsServiceType createServiceTypeIfNecessary(String serviceName) {
        preloadExistingTypes();
        OnmsServiceType type = m_typeCache.get().get(serviceName);
        if (type == null) {
            type = loadServiceType(serviceName);
            m_typeCache.get().put(serviceName, type);
        }
        return type;
    }
    
    @Transactional
    public OnmsCategory createCategoryIfNecessary(String name) {
        preloadExistingCategories();
        
        OnmsCategory category = m_categoryCache.get().get(name);
        if (category == null) {    
            category = loadCategory(name);
            m_categoryCache.get().put(category.getName(), category);
        }
        return category;
    }
    
    @Transactional(readOnly=true)
    public Map<String, Integer> getForeignIdToNodeIdMap(String foreignSource) {
        return m_nodeDao.getForeignIdToNodeIdMap(foreignSource);
    }
    
    @Transactional
    public void setNodeParentAndDependencies(final String foreignSource, final String foreignId, final String parentForeignId, final String parentNodeLabel) {

        final OnmsNode node = findNodebyForeignId(foreignSource, foreignId);
        if (node == null) {
            return;
        }
        
        final OnmsNode parent = findParent(foreignSource, parentForeignId, parentNodeLabel);

        setParent(node, parent);
        setPathDependency(node, parent);

        m_nodeDao.update(node);
    }

    private void preloadExistingTypes() {
        if (m_typeCache.get() == null) {
            m_typeCache.set(loadServiceTypeMap());
        }
    }
    
    private HashMap<String, OnmsServiceType> loadServiceTypeMap() {
        HashMap<String, OnmsServiceType> serviceTypeMap = new HashMap<String, OnmsServiceType>();
        for (OnmsServiceType svcType : m_serviceTypeDao.findAll()) {
            serviceTypeMap.put(svcType.getName(), svcType);
        }
        return serviceTypeMap;
    }
    
    @Transactional
    private OnmsServiceType loadServiceType(String serviceName) {
        OnmsServiceType type;
        type = m_serviceTypeDao.findByName(serviceName);
        
        if (type == null) {
            type = new OnmsServiceType(serviceName);
            m_serviceTypeDao.save(type);
        }
        return type;
    }
    
    private void preloadExistingCategories() {
        if (m_categoryCache.get() == null) {
            m_categoryCache.set(loadCategoryMap());
        }
    }
    
    @Transactional(readOnly=true)
    private HashMap<String, OnmsCategory> loadCategoryMap() {
        HashMap<String, OnmsCategory> categoryMap = new HashMap<String, OnmsCategory>();
        for (OnmsCategory category : m_categoryDao.findAll()) {
            categoryMap.put(category.getName(), category);
        }
        return categoryMap;
    }
    
    @Transactional
    private OnmsCategory loadCategory(String name) {
        OnmsCategory category;
        category = m_categoryDao.findByName(name);
        if (category == null) {
            category = new OnmsCategory(name);
            m_categoryDao.save(category);
        }
        return category;
    }
    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    private OnmsNode findNodebyNodeLabel(String label) {
        Collection<OnmsNode> nodes = m_nodeDao.findByLabel(label);
    	if (nodes.size() == 1) {
            return nodes.iterator().next();
        }
    	
    	log().error("Unable to locate a unique node using label "+label+" "+nodes.size()+" nodes found.  Ignoring relationship.");
    	return null;
    }
    
    @Transactional(readOnly=true)
    private OnmsNode findNodebyForeignId(String foreignSource, String foreignId) {
        return m_nodeDao.findByForeignId(foreignSource, foreignId);
    }
    
    @Transactional(readOnly=true)
    private OnmsNode findParent(String foreignSource, String parentForeignId, String parentNodeLabel) {
        if (parentForeignId != null) {
            return findNodebyForeignId(foreignSource, parentForeignId);
        } else {
            if (parentNodeLabel != null) {
                return findNodebyNodeLabel(parentNodeLabel);
            }
        }
    	
    	return null;
    }
    
    private void setPathDependency(OnmsNode node, OnmsNode parent) {
        
        if (node == null) {
            return;
        }
        
        OnmsIpInterface critIface = null;
    	if (parent != null) {
    		critIface = parent.getCriticalInterface();
    	}
    	
        log().info("Setting criticalInterface of node: "+node+" to: "+critIface);
    	node.setPathElement(critIface == null ? null : new PathElement(critIface.getIpAddress(), "ICMP"));

    }
    
    private void setParent(OnmsNode node, OnmsNode parent) {

        if (node == null) {
            return;
        }

        log().info("Setting parent of node: "+node+" to: "+parent);
        node.setParent(parent);

        m_nodeDao.update(node);
    }
    
    public NodeScanSchedule getScheduleForNode(int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        return createScheduleForNode(node);
    }
    
    public List<NodeScanSchedule> getScheduleForNodes() {
        List<OnmsNode> nodes = m_nodeDao.findAll();
        
        List<NodeScanSchedule> scheduledNodes = new ArrayList<NodeScanSchedule>();
        
        for(OnmsNode node : nodes) {
            NodeScanSchedule nodeScanSchedule = createScheduleForNode(node);
            if (nodeScanSchedule != null) {
                scheduledNodes.add(nodeScanSchedule);
            }
        }
        
        return scheduledNodes;
    }
    
    private NodeScanSchedule createScheduleForNode(OnmsNode node) {
        Assert.notNull(node, "Node may not be null");

        OnmsForeignSource fs = null;
        try {
            fs = m_foreignSourceRepository.getForeignSource(node.getForeignSource());
        } catch (ForeignSourceRepositoryException e) {
            log().warn("unable to get foreign source repository", e);
        }

        Duration scanInterval = fs.getScanInterval();
        Duration initialDelay = Duration.ZERO;
        if (node.getLastCapsdPoll() != null) {
            DateTime nextPoll = new DateTime(node.getLastCapsdPoll().getTime()).plus(scanInterval);
            DateTime now = new DateTime();
            if (nextPoll.isAfter(now)) {
                initialDelay = new Duration(now, nextPoll);
            }
        }
        
        NodeScanSchedule nSchedule = new NodeScanSchedule(node.getId(), node.getForeignSource(), node.getForeignId(), initialDelay, scanInterval);
        
        return nSchedule;
    }

    public void setForeignSourceRepository(ForeignSourceRepository foriengSourceRepository) {
        m_foreignSourceRepository = foriengSourceRepository;
    }

    public ForeignSourceRepository getForeignSourceRepository() {
        return m_foreignSourceRepository;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.service.ProvisionService#loadRequisition(java.lang.String, org.springframework.core.io.Resource)
     */
    public OnmsRequisition loadRequisition(Resource resource) {
        return m_foreignSourceRepository.importRequisition(resource);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.service.ProvisionService#updateNodeInfo(org.opennms.netmgt.model.OnmsNode)
     */
    public void updateNodeAttributes(OnmsNode node) {
        OnmsNode dbNode;
        if (node.getId() != null) {
            dbNode = m_nodeDao.get(node.getId());
        } else {
            dbNode = m_nodeDao.findByForeignId(node.getForeignSource(), node.getForeignId());
        }
        
        dbNode.mergeNodeAttributes(node);
    }
    
    

}
