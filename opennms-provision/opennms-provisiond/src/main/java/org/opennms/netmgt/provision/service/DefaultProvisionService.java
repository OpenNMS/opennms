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

import static org.springframework.util.ObjectUtils.nullSafeEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PathElement;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.OnmsForeignSource;
import org.opennms.netmgt.provision.service.operations.AddEventVisitor;
import org.opennms.netmgt.provision.service.operations.DeleteEventVisitor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * DefaultProvisionService
 *
 * @author brozow
 */
public class DefaultProvisionService implements ProvisionService {

    public static class SnmpInterfaceUpdater {
        
        OnmsNode m_dbNode;
        Map<Integer, OnmsSnmpInterface> m_ifIndexToSnmpInterface;
    
        public SnmpInterfaceUpdater(OnmsNode db, OnmsNode imported) {
            m_dbNode = db;
            m_ifIndexToSnmpInterface = mapIfIndexToSnmpInterface(imported.getSnmpInterfaces());
        }
    
        private Map<Integer, OnmsSnmpInterface> mapIfIndexToSnmpInterface(Set<OnmsSnmpInterface> snmpInterfaces) {
            Map<Integer, OnmsSnmpInterface> map = new HashMap<Integer, OnmsSnmpInterface>();
            for (OnmsSnmpInterface snmpIface : snmpInterfaces) {
                if (snmpIface.getIfIndex() != null) {
                    map.put(snmpIface.getIfIndex(), snmpIface);
                }
            }
            return map;
        }
    
        public void execute() {
            for (Iterator<OnmsSnmpInterface> it = getExistingInterfaces().iterator(); it.hasNext();) {
                OnmsSnmpInterface iface = it.next();
                OnmsSnmpInterface imported = getImportedVersion(iface);
    
                if (imported == null) {
                    it.remove();
                    markAsProcessed(iface);
                } else {
                    update(imported, iface);
                    markAsProcessed(iface);
                }
    
            }
            addNewInterfaces();
        }
        
        private void update(OnmsSnmpInterface importedSnmpIface, OnmsSnmpInterface snmpIface) {
            
            if (!nullSafeEquals(snmpIface.getIfAdminStatus(), importedSnmpIface.getIfAdminStatus())) {
                snmpIface.setIfAdminStatus(importedSnmpIface.getIfAdminStatus());
            }
            
            if (!nullSafeEquals(snmpIface.getIfAlias(), importedSnmpIface.getIfAlias())) {
                snmpIface.setIfAlias(importedSnmpIface.getIfAlias());
            }
            
            if (!nullSafeEquals(snmpIface.getIfDescr(), importedSnmpIface.getIfDescr())) {
                snmpIface.setIfDescr(importedSnmpIface.getIfDescr());
            }
                
            if (!nullSafeEquals(snmpIface.getIfName(), importedSnmpIface.getIfName())) {
                snmpIface.setIfName(importedSnmpIface.getIfName());
            }
            
            if (!nullSafeEquals(snmpIface.getIfOperStatus(), importedSnmpIface.getIfOperStatus())) {
                snmpIface.setIfOperStatus(importedSnmpIface.getIfOperStatus());
            }
            
            if (!nullSafeEquals(snmpIface.getIfSpeed(), importedSnmpIface.getIfSpeed())) {
                snmpIface.setIfSpeed(importedSnmpIface.getIfSpeed());
            }
            
            if (!nullSafeEquals(snmpIface.getIfType(), importedSnmpIface.getIfType())) {
                snmpIface.setIfType(importedSnmpIface.getIfType());
            }
    
            if (!nullSafeEquals(snmpIface.getIpAddress(), importedSnmpIface.getIpAddress())) {
                snmpIface.setIpAddress(importedSnmpIface.getIpAddress());
            }
            
            if (!nullSafeEquals(snmpIface.getNetMask(), importedSnmpIface.getNetMask())) {
                snmpIface.setNetMask(importedSnmpIface.getNetMask());
            }
            
            if (!nullSafeEquals(snmpIface.getPhysAddr(), importedSnmpIface.getPhysAddr())) {
                snmpIface.setPhysAddr(importedSnmpIface.getPhysAddr());
            }
            
        }
    
        private void markAsProcessed(OnmsSnmpInterface iface) {
            m_ifIndexToSnmpInterface.remove(iface.getIfIndex());
        }
    
        private OnmsSnmpInterface getImportedVersion(OnmsSnmpInterface iface) {
            return m_ifIndexToSnmpInterface.get(iface.getIfIndex());
        }
    
        private Set<OnmsSnmpInterface> getExistingInterfaces() {
            return m_dbNode.getSnmpInterfaces();
       }
        
