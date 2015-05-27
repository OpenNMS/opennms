package org.opennms.netmgt.dao.support;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.core.utils.LazyList;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

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
        final List<OnmsResource> children = Lists.newLinkedList();
        for (OnmsResourceType resourceType : getAvailableResourceTypes()) {
            for (OnmsResource resource : resourceType.getResourcesForParent(m_parent)) {
                resource.setParent(m_parent);
                children.add(resource);
            }
        }
        return children;
    }

    private Collection<OnmsResourceType> getAvailableResourceTypes() {
        return m_resourceDao.getResourceTypes().stream()
                .filter(t -> t.isResourceTypeOnParent(m_parent))
                .collect(Collectors.toList());
    }

}
