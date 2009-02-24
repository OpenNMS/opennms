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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.modelimport.Asset;
import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.web.BeanUtils;
import org.opennms.web.Util;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.opennms.web.svclayer.dao.ManualProvisioningDao;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultManualProvisioningService implements
        ManualProvisioningService {

    private ManualProvisioningDao m_provisioningDao;
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private ServiceTypeDao m_serviceTypeDao;
    
    public void setProvisioningDao(ManualProvisioningDao provisioningDao) {
        m_provisioningDao = provisioningDao;
    }
    
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }
    
    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }

    public ModelImport addCategoryToNode(String groupName, String pathToNode, String categoryName) {
        
        ModelImport group = m_provisioningDao.get(groupName);
        
        Node node = BeanUtils.getPathValue(group, pathToNode, Node.class);
        
        Category category = new Category();
        category.setName(categoryName);
        node.addCategory(0, category);
        
        m_provisioningDao.save(groupName, group);
        
        return m_provisioningDao.get(groupName);
    }
    
    public ModelImport addAssetFieldToNode(String groupName, String pathToNode, String assetName, String assetValue) {
        ModelImport group = m_provisioningDao.get(groupName);
        Node node = BeanUtils.getPathValue(group, pathToNode, Node.class);

        Asset asset = new Asset();
        asset.setName(assetName);
        asset.setValue(assetValue);
        node.addAsset(asset);

        m_provisioningDao.save(groupName, group);

        return m_provisioningDao.get(groupName);
    }

    public ModelImport addInterfaceToNode(String groupName, String pathToNode,
            String ipAddr) {
        ModelImport group = m_provisioningDao.get(groupName);
        Assert.notNull(group, "Group should not be Null and is null groupName: " + groupName);
        Node node = BeanUtils.getPathValue(group, pathToNode, Node.class);
        Assert.notNull(node, "Node should not be Null and pathToNode: " + pathToNode);
        
        String snmpPrimary = "P";
        if (node.getInterfaceCount() > 0) {
            snmpPrimary = "S";
        }

        Interface iface = createInterface(ipAddr, snmpPrimary);
        node.addInterface(0, iface);
        
        m_provisioningDao.save(groupName, group);
        
        return m_provisioningDao.get(groupName);
    }

    private Interface createInterface(String ipAddr, String snmpPrimary) {
        Interface iface = new Interface();
        iface.setIpAddr(ipAddr);
        iface.setStatus(1);
        iface.setSnmpPrimary(snmpPrimary);
        return iface;
    }

    public ModelImport addNewNodeToGroup(String groupName, String nodeLabel) {
        ModelImport group = m_provisioningDao.get(groupName);
        
        Node node = createNode(nodeLabel, String.valueOf(System.currentTimeMillis()));
        node.setBuilding(groupName);
        
        group.addNode(0, node);
        
        m_provisioningDao.save(groupName, group);
        return m_provisioningDao.get(groupName);
    }

    private Node createNode(String nodeLabel, String foreignId) {
        Node node = new Node();
        node.setNodeLabel(nodeLabel);
        node.setForeignId(foreignId);
        return node;
    }

    public ModelImport addServiceToInterface(String groupName, String pathToInterface, String serviceName) {
        ModelImport group = m_provisioningDao.get(groupName);
        
        Interface iface = BeanUtils.getPathValue(group, pathToInterface, Interface.class);
        
        MonitoredService monSvc = createService(serviceName);
        iface.addMonitoredService(0, monSvc);

        
        m_provisioningDao.save(groupName, group);
        
        return m_provisioningDao.get(groupName);
    }

    public ModelImport getProvisioningGroup(String name) {
        return m_provisioningDao.get(name);
    }
    
    public ModelImport saveProvisioningGroup(String groupName, ModelImport group) {
        m_provisioningDao.save(groupName, group);
        return m_provisioningDao.get(groupName);
    }

    public Collection<String> getProvisioningGroupNames() {
        return m_provisioningDao.getProvisioningGroupNames();
    }
    
    public ModelImport createProvisioningGroup(String name) {
        ModelImport group = new ModelImport();
        group.setForeignSource(name);
        
        m_provisioningDao.save(name, group);
        return m_provisioningDao.get(name);
    }

    private MonitoredService createService(String serviceName) {
        MonitoredService svc = new MonitoredService();
        svc.setServiceName(serviceName);
        return svc;
    }


    public void importProvisioningGroup(String groupName) {

        // first we update the import timestamp
        ModelImport group = getProvisioningGroup(groupName);
        group.setLastImport(new Date());
        saveProvisioningGroup(groupName, group);
        
        
        // then we send an event to the importer
        EventProxy proxy = Util.createEventProxy();
        
        String url = m_provisioningDao.getUrlForGroup(groupName);
        Assert.notNull(url, "Could not find url for group "+groupName+".  Does it exists?");
        
        EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, "Web");
        bldr.addParam(EventConstants.PARM_URL, url);
        
        try {
            proxy.send(bldr.getEvent());
        } catch (EventProxyException e) {
            throw new DataAccessResourceFailureException("Unable to send event to import group "+groupName, e);
        }
    }

    public ModelImport deletePath(String groupName, String pathToDelete) {
        ModelImport group = m_provisioningDao.get(groupName);

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
        
        m_provisioningDao.save(groupName, group);
    
        return m_provisioningDao.get(groupName);
    }

    public Collection<ModelImport> getAllGroups() {
        Collection<ModelImport> groups = new LinkedList<ModelImport>();
        
        for(String groupName : getProvisioningGroupNames()) {
            groups.add(getProvisioningGroup(groupName));
        }
        
        return groups;
    }

    public void deleteProvisioningGroup(String groupName) {
        m_provisioningDao.delete(groupName);
    }

    public void deleteAllNodes(String groupName) {
        ModelImport group = m_provisioningDao.get(groupName);
        group.removeAllNode();
        m_provisioningDao.save(groupName, group);
    }

    public Map<String, Integer> getGroupDbNodeCounts() {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        
        for(String groupName : getProvisioningGroupNames()) {
            counts.put(groupName, m_nodeDao.getNodeCountForForeignSource(groupName));
        }
        
        return counts;
        
    }

    public Collection<String> getNodeCategoryNames() {
        Collection<String> names = new LinkedList<String>();
        for (OnmsCategory category : m_categoryDao.findAll()) {
            names.add(category.getName());
        }
        return names;
    }
    
    public Collection<String> getServiceTypeNames() {
        Collection<String> names = new LinkedList<String>();
        for(OnmsServiceType svcType : m_serviceTypeDao.findAll()) {
            names.add(svcType.getName());
        }
        return names;
    }

    public Collection<String> getAssetFieldNames() {
        return BeanUtils.getProperties(new OnmsAssetRecord());
    }



}