        private void addNewInterfaces() {
            for (OnmsSnmpInterface snmpIface : getNewInterfaces()) {
                m_dbNode.addSnmpInterface(snmpIface);
            }
        }
    
        private Collection<OnmsSnmpInterface> getNewInterfaces() {
            return m_ifIndexToSnmpInterface.values();
        }
    
    
    }
    public static class InterfaceUpdater {
        
        private final DefaultProvisionService m_provisionService;
        private final OnmsNode m_node;
        private final Map<String, OnmsIpInterface> m_ipAddrToImportIfs;
        private final boolean m_snmpDataUpdated;
    
        public InterfaceUpdater(DefaultProvisionService provisionService, OnmsNode node, OnmsNode imported, boolean snmpDataUpdated) {
            m_provisionService = provisionService;
            m_node = node;
            m_snmpDataUpdated = snmpDataUpdated;
            m_ipAddrToImportIfs = getIpAddrToInterfaceMap(imported);
    
        }
        
        private Map<String, OnmsIpInterface> getIpAddrToInterfaceMap(OnmsNode imported) {
            Map<String, OnmsIpInterface> ipAddrToIface = new HashMap<String, OnmsIpInterface>();
            for (OnmsIpInterface iface : imported.getIpInterfaces()) {
                ipAddrToIface.put(iface.getIpAddress(), iface);
            }
            return ipAddrToIface;
        }
    
        public DefaultProvisionService getProvisionService() {
            return m_provisionService;
        }
    
        public void execute() {
            for (Iterator<OnmsIpInterface> it = getExistingInterfaces().iterator(); it.hasNext();) {
                OnmsIpInterface iface = it.next();
                OnmsIpInterface imported = getImportedVersion(iface);
                
                if (imported == null) {
                    it.remove();
                    iface.visit(new DeleteEventVisitor(getProvisionService().getEventForwarder()));
                    markAsProcessed(iface);
                } else {
                    update(imported, iface);
                    markAsProcessed(iface);
                }
                
            }
            addNewInterfaces();
        }
    
        private void addNewInterfaces() {
            for (OnmsIpInterface iface : getNewInterfaces()) {
                m_node.addIpInterface(iface);
                if (iface.getIfIndex() != null) {
                    iface.setSnmpInterface(m_node.getSnmpInterfaceWithIfIndex(iface.getIfIndex()));
                }
                iface.visit(new AddEventVisitor(getProvisionService().getEventForwarder()));
            }
        }
    
        private OnmsIpInterface getImportedVersion(OnmsIpInterface iface) {
            return m_ipAddrToImportIfs.get(iface.getIpAddress());
        }
    
        private Collection<OnmsIpInterface> getNewInterfaces() {
            return m_ipAddrToImportIfs.values();
        }
    
        private void markAsProcessed(OnmsIpInterface iface) {
            m_ipAddrToImportIfs.remove(iface.getIpAddress());
        }
    
        private void update(OnmsIpInterface imported, OnmsIpInterface iface) {
            if (!nullSafeEquals(iface.getIsManaged(), imported.getIsManaged())) {
                iface.setIsManaged(imported.getIsManaged());
            }
            
            if (!nullSafeEquals(iface.getIsSnmpPrimary(), imported.getIsSnmpPrimary())) {
                iface.setIsSnmpPrimary(imported.getIsSnmpPrimary());
                // TODO: send snmpPrimary event
            }
            
            if (m_snmpDataUpdated) {
            	updateSnmpInterface(imported, iface);
            }
            
           if (!nullSafeEquals(iface.getIpStatus(), imported.getIpStatus())) {
            iface.setIpStatus(imported.getIpStatus());
        }
           
           if (!nullSafeEquals(iface.getIpHostName(), imported.getIpHostName())) {
            iface.setIpHostName(imported.getIpHostName());
        }
           
           updateServices(iface, imported);
        }
    
    	private void updateSnmpInterface(OnmsIpInterface imported, OnmsIpInterface iface) {
    
    		if (nullSafeEquals(iface.getIfIndex(), imported.getIfIndex())) {
                // no need to change anything
                return;
            }
            
            if (imported.getSnmpInterface() == null) {
                // there is no longer an snmpInterface associated with the ipInterface
                iface.setSnmpInterface(null);
            } else {
                // locate the snmpInterface on this node that has the new ifIndex and set it
                // into the interface
                OnmsSnmpInterface snmpIface = m_node.getSnmpInterfaceWithIfIndex(imported.getIfIndex());
                iface.setSnmpInterface(snmpIface);
            }
            
            
            
    	}
        
        private void updateServices(OnmsIpInterface iface, OnmsIpInterface imported) {
            new DefaultProvisionService.ServiceUpdater(getProvisionService(), iface, imported).execute();
        }
    
