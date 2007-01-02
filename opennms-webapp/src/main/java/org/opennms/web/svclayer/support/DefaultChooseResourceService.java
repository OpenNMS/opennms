package org.opennms.web.svclayer.support;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.web.svclayer.ChooseResourceService;
import org.springframework.beans.factory.InitializingBean;

public class DefaultChooseResourceService implements ChooseResourceService, InitializingBean {

    public ResourceDao m_resourceDao;

    public ChooseResourceModel findChildResources(String resourceId, String endUrl) {
        if (resourceId == null) {
            throw new IllegalArgumentException("resourceId parameter may not be null");
        }
        
        if (endUrl == null) {
            throw new IllegalArgumentException("endUrl parameter may not be null");
        }

        ChooseResourceModel model = new ChooseResourceModel();
        model.setEndUrl(endUrl);
        
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);

        model.setResource(resource);
        Map<OnmsResourceType, List<OnmsResource>> resourceTypeMap = new LinkedHashMap<OnmsResourceType, List<OnmsResource>>();
        
        for (OnmsResource childResource : resource.getChildResources()) {
            if (!resourceTypeMap.containsKey(childResource.getResourceType())) {
                resourceTypeMap.put(childResource.getResourceType(), new LinkedList<OnmsResource>());
            }
            resourceTypeMap.get(childResource.getResourceType()).add(childResource);
        }
        
        model.setResourceTypes(resourceTypeMap);

        return model;
    }

    public void afterPropertiesSet() {
        if (m_resourceDao == null) {
            throw new IllegalStateException("resourceDao property not set");
        }
    }

    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

}
