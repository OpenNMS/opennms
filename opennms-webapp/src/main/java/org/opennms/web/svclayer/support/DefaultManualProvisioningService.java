package org.opennms.web.svclayer.support;

import java.util.Collection;

import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.web.BeanUtils;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.opennms.web.svclayer.dao.ManualProvisioningDao;

public class DefaultManualProvisioningService implements
        ManualProvisioningService {

    private ManualProvisioningDao m_provisioningDao;

    public ModelImport addCategoryToNode(String groupName, String pathToNode, String categoryName) {
        
        ModelImport group = m_provisioningDao.get(groupName);
        
        Node node = BeanUtils.getPathValue(group, pathToNode, Node.class);
        
        Category category = new Category();
        category.setName(categoryName);
        node.addCategory(category);
        
        m_provisioningDao.save(groupName, group);
        
        return group;
    }

    public ModelImport addInterfaceToNode(String groupName, String pathToNode,
            String ipAddr) {
        ModelImport group = m_provisioningDao.get(groupName);
        
        Node node = BeanUtils.getPathValue(group, pathToNode, Node.class);

        Interface iface = new Interface();
        iface.setIpAddr(ipAddr);
        node.addInterface(iface);
        
        m_provisioningDao.save(groupName, group);
        
        return group;
    }

    public ModelImport addNewNodeToGroup(String groupName, String nodeLabel) {
        ModelImport group = m_provisioningDao.get(groupName);
        
        Node node = new Node();
        node.setNodeLabel(nodeLabel);
        
        group.addNode(node);
        
        m_provisioningDao.save(groupName, group);
        return group;
    }

    public ModelImport addServiceToInterface(String groupName, String pathToInterface, String serviceName) {
        ModelImport group = m_provisioningDao.get(groupName);
        
        Interface iface = BeanUtils.getPathValue(group, pathToInterface, Interface.class);
        
        MonitoredService monSvc = new MonitoredService();
        monSvc.setServiceName(serviceName);
        iface.addMonitoredService(monSvc);

        
        m_provisioningDao.save(groupName, group);
        
        return group;
    }

    public ModelImport createProvisiongGroup(String groupName) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public ModelImport getProvisioningGroup(String name) {
        return m_provisioningDao.get(name);
    }

    public Collection<String> getProvisioningGroupNames() {
        return m_provisioningDao.getProvisioningGroupNames();
    }

    public void importProvisioningGroup(String groupName) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public ModelImport setCategoryName(String groupName, String pathToCategory,
            String categoryName) {
        // TODO Auto-generated method stub
        return null;
    }

    public ModelImport setIpAddress(String groupName, String pathToInterface,
            String ipAddr) {
        // TODO Auto-generated method stub
        return null;
    }

    public ModelImport setNodeLabel(String groupName, String pathToNode,
            String newLabel) {
        // TODO Auto-generated method stub
        return null;
    }

    public ModelImport setServiceName(String groupName, String pathToService,
            String serviceName) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setProvisioningDao(ManualProvisioningDao provisioningDao) {
        m_provisioningDao = provisioningDao;
    }


}
