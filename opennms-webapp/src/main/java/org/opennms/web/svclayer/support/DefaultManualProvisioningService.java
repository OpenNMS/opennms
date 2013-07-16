/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.beanutils.MethodUtils;
import org.opennms.core.utils.PropertyPath;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.CapsdConfig;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.web.api.Util;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>DefaultManualProvisioningService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultManualProvisioningService implements ManualProvisioningService {

    private ForeignSourceRepository m_deployedForeignSourceRepository;
    private ForeignSourceRepository m_pendingForeignSourceRepository;
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private ServiceTypeDao m_serviceTypeDao;
    private CapsdConfig m_capsdConfig;
    
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    /**
     * <p>Constructor for DefaultManualProvisioningService.</p>
     */
    public DefaultManualProvisioningService() {
        
    }
    
    /**
     * <p>setDeployedForeignSourceRepository</p>
     *
     * @param repository a {@link org.opennms.netmgt.provision.persist.ForeignSourceRepository} object.
     */
    public void setDeployedForeignSourceRepository(final ForeignSourceRepository repository) {
        m_writeLock.lock();
        try {
            m_deployedForeignSourceRepository = repository;
        } finally {
            m_writeLock.unlock();
        }
    }
    
    /**
     * <p>setPendingForeignSourceRepository</p>
     *
     * @param repository a {@link org.opennms.netmgt.provision.persist.ForeignSourceRepository} object.
     */
    public void setPendingForeignSourceRepository(final ForeignSourceRepository repository) {
        m_writeLock.lock();
        try {
            m_pendingForeignSourceRepository = repository;
        } finally {
            m_writeLock.unlock();
        }
    }
    
    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(final NodeDao nodeDao) {
        m_writeLock.lock();
        try {
            m_nodeDao = nodeDao;
        } finally {
            m_writeLock.unlock();
        }
    }
    
    /**
     * <p>setCategoryDao</p>
     *
     * @param categoryDao a {@link org.opennms.netmgt.dao.api.CategoryDao} object.
     */
    public void setCategoryDao(final CategoryDao categoryDao) {
        m_writeLock.lock();
        try {
            m_categoryDao = categoryDao;
        } finally {
            m_writeLock.unlock();
        }
    }
    
    /**
     * <p>setServiceTypeDao</p>
     *
     * @param serviceTypeDao a {@link org.opennms.netmgt.dao.api.ServiceTypeDao} object.
     */
    public void setServiceTypeDao(final ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }
    
    public void setCapsdConfig(final CapsdConfig capsdConfig) {
        m_capsdConfig = capsdConfig;
    }

    /** {@inheritDoc} */
    @Override
    public Requisition addCategoryToNode(final String groupName, final String pathToNode, final String categoryName) {
        m_writeLock.lock();
        try {
            final Requisition group = getProvisioningGroup(groupName);
            
            final RequisitionNode node = PropertyUtils.getPathValue(group, pathToNode, RequisitionNode.class);
            
            // final int catCount = node.getCategoryCount();
            final RequisitionCategory category = new RequisitionCategory();
            category.setName(categoryName);
            node.putCategory(category);
            // Assert.isTrue(node.getCategoryCount() == (catCount + 1), "Category was not added correctly");
    
            m_pendingForeignSourceRepository.save(group);
            m_pendingForeignSourceRepository.flush();
            return m_pendingForeignSourceRepository.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Requisition addAssetFieldToNode(final String groupName, final String pathToNode, final String assetName, final String assetValue) {
        m_writeLock.lock();
        try {
            final Requisition group = getProvisioningGroup(groupName);
            final RequisitionNode node = PropertyUtils.getPathValue(group, pathToNode, RequisitionNode.class);
    
            // final int assetCount = node.getAssetCount();
            final RequisitionAsset asset = new RequisitionAsset();
            asset.setName(assetName);
            asset.setValue(assetValue);
            node.putAsset(asset);
            // Assert.isTrue(node.getCategoryCount() == (assetCount + 1), "Asset was not added correctly");
    
            m_pendingForeignSourceRepository.save(group);
            m_pendingForeignSourceRepository.flush();
            return m_pendingForeignSourceRepository.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Requisition addInterfaceToNode(final String groupName, final String pathToNode, final String ipAddr) {
        m_writeLock.lock();
        try {
            final Requisition group = getProvisioningGroup(groupName);
            Assert.notNull(group, "Group should not be Null and is null groupName: " + groupName);
            final RequisitionNode node = PropertyUtils.getPathValue(group, pathToNode, RequisitionNode.class);
            Assert.notNull(node, "Node should not be Null and pathToNode: " + pathToNode);

            PrimaryType snmpPrimary = PrimaryType.PRIMARY;
            if (node.getInterfaceCount() > 0) {
                snmpPrimary = PrimaryType.SECONDARY;
            }
    
            // final int ifaceCount = node.getInterfaceCount();
            final RequisitionInterface iface = createInterface(ipAddr, snmpPrimary);
            node.putInterface(iface);
            // Assert.isTrue(node.getInterfaceCount() == (ifaceCount + 1), "Interface was not added correctly");
    
            m_pendingForeignSourceRepository.save(group);
            m_pendingForeignSourceRepository.flush();
            return m_pendingForeignSourceRepository.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    private RequisitionInterface createInterface(final String ipAddr, final PrimaryType snmpPrimary) {
        final RequisitionInterface iface = new RequisitionInterface();
        iface.setIpAddr(ipAddr);
        iface.setStatus(1);
        iface.setSnmpPrimary(snmpPrimary);
        return iface;
    }

    /** {@inheritDoc} */
    @Override
    public Requisition addNewNodeToGroup(final String groupName, final String nodeLabel) {
        m_writeLock.lock();
        
        try {
            final Requisition group = getProvisioningGroup(groupName);
    
            final RequisitionNode node = createNode(nodeLabel, String.valueOf(System.currentTimeMillis()));
            node.setBuilding(groupName);
            group.insertNode(node);
            
            m_pendingForeignSourceRepository.save(group);
            m_pendingForeignSourceRepository.flush();
            return m_pendingForeignSourceRepository.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    private RequisitionNode createNode(final String nodeLabel, final String foreignId) {
        final RequisitionNode node = new RequisitionNode();
        node.setNodeLabel(nodeLabel);
        node.setForeignId(foreignId);
        return node;
    }

    /** {@inheritDoc} */
    @Override
    public Requisition addServiceToInterface(final String groupName, final String pathToInterface, final String serviceName) {
        m_writeLock.lock();
        
        try {
            final Requisition group = getProvisioningGroup(groupName);
            
            final RequisitionInterface iface = PropertyUtils.getPathValue(group, pathToInterface, RequisitionInterface.class);
            
            final RequisitionMonitoredService monSvc = createService(serviceName);
            iface.insertMonitoredService(monSvc);
    
            m_pendingForeignSourceRepository.save(group);
            m_pendingForeignSourceRepository.flush();
            return m_pendingForeignSourceRepository.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Requisition getProvisioningGroup(final String name) {
        m_readLock.lock();
        try {
            m_pendingForeignSourceRepository.flush();
            final Requisition pending  = m_pendingForeignSourceRepository.getRequisition(name);

            if (pending == null) {
                m_deployedForeignSourceRepository.flush();
                final Requisition deployed = m_deployedForeignSourceRepository.getRequisition(name);
                return deployed;
            }

            return pending;
        } finally {
            m_readLock.unlock();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Requisition saveProvisioningGroup(final String groupName, final Requisition group) {
        m_writeLock.lock();
        try {
            group.setForeignSource(groupName);
            m_pendingForeignSourceRepository.save(group);
            m_pendingForeignSourceRepository.flush();
            return m_pendingForeignSourceRepository.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    /**
     * <p>getProvisioningGroupNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<String> getProvisioningGroupNames() {
        m_readLock.lock();
        try {
            m_deployedForeignSourceRepository.flush();
            final Set<String> names = new TreeSet<String>();
            for (final Requisition r : m_deployedForeignSourceRepository.getRequisitions()) {
                names.add(r.getForeignSource());
            }
            m_pendingForeignSourceRepository.flush();
            for (final Requisition r : m_pendingForeignSourceRepository.getRequisitions()) {
                names.add(r.getForeignSource());
            }
            return names;
        } finally {
            m_readLock.unlock();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Requisition createProvisioningGroup(final String name) {
        m_writeLock.lock();
        try {
            final Requisition group = new Requisition();
            group.setForeignSource(name);
    
            m_pendingForeignSourceRepository.save(group);
            m_pendingForeignSourceRepository.flush();
            return m_pendingForeignSourceRepository.getRequisition(name);
        } finally {
            m_writeLock.unlock();
        }
    }

    private RequisitionMonitoredService createService(final String serviceName) {
        final RequisitionMonitoredService svc = new RequisitionMonitoredService();
        svc.setServiceName(serviceName);
        return svc;
    }


    /** {@inheritDoc} */
    @Override
    public void importProvisioningGroup(final String groupName) {
        m_writeLock.lock();
        
        try {
            // first we update the import timestamp
            final Requisition group = getProvisioningGroup(groupName);
            group.updateDateStamp();
            saveProvisioningGroup(groupName, group);
            
            // then we send an event to the importer
            final EventProxy proxy = Util.createEventProxy();
    
            m_pendingForeignSourceRepository.flush();
            final String url = m_pendingForeignSourceRepository.getRequisitionURL(groupName).toString();
            Assert.notNull(url, "Could not find url for group "+groupName+".  Does it exists?");
            
            final EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, "Web");
            bldr.addParam(EventConstants.PARM_URL, url);
            
            try {
                proxy.send(bldr.getEvent());
            } catch (final EventProxyException e) {
                throw new DataAccessResourceFailureException("Unable to send event to import group "+groupName, e);
            }
        } finally {
            m_writeLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Requisition deletePath(final String groupName, final String pathToDelete) {
        m_writeLock.lock();
        
        try {
            final Requisition group = getProvisioningGroup(groupName);
    
            final PropertyPath path = new PropertyPath(pathToDelete);
            
            final Object objToDelete = path.getValue(group);
            final Object parentObject = path.getParent() == null ? group : path.getParent().getValue(group);
            
            final String propName = path.getPropertyName();
            final String methodSuffix = Character.toUpperCase(propName.charAt(0))+propName.substring(1);
            final String methodName = "delete"+methodSuffix;
    
            try {
                MethodUtils.invokeMethod(parentObject, methodName, new Object[] { objToDelete });
            } catch (final NoSuchMethodException e) {
                throw new IllegalArgumentException("Unable to find method "+methodName+" on object of type "+parentObject.getClass(), e);
            } catch (final IllegalAccessException e) {
                throw new IllegalArgumentException("unable to access property "+pathToDelete, e);
            } catch (final InvocationTargetException e) {
                throw new IllegalArgumentException("an execption occurred deleting "+pathToDelete, e);
            }
            
            m_pendingForeignSourceRepository.save(group);
            m_pendingForeignSourceRepository.flush();
            return m_pendingForeignSourceRepository.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    /**
     * <p>getAllGroups</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<Requisition> getAllGroups() {
        m_readLock.lock();
        
        try {
            final Collection<Requisition> groups = new LinkedList<Requisition>();
    
            for(final String groupName : getProvisioningGroupNames()) {
                groups.add(getProvisioningGroup(groupName));
            }
            
            return groups;
        } finally {
            m_readLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteProvisioningGroup(final String groupName) {
        m_writeLock.lock();
        
        try {
            final Requisition r = getProvisioningGroup(groupName);
            if (r != null) {
                m_deployedForeignSourceRepository.delete(r);
                m_deployedForeignSourceRepository.flush();
                m_pendingForeignSourceRepository.delete(r);
                m_pendingForeignSourceRepository.flush();
            }
        } finally {
            m_writeLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteAllNodes(final String groupName) {
        m_writeLock.lock();
        
        try {
            m_deployedForeignSourceRepository.flush();
            Requisition group = m_deployedForeignSourceRepository.getRequisition(groupName);
            if (group != null) {
                group.setNodes(new ArrayList<RequisitionNode>());
                m_deployedForeignSourceRepository.save(group);
            }
    
            m_pendingForeignSourceRepository.flush();
            group = m_pendingForeignSourceRepository.getRequisition(groupName);
            if (group != null) {
                group.setNodes(new ArrayList<RequisitionNode>());
                m_pendingForeignSourceRepository.save(group);
            }
        } finally {
            m_writeLock.unlock();
        }
    }

    /**
     * <p>getGroupDbNodeCounts</p>
     *
     * @return a java$util$Map object.
     */
    @Override
    public Map<String, Integer> getGroupDbNodeCounts() {
        m_readLock.lock();
        
        try {
            final Map<String, Integer> counts = new HashMap<String, Integer>();
            
            for(final String groupName : getProvisioningGroupNames()) {
                counts.put(groupName, m_nodeDao.getNodeCountForForeignSource(groupName));
            }
            
            return counts;
        } finally {
            m_readLock.unlock();
        }
    }

    /**
     * <p>getNodeCategoryNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<String> getNodeCategoryNames() {
        m_readLock.lock();
        
        try {
            final Collection<String> names = new LinkedList<String>();
            for (final OnmsCategory category : m_categoryDao.findAll()) {
                names.add(category.getName());
            }
            return names;
        } finally {
            m_readLock.unlock();
        }
    }
    
    /**
     * <p>getServiceTypeNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<String> getServiceTypeNames(String groupName) {
        final SortedSet<String> serviceNames = new TreeSet<String>();

        m_readLock.lock();
        try {
            m_pendingForeignSourceRepository.flush();
            final ForeignSource pendingForeignSource = m_pendingForeignSourceRepository.getForeignSource(groupName);
            serviceNames.addAll(pendingForeignSource.getDetectorNames());

            m_deployedForeignSourceRepository.flush();
            final ForeignSource deployedForeignSource = m_deployedForeignSourceRepository.getForeignSource(groupName);
            serviceNames.addAll(deployedForeignSource.getDetectorNames());

            for (final OnmsServiceType type : m_serviceTypeDao.findAll()) {
                serviceNames.add(type.getName());
            }
            serviceNames.addAll(m_capsdConfig.getConfiguredProtocols());
            return serviceNames;
        } finally {
            m_readLock.unlock();
        }
    }

    /**
     * <p>getAssetFieldNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<String> getAssetFieldNames() {
        m_readLock.lock();
        
        try {
            return PropertyUtils.getProperties(new OnmsAssetRecord());
        } finally {
            m_readLock.unlock();
        }
    }

}