        private Set<OnmsIpInterface> getExistingInterfaces() {
            return m_node.getIpInterfaces();
        }
    
    }
    public static class ServiceUpdater {
        private final DefaultProvisionService m_provisionService;
        private final OnmsIpInterface m_iface;
        private Map<OnmsServiceType, OnmsMonitoredService> m_svcTypToSvcMap;
    
        public ServiceUpdater(DefaultProvisionService provisionService, OnmsIpInterface iface, OnmsIpInterface imported) {
            m_provisionService = provisionService;
            m_iface = iface;
            
            createSvcTypeToSvcMap(imported);
        }
        
        public DefaultProvisionService getProvisionService() {
            return m_provisionService;
        }
        
        private void debugf(String format, Object... args) {
            Category log = ThreadCategory.getInstance(getClass());
            if (log.isDebugEnabled()) {
                log.debug(String.format(format, args));
            }
        }
    
        private void createSvcTypeToSvcMap(OnmsIpInterface imported) {
            m_svcTypToSvcMap = new HashMap<OnmsServiceType, OnmsMonitoredService>();
            for (OnmsMonitoredService svc : imported.getMonitoredServices()) {
                m_svcTypToSvcMap.put(svc.getServiceType(), svc);
            }
        }
    
        public void execute() {
            for (Iterator<OnmsMonitoredService> it = getExisting().iterator(); it.hasNext();) {
                OnmsMonitoredService svc = it.next();
                OnmsMonitoredService imported = getImportedVersion(svc);
                if (imported == null) {
                    it.remove();
                    svc.visit(new DeleteEventVisitor(getProvisionService().getEventForwarder()));
                }
                else {
                    update(svc);
                }
                markAsProcessed(svc);
            }
            addNewServices();
        }
    
        private void addNewServices() {
            Collection<OnmsMonitoredService> newServices = getNewServices();
            debugf("%s has %d new services.", m_iface.getNode().getLabel(), newServices.size());
            for (OnmsMonitoredService svc : newServices) {
                svc.setIpInterface(m_iface);
                m_iface.getMonitoredServices().add(svc);
                svc.visit(new AddEventVisitor(getProvisionService().getEventForwarder()));
            }
        }
    
        private Collection<OnmsMonitoredService> getNewServices() {
            return m_svcTypToSvcMap.values();
        }
    
        private void markAsProcessed(OnmsMonitoredService svc) {
            m_svcTypToSvcMap.remove(svc.getServiceType());
        }
    
        private void update(OnmsMonitoredService svc) {
            // nothing to do here
        }
    
        private OnmsMonitoredService getImportedVersion(OnmsMonitoredService svc) {
            return m_svcTypToSvcMap.get(svc.getServiceType());
        }
    
        Set<OnmsMonitoredService> getExisting() {
            return m_iface.getMonitoredServices();
        }
    
    }
    
    private DistPollerDao m_distPollerDao;
    
    private NodeDao m_nodeDao;
    
    private ServiceTypeDao m_serviceTypeDao;
    
    private CategoryDao m_categoryDao;
    
    private EventForwarder m_eventForwarder;
    
    private ForeignSourceRepository m_foreignSourceRepository;
    
    private final ThreadLocal<HashMap<String, OnmsServiceType>> m_typeCache = new ThreadLocal<HashMap<String, OnmsServiceType>>();
    private final ThreadLocal<HashMap<String, OnmsCategory>> m_categoryCache = new ThreadLocal<HashMap<String, OnmsCategory>>();

