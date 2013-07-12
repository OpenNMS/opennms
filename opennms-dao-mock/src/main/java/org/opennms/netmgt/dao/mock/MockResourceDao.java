package org.opennms.netmgt.dao.mock;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

public class MockResourceDao implements ResourceDao {

    @Override
    public File getRrdDirectory() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public File getRrdDirectory(boolean verify) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsResourceType> getResourceTypes() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsResource getResourceById(String id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsResource> getResourceListById(String id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsResource> findNodeResources() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsResource> findDomainResources() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsResource> findNodeSourceResources() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsResource> findTopLevelResources() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsResource getResourceForNode(OnmsNode node) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface, OnmsLocationMonitor locationMonitor) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
