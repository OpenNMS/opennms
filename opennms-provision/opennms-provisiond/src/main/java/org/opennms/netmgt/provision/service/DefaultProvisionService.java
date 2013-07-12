/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.support.CreateIfNecessaryTemplate;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PathElement;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.events.AddEventVisitor;
import org.opennms.netmgt.model.events.DeleteEventVisitor;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.UpdateEventVisitor;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.OnmsNodeRequisition;
import org.opennms.netmgt.provision.persist.RequisitionFileUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * DefaultProvisionService
 *
 * @author brozow
 * @version $Id: $
 */
@Service
public class DefaultProvisionService implements ProvisionService, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultProvisionService.class);
    
    private final static String FOREIGN_SOURCE_FOR_DISCOVERED_NODES = null;
    
    /**
     * ServiceTypeFulfiller
     *
     * @author brozow
     */
    private final class ServiceTypeFulfiller extends AbstractEntityVisitor {
        @Override
        public void visitMonitoredService(OnmsMonitoredService monSvc) {
            OnmsServiceType dbType = monSvc.getServiceType();
            if (dbType.getId() == null) {
                dbType = createServiceTypeIfNecessary(dbType.getName());
            }
            monSvc.setServiceType(dbType);
        }
    }

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
    @Qualifier("fastFused")
    private ForeignSourceRepository m_foreignSourceRepository;
    
    @Autowired
    @Qualifier("fastPending")
    private ForeignSourceRepository m_pendingForeignSourceRepository;

    @Autowired
    private PluginRegistry m_pluginRegistry;
    
    @Autowired
    private PlatformTransactionManager m_transactionManager;
    
    private final ThreadLocal<HashMap<String, OnmsServiceType>> m_typeCache = new ThreadLocal<HashMap<String, OnmsServiceType>>();
    private final ThreadLocal<HashMap<String, OnmsCategory>> m_categoryCache = new ThreadLocal<HashMap<String, OnmsCategory>>();
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        RequisitionFileUtils.deleteAllSnapshots(m_pendingForeignSourceRepository);
    }

    /**
     * <p>isDiscoveryEnabled</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isDiscoveryEnabled() {
        return System.getProperty("org.opennms.provisiond.enableDiscovery", "true").equalsIgnoreCase("true");
    }

    @Override
    public boolean isRequisitionedEntityDeletionEnabled() {
        return System.getProperty("org.opennms.provisiond.enableDeletionOfRequisitionedEntities", "false").equalsIgnoreCase("true");
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public void insertNode(final OnmsNode node) {
        
        node.setDistPoller(createDistPollerIfNecessary("localhost", "127.0.0.1"));
        m_nodeDao.save(node);
        m_nodeDao.flush();
        
        final EntityVisitor eventAccumlator = new AddEventVisitor(m_eventForwarder);

        node.visit(eventAccumlator);
        
    }
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public void updateNode(final OnmsNode node) {

    	final OnmsNode dbNode = m_nodeDao.getHierarchy(node.getId());

        dbNode.mergeNode(node, m_eventForwarder, false);
    
        m_nodeDao.update(dbNode);
        m_nodeDao.flush();

        final EntityVisitor eventAccumlator = new UpdateEventVisitor(m_eventForwarder);

        node.visit(eventAccumlator);
        
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public void deleteNode(final Integer nodeId) {
        
    	final OnmsNode node = m_nodeDao.get(nodeId);

    	if (node != null && shouldDelete(node)) {
            m_nodeDao.delete(node);
            node.visit(new DeleteEventVisitor(m_eventForwarder));
        }
        
    
    }

	private boolean shouldDelete(final OnmsNode node) {
		String foreignSource = node.getForeignSource();
		
		// only delete services that are on discovered nodes if discovery is enabled 
		// meaning provisiond is managing discovered nodes rather than capsd
		if (foreignSource == null) return isDiscoveryEnabled();
		
		// if we enable deletion of requisitioned entities then we can delete this 
		if (isRequisitionedEntityDeletionEnabled()) return true;
		
		// otherwise only delete if it is not requistioned
		return !isRequisitioned(node);

	}
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public void deleteInterface(final Integer nodeId, final String ipAddr) {
    	final OnmsIpInterface iface = m_ipInterfaceDao.findByNodeIdAndIpAddress(nodeId, ipAddr);
        if (iface != null && shouldDelete(iface)) {
            m_ipInterfaceDao.delete(iface);
            iface.visit(new DeleteEventVisitor(m_eventForwarder));
        }
        
    }

	private boolean shouldDelete(final OnmsIpInterface iface) {
		
		String foreignSource = iface.getNode().getForeignSource();
		
		// only delete services that are on discovered nodes if discovery is enabled 
		// meaning provisiond is managing discovered nodes rather than capsd
		if (foreignSource == null) return isDiscoveryEnabled();
		
		// if we enable deletion of requisitioned entities then we can delete this 
		if (isRequisitionedEntityDeletionEnabled()) return true;
		
		// otherwise only delete if it is not requistioned
		return !isRequisitioned(iface);

	}
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public void deleteService(final Integer nodeId, final InetAddress addr, final String service) {
    	final OnmsMonitoredService monSvc = m_monitoredServiceDao.get(nodeId, addr, service);
        if (monSvc != null && shouldDelete(monSvc)) {
            m_monitoredServiceDao.delete(monSvc);
            monSvc.visit(new DeleteEventVisitor(m_eventForwarder));
        }
        
    }

	private boolean shouldDelete(final OnmsMonitoredService monSvc) {
		String foreignSource = monSvc.getIpInterface().getNode().getForeignSource();
		
		// only delete services that are on discovered nodes if discovery is enabled 
		// meaning provisiond is managing discovered nodes rather than capsd
		if (foreignSource == null) return isDiscoveryEnabled();
		
		// if we enable deletion of requisitioned entities then we can delete this 
		if (isRequisitionedEntityDeletionEnabled()) return true;
		
		// otherwise only delete if it is not requistioned
		return !isRequisitioned(monSvc);
		
		
	}
	
	public boolean isRequisitioned(OnmsNode node) {
		String foreignSource = node.getForeignSource();
		String foreignId = node.getForeignId();
		
		// is this a discovered node
		if (foreignSource == null) return false;
		
		OnmsNode reqNode = getRequisitionedNode(foreignSource, foreignId);
		if (reqNode == null) { 
			// this is no requisition node?
			LOG.error("No requistion exists for node with foreignSource {} and foreignId {}.  Treating node as unrequistioned", foreignSource, foreignId);
			return false;
		} else {
			return true;
		}
		
	}
	
	public boolean isRequisitioned(OnmsIpInterface ip) {
		String foreignSource = ip.getNode().getForeignSource();
		String foreignId = ip.getNode().getForeignId();
		
		// is this a discovered node
		if (foreignSource == null) return false;
		
		OnmsNode reqNode = getRequisitionedNode(foreignSource, foreignId);
		if (reqNode == null) { 
			// this is no requisition node?
			LOG.error("No requistion exists for node with foreignSource {} and foreignId {}.  Treating node as unrequistioned", foreignSource, foreignId);
			return false;
		}
		
		OnmsIpInterface reqIp = reqNode.getIpInterfaceByIpAddress(ip.getIpAddress());
		// if we found the ip then its a requisitioned interface
		return reqIp != null;
		
	}
	
	public boolean isRequisitioned(OnmsMonitoredService monSvc) {
		String foreignSource = monSvc.getIpInterface().getNode().getForeignSource();
		String foreignId = monSvc.getIpInterface().getNode().getForeignId();
		
		// is this a discovered node
		if (foreignSource == null) return false;
		
		OnmsNode reqNode = getRequisitionedNode(foreignSource, foreignId);
		if (reqNode == null) { 
			// this is no requisition node?
			LOG.error("No requistion exists for node with foreignSource {} and foreignId {}.  Treating node as unrequistioned", foreignSource, foreignId);
			return false;
		}
		
		OnmsIpInterface reqIp = reqNode.getIpInterfaceByIpAddress(monSvc.getIpAddress());
		if (reqIp == null) {
			// there is no matching requistion ip so the interface was discovered
			return false;
		}
		
		OnmsMonitoredService reqSvc =  reqIp.getMonitoredServiceByServiceType(monSvc.getServiceName());
		
		// if we found the service then its a requistion service
		return reqSvc != null;
	}
	
    private void assertNotNull(final Object o, final String format, final Object... args) {
        if (o == null) {
            throw new IllegalArgumentException(String.format(format, args));
        }
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsIpInterface updateIpInterfaceAttributes(final Integer nodeId, final OnmsIpInterface scannedIface) {
    	final OnmsSnmpInterface snmpInterface = scannedIface.getSnmpInterface();
        if (snmpInterface != null && snmpInterface.getIfIndex() != null) {
            scannedIface.setSnmpInterface(updateSnmpInterfaceAttributes(nodeId, snmpInterface));
        }
        
        return new UpsertTemplate<OnmsIpInterface, IpInterfaceDao>(m_transactionManager, m_ipInterfaceDao) {

            @Override
            protected OnmsIpInterface query() {
                OnmsIpInterface dbIface = m_ipInterfaceDao.findByNodeIdAndIpAddress(nodeId, str(scannedIface.getIpAddress()));
                LOG.debug("Updating interface attributes for DB interface {} for node {} with ip {}", dbIface, nodeId, str(scannedIface.getIpAddress()));
                return dbIface;
            }

            @Override
            protected OnmsIpInterface doUpdate(final OnmsIpInterface dbIface) {
                if(dbIface.isManaged() && !scannedIface.isManaged()){
                    final Set<OnmsMonitoredService> monSvcs = dbIface.getMonitoredServices();
                    
                    for(final OnmsMonitoredService monSvc : monSvcs){
                        monSvc.visit(new DeleteEventVisitor(m_eventForwarder));
                    }
                    monSvcs.clear();
                }
                
                dbIface.mergeInterfaceAttributes(scannedIface);
                LOG.info("Updating IpInterface {}", dbIface);
                m_ipInterfaceDao.update(dbIface);
                m_ipInterfaceDao.flush();
                return dbIface;
            }

            @Override
            protected OnmsIpInterface doInsert() {
                final OnmsNode dbNode = m_nodeDao.load(nodeId);
                assertNotNull(dbNode, "no node found with nodeId %d", nodeId);
                // for performance reasons we don't add the ip interface to the node so we avoid loading all the interfaces
                // setNode only sets the node in the interface
                scannedIface.setNode(dbNode);
                saveOrUpdate(scannedIface);
                final AddEventVisitor visitor = new AddEventVisitor(m_eventForwarder);
                scannedIface.visit(visitor);
                m_ipInterfaceDao.flush();
                return scannedIface;
            }
        }.execute();
        

    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsSnmpInterface updateSnmpInterfaceAttributes(final Integer nodeId, final OnmsSnmpInterface snmpInterface) {
        return new UpsertTemplate<OnmsSnmpInterface, SnmpInterfaceDao>(m_transactionManager, m_snmpInterfaceDao) {

            @Override
            public OnmsSnmpInterface query() {
                final OnmsSnmpInterface dbSnmpIface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, snmpInterface.getIfIndex());
                LOG.debug("nodeId = {}, ifIndex = {}, dbSnmpIface = {}", nodeId, snmpInterface.getIfIndex(), dbSnmpIface);
                return dbSnmpIface;
            }

            @Override
            public OnmsSnmpInterface doUpdate(OnmsSnmpInterface dbSnmpIface) {
                // update the interface that was found
                dbSnmpIface.mergeSnmpInterfaceAttributes(snmpInterface);
                LOG.info("Updating SnmpInterface {}", dbSnmpIface);
                m_snmpInterfaceDao.update(dbSnmpIface);
                m_snmpInterfaceDao.flush();
                return dbSnmpIface;
            }

            @Override
            public OnmsSnmpInterface doInsert() {
                // add the interface to the node, if it wasn't found
                final OnmsNode dbNode = m_nodeDao.load(nodeId);
                assertNotNull(dbNode, "no node found with nodeId %d", nodeId);
                // for performance reasons we don't add the snmp interface to the node so we avoid loading all the interfaces
                // setNode only sets the node in the interface
                snmpInterface.setNode(dbNode);
                LOG.info("Saving SnmpInterface {}", snmpInterface);
                m_snmpInterfaceDao.save(snmpInterface);
                m_snmpInterfaceDao.flush();
                return snmpInterface;
            }
            
        }.execute();
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsMonitoredService addMonitoredService(final Integer ipInterfaceId, final String svcName) {
    	final OnmsIpInterface iface = m_ipInterfaceDao.get(ipInterfaceId);
        assertNotNull(iface, "could not find interface with id %d", ipInterfaceId);
        return addMonitoredService(iface, svcName);
        
    }

    private OnmsMonitoredService addMonitoredService(final OnmsIpInterface iface, final String svcName) {
        final OnmsServiceType svcType = createServiceTypeIfNecessary(svcName);
        
        return new CreateIfNecessaryTemplate<OnmsMonitoredService, MonitoredServiceDao>(m_transactionManager, m_monitoredServiceDao) {

            @Override
            protected OnmsMonitoredService query() {
                return iface.getMonitoredServiceByServiceType(svcName);
            }

            @Override
            protected OnmsMonitoredService doInsert() {
                OnmsMonitoredService svc = new OnmsMonitoredService(iface, svcType);
                svc.setStatus("A");
                m_ipInterfaceDao.saveOrUpdate(iface);
                m_ipInterfaceDao.flush();
                final AddEventVisitor visitor = new AddEventVisitor(m_eventForwarder);
                svc.visit(visitor);
                return svc;
            }
            
        }.execute();
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsMonitoredService addMonitoredService(final Integer nodeId, final String ipAddress, final String svcName) {
    	final OnmsIpInterface iface = m_ipInterfaceDao.findByNodeIdAndIpAddress(nodeId, ipAddress);
        assertNotNull(iface, "could not find interface with nodeid %d and ipAddr %s", nodeId, ipAddress);
        return addMonitoredService(iface, svcName);
    }

    @Transactional
    @Override
    public OnmsMonitoredService updateMonitoredServiceState(final Integer nodeId, final String ipAddress, final String svcName) {
        final OnmsIpInterface iface = m_ipInterfaceDao.findByNodeIdAndIpAddress(nodeId, ipAddress);
        assertNotNull(iface, "could not find interface with nodeid %d and ipAddr %s", nodeId, ipAddress);

        return new UpsertTemplate<OnmsMonitoredService, MonitoredServiceDao>(m_transactionManager, m_monitoredServiceDao) {

            @Override
            protected OnmsMonitoredService query() {
                return iface.getMonitoredServiceByServiceType(svcName);
            }

            @Override
            protected OnmsMonitoredService doUpdate(OnmsMonitoredService dbObj) { // NMS-3906
                LOG.debug("current status of service {} on node with IP {} is {} ", dbObj.getServiceName(), dbObj.getIpAddress().getHostAddress(), dbObj.getStatus());
                if ("S".equals(dbObj.getStatus())) {
                    LOG.debug("suspending polling for service {} on node with IP {}", dbObj.getServiceName(), dbObj.getIpAddress().getHostAddress());
                    dbObj.setStatus("F");
                    m_monitoredServiceDao.update(dbObj);
                    sendEvent(EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI, dbObj);
                }
                if ("R".equals(dbObj.getStatus())) {
                    LOG.debug("resume polling for service {} on node with IP {}", dbObj.getServiceName(), dbObj.getIpAddress().getHostAddress());
                    dbObj.setStatus("A");
                    m_monitoredServiceDao.update(dbObj);
                    sendEvent(EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI, dbObj);
                }
                return dbObj;
            }

            @Override
            protected OnmsMonitoredService doInsert() {
                return null;
            }

            private void sendEvent(String eventUEI, OnmsMonitoredService dbObj) {
                final EventBuilder bldr = new EventBuilder(eventUEI, "ProvisionService");
                bldr.setNodeid(dbObj.getNodeId());
                bldr.setInterface(dbObj.getIpAddress());
                bldr.setService(dbObj.getServiceName());
                m_eventForwarder.sendNow(bldr.getEvent());
            }

        }.execute();
    }

    /**
     * <p>clearCache</p>
     */
    @Transactional
    @Override
    public void clearCache() {
        m_nodeDao.clear();
        m_nodeDao.flush();
    }
    
    /** {@inheritDoc} */
    @Override
    public OnmsDistPoller createDistPollerIfNecessary(final String dpName, final String dpAddr) {
        return createDistPollerIfNecessary(new OnmsDistPoller(dpName, dpAddr));
    }
    
    public OnmsDistPoller createDistPollerIfNecessary(OnmsDistPoller scannedDistPoller) {
        
        final OnmsDistPoller distPoller = scannedDistPoller == null ? new OnmsDistPoller("localhost", "127.0.0.1") : scannedDistPoller;
        
        return new CreateIfNecessaryTemplate<OnmsDistPoller, DistPollerDao>(m_transactionManager, m_distPollerDao) {

            @Override
            protected OnmsDistPoller query() {
                return m_distPollerDao.get(distPoller.getName());
            }

            @Override
            public OnmsDistPoller doInsert() {
                m_distPollerDao.save(distPoller);
                m_distPollerDao.flush();
                return distPoller;
            }
        }.execute();
        
    }
    
    

    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsNode getRequisitionedNode(final String foreignSource, final String foreignId) throws ForeignSourceRepositoryException {
    	final OnmsNodeRequisition nodeReq = m_foreignSourceRepository.getNodeRequisition(foreignSource, foreignId);
        if (nodeReq == null) {
			LOG.warn("nodeReq for node {}:{} cannot be null!", foreignSource, foreignId);
            return null;
        }
        final OnmsNode node = nodeReq.constructOnmsNodeFromRequisition();
        
        // fill in real database categories
        final HashSet<OnmsCategory> dbCategories = new HashSet<OnmsCategory>();
        for(final OnmsCategory category : node.getCategories()) {
            dbCategories.add(createCategoryIfNecessary(category.getName()));
        }
        
        node.setCategories(dbCategories);
        
        // fill in real service types
        node.visit(new ServiceTypeFulfiller());
        
        return node;
    }
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsServiceType createServiceTypeIfNecessary(final String serviceName) {
        preloadExistingTypes();
        OnmsServiceType type = m_typeCache.get().get(serviceName);
        if (type == null) {
            type = loadServiceType(serviceName);
            m_typeCache.get().put(serviceName, type);
        }
        return type;
    }
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsCategory createCategoryIfNecessary(final String name) {
        preloadExistingCategories();
        
        OnmsCategory category = m_categoryCache.get().get(name);
        if (category == null) {    
            category = loadCategory(name);
            m_categoryCache.get().put(category.getName(), category);
        }
        return category;
    }
    
    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public Map<String, Integer> getForeignIdToNodeIdMap(final String foreignSource) {
        return m_nodeDao.getForeignIdToNodeIdMap(foreignSource);
    }
    
    /** {@inheritDoc} */
    @Override
    @Transactional
    public void setNodeParentAndDependencies(final String foreignSource, final String foreignId, final String parentForeignSource, final String parentForeignId, final String parentNodeLabel) {

        final OnmsNode node = findNodebyForeignId(foreignSource, foreignId);
        if (node == null) {
            return;
        }
        
        final OnmsNode parent = findParent(parentForeignSource, parentForeignId, parentNodeLabel);

        setParent(node, parent);
        setPathDependency(node, parent);

        m_nodeDao.update(node);
        m_nodeDao.flush();
    }

    private void preloadExistingTypes() {
        if (m_typeCache.get() == null) {
            m_typeCache.set(loadServiceTypeMap());
        }
    }
    
    @Transactional(readOnly=true)
    private HashMap<String, OnmsServiceType> loadServiceTypeMap() {
        final HashMap<String, OnmsServiceType> serviceTypeMap = new HashMap<String, OnmsServiceType>();
        for (final OnmsServiceType svcType : m_serviceTypeDao.findAll()) {
            serviceTypeMap.put(svcType.getName(), svcType);
        }
        return serviceTypeMap;
    }
    
    @Transactional
    private OnmsServiceType loadServiceType(final String serviceName) {
        return new CreateIfNecessaryTemplate<OnmsServiceType, ServiceTypeDao>(m_transactionManager, m_serviceTypeDao) {

            @Override
            protected OnmsServiceType query() {
                return m_serviceTypeDao.findByName(serviceName);
            }

            @Override
            public OnmsServiceType doInsert() {
                OnmsServiceType type = new OnmsServiceType(serviceName);
                m_serviceTypeDao.save(type);
                m_serviceTypeDao.flush();
                return type;
            }
            
        }.execute();
    }
    
    private void preloadExistingCategories() {
        if (m_categoryCache.get() == null) {
            m_categoryCache.set(loadCategoryMap());
        }
    }
    
    @Transactional(readOnly=true)
    private HashMap<String, OnmsCategory> loadCategoryMap() {
        final HashMap<String, OnmsCategory> categoryMap = new HashMap<String, OnmsCategory>();
        for (final OnmsCategory category : m_categoryDao.findAll()) {
            categoryMap.put(category.getName(), category);
        }
        return categoryMap;
    }
    
    @Transactional
    private OnmsCategory loadCategory(final String name) {
        return new CreateIfNecessaryTemplate<OnmsCategory, CategoryDao>(m_transactionManager, m_categoryDao) {

            @Override
            protected OnmsCategory query() {
                return m_categoryDao.findByName(name);
            }

            @Override
            public OnmsCategory doInsert() {
                OnmsCategory category = new OnmsCategory(name);
                m_categoryDao.save(category);
                m_categoryDao.flush();
                return category;
            }
        }.execute();
        
    }
    
    @Transactional(readOnly=true)
    private OnmsNode findNodebyNodeLabel(final String label) {
        Collection<OnmsNode> nodes = m_nodeDao.findByLabel(label);
    	if (nodes.size() == 1) {
            return nodes.iterator().next();
        }
    	LOG.error("Unable to locate a unique node using label {}: {} nodes found.  Ignoring relationship.", label, nodes.size());
    	return null;
    }
    
    @Transactional(readOnly=true)
    private OnmsNode findNodebyForeignId(final String foreignSource, final String foreignId) {
        return m_nodeDao.findByForeignId(foreignSource, foreignId);
    }
    
    @Transactional(readOnly=true)
    private OnmsNode findParent(final String foreignSource, final String parentForeignId, final String parentNodeLabel) {
        if (parentForeignId != null) {
            return findNodebyForeignId(foreignSource, parentForeignId);
        } else {
            if (parentNodeLabel != null) {
                return findNodebyNodeLabel(parentNodeLabel);
            }
        }
    	
    	return null;
    }
    
    private void setPathDependency(final OnmsNode node, final OnmsNode parent) {
        if (node == null) return;
        
        OnmsIpInterface critIface = null;
    	if (parent != null) {
    		critIface = parent.getCriticalInterface();
    	}

    	LOG.info("Setting criticalInterface of node: {} to: {}", node, critIface);
    	node.setPathElement(critIface == null ? null : new PathElement(str(critIface.getIpAddress()), "ICMP"));

    }
    
    @Transactional
    private void setParent(final OnmsNode node, final OnmsNode parent) {
        if (node == null) return;

        LOG.info("Setting parent of node: {} to: {}", node, parent);
        node.setParent(parent);

        m_nodeDao.update(node);
        m_nodeDao.flush();
    }
    
    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public NodeScanSchedule getScheduleForNode(final int nodeId, final boolean force) {
        return createScheduleForNode(m_nodeDao.get(nodeId), force);
    }
    
    /**
     * <p>getScheduleForNodes</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Transactional(readOnly=true)
    @Override
    public List<NodeScanSchedule> getScheduleForNodes() {
        Assert.notNull(m_nodeDao, "Node DAO is null and is not supposed to be");
        final List<OnmsNode> nodes = isDiscoveryEnabled() ? m_nodeDao.findAll() : m_nodeDao.findAllProvisionedNodes();
        
        final List<NodeScanSchedule> scheduledNodes = new ArrayList<NodeScanSchedule>();
        for(final OnmsNode node : nodes) {
        	final NodeScanSchedule nodeScanSchedule = createScheduleForNode(node, false);
            if (nodeScanSchedule != null) {
                scheduledNodes.add(nodeScanSchedule);
            }
        }
        
        return scheduledNodes;
    }
    
    private NodeScanSchedule createScheduleForNode(final OnmsNode node, final boolean force) {
        Assert.notNull(node, "Node may not be null");
        final String actualForeignSource = node.getForeignSource();
        if (actualForeignSource == null && !isDiscoveryEnabled()) {
			LOG.info("Not scheduling node {} to be scanned since it has a null foreignSource and handling of discovered nodes is disabled in provisiond", node);
            return null;
        }

        final String effectiveForeignSource = actualForeignSource == null ? "default" : actualForeignSource;
        try {
        	final ForeignSource fs = m_foreignSourceRepository.getForeignSource(effectiveForeignSource);

        	final Duration scanInterval = fs.getScanInterval();
        	Duration initialDelay = Duration.ZERO;
            if (node.getLastCapsdPoll() != null && !force) {
            	final DateTime nextPoll = new DateTime(node.getLastCapsdPoll().getTime()).plus(scanInterval);
                final DateTime now = new DateTime();
                if (nextPoll.isAfter(now)) {
                    initialDelay = new Duration(now, nextPoll);
                }
            }

            return new NodeScanSchedule(node.getId(), actualForeignSource, node.getForeignId(), initialDelay, scanInterval);
        } catch (final ForeignSourceRepositoryException e) {
            LOG.warn("unable to get foreign source '{}' from repository", effectiveForeignSource, e);
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setForeignSourceRepository(final ForeignSourceRepository foreignSourceRepository) {
        m_foreignSourceRepository = foreignSourceRepository;
    }

    /**
     * <p>getForeignSourceRepository</p>
     *
     * @return a {@link org.opennms.netmgt.provision.persist.ForeignSourceRepository} object.
     */
    public ForeignSourceRepository getForeignSourceRepository() {
        return m_foreignSourceRepository;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.service.ProvisionService#loadRequisition(java.lang.String, org.springframework.core.io.Resource)
     */
    /** {@inheritDoc} */
    @Override
    public Requisition loadRequisition(final Resource resource) {
    	final Requisition r = m_foreignSourceRepository.importResourceRequisition(resource);
        r.updateLastImported();
        m_foreignSourceRepository.save(r);
        return r;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.service.ProvisionService#updateNodeInfo(org.opennms.netmgt.model.OnmsNode)
     */
    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsNode updateNodeAttributes(final OnmsNode node) {

        return new UpsertTemplate<OnmsNode, NodeDao>(m_transactionManager, m_nodeDao) {

            @Override
            protected OnmsNode query() {
                return getDbNode(node);
            }

            @Override
            protected OnmsNode doUpdate(OnmsNode dbNode) {
                dbNode.mergeNodeAttributes(node, m_eventForwarder);
                return saveOrUpdate(dbNode);
            }

            @Override
            protected OnmsNode doInsert() {
                node.setDistPoller(createDistPollerIfNecessary(node.getDistPoller()));
                return saveOrUpdate(node);
            }
        }.execute();
        
    }

    @Transactional(readOnly=true)
    private OnmsNode getDbNode(final OnmsNode node) {
        OnmsNode dbNode;
        if (node.getId() != null) {
            dbNode = m_nodeDao.get(node.getId());
        } else {
            dbNode = m_nodeDao.findByForeignId(node.getForeignSource(), node.getForeignId());
        }
        return dbNode;
    }

    @Transactional
    private OnmsNode saveOrUpdate(final OnmsNode node) {
    	final Set<OnmsCategory> updatedCategories = new HashSet<OnmsCategory>();
        for(final Iterator<OnmsCategory> it = node.getCategories().iterator(); it.hasNext(); ) {
            final OnmsCategory category = it.next();
            if (category.getId() == null) {
                it.remove();
                updatedCategories.add(createCategoryIfNecessary(category.getName()));
            }
        }
        
        node.getCategories().addAll(updatedCategories);
        
        m_nodeDao.saveOrUpdate(node);
        m_nodeDao.flush();
        
        return node;
        
    }
    
    @Transactional
    private OnmsIpInterface saveOrUpdate(final OnmsIpInterface iface) {
        iface.visit(new ServiceTypeFulfiller());
        LOG.info("SaveOrUpdating IpInterface {}", iface);
        m_ipInterfaceDao.saveOrUpdate(iface);
        m_ipInterfaceDao.flush();
        
        return iface;
        
    }
    
    /** {@inheritDoc} */
    @Override
    public List<ServiceDetector> getDetectorsForForeignSource(final String foreignSourceName) {
    	final ForeignSource foreignSource = m_foreignSourceRepository.getForeignSource(foreignSourceName);
        assertNotNull(foreignSource, "Expected a foreignSource with name %s", foreignSourceName);
        
        final List<PluginConfig> detectorConfigs = foreignSource.getDetectors();
        if (detectorConfigs == null) {
            return new ArrayList<ServiceDetector>(m_pluginRegistry.getAllPlugins(ServiceDetector.class));
        }
        
        final List<ServiceDetector> detectors = new ArrayList<ServiceDetector>(detectorConfigs.size());
        for(final PluginConfig detectorConfig : detectorConfigs) {
            final ServiceDetector detector = m_pluginRegistry.getPluginInstance(ServiceDetector.class, detectorConfig);
            if (detector == null) {
				LOG.error("Configured plugin does not exist: {}", detectorConfig);
            } else {
                detector.setServiceName(detectorConfig.getName());
                detector.init();
                detectors.add(detector);
            }
        }

        return detectors;
    }

    /** {@inheritDoc} */
    @Override
    public List<NodePolicy> getNodePoliciesForForeignSource(final String foreignSourceName) {
        return getPluginsForForeignSource(NodePolicy.class, foreignSourceName);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<IpInterfacePolicy> getIpInterfacePoliciesForForeignSource(final String foreignSourceName) {
        return getPluginsForForeignSource(IpInterfacePolicy.class, foreignSourceName);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<SnmpInterfacePolicy> getSnmpInterfacePoliciesForForeignSource(final String foreignSourceName) {
        return getPluginsForForeignSource(SnmpInterfacePolicy.class, foreignSourceName);
    }

    
    /**
     * <p>getPluginsForForeignSource</p>
     *
     * @param pluginClass a {@link java.lang.Class} object.
     * @param foreignSourceName a {@link java.lang.String} object.
     * @param <T> a T object.
     * @return a {@link java.util.List} object.
     */
    public <T> List<T> getPluginsForForeignSource(final Class<T> pluginClass, final String foreignSourceName) {
    	final ForeignSource foreignSource = m_foreignSourceRepository.getForeignSource(foreignSourceName);
        assertNotNull(foreignSource, "Expected a foreignSource with name %s", foreignSourceName);
        
        final List<PluginConfig> configs = foreignSource.getPolicies();
        if (configs == null) {
            return Collections.emptyList(); 
        }
        
        final List<T> plugins = new ArrayList<T>(configs.size());
        for(final PluginConfig config : configs) {
            final T plugin = m_pluginRegistry.getPluginInstance(pluginClass, config);
            if (plugin == null) {
                LOG.trace("Configured plugin is not appropropriate for policy class {}: {}", pluginClass, config);
            } else {
                plugins.add(plugin);
            }
        }

        return plugins;
        
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public void deleteObsoleteInterfaces(final Integer nodeId, final Date scanStamp) {
    	final List<OnmsIpInterface> obsoleteInterfaces = m_nodeDao.findObsoleteIpInterfaces(nodeId, scanStamp);
        
    	for(final OnmsIpInterface iface : obsoleteInterfaces) {
            iface.visit(new DeleteEventVisitor(m_eventForwarder));
        }
        
        m_nodeDao.deleteObsoleteInterfaces(nodeId, scanStamp);
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public void updateNodeScanStamp(final Integer nodeId, final Date scanStamp) {
        m_nodeDao.updateNodeScanStamp(nodeId, scanStamp);
        m_nodeDao.flush();
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsIpInterface setIsPrimaryFlag(final Integer nodeId, final String ipAddress) {
        // TODO upsert? not sure if this needs one.. leave the todo here in case
        if (nodeId == null) {
            LOG.debug("nodeId is null!");
            return null;
        } else if (ipAddress == null) {
            LOG.debug("ipAddress is null!");
            return null;
        }
        final OnmsIpInterface svcIface = m_ipInterfaceDao.findByNodeIdAndIpAddress(nodeId, ipAddress);
        if (svcIface == null) {
            LOG.info("unable to find IPInterface for nodeId={}, ipAddress={}", nodeId, ipAddress);
            return null;
        }
        OnmsIpInterface primaryIface = null;
        if (svcIface.isPrimary()) {
            primaryIface = svcIface;
        } 
        else if (svcIface.getNode().getPrimaryInterface() == null) {
            svcIface.setIsSnmpPrimary(PrimaryType.PRIMARY);
            m_ipInterfaceDao.saveOrUpdate(svcIface);
            m_ipInterfaceDao.flush();
            primaryIface= svcIface;
        } else {
            svcIface.setIsSnmpPrimary(PrimaryType.SECONDARY);
            m_ipInterfaceDao.saveOrUpdate(svcIface);
            m_ipInterfaceDao.flush();
        }
        
        m_ipInterfaceDao.initialize(primaryIface);
        return primaryIface;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsIpInterface getPrimaryInterfaceForNode(final OnmsNode node) {
    	final OnmsNode dbNode = getDbNode(node);
        if (dbNode == null) {
            return null;
        }
        else {
        	final OnmsIpInterface primaryIface = dbNode.getPrimaryInterface();
            if (primaryIface != null) {
                m_ipInterfaceDao.initialize(primaryIface);
                m_ipInterfaceDao.initialize(primaryIface.getMonitoredServices());
            }
            return primaryIface;
        }
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsNode createUndiscoveredNode(final String ipAddress) {
        
        OnmsNode node = new UpsertTemplate<OnmsNode, NodeDao>(m_transactionManager, m_nodeDao) {

            @Override
            protected OnmsNode query() {
                List<OnmsNode> nodes = m_nodeDao.findByForeignSourceAndIpAddress(FOREIGN_SOURCE_FOR_DISCOVERED_NODES, ipAddress);
                return nodes.size() > 0 ? nodes.get(0) : null;
            }

            @Override
            protected OnmsNode doUpdate(OnmsNode existingNode) {
                // we found an existing node so exit by returning null;
                return null;
            }

            @Override
            protected OnmsNode doInsert() {
                final Date now = new Date();
                
                final String hostname = getHostnameForIp(ipAddress);
                
                // @ipv6
                final OnmsNode node = new OnmsNode(createDistPollerIfNecessary("localhost", "127.0.0.1"));
                node.setLabel(hostname == null ? ipAddress : hostname);
                node.setLabelSource(hostname == null ? "A" : "H");
                node.setForeignSource(FOREIGN_SOURCE_FOR_DISCOVERED_NODES);
                node.setType("A");
                node.setLastCapsdPoll(now);
                
                final OnmsIpInterface iface = new OnmsIpInterface(InetAddressUtils.addr(ipAddress), node);
                iface.setIsManaged("M");
                iface.setIpHostName(hostname);
                iface.setIsSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
                iface.setIpLastCapsdPoll(now);
                
                m_nodeDao.save(node);
                m_nodeDao.flush();
                return node;
            }
        }.execute();
        
        if (node != null) {
            
            // we do this here rather than in the doInsert method because
            // the doInsert may abort
            node.visit(new AddEventVisitor(m_eventForwarder));
        }
        
        return node;
        
    }
    
    private String getHostnameForIp(final String address) {
    	return addr(address).getCanonicalHostName();
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsNode getNode(final Integer nodeId) {
    	final OnmsNode node = m_nodeDao.get(nodeId);
        m_nodeDao.initialize(node);
        m_nodeDao.initialize(node.getCategories());
        m_nodeDao.initialize(node.getIpInterfaces());
        return node;
    }
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public OnmsNode getDbNodeInitCat(final Integer nodeId) {
    	final OnmsNode node = m_nodeDao.get(nodeId);
        m_nodeDao.initialize(node.getCategories());
        m_nodeDao.initialize(node.getDistPoller());
        return node;
    }
}
