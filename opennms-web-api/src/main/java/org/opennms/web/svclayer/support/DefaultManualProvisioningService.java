/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.beanutils.MethodUtils;
import org.opennms.core.spring.PropertyPath;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.requisition.OnmsForeignSource;
import org.opennms.netmgt.model.requisition.OnmsRequisition;
import org.opennms.netmgt.model.requisition.OnmsRequisitionInterface;
import org.opennms.netmgt.model.requisition.OnmsRequisitionMonitoredService;
import org.opennms.netmgt.model.requisition.OnmsRequisitionNode;
import org.opennms.netmgt.provision.persist.ForeignSourceService;
import org.opennms.netmgt.provision.persist.RequisitionService;
import org.opennms.netmgt.provision.persist.requisition.ImportRequest;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * <p>DefaultManualProvisioningService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@Transactional
@Deprecated
public class DefaultManualProvisioningService implements ManualProvisioningService {

    private static final List<String> ASSETS_BLACKLIST = new ArrayList<String>();

    @Autowired
    private RequisitionService requisitionService;

    @Autowired
    @Qualifier("default")
    private ForeignSourceService foreignSourceService;

    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private ServiceTypeDao m_serviceTypeDao;
    private PollerConfig m_pollerConfig;

    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    static {
        ASSETS_BLACKLIST.add("id");
        ASSETS_BLACKLIST.add("class");
        ASSETS_BLACKLIST.add("geolocation");
        ASSETS_BLACKLIST.add("node");
    }

    public void setNodeDao(final NodeDao nodeDao) {
        m_writeLock.lock();
        try {
            m_nodeDao = nodeDao;
        } finally {
            m_writeLock.unlock();
        }
    }

    public void setCategoryDao(final CategoryDao categoryDao) {
        m_writeLock.lock();
        try {
            m_categoryDao = categoryDao;
        } finally {
            m_writeLock.unlock();
        }
    }

    public void setServiceTypeDao(final ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }

    public void setPollerConfig(final PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    @Override
    public OnmsRequisition addCategoryToNode(final String groupName, final String pathToNode, final String categoryName) {
        m_writeLock.lock();
        try {
            final OnmsRequisition group = getProvisioningGroup(groupName);
            final OnmsRequisitionNode node = PropertyUtils.getPathValue(group, pathToNode, OnmsRequisitionNode.class);
            node.addCategory(categoryName);

            requisitionService.saveOrUpdateRequisition(group);
            return requisitionService.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    @Override
    public OnmsRequisition addAssetFieldToNode(final String groupName, final String pathToNode, final String assetName, final String assetValue) {
        m_writeLock.lock();
        try {
            final OnmsRequisition group = getProvisioningGroup(groupName);
            final OnmsRequisitionNode node = PropertyUtils.getPathValue(group, pathToNode, OnmsRequisitionNode.class);
            node.addAsset(assetName, assetValue);

            requisitionService.saveOrUpdateRequisition(group);
            return requisitionService.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    @Override
    public OnmsRequisition addInterfaceToNode(final String groupName, final String pathToNode, final String ipAddr) {
        m_writeLock.lock();
        try {
            final OnmsRequisition group = getProvisioningGroup(groupName);
            Assert.notNull(group, "Group should not be Null and is null groupName: " + groupName);
            final OnmsRequisitionNode node = PropertyUtils.getPathValue(group, pathToNode, OnmsRequisitionNode.class);
            Assert.notNull(node, "Node should not be Null and pathToNode: " + pathToNode);

            PrimaryType snmpPrimary = PrimaryType.PRIMARY;
            if (node.getInterfaces().size() > 0) {
                snmpPrimary = PrimaryType.SECONDARY;
            }

            final OnmsRequisitionInterface iface = createInterface(ipAddr, snmpPrimary);
            node.addInterface(iface);

            requisitionService.saveOrUpdateRequisition(group);
            return requisitionService.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    private OnmsRequisitionInterface createInterface(final String ipAddr, final PrimaryType snmpPrimary) {
        final OnmsRequisitionInterface iface = new OnmsRequisitionInterface();
        iface.setIpAddress(ipAddr);
        iface.setStatus(1);
        iface.setSnmpPrimary(snmpPrimary);
        return iface;
    }

    @Override
    public OnmsRequisition addNewNodeToGroup(final String groupName, final String nodeLabel) {
        m_writeLock.lock();

        try {
            final OnmsRequisition group = getProvisioningGroup(groupName);

            final OnmsRequisitionNode node = createNode(nodeLabel, String.valueOf(System.currentTimeMillis()));
            node.setBuilding(groupName);
            group.addNode(node);

            requisitionService.saveOrUpdateRequisition(group);
            return requisitionService.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    private OnmsRequisitionNode createNode(final String nodeLabel, final String foreignId) {
        final OnmsRequisitionNode node = new OnmsRequisitionNode();
        node.setNodeLabel(nodeLabel);
        node.setForeignId(foreignId);
        return node;
    }

    @Override
    public OnmsRequisition addServiceToInterface(final String groupName, final String pathToInterface, final String serviceName) {
        m_writeLock.lock();

        try {
            final OnmsRequisition group = getProvisioningGroup(groupName);

            final OnmsRequisitionInterface iface = PropertyUtils.getPathValue(group, pathToInterface, OnmsRequisitionInterface.class);

            final OnmsRequisitionMonitoredService monSvc = createService(serviceName);
            iface.addMonitoredService(monSvc);

            requisitionService.saveOrUpdateRequisition(group);
            return requisitionService.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    @Override
    public OnmsRequisition getProvisioningGroup(final String name) {
        m_readLock.lock();
        try {
            return requisitionService.getRequisition(name);
        } finally {
            m_readLock.unlock();
        }
    }

    @Override
    public OnmsRequisition saveProvisioningGroup(final String groupName, final OnmsRequisition group) {
        m_writeLock.lock();
        try {
            trimWhitespace(group);
            group.setForeignSource(groupName);
            requisitionService.saveOrUpdateRequisition(group);
            return requisitionService.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    @Override
    public Collection<String> getProvisioningGroupNames() {
        m_readLock.lock();
        try {
            final Set<String> names = new TreeSet<>();
            for (final OnmsRequisition r : requisitionService.getRequisitions()) {
                names.add(r.getForeignSource());
            }
            return names;
        } finally {
            m_readLock.unlock();
        }
    }

    @Override
    public OnmsRequisition createProvisioningGroup(final String name) {
        m_writeLock.lock();
        try {
            final OnmsRequisition group = new OnmsRequisition();
            group.setForeignSource(name);

            requisitionService.saveOrUpdateRequisition(group);
            return requisitionService.getRequisition(name);
        } finally {
            m_writeLock.unlock();
        }
    }

    private OnmsRequisitionMonitoredService createService(final String serviceName) {
        final OnmsRequisitionMonitoredService svc = new OnmsRequisitionMonitoredService();
        svc.setServiceName(serviceName);
        return svc;
    }


    @Override
    public void importProvisioningGroup(final String requisitionName) {
        m_writeLock.lock();
        try {
            requisitionService.triggerImport(new ImportRequest("Web").withForeignSource(requisitionName));
        } finally {
            m_writeLock.unlock();
        }
    }

    @Override
    public OnmsRequisition deletePath(final String groupName, final String pathToDelete) {
        m_writeLock.lock();

        try {
            final OnmsRequisition group = getProvisioningGroup(groupName);

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

            requisitionService.saveOrUpdateRequisition(group);
            return requisitionService.getRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    @Override
    public Collection<OnmsRequisition> getAllGroups() {
        m_readLock.lock();

        try {
            final Collection<OnmsRequisition> groups = new LinkedList<>();

            for(final String groupName : getProvisioningGroupNames()) {
                groups.add(getProvisioningGroup(groupName));
            }

            return groups;
        } finally {
            m_readLock.unlock();
        }
    }

    @Override
    public void deleteProvisioningGroup(final String groupName) {
        m_writeLock.lock();

        try {
            requisitionService.deleteRequisition(groupName);
        } finally {
            m_writeLock.unlock();
        }
    }

    @Override
    public void deleteAllNodes(final String groupName) {
        m_writeLock.lock();

        try {
            OnmsRequisition group = requisitionService.getRequisition(groupName);
            if (group != null) {
                group.setNodes(new ArrayList<>());
                requisitionService.saveOrUpdateRequisition(group);
            }
        } finally {
            m_writeLock.unlock();
        }
    }

    @Override
    public Map<String, Integer> getGroupDbNodeCounts() {
        m_readLock.lock();

        try {
            final Map<String, Integer> counts = new HashMap<>();

            for(final String groupName : getProvisioningGroupNames()) {
                counts.put(groupName, m_nodeDao.getNodeCountForForeignSource(groupName));
            }

            return counts;
        } finally {
            m_readLock.unlock();
        }
    }

    @Override
    public Collection<String> getNodeCategoryNames() {
        m_readLock.lock();

        try {
            final Collection<String> names = new LinkedList<>();
            for (final OnmsCategory category : m_categoryDao.findAll()) {
                names.add(category.getName());
            }
            return names;
        } finally {
            m_readLock.unlock();
        }
    }

    @Override
    public Collection<String> getServiceTypeNames(String groupName) {
        m_readLock.lock();
        try {
            final SortedSet<String> serviceNames = new TreeSet<>();
            final OnmsForeignSource pendingForeignSource = foreignSourceService.getForeignSource(groupName);
            serviceNames.addAll(pendingForeignSource.getDetectorNames());

            for (final OnmsServiceType type : m_serviceTypeDao.findAll()) {
                serviceNames.add(type.getName());
            }

            // Include all of the service names defined in the poller configuration
            if (m_pollerConfig != null && m_pollerConfig.getServiceMonitors() != null && ! m_pollerConfig.getServiceMonitors().isEmpty()) {
                serviceNames.addAll(m_pollerConfig.getServiceMonitors().keySet());
            }

            return serviceNames;
        } finally {
            m_readLock.unlock();
        }
    }

    @Override
    public Collection<String> getAssetFieldNames() {
        m_readLock.lock();

        try {
            final Collection<String> assets = PropertyUtils.getProperties(new OnmsAssetRecord());
            assets.removeIf(a -> ASSETS_BLACKLIST.contains(a));
            return assets;
        } finally {
            m_readLock.unlock();
        }
    }

    /**
     * Removes leading and trailing whitespace from fields that should not have any
     */
    private void trimWhitespace(OnmsRequisition req) {
        for (OnmsRequisitionNode node : req.getNodes()) {
            if (node.getForeignId() != null) {
                node.setForeignId(node.getForeignId().trim());
            }
            if (node.getParentForeignSource() != null) {
                node.setParentForeignSource(node.getParentForeignSource().trim());
            }
            if (node.getParentForeignId() != null) {
                node.setParentForeignId(node.getParentForeignId().trim());
            }
            for (OnmsRequisitionInterface intf : node.getInterfaces()) {
                if (intf.getIpAddress() != null) {
                    intf.setIpAddress(intf.getIpAddress().trim());
                }
            }
        }
    }

    public void setRequisitionService(RequisitionService requisitionService) {
        this.requisitionService = requisitionService;
    }

    public void setForeignSourceService(ForeignSourceService foreignSourceService) {
        this.foreignSourceService = foreignSourceService;
    }
}
