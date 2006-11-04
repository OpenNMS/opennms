package org.opennms.web.svclayer;

import java.util.Collection;

import org.opennms.netmgt.config.modelimport.ModelImport;

public interface ManualProvisioningService {
    
    Collection<String> getProvisioningGroupNames();
    
    ModelImport getProvisioningGroup(String name);
    
    ModelImport createProvisiongGroup(String groupName);
    
    ModelImport addNewNodeToGroup(String groupName, String nodeLabel);
    
    ModelImport setNodeLabel(String groupName, String pathToNode, String newLabel);
    
    ModelImport addCategoryToNode(String groupName, String pathToNode, String categoryName);
    
    ModelImport setCategoryName(String groupName, String pathToCategory, String categoryName);
    
    ModelImport addInterfaceToNode(String groupName, String pathToNode, String ipAddr);
    
    ModelImport setIpAddress(String groupName, String pathToInterface, String ipAddr);
    
    ModelImport addServiceToInterface(String groupName, String pathToInterface, String serviceName);
    
    ModelImport setServiceName(String groupName, String pathToService, String serviceName);
    
    void importProvisioningGroup(String groupName);

}
