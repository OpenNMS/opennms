package org.opennms.netmgt.dao.support;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.core.utils.LazyList;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

import com.google.common.base.Preconditions;

public class LazyChildResourceLoader implements LazyList.Loader<OnmsResource> {
    private final ResourceDao m_resourceDao;
    private OnmsResource m_parent;

    public LazyChildResourceLoader(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    public void setParent(OnmsResource parent) {
        m_parent = parent;
    }

    @Override
    public List<OnmsResource> load() {
        Preconditions.checkNotNull(m_parent, "parent attribute");
        // Gather the lists of children from all the available resource types and merge them
        // into a single list
        List<OnmsResource> children = getAvailableResourceTypes().parallelStream()
                .map(t -> t.getResourcesForParent(m_parent))
                .flatMap(rs -> rs.stream())
                .collect(Collectors.toList());

        // Set the parent field on all of the resources
        children.stream().forEach(c -> c.setParent(m_parent));
        return children;
    }

    private Collection<OnmsResourceType> getAvailableResourceTypes() {
        return m_resourceDao.getResourceTypes().parallelStream()
                .filter(t -> t.isResourceTypeOnParent(m_parent))
                .collect(Collectors.toList());
    }
}
