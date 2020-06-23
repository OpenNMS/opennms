/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.mock;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ServiceSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockMonitoredServiceDao extends AbstractMockDao<OnmsMonitoredService, Integer> implements MonitoredServiceDao {
    private static final Logger LOG = LoggerFactory.getLogger(MockMonitoredServiceDao.class);
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public Integer save(final OnmsMonitoredService svc) {
        updateParent(svc);
        Integer retval = super.save(svc);
        updateSubObjects(svc);
        return retval;
    }

    @Override
    public void update(final OnmsMonitoredService svc) {
        updateParent(svc);
        super.update(svc);
        updateSubObjects(svc);
    }
    
    private void updateParent(final OnmsMonitoredService svc) {
        if (svc.getIpInterface() != null && svc.getIpInterface().getId() != null) {
            final OnmsIpInterface iface = getIpInterfaceDao().get(svc.getIpInterface().getId());
            if (iface != null && iface != svc.getIpInterface()) {
                LOG.debug("merging interface {} into interface {}", svc.getIpInterface(), iface);
                iface.mergeInterface(svc.getIpInterface(), new NullEventForwarder(), false);
                svc.setIpInterface(iface);
            }
            if (!svc.getIpInterface().getMonitoredServices().contains(svc)) {
                svc.getIpInterface().addMonitoredService(svc);
            }
        }
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
    public OnmsMonitoredService get(final Integer nodeId, final InetAddress ipAddress, final Integer serviceId) {
        for (final OnmsMonitoredService svc : findAll()) {
            if (svc.getNodeId() == nodeId && svc.getIpAddress().equals(ipAddress) && serviceId == svc.getId()) {
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
        final List<OnmsMonitoredService> services = new ArrayList<>();
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
    public List<OnmsMonitoredService> findAllServices() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Set<OnmsMonitoredService> findByApplication(final OnmsApplication application) {
        final Set<OnmsMonitoredService> services = new HashSet<>();
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
