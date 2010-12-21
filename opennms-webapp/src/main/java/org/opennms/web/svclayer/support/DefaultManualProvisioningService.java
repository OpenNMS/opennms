/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 2007 Aug 03: Change Castor methods clearX -> removeAllX. - dj@opennms.org
 * 
 * Created: November 3, 2006
 *
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


package org.opennms.web.svclayer.support;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.MethodUtils;
import org.opennms.core.utils.PropertyPath;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.web.BeanUtils;
import org.opennms.web.api.Util;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>DefaultManualProvisioningService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultManualProvisioningService implements ManualProvisioningService {

    private ForeignSourceRepository m_deployedForeignSourceRepository;
    private ForeignSourceRepository m_pendingForeignSourceRepository;
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private ServiceTypeDao m_serviceTypeDao;
    
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
    public void setDeployedForeignSourceRepository(ForeignSourceRepository repository) {
        m_deployedForeignSourceRepository = repository;
    }
    
    /**
     * <p>setPendingForeignSourceRepository</p>
     *
     * @param repository a {@link org.opennms.netmgt.provision.persist.ForeignSourceRepository} object.
     */
    public void setPendingForeignSourceRepository(ForeignSourceRepository repository) {
        m_pendingForeignSourceRepository = repository;
    }
    
    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    /**
     * <p>setCategoryDao</p>
     *
     * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
     */
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }
    
    /**
     * <p>setServiceTypeDao</p>
     *
     * @param serviceTypeDao a {@link org.opennms.netmgt.dao.ServiceTypeDao} object.
     */
    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }

    /** {@inheritDoc} */
    public Requisition addCategoryToNode(String groupName, String pathToNode, String categoryName) {
        Requisition group = getProvisioningGroup(groupName);
        
        RequisitionNode node = BeanUtils.getPathValue(group, pathToNode, RequisitionNode.class);
        
        RequisitionCategory category = new RequisitionCategory();
        category.setName(categoryName);
        node.insertCategory(category);

        m_pendingForeignSourceRepository.save(group);

        return m_pendingForeignSourceRepository.getRequisition(groupName);
    }
    
    /** {@inheritDoc} */
    public Requisition addAssetFieldToNode(String groupName, String pathToNode, String assetName, String assetValue) {
        Requisition group = getProvisioningGroup(groupName);
        RequisitionNode node = BeanUtils.getPathValue(group, pathToNode, RequisitionNode.class);

        RequisitionAsset asset = new RequisitionAsset();
        asset.setName(assetName);
        asset.setValue(assetValue);
        node.insertAsset(asset);

        m_pendingForeignSourceRepository.save(group);
        return m_pendingForeignSourceRepository.getRequisition(groupName);
    }

    /** {@inheritDoc} */
    public Requisition addInterfaceToNode(String groupName, String pathToNode, String ipAddr) {
        Requisition group = getProvisioningGroup(groupName);
        Assert.notNull(group, "Group should not be Null and is null groupName: " + groupName);
        RequisitionNode node = BeanUtils.getPathValue(group, pathToNode, RequisitionNode.class);
        Assert.notNull(node, "Node should not be Null and pathToNode: " + pathToNode);
        
        String snmpPrimary = "P";
        if (node.getInterfaceCount() > 0) {
            snmpPrimary = "S";
        }

        RequisitionInterface iface = createInterface(ipAddr, snmpPrimary);
        node.insertInterface(iface);

        m_pendingForeignSourceRepository.save(group);
        return m_pendingForeignSourceRepository.getRequisition(groupName);
    }

    private RequisitionInterface createInterface(String ipAddr, String snmpPrimary) {
        RequisitionInterface iface = new RequisitionInterface();
        iface.setIpAddr(ipAddr);
        iface.setStatus(1);
        iface.setSnmpPrimary(snmpPrimary);
        return iface;
    }

    /** {@inheritDoc} */
    public Requisition addNewNodeToGroup(String groupName, String nodeLabel) {
        Requisition group = getProvisioningGroup(groupName);

        RequisitionNode node = createNode(nodeLabel, String.valueOf(System.currentTimeMillis()));
        node.setBuilding(groupName);
        group.insertNode(node);
        
        m_pendingForeignSourceRepository.save(group);
        return m_pendingForeignSourceRepository.getRequisition(groupName);
    }

    private RequisitionNode createNode(String nodeLabel, String foreignId) {
        RequisitionNode node = new RequisitionNode();
        node.setNodeLabel(nodeLabel);
        node.setForeignId(foreignId);
        return node;
    }

    /** {@inheritDoc} */
    public Requisition addServiceToInterface(String groupName, String pathToInterface, String serviceName) {
        Requisition group = getProvisioningGroup(groupName);
        
        RequisitionInterface iface = BeanUtils.getPathValue(group, pathToInterface, RequisitionInterface.class);
        
        RequisitionMonitoredService monSvc = createService(serviceName);
        iface.insertMonitoredService(monSvc);

        m_pendingForeignSourceRepository.save(group);
        return m_pendingForeignSourceRepository.getRequisition(groupName);
    }

    /** {@inheritDoc} */
    public Requisition getProvisioningGroup(String name) {
        Requisition deployed  = m_deployedForeignSourceRepository.getRequisition(name);
        Requisition pending = m_pendingForeignSourceRepository.getRequisition(name);
        
        return (pending == null)? deployed : pending;
    }
    
    /** {@inheritDoc} */
    public Requisition saveProvisioningGroup(String groupName, Requisition group) {
        group.setForeignSource(groupName);
        m_pendingForeignSourceRepository.save(group);
        return m_pendingForeignSourceRepository.getRequisition(groupName);
    }

    /**
     * <p>getProvisioningGroupNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getProvisioningGroupNames() {
        Set<String> names = new TreeSet<String>();
        for (Requisition r : m_deployedForeignSourceRepository.getRequisitions()) {
            names.add(r.getForeignSource());
        }
        for (Requisition r : m_pendingForeignSourceRepository.getRequisitions()) {
            names.add(r.getForeignSource());
        }
        return names;
    }
    
    /** {@inheritDoc} */
    public Requisition createProvisioningGroup(String name) {
        Requisition group = new Requisition();
        group.setForeignSource(name);

        m_pendingForeignSourceRepository.save(group);
        return m_pendingForeignSourceRepository.getRequisition(name);
    }

    private RequisitionMonitoredService createService(String serviceName) {
        RequisitionMonitoredService svc = new RequisitionMonitoredService();
        svc.setServiceName(serviceName);
        return svc;
    }


    /** {@inheritDoc} */
    public void importProvisioningGroup(String groupName) {

        // first we update the import timestamp
        Requisition group = getProvisioningGroup(groupName);
        group.updateDateStamp();
        saveProvisioningGroup(groupName, group);
        
        // then we send an event to the importer
        EventProxy proxy = Util.createEventProxy();

        String url = m_pendingForeignSourceRepository.getRequisitionURL(groupName).toString();
        Assert.notNull(url, "Could not find url for group "+groupName+".  Does it exists?");
        
        EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, "Web");
        bldr.addParam(EventConstants.PARM_URL, url);
        
        try {
            proxy.send(bldr.getEvent());
        } catch (EventProxyException e) {
            throw new DataAccessResourceFailureException("Unable to send event to import group "+groupName, e);
        }
    }

    /** {@inheritDoc} */
    public Requisition deletePath(String groupName, String pathToDelete) {
        Requisition group = getProvisioningGroup(groupName);

        PropertyPath path = new PropertyPath(pathToDelete);
        
        Object objToDelete = path.getValue(group);
        Object parentObject = path.getParent() == null ? group : path.getParent().getValue(group);
        
        String propName = path.getPropertyName();
        String methodSuffix = Character.toUpperCase(propName.charAt(0))+propName.substring(1);
        String methodName = "remove"+methodSuffix;

        try {
            MethodUtils.invokeMethod(parentObject, methodName, new Object[] { objToDelete });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Unable to find method "+methodName+" on object of type "+parentObject.getClass(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("unable to access property "+pathToDelete, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("an execption occurred deleting "+pathToDelete, e);
        }
        
        m_pendingForeignSourceRepository.save(group);
        return m_pendingForeignSourceRepository.getRequisition(groupName);
    }

    /**
     * <p>getAllGroups</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Requisition> getAllGroups() {
        Collection<Requisition> groups = new LinkedList<Requisition>();

        for(String groupName : getProvisioningGroupNames()) {
            groups.add(getProvisioningGroup(groupName));
        }
        
        return groups;
    }

    /** {@inheritDoc} */
    public void deleteProvisioningGroup(String groupName) {
        Requisition r = getProvisioningGroup(groupName);
        if (r != null) {
            m_pendingForeignSourceRepository.delete(r);
            m_deployedForeignSourceRepository.delete(r);
        }
    }

    /** {@inheritDoc} */
    public void deleteAllNodes(String groupName) {
        Requisition group = m_deployedForeignSourceRepository.getRequisition(groupName);
        if (group != null) {
            group.setNodes(new ArrayList<RequisitionNode>());
            m_deployedForeignSourceRepository.save(group);
        }

        group = m_pendingForeignSourceRepository.getRequisition(groupName);
        if (group != null) {
            group.setNodes(new ArrayList<RequisitionNode>());
            m_pendingForeignSourceRepository.save(group);
        }
    }

    /**
     * <p>getGroupDbNodeCounts</p>
     *
     * @return a java$util$Map object.
     */
    public Map<String, Integer> getGroupDbNodeCounts() {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        
        for(String groupName : getProvisioningGroupNames()) {
            counts.put(groupName, m_nodeDao.getNodeCountForForeignSource(groupName));
        }
        
        return counts;
        
    }

    /**
     * <p>getNodeCategoryNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getNodeCategoryNames() {
        Collection<String> names = new LinkedList<String>();
        for (OnmsCategory category : m_categoryDao.findAll()) {
            names.add(category.getName());
        }
        return names;
    }
    
    /**
     * <p>getServiceTypeNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getServiceTypeNames() {
        Collection<String> names = new LinkedList<String>();
        for(OnmsServiceType svcType : m_serviceTypeDao.findAll()) {
            names.add(svcType.getName());
        }
        return names;
    }

    /**
     * <p>getAssetFieldNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getAssetFieldNames() {
        return BeanUtils.getProperties(new OnmsAssetRecord());
    }

}
