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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.opennms.netmgt.model.events.UpdateEventVisitor;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.OnmsNodeRequisition;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
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
    @Qualifier("deployed")
    private ForeignSourceRepository m_foreignSourceRepository;
    
    @Autowired
    private PluginRegistry m_pluginRegistry;
    
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
    public void updateNode(OnmsNode node, boolean x, boolean xx) {
        
        OnmsNode dbNode = m_nodeDao.getHierarchy(node.getId());

        dbNode.mergeNode(node, m_eventForwarder);
    
        m_nodeDao.update(dbNode);
        EntityVisitor eventAccumlator = new UpdateEventVisitor(m_eventForwarder);
        //TODO: update the node in the scheduledList of Nodes
        node.visit(eventAccumlator);
        
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
    
    
    
    public OnmsIpInterface updateIpInterfaceAttributes(Integer nodeId, OnmsIpInterface scannedIface) {
        if (scannedIface.getSnmpInterface() != null) {
            scannedIface.setSnmpInterface(updateSnmpInterfaceAttributes(nodeId, scannedIface.getSnmpInterface()));
        }
        
        OnmsIpInterface dbIface = m_ipInterfaceDao.findByNodeIdAndIpAddress(nodeId, scannedIface.getIpAddress());
        if (dbIface != null) {
            dbIface.mergeInterfaceAttributes(scannedIface);
            m_ipInterfaceDao.save(dbIface);
            return dbIface;
        } else {
            OnmsNode dbNode = m_nodeDao.get(nodeId);
            assertNotNull(dbNode, "no node found with nodeId %d", nodeId);
            dbNode.addIpInterface(scannedIface);
            m_nodeDao.update(dbNode);
            AddEventVisitor visitor = new AddEventVisitor(m_eventForwarder);
            scannedIface.visit(visitor);
            return scannedIface;
        }

    }

    public OnmsSnmpInterface updateSnmpInterfaceAttributes(Integer nodeId, OnmsSnmpInterface snmpInterface) {
        OnmsSnmpInterface dbSnmpIface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, snmpInterface.getIfIndex());
        if (dbSnmpIface != null) {
            // update the interface that was found
            dbSnmpIface.mergeSnmpInterfaceAttributes(snmpInterface);
            m_snmpInterfaceDao.update(dbSnmpIface);
            return dbSnmpIface;
        } else {
            // add the interface to the node, if it wasn't found
            OnmsNode dbNode = m_nodeDao.get(nodeId);
            assertNotNull(dbNode, "no node found with nodeId %d", nodeId);
            dbNode.addSnmpInterface(snmpInterface);
            m_nodeDao.update(dbNode);
            return snmpInterface;
        }
    }

    public OnmsMonitoredService addMonitoredService(Integer ipInterfaceId, String svcName) {
        OnmsIpInterface iface = m_ipInterfaceDao.get(ipInterfaceId);
        assertNotNull(iface, "could not find interface with id %d", ipInterfaceId);
        OnmsServiceType svcType = m_serviceTypeDao.findByName(svcName);
        if (svcType == null) {
            svcType = new OnmsServiceType(svcName);
            m_serviceTypeDao.save(svcType);
        }
        
        OnmsMonitoredService svc = iface.getMonitoredServiceByServiceType(svcName);
        if (svc != null) {
            m_monitoredServiceDao.saveOrUpdate(svc);
        } else {
        
            // this adds the service to the interface as a side effect
            svc = new OnmsMonitoredService(iface, svcType);
            svc.setStatus("A");
            m_ipInterfaceDao.saveOrUpdate(iface);
            AddEventVisitor visitor = new AddEventVisitor(m_eventForwarder);
            svc.visit(visitor);
        }

        
        return svc;
    }

    public OnmsMonitoredService addMonitoredService(Integer nodeId, String ipAddress, String svcName) {
        OnmsIpInterface iface = m_ipInterfaceDao.findByNodeIdAndIpAddress(nodeId, ipAddress);
        assertNotNull(iface, "could not find interface with nodeid %d and ipAddr %s", nodeId, ipAddress);
        OnmsServiceType svcType = m_serviceTypeDao.findByName(svcName);
        if (svcType == null) {
            svcType = new OnmsServiceType(svcName);
            m_serviceTypeDao.save(svcType);
        }
        
        OnmsMonitoredService svc = iface.getMonitoredServiceByServiceType(svcName);
        if (svc != null) {
            m_monitoredServiceDao.saveOrUpdate(svc);
        } else {
        
            // this adds the service to the interface as a side effect
            svc = new OnmsMonitoredService(iface, svcType);
            svc.setStatus("A");
            m_ipInterfaceDao.saveOrUpdate(iface);
            AddEventVisitor visitor = new AddEventVisitor(m_eventForwarder);
            svc.visit(visitor);
        }

        
        return svc;
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
        Assert.notNull(m_nodeDao, "Node DAO is null and is not supposed to be");
        List<OnmsNode> nodes = m_nodeDao.findAllProvisionedNodes();
        
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

        ForeignSource fs = null;
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
    public Requisition loadRequisition(Resource resource) {
        return m_foreignSourceRepository.importRequisition(resource);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.service.ProvisionService#updateNodeInfo(org.opennms.netmgt.model.OnmsNode)
     */
    public OnmsNode updateNodeAttributes(OnmsNode node) {
        OnmsNode dbNode;
        if (node.getId() != null) {
            dbNode = m_nodeDao.get(node.getId());
        } else {
            dbNode = m_nodeDao.findByForeignId(node.getForeignSource(), node.getForeignId());
        }

        if (dbNode == null) {
            OnmsDistPoller scannedPoller = node.getDistPoller();
            OnmsDistPoller dbPoller;
            if (scannedPoller == null) {
                dbPoller = m_distPollerDao.get("locahost");
            } else {
                dbPoller = m_distPollerDao.get(scannedPoller.getName());
                if (dbPoller == null) {
                    m_distPollerDao.save(scannedPoller);
                    dbPoller = scannedPoller;
                }
            }
            
            node.setDistPoller(dbPoller);
            
            m_nodeDao.save(node);
            
            return node;
            
        } else {
            dbNode.mergeNodeAttributes(node);
            m_nodeDao.saveOrUpdate(dbNode);
            
            return dbNode;
        }
    }

    public List<ServiceDetector> getDetectorsForForeignSource(String foreignSourceName) {
        ForeignSource foreignSource = m_foreignSourceRepository.getForeignSource(foreignSourceName);
        assertNotNull(foreignSource, "Expected a foreignSource with name %s", foreignSourceName);
        
        Set<PluginConfig> detectorConfigs = foreignSource.getDetectors();
        if (detectorConfigs == null) {
            return new ArrayList<ServiceDetector>(m_pluginRegistry.getAllPlugins(ServiceDetector.class));
        }
        
        List<ServiceDetector> detectors = new ArrayList<ServiceDetector>(detectorConfigs.size());
        for(PluginConfig detectorConfig : detectorConfigs) {
            ServiceDetector detector = m_pluginRegistry.getPluginInstance(ServiceDetector.class, detectorConfig);
            if (detector == null) {
                error("Configured plugin does not exist: %s", detectorConfig);
            } else {
                detector.setServiceName(detectorConfig.getName());
                detector.init();
                detectors.add(detector);
            }
        }

        return detectors;
    }

    public List<NodePolicy> getNodePoliciesForForeignSource(String foreignSourceName) {
        return getPluginsForForeignSource(NodePolicy.class, foreignSourceName);
    }
    
    public List<IpInterfacePolicy> getIpInterfacePoliciesForForeignSource(String foreignSourceName) {
        return getPluginsForForeignSource(IpInterfacePolicy.class, foreignSourceName);
    }
    
    public List<SnmpInterfacePolicy> getSnmpInterfacePoliciesForForeignSource(String foreignSourceName) {
        return getPluginsForForeignSource(SnmpInterfacePolicy.class, foreignSourceName);
    }

    
    public <T> List<T> getPluginsForForeignSource(Class<T> pluginClass, String foreignSourceName) {
        ForeignSource foreignSource = m_foreignSourceRepository.getForeignSource(foreignSourceName);
        assertNotNull(foreignSource, "Expected a foreignSource with name %s", foreignSourceName);
        
        Set<PluginConfig> configs = foreignSource.getPolicies();
        if (configs == null) {
            return Collections.emptyList(); 
        }
        
        List<T> plugins = new ArrayList<T>(configs.size());
        for(PluginConfig config : configs) {
            T plugin = m_pluginRegistry.getPluginInstance(pluginClass, config);
            if (plugin == null) {
                error("Configured plugin does not exist: %s", config);
            } else {
                plugins.add(plugin);
            }
        }

        return plugins;
        
    }

    public void deleteObsoleteInterfaces(Integer nodeId, Date scanStamp) {
        m_nodeDao.deleteObsoleteInterfaces(nodeId, scanStamp);
    }

    public void updateNodeScanStamp(Integer nodeId, Date scanStamp) {
        m_nodeDao.updateNodeScanStamp(nodeId, scanStamp);
    }

    private void error(String format, Object... args) {
        ThreadCategory.getInstance(getClass()).error(String.format(format, args));
    }
}
