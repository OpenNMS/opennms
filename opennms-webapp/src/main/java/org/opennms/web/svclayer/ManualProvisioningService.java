package org.opennms.web.svclayer;

import java.util.Collection;

import org.opennms.netmgt.config.modelimport.ModelImport;

public interface ManualProvisioningService {
    
    Collection<String> getProvisioningGroupNames();
    
    ModelImport getProvisioningGroup(String name);
    
    ModelImport createProvisioningGroup(String name);
    
    ModelImport saveProvisioningGroup(String groupName, ModelImport groupData);
    
    ModelImport addNewNodeToGroup(String groupName, String nodeLabel);
    
    ModelImport addCategoryToNode(String groupName, String pathToNode, String categoryName);
    
    ModelImport addInterfaceToNode(String groupName, String pathToNode, String ipAddr);
    
    ModelImport addServiceToInterface(String groupName, String pathToInterface, String serviceName);
    
    ModelImport deletePath(String groupName, String pathToDelete);
    
    void importProvisioningGroup(String groupName);

}
