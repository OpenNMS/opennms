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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceChildEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.EdgeEntityVisitor;
import org.opennms.netmgt.bsm.persistence.api.IPServiceEdgeEntity;
import org.opennms.netmgt.dao.util.ReductionKeyHelper;
import org.opennms.netmgt.bsm.persistence.api.SingleReductionKeyEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.AbstractMapFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.AbstractReductionFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.api.ResourceLocationFactory;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceRequestDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.MapFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.ReduceFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ChildEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionsManager;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

/**
 * Helper class to create all kinds of objects for writing BSM tests.
 */
public class BsmTestUtils {

    public static BusinessServiceRequestDTO toRequestDto(BusinessServiceEntity input) {
        Objects.requireNonNull(input);
        BusinessServiceRequestDTO request = new BusinessServiceRequestDTO();
        request.setName(input.getName());
        request.setAttributes(new HashMap<>(input.getAttributes()));
        request.setReduceFunction(transform(input.getReductionFunction()));
        input.getEdges().forEach(eachEdge -> eachEdge.accept(new EdgeEntityVisitor<Void>() {
            @Override
            public Void visit(BusinessServiceChildEdgeEntity edgeEntity) {
                request.addChildService(
                        edgeEntity.getChild().getId(),
                        transform(edgeEntity.getMapFunction()),
                        edgeEntity.getWeight());
                return null;
            }

            @Override
            public Void visit(SingleReductionKeyEdgeEntity edgeEntity) {
                request.addReductionKey(
                        edgeEntity.getReductionKey(),
                        transform(edgeEntity.getMapFunction()),
                        edgeEntity.getWeight(),
                        edgeEntity.getFriendlyName());
                return null;
            }

            @Override
            public Void visit(IPServiceEdgeEntity edgeEntity) {
                request.addIpService(
                        edgeEntity.getIpService().getId(),
                        transform(edgeEntity.getMapFunction()),
                        edgeEntity.getWeight(),
                        edgeEntity.getFriendlyName());
                return null;
            }
        }));
        return request;
    }

    public static ReduceFunctionDTO transform(AbstractReductionFunctionEntity input) {
        Objects.requireNonNull(input);
        ReductionFunction reductionFunction = new ReduceFunctionMapper().toServiceFunction(input);
        return new FunctionsManager().getReduceFunctionDTO(reductionFunction);
    }

    private static MapFunctionDTO transform(AbstractMapFunctionEntity input) {
        Objects.requireNonNull(input);
        MapFunction mapFunction = new MapFunctionMapper().toServiceFunction(input);
        return new FunctionsManager().getMapFunctionDTO(mapFunction);
    }

    public static BusinessServiceResponseDTO toResponseDto(BusinessServiceEntity input) {
        Objects.requireNonNull(input);
        BusinessServiceResponseDTO response = new BusinessServiceResponseDTO();
        response.setId(input.getId());
        response.setName(input.getName());
        response.setReduceFunction(transform(input.getReductionFunction()));
        response.setOperationalStatus(Status.INDETERMINATE); // we assume INDETERMINATE
        response.setAttributes(input.getAttributes());
        response.setLocation(ResourceLocationFactory.createBusinessServiceLocation(input.getId().toString()));
        response.setReductionKeys(input.getReductionKeyEdges().stream().map(BsmTestUtils::toResponseDTO).collect(Collectors.toList()));
        response.setIpServices(input.getIpServiceEdges().stream().map(BsmTestUtils::toResponseDTO).collect(Collectors.toList()));
        response.setChildren(input.getChildEdges().stream().map(BsmTestUtils::toResponseDTO).collect(Collectors.toList()));
        response.setParentServices(Sets.newHashSet()); // do not know that here
        return response;
    }

    public static ChildEdgeResponseDTO toResponseDTO(BusinessServiceChildEdgeEntity input) {
        ChildEdgeResponseDTO edge = new ChildEdgeResponseDTO();
        edge.setLocation(ResourceLocationFactory.createBusinessServiceEdgeLocation(input.getBusinessService().getId(), input.getId()));
        edge.setReductionKeys(edge.getReductionKeys());
        edge.setMapFunction(transform(input.getMapFunction()));
        edge.setId(input.getId());
        edge.setChildId(input.getChild().getId());
        edge.setWeight(input.getWeight());
        edge.setOperationalStatus(Status.INDETERMINATE); // we assume INDETERMINATE
        return edge;
    }

