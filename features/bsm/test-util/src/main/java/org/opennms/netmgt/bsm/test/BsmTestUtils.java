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
 *      http://www.gnu.org/licenses/
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
import org.opennms.netmgt.bsm.persistence.api.OnmsMonitoredServiceHelper;
import org.opennms.netmgt.bsm.persistence.api.functions.map.AbstractMapFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.AbstractReductionFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.MostCriticalEntity;
import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.mapreduce.MapFunction;
import org.opennms.netmgt.bsm.service.model.mapreduce.ReductionFunction;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceRequestDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.MapFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.ReduceFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ChildEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.EdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeResponseDTO;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

/**
 * Helper class to create all kinds of objects for writing BSM tests.
 */
public class BsmTestUtils {

    public static BusinessServiceRequestDTO toRequestDto(BusinessServiceEntity input) {
        BusinessServiceRequestDTO request = new BusinessServiceRequestDTO();
        request.setName(input.getName());
        request.setAttributes(new HashMap<>(input.getAttributes()));
        request.setReduceFunction(transform(input.getReductionFunction()));
        request.setChildServices(input.getChildEdges().stream().map(c -> {
            EdgeRequestDTO<Long> eachEdge = new EdgeRequestDTO();
            eachEdge.setValue(c.getChild().getId());
            eachEdge.setMapFunction(transform(c.getMapFunction()));
            return eachEdge;
        }).collect(Collectors.toSet()));
        request.setIpServices(input.getIpServiceEdges().stream().map(ip -> {
            EdgeRequestDTO<Integer> eachEdge = new EdgeRequestDTO();
            eachEdge.setValue(ip.getIpService().getId());
            eachEdge.setMapFunction(transform(ip.getMapFunction()));
            return eachEdge;
        }).collect(Collectors.toSet()));
        request.setReductionKeys(input.getReductionKeyEdges().stream().map(rk -> {
            EdgeRequestDTO<String> eachEdge = new EdgeRequestDTO();
            eachEdge.setValue(rk.getReductionKey());
            eachEdge.setMapFunction(transform(rk.getMapFunction()));
            return eachEdge;
        }).collect(Collectors.toSet()));
        return request;
    }

    private static ReduceFunctionDTO transform(AbstractReductionFunctionEntity input) {
        ReductionFunction reductionFunction = new ReduceFunctionMapper().toServiceFunction(input);
        ReduceFunctionDTO.Type type = ReduceFunctionDTO.Type.valueOf(reductionFunction.getClass());
        return type.toDTO(reductionFunction);
    }

    private static MapFunctionDTO transform(AbstractMapFunctionEntity input) {
        MapFunction mapFunction = new MapFunctionMapper().toServiceFunction(input);
        MapFunctionDTO.Type type = MapFunctionDTO.Type.valueOf(mapFunction.getClass());
        return type.toDTO(mapFunction);
    }

    public static BusinessServiceResponseDTO toResponseDto(BusinessServiceEntity input) {
        BusinessServiceResponseDTO response = new BusinessServiceResponseDTO();
        response.setId(input.getId());
        response.setName(input.getName());
        response.setReduceFunction(transform(input.getReductionFunction()));
        response.setOperationalStatus(null); // do not know that here
        response.setAttributes(input.getAttributes());
        response.setReductionKeys(input.getReductionKeyEdges().stream().map(it -> {
            ReductionKeyEdgeResponseDTO edge = new ReductionKeyEdgeResponseDTO();
            edge.setReductionKeys(it.getReductionKeys());
            edge.setReductionKey(it.getReductionKey());
            edge.setMapFunction(transform(it.getMapFunction()));
            edge.setId(it.getId());
            edge.setOperationalStatus(null); // we do not know that here
            return edge;
        }).collect(Collectors.toSet()));
        response.setIpServices(input.getIpServiceEdges().stream().map(it -> {
            IpServiceResponseDTO ipService = new IpServiceResponseDTO();
            ipService.setReductionKeys(OnmsMonitoredServiceHelper.getReductionKeys(it.getIpService()));
            ipService.setNodeLabel("dummy"); // do not know that here
            ipService.setServiceName(it.getIpService().getServiceName());
            ipService.setId(it.getIpService().getId());
            ipService.setIpAddress(InetAddressUtils.toIpAddrString(it.getIpService().getIpAddress()));

            IpServiceEdgeResponseDTO edge = new IpServiceEdgeResponseDTO();
            edge.setReductionKeys(OnmsMonitoredServiceHelper.getReductionKeys(it.getIpService()));
            edge.setIpService(ipService);
            edge.setMapFunction(transform(it.getMapFunction()));
            edge.setId(it.getId());
            edge.setOperationalStatus(null); // do not know that here
            return edge;
        }).collect(Collectors.toSet()));
        response.setChildren(input.getChildEdges().stream().map(it -> {
            ChildEdgeResponseDTO edge = new ChildEdgeResponseDTO();
            edge.setReductionKeys(edge.getReductionKeys());
            edge.setMapFunction(transform(it.getMapFunction()));
            edge.setId(it.getId());
            edge.setChildId(it.getChild().getId());
            edge.setOperationalStatus(null); // do not know that here
            return edge;
        }).collect(Collectors.toSet()));
        response.setParentServices(Sets.newHashSet()); // do not know that here // TODO MVR we have tos et that here somehow
        return response;
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

    private static OnmsAlarm createAlarm(OnmsMonitoredService monitoredService, OnmsSeverity severity) {
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

    public static AlarmWrapper createAlarmWrapper(OnmsMonitoredService monitoredService, OnmsSeverity severity) {
        final OnmsAlarm alarm = createAlarm(monitoredService, severity);
        return new AlarmWrapper() {
            @Override
            public String getReductionKey() {
                return alarm.getReductionKey();
            }

            @Override
            public Status getStatus() {
                return SeverityMapper.toStatus(alarm.getSeverity());
            }

            @Override
            public Integer getId() {
                return alarm.getId();
            }
        };
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

    public static BusinessServiceEntity createDummyBusinessService(String serviceName) {
        return new BusinessServiceEntityBuilder()
                .name(serviceName)
                .reduceFunction(new MostCriticalEntity())
                .toEntity();
    }

}
