/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.mock;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    @Override
    public List<OnmsMonitoredService> findByNode(final int nodeId) {
        return findAll().stream()
                 .filter(svc -> svc.getNodeId() == nodeId)
                 .collect(Collectors.toList());
    }
}
