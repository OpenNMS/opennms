package org.opennms.netmgt.dao.support;

import java.util.Set;

import org.opennms.core.utils.LazySet;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;

public class LazyResourceAttributeLoader implements LazySet.Loader<OnmsAttribute> {

    private final ResourceStorageDao m_resourceStorageDao;
    
    private final ResourcePath m_path;

    public LazyResourceAttributeLoader(ResourceStorageDao resourceStorageDao, ResourcePath path) {
        m_resourceStorageDao = resourceStorageDao;
        m_path = path;
    }

    @Override
    public Set<OnmsAttribute> load() {
        return m_resourceStorageDao.getAttributes(m_path);
    }
}
