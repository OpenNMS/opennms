package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ResourceService {
    public List<OnmsResource> findNodeResources();
    public List<OnmsResource> findDomainResources();
    public List<OnmsResource> findNodeChildResources(int nodeId);
    public List<OnmsResource> findDomainChildResources(String domain);
    public List<OnmsResource> findChildResources(OnmsResource resource, String... resourceTypeMatches);
    public OnmsResource getResourceById(String id);
    public PrefabGraph[] findPrefabGraphsForResource(OnmsResource resource);
    public PrefabGraph getPrefabGraph(String name);
    public PrefabGraph[] findPrefabGraphsForChildResources(OnmsResource resource, String... resourceTypeMatches);
}
