/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceRequestDTO;

import com.google.common.base.Throwables;

/**
 * Helper class to create all kinds of objects for writing BSM tests.
 */
public class BsmTestUtils {

    public static BusinessServiceRequestDTO toRequestDto(BusinessServiceEntity input) {
        BusinessServiceRequestDTO request = new BusinessServiceRequestDTO();
        request.setName(input.getName());
        request.setAttributes(new HashMap<>(input.getAttributes()));
        request.setChildServices(input.getChildServices().stream().map(s -> s.getId()).collect(Collectors.toSet()));
        request.setIpServices(input.getIpServices().stream().map(s -> s.getId()).collect(Collectors.toSet()));
        return request;
    }

    // convert to json
    public static String toJson(BusinessServiceRequestDTO request) {
        try {
            return new ObjectMapper().writeValueAsString(request);
        } catch (IOException io) {
            throw Throwables.propagate(io);
        }
    }

    // convert to xml
    public static String toXml(BusinessServiceRequestDTO request) {
        return JaxbUtils.marshal(request);
    }

    public static OnmsAlarm createAlarm(OnmsMonitoredService monitoredService, OnmsSeverity severity) {
        return createAlarm(
                Objects.requireNonNull(monitoredService.getNodeId()),
                Objects.requireNonNull(InetAddressUtils.toIpAddrString(monitoredService.getIpAddress())),
                Objects.requireNonNull(monitoredService.getServiceName()),
                Objects.requireNonNull(severity));
    }

    private static OnmsAlarm createAlarm(int nodeId, String ip, String service, OnmsSeverity severity) {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setUei(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
        alarm.setSeverity(severity);
        alarm.setReductionKey(String.format("%s::%s:%s:%s", EventConstants.NODE_LOST_SERVICE_EVENT_UEI, nodeId, ip, service));
        return alarm;
    }

    public static OnmsMonitoredService createMonitoredService(final int nodeId, final String ipAddress, final String serviceName) {
        return new OnmsMonitoredService() {
            private static final long serialVersionUID = 8510675581667310365L;

            public Integer getNodeId() {
                return nodeId;
            }

            public InetAddress getIpAddress() {
                try {
                    return InetAddress.getByName(ipAddress);
                } catch (UnknownHostException e) {
                    throw Throwables.propagate(e);
                }
            }

            public String getServiceName() {
                return serviceName;
            }

            public String toString() {
                return getServiceName();
            }
        };
    }

    // creates a simple hierarchy with 1 parent and 2 childs.
    // Each children has one ip service atached. The parent has not.
    public static BsmTestData createSimpleHierarchy() {
        // Create a simple hierarchy
        BusinessServiceEntity child1 = new BusinessServiceEntityBuilder()
                .name("Child 1")
                .addIpService(createMonitoredService(1, "192.168.1.1", "ICMP"))
                .toEntity();

        BusinessServiceEntity child2 = new BusinessServiceEntityBuilder()
                .name("Child 2")
                .addIpService(createMonitoredService(2, "192.168.1.2", "SNMP"))
                .toEntity();

        BusinessServiceEntity root = new BusinessServiceEntityBuilder()
                .name("Parent")
                .addChildren(child1)
                .addChildren(child2)
                .toEntity();

        child1.getParentServices().add(root);
        child2.getParentServices().add(root);

        return new BsmTestData(child1, child2, root);
    }

    public static BusinessServiceEntity createDummyBusinessService(String serviceName) {
        return new BusinessServiceEntityBuilder()
                .name(serviceName)
                .toEntity();
    }

}
