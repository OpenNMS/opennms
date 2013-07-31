package org.opennms.netmgt.dao.mock;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ServiceSelector;

public class MockMonitoredServiceDao extends AbstractMockDao<OnmsMonitoredService, Integer> implements MonitoredServiceDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public void save(final OnmsMonitoredService svc) {
        super.save(svc);
        updateSubObjects(svc);
    }

    @Override
    public void update(final OnmsMonitoredService svc) {
        super.update(svc);
        updateSubObjects(svc);
    }

    private void updateSubObjects(final OnmsMonitoredService svc) {
        final OnmsServiceType serviceType = svc.getServiceType();
        final OnmsServiceType existingServiceType = getServiceTypeDao().findByName(serviceType.getName());
        if (existingServiceType != null && existingServiceType.getId() != serviceType.getId()) {
            svc.setServiceType(existingServiceType);
        }
        getServiceTypeDao().saveOrUpdate(svc.getServiceType());
    }

    @Override
    public void flush() {
        super.flush();
        for (final OnmsMonitoredService svc : findAll()) {
            updateSubObjects(svc);
        }
    }

    @Override
    protected void generateId(final OnmsMonitoredService svc) {
        svc.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsMonitoredService service) {
        return service.getId();
    }

    @Override
    public OnmsMonitoredService get(final Integer nodeId, final String ipAddr, final Integer ifIndex, final Integer serviceId) {
        for (final OnmsMonitoredService svc : findAll()) {
            if (svc.getNodeId() == nodeId && svc.getIpAddress().equals(addr(ipAddr)) && ifIndex == svc.getIfIndex() && serviceId == svc.getId()) {
                return svc;
            }
        }
        return null;
    }

    @Override
    public OnmsMonitoredService get(final Integer nodeId, final String ipAddr, final Integer serviceId) {
        for (final OnmsMonitoredService svc : findAll()) {
            if (svc.getNodeId() == nodeId && svc.getIpAddress().equals(ipAddr) && serviceId == svc.getId()) {
                return svc;
            }
        }
        return null;
    }

    @Override
    public OnmsMonitoredService get(final Integer nodeId, final InetAddress ipAddr, final Integer ifIndex, final Integer serviceId) {
        for (final OnmsMonitoredService svc : findAll()) {
            if (svc.getNodeId() == nodeId && svc.getIpAddress().equals(ipAddr) && ifIndex == svc.getIfIndex() && serviceId == svc.getId()) {
                return svc;
            }
        }
        return null;
    }

    @Override
    public OnmsMonitoredService get(final Integer nodeId, final InetAddress ipAddress, final String svcName) {
        for (final OnmsMonitoredService svc : findAll()) {
            if (nodeId.equals(svc.getNodeId()) && ipAddress.equals(svc.getIpAddress()) && svcName.equals(svc.getServiceName())) {
                return svc;
            }
        }
        return null;
    }

    @Override
    public List<OnmsMonitoredService> findByType(final String typeName) {
        final List<OnmsMonitoredService> services = new ArrayList<OnmsMonitoredService>();
        for (final OnmsMonitoredService svc : findAll()) {
            if (typeName.equals(svc.getServiceType().getName())) {
                services.add(svc);
            }
        }
        return services;
    }

    @Override
    public List<OnmsMonitoredService> findMatchingServices(final ServiceSelector serviceSelector) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Set<OnmsMonitoredService> findByApplication(final OnmsApplication application) {
        final Set<OnmsMonitoredService> services = new HashSet<OnmsMonitoredService>();
        for (final OnmsMonitoredService svc : findAll()) {
            if (svc.getApplications().contains(application)) {
                services.add(svc);
            }
        }
        return services;
    }

    @Override
    public OnmsMonitoredService getPrimaryService(final Integer nodeId, final String svcName) {
        for (final OnmsMonitoredService svc : findAll()) {
            if (svc.getNodeId() == nodeId && svcName.equals(svc.getServiceName()) && svc.getIpInterface().isPrimary()) {
                return svc;
            }
        }
        return null;
    }

}