    public static IpServiceEdgeResponseDTO toResponseDTO(IPServiceEdgeEntity input) {
        IpServiceResponseDTO ipService = new IpServiceResponseDTO();
        ipService.setNodeLabel("dummy"); // do not know that here
        ipService.setServiceName(input.getIpService().getServiceName());
        ipService.setId(input.getIpService().getId());
        ipService.setIpAddress(InetAddressUtils.toIpAddrString(input.getIpService().getIpAddress()));

        IpServiceEdgeResponseDTO edge = new IpServiceEdgeResponseDTO();
        edge.setLocation(ResourceLocationFactory.createBusinessServiceEdgeLocation(input.getBusinessService().getId(), input.getId()));
        edge.setReductionKeys(ReductionKeyHelper.getReductionKeys(input.getIpService()));
        edge.setIpService(ipService);
        edge.setMapFunction(transform(input.getMapFunction()));
        edge.setId(input.getId());
        edge.setWeight(input.getWeight());
        edge.setFriendlyName(input.getFriendlyName());
        edge.setOperationalStatus(Status.INDETERMINATE); // we assume INDETERMINATE
        return edge;
    }

    public static ReductionKeyEdgeResponseDTO toResponseDTO(SingleReductionKeyEdgeEntity input) {
        ReductionKeyEdgeResponseDTO edge = new ReductionKeyEdgeResponseDTO();
        edge.setLocation(ResourceLocationFactory.createBusinessServiceEdgeLocation(input.getBusinessService().getId(), input.getId()));
        edge.setReductionKeys(input.getReductionKeys());
        edge.setReductionKey(input.getReductionKey());
        edge.setMapFunction(transform(input.getMapFunction()));
        edge.setId(input.getId());
        edge.setWeight(input.getWeight());
        edge.setFriendlyName(input.getFriendlyName());
        edge.setOperationalStatus(Status.INDETERMINATE); // we assume INDETERMINATE
        return edge;
    }

    // convert to json
    public static <T> String toJson(T input) {
        Objects.requireNonNull(input);

        try {
            return JacksonUtils.createDefaultObjectMapper().writeValueAsString(input);
        } catch (IOException io) {
            throw Throwables.propagate(io);
        }
    }

    // convert to xml
    public static <T> String toXml(T input) {
        Objects.requireNonNull(input);
        return JaxbUtils.marshal(input);
    }

    private static OnmsAlarm createAlarm(OnmsMonitoredService monitoredService, OnmsSeverity severity) {
        return createAlarm(
                Objects.requireNonNull(monitoredService.getNodeId()),
                Objects.requireNonNull(InetAddressUtils.toIpAddrString(monitoredService.getIpAddress())),
                Objects.requireNonNull(monitoredService.getServiceName()),
                Objects.requireNonNull(severity));
    }

    private static OnmsAlarm createAlarm(int nodeId, String ip, String service, OnmsSeverity severity) {
        OnmsAlarm alarm = createAlarm(
                EventConstants.NODE_LOST_SERVICE_EVENT_UEI,
                severity,
                String.format("%s::%s:%s:%s",
                        EventConstants.NODE_LOST_SERVICE_EVENT_UEI,
                        nodeId,
                        ip,
                        service));
        return alarm;
    }

    private static OnmsAlarm createAlarm(String uei, OnmsSeverity severity, String reductionKey) {
        OnmsAlarm customAlarm = new OnmsAlarm();
        customAlarm.setUei(Objects.requireNonNull(uei));
        customAlarm.setSeverity(Objects.requireNonNull(severity));
        customAlarm.setReductionKey(Objects.requireNonNull(reductionKey));
        return customAlarm;
    }

    public static AlarmWrapper createAlarmWrapper(String uei, OnmsSeverity severity, String reductionKey) {
       return createAlarmWrapper(createAlarm(uei, severity, reductionKey));
    }

    public static AlarmWrapper createAlarmWrapper(OnmsMonitoredService monitoredService, OnmsSeverity severity) {
        return createAlarmWrapper(createAlarm(monitoredService, severity));
    }

    private static AlarmWrapper createAlarmWrapper(final OnmsAlarm alarm) {
        return new AlarmWrapper() {
            @Override
            public String getReductionKey() {
                return alarm.getReductionKey();
            }

            @Override
            public Status getStatus() {
                return SeverityMapper.toStatus(alarm.getSeverity());
            }
        };
    }

    public static OnmsMonitoredService createMonitoredService(final int serviceId, final int nodeId, final String ipAddress, final String serviceName) {
        return new OnmsMonitoredService() {
            private static final long serialVersionUID = 8510675581667310365L;

            @Override
            public Integer getId() {
                return serviceId;
            }

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
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();
    }
}
