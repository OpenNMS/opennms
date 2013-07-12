package org.opennms.netmgt.dao.mock;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;

public class MockIpInterfaceDao extends AbstractMockDao<OnmsIpInterface, Integer> implements IpInterfaceDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public void save(final OnmsIpInterface iface) {
        super.save(iface);
        updateSubObjects(iface);
    }

    @Override
    public void update(final OnmsIpInterface iface) {
        super.update(iface);
        updateSubObjects(iface);
    }

    @Override
    public void flush() {
        super.flush();
        for (final OnmsIpInterface iface : findAll()) {
            updateSubObjects(iface);
        }
    }

    private void updateSubObjects(final OnmsIpInterface iface) {
        for (final OnmsMonitoredService svc : iface.getMonitoredServices()) {
            getMonitoredServiceDao().saveOrUpdate(svc);
        }
    }

    @Override
    public void delete(final OnmsIpInterface iface) {
        super.delete(iface);
        for (final OnmsMonitoredService svc : iface.getMonitoredServices()) {
            getMonitoredServiceDao().delete(svc);
        }
    }

    @Override
    protected void generateId(final OnmsIpInterface iface) {
        iface.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsIpInterface iface) {
        return iface.getId();
    }

    @SuppressWarnings("deprecation")
    @Override
    public OnmsIpInterface get(final OnmsNode node, final String ipAddress) {
        for (final OnmsIpInterface iface : findAll()) {
            if (node.equals(iface.getNode()) && ipAddress.equals(iface.getIpAddressAsString())) {
                return iface;
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public OnmsIpInterface findByNodeIdAndIpAddress(final Integer nodeId, final String ipAddress) {
        for (final OnmsIpInterface iface : findAll()) {
            if (iface.getNode().getId().equals(nodeId) && ipAddress.equals(iface.getIpAddressAsString())) {
                return iface;
            }
        }
        return null;
    }

    @Override
    public OnmsIpInterface findByForeignKeyAndIpAddress(final String foreignSource, final String foreignId, final String ipAddress) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsIpInterface> findByIpAddress(final String ipAddress) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsIpInterface> findByNodeId(final Integer nodeId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsIpInterface> findByServiceType(final String svcName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsIpInterface> findHierarchyByServiceType(final String svcName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<InetAddress, Integer> getInterfacesForNodes() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsIpInterface findPrimaryInterfaceByNodeId(final Integer nodeId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
