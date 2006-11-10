package org.opennms.web.svclayer.dao;

import java.util.Collection;

import org.opennms.netmgt.config.modelimport.ModelImport;

public interface ManualProvisioningDao {

    Collection<String> getProvisioningGroupNames();

    ModelImport get(String name);

    void save(String groupName, ModelImport group);

    String getUrlForGroup(String groupName);

    void delete(String groupName);

}