    /**
     * @param distPollerDao the distPollerDao to set
     */
    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }
    
    /**
     * @return the nodeDao
     */
    private NodeDao getNodeDao() {
        return m_nodeDao;
    }
    
    /**
     * @param nodeDao the nodeDao to set
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    /**
     * @param serviceTypeDao the serviceTypeDao to set
     */
    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }

    /**
     * @param categoryDao the categoryDao to set
     */
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    /**
     * @return the eventForwarder
     */
    private EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * @param eventForwarder the eventForwarder to set
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
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
    
    public void clearCache() {
        getNodeDao().clear();
    }
    
    @Transactional
    public void updateNode(OnmsNode node, boolean snmpDataForNodeUpToDate, boolean snmpDataForInterfacesUpToDate) {
        
        OnmsNode db = getNodeDao().getHierarchy(node.getId());
    
    	// verify that the node label is still the same
    	if (!db.getLabel().equals(node.getLabel())) {
    		db.setLabel(node.getLabel());
    		// TODO: nodeLabelChanged event
    	}
    
        if (!nullSafeEquals(db.getForeignSource(), node.getForeignSource())) {
            db.setForeignSource(node.getForeignSource());
        }
    
        if (!nullSafeEquals(db.getForeignId(), node.getForeignId())) {
            db.setForeignId(node.getForeignId());
        }
    
        if (snmpDataForNodeUpToDate) {
    
    		if (!nullSafeEquals(db.getSysContact(), node.getSysContact())) {
    			db.setSysContact(node.getSysContact());
    		}
    
    		if (!nullSafeEquals(db.getSysDescription(), node.getSysDescription())) {
    			db.setSysDescription(node.getSysDescription());
    		}
    
    		if (!nullSafeEquals(db.getSysLocation(), node.getSysLocation())) {
    			db.setSysLocation(node.getSysLocation());
    		}
    
    		if (!nullSafeEquals(db.getSysName(), node.getSysName())) {
    			db.setSysName(node.getSysName());
    		}
    
    		if (!nullSafeEquals(db.getSysObjectId(), node.getSysObjectId())) {
    			db.setSysObjectId(node.getSysObjectId());
    		}
    		
    	}
    
        if (snmpDataForInterfacesUpToDate) {
            new SnmpInterfaceUpdater(db, node).execute();
        }
    
    
        new InterfaceUpdater(this, db, node, snmpDataForInterfacesUpToDate).execute();
        
    	if (!db.getCategories().equals(node.getCategories())) {
            db.setCategories(node.getCategories());
        }
    
        getNodeDao().update(db);
        
        //TODO: update the node in the scheduledList of Nodes
        
    }
    
    @Transactional
    public void deleteNode(Integer nodeId) {
        
        OnmsNode node = getNodeDao().get(nodeId);
    	if (node != null) {
    
    	    getNodeDao().delete(node);
    
    	    node.visit(new DeleteEventVisitor(getEventForwarder()));
    	}
    	
    	//TODO: Should remove the node from the scheduled list of nodes
    
    }
    
    @Transactional
    public void insertNode(OnmsNode node) {
        

        OnmsDistPoller distPoller = m_distPollerDao.get("localhost");

        node.setDistPoller(distPoller);
        getNodeDao().save(node);

        EntityVisitor eventAccumlator = new AddEventVisitor(getEventForwarder());

        node.visit(eventAccumlator);
        
        //TODO: Update node in schedule
    }
    
    @Transactional
    public OnmsServiceType createServiceTypeIfNecessary(String serviceName) {
        proloadExistingTypes();
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
        return getNodeDao().getForeignIdToNodeIdMap(foreignSource);
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

        getNodeDao().update(node);
    }

    private void proloadExistingTypes() {
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
    
    @Transactional(readOnly=true)
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
    
    @Transactional(readOnly=true)
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
        Collection<OnmsNode> nodes = getNodeDao().findByLabel(label);
    	if (nodes.size() == 1) {
            return nodes.iterator().next();
        }
    	
    	log().error("Unable to locate a unique node using label "+label+" "+nodes.size()+" nodes found.  Ignoring relationship.");
    	return null;
    }
    
    @Transactional(readOnly=true)
    private OnmsNode findNodebyForeignId(String foreignSource, String foreignId) {
        return getNodeDao().findByForeignId(foreignSource, foreignId);
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

        getNodeDao().update(node);
    }
    
    public NodeScanSchedule getScheduleForNode(int nodeId) {
        OnmsNode node = getNodeDao().get(nodeId);
        return createScheduleForNode(node);
    }
    
    public List<NodeScanSchedule> getScheduleForNodes() {
        List<OnmsNode> nodes = getNodeDao().findAll();
        
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
        long now = System.currentTimeMillis();

        OnmsForeignSource fs = null;
        try {
            fs = m_foreignSourceRepository.get(node.getForeignSource());
        } catch (ForeignSourceRepositoryException e) {
            log().warn("unable to get foreign source repository", e);
        }

        long scanInterval = fs == null ? 86400000 : fs.getScanInterval();
        
        long lastPoll = (node.getLastCapsdPoll() == null ? 0 : node.getLastCapsdPoll().getTime());
        long nextPoll = lastPoll + scanInterval;
        long initialDelay = Math.max(0, nextPoll - now);
        
        NodeScanSchedule nSchedule = new NodeScanSchedule();
        nSchedule.setForeignSource(node.getForeignSource());
        nSchedule.setInitialDelay(initialDelay);
        nSchedule.setNodeId(node.getId()); 
        nSchedule.setScanInterval(scanInterval);
        
        return nSchedule;
    }

    public void setForeignSourceRepository(ForeignSourceRepository foriengSourceRepository) {
        m_foreignSourceRepository = foriengSourceRepository;
    }

    public ForeignSourceRepository getForeignSourceRepository() {
        return m_foreignSourceRepository;
    }

}
