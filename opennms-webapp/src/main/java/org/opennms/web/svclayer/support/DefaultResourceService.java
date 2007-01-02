package org.opennms.web.svclayer.support;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.GraphDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;

public class DefaultResourceService implements ResourceService, InitializingBean {
    private ResourceDao m_resourceDao;
    private GraphDao m_graphDao;

    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }
    
    public GraphDao getGraphDao() {
        return m_graphDao;
    }

    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }
    
    public void afterPropertiesSet() throws Exception {
        if (m_resourceDao == null) {
            throw new IllegalStateException("resourceDao property is not set");
        }
    }

    public List<OnmsResource> findDomainResources() {
        return m_resourceDao.findDomainResources();
    }

    public List<OnmsResource> findNodeResources() {
        return m_resourceDao.findNodeResources();
    }

    public List<OnmsResource> findNodeChildResources(int nodeId) {
        OnmsResource resource = m_resourceDao.getResourceById(OnmsResource.createResourceId("node", Integer.toString(nodeId)));
        List<OnmsResource> resources = resource.getChildResources();
        resources.size(); // Get the size to force the list to be loaded
        return resources;
    }

    public List<OnmsResource> findDomainChildResources(String domain) {
        OnmsResource resource = m_resourceDao.getResourceById(OnmsResource.createResourceId("domain", domain));
        List<OnmsResource> resources = resource.getChildResources();
        resources.size(); // Get the size to force the list to be loaded
        return resources;
    }
    
    public List<OnmsResource> findChildResources(OnmsResource resource, String... resourceTypeMatches) {
        List<OnmsResource> matchingChildResources = new LinkedList<OnmsResource>();
        
        for (OnmsResource childResource : resource.getChildResources()) {
            boolean addGraph = false;
            if (resourceTypeMatches.length > 0) {
                for (String resourceTypeMatch : resourceTypeMatches) {
                    if (resourceTypeMatch.equals(childResource.getResourceType().getName())) {
                        addGraph = true;
                        break;
                    }
                }
            } else {
                addGraph = true;
            }
        
            if (addGraph) {
                matchingChildResources.add(childResource);
            }
        }

        return matchingChildResources;
    }


    public OnmsResource getResourceById(String id) {
        return m_resourceDao.getResourceById(id);
    }
    
    public PrefabGraph[] findPrefabGraphsForResource(OnmsResource resource) {
        return m_graphDao.getPrefabGraphsForResource(resource);
    }
    
    public PrefabGraph[] findPrefabGraphsForChildResources(OnmsResource resource, String... resourceTypeMatches) {
        Map<String, PrefabGraph> childGraphs = new LinkedHashMap<String, PrefabGraph>();
        for (OnmsResource r : findChildResources(resource, resourceTypeMatches)) {
            for (PrefabGraph g : findPrefabGraphsForResource(r)) {
                childGraphs.put(g.getName(), g);
            }
        }
        return childGraphs.values().toArray(new PrefabGraph[childGraphs.size()]);
    }

    public PrefabGraph getPrefabGraph(String name) {
        return m_graphDao.getPrefabGraph(name);
    }

}
