/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.web.rest.api.ResourceLocationFactory;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceListDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceRequestDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.MapFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.MapFunctionListDTO;
import org.opennms.web.rest.v2.bsm.model.MapFunctionType;
import org.opennms.web.rest.v2.bsm.model.ReduceFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.ReduceFunctionListDTO;
import org.opennms.web.rest.v2.bsm.model.ReduceFunctionType;
import org.opennms.web.rest.v2.bsm.model.edge.AbstractEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ChildEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ChildEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

@Component
@Path("business-services")
@Transactional
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
public class BusinessServiceRestService {

    @Autowired
    private BusinessServiceManager businessServiceManager;

    protected BusinessServiceManager getManager() {
        return businessServiceManager;
    }

    @GET
    public Response list() {
        List<BusinessService> services = getManager().getAllBusinessServices();
        if (services == null || services.isEmpty()) {
            return Response.noContent().build();
        }
        BusinessServiceListDTO serviceList = new BusinessServiceListDTO(services);
        return Response.ok(serviceList).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") Long id) {
        BusinessService service = getManager().getBusinessServiceById(id);

        final BusinessServiceResponseDTO response = new BusinessServiceResponseDTO();
        response.setId(service.getId());
        response.setName(service.getName());
        response.setAttributes(service.getAttributes());
        response.setLocation(ResourceLocationFactory.createBusinessServiceLocation(service.getId().toString()));
        response.setParentServices(service.getParentServices().stream().map(BusinessService::getId).collect(Collectors.toSet()));
        response.setOperationalStatus(service.getOperationalStatus());
        response.setReduceFunction(transform(service.getReduceFunction()));
        response.setIpServices(service.getIpServiceEdges()
                .stream()
                .map(edge -> transform(edge))
                .collect(Collectors.toList()));
        response.setChildren(service.getChildEdges()
                .stream()
                .map(edge -> transform(edge))
                .collect(Collectors.toList()));
        response.setReductionKeys(service.getReductionKeyEdges()
                .stream()
                .map(edge -> transform(edge))
                .collect(Collectors.toList()));
        return Response.ok(response).build();
    }

    @POST
    public Response create(@Context final UriInfo uriInfo, final BusinessServiceRequestDTO request) {
        final BusinessService service = getManager().createBusinessService();
        service.setName(request.getName());
        service.setAttributes(request.getAttributes());
        service.setReduceFunction(transform(request.getReduceFunction()));
        request.getReductionKeys()
                .stream()
                .forEach(rkEdge -> service.addReductionKeyEdge(
                        rkEdge.getReductionKey(),
                        transform(rkEdge.getMapFunction()),
                        rkEdge.getWeight()));
        request.getIpServices()
                .stream()
                .forEach(ipEdge -> service.addIpServiceEdge(
                        getManager().getIpServiceById(ipEdge.getIpServiceId()),
                        transform(ipEdge.getMapFunction()),
                        ipEdge.getWeight()));
        request.getChildServices()
                .stream()
                .forEach(childEdge -> service.addChildEdge(
                        getManager().getBusinessServiceById(childEdge.getChildId()),
                        transform(childEdge.getMapFunction()),
                        childEdge.getWeight()));
        getManager().saveBusinessService(service);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, service.getId())).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        final BusinessService service = getManager().getBusinessServiceById(id);
        getManager().deleteBusinessService(service);

        return Response.ok().build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") final Long id, final BusinessServiceRequestDTO request) {
        final BusinessService service = getManager().getBusinessServiceById(id);
        service.setName(request.getName());
        service.setAttributes(request.getAttributes());
        service.setReduceFunction(transform(request.getReduceFunction()));
        service.setReductionKeyEdges(Sets.newHashSet());
        request.getReductionKeys()
            .forEach(rkEdge ->
                    getManager().addReductionKeyEdge(
                            service,
                            rkEdge.getReductionKey(),
                            transform(rkEdge.getMapFunction()),
                            rkEdge.getWeight()));
        service.setIpServiceEdges(Sets.newHashSet());
        request.getIpServices()
                .forEach(ipEdge ->
                    getManager().addIpServiceEdge(
                            service,
                            getManager().getIpServiceById(ipEdge.getIpServiceId()),
                            transform(ipEdge.getMapFunction()),
                            ipEdge.getWeight()));
        service.setChildEdges(Sets.newHashSet());
        request.getChildServices()
                .forEach(childEdge ->
                    getManager().addChildEdge(
                            service,
                            getManager().getBusinessServiceById(childEdge.getChildId()),
                            transform(childEdge.getMapFunction()),
                            childEdge.getWeight()));
        getManager().saveBusinessService(service);

        return Response.noContent().build();
    }

    @GET
    @Path("/edges/{edgeId}")
    public Response getEdgeById(@PathParam("edgeId") final Long edgeId) {
        Edge edge = getManager().getEdgeById(edgeId);
        AbstractEdgeResponseDTO edgeDTO = transform(edge);
        return Response.ok().entity(edgeDTO).build();
    }

    @POST
    @Path("{id}/ip-service-edge")
    // Add IpService
    public Response addIpServiceEdge(@PathParam("id") final Long serviceId,
                            final IpServiceEdgeRequestDTO edgeRequest) {
        final BusinessService businessService = getManager().getBusinessServiceById(serviceId);
        final IpService ipService = getManager().getIpServiceById(edgeRequest.getIpServiceId());
        boolean changed = getManager().addIpServiceEdge(businessService, ipService, transform(edgeRequest.getMapFunction()), edgeRequest.getWeight());
        if (!changed) {
            return Response.notModified().build();
        }
        businessService.save();
        return Response.ok().build();
    }

    @POST
    @Path("{id}/reduction-key-edge")
    // Add Reduction Key
    public Response addReductionKeyEdge(@PathParam("id") final Long serviceId,
                            final ReductionKeyEdgeRequestDTO edgeRequest) {
        final BusinessService businessService = getManager().getBusinessServiceById(serviceId);
        boolean changed = getManager().addReductionKeyEdge(businessService, edgeRequest.getReductionKey(), transform(edgeRequest.getMapFunction()), edgeRequest.getWeight());
        if (!changed) {
            return Response.notModified().build();
        }
        businessService.save();
        return Response.ok().build();
    }

    @POST
    @Path("{id}/child-edge")
    // Add Child Service
    public Response addChildServiceEdge(@PathParam("id") final Long serviceId,
                            final ChildEdgeRequestDTO edgeRequest) {
        final BusinessService parentService = getManager().getBusinessServiceById(serviceId);
        final BusinessService childService = getManager().getBusinessServiceById(edgeRequest.getChildId());
        boolean changed = getManager().addChildEdge(parentService, childService, transform(edgeRequest.getMapFunction()), edgeRequest.getWeight());
        if (!changed) {
            return Response.notModified().build();
        }
        parentService.save();
        return Response.ok().build();
    }

    @DELETE
    @Path("{id}/edges/{edgeId}")
    public Response removeEdge(@PathParam("id") final Long serviceId,
                               @PathParam("edgeId") final Long edgeId) {
        final BusinessService service = getManager().getBusinessServiceById(serviceId);
        final Edge edge = getManager().getEdgeById(edgeId);
        boolean changed = getManager().deleteEdge(service, edge);
        if (!changed) {
            return Response.notModified().build();
        }
        service.save();
        return Response.ok().build();
    }

    @POST
    @Path("daemon/reload")
    public Response reload() {
        getManager().triggerDaemonReload();
        return Response.ok().build();
    }

    @GET
    @Path("functions/map")
    public Response listMapFunctions() {
        List<MapFunction> mapFunctions = getManager().listMapFunctions();
        if (mapFunctions == null || mapFunctions.isEmpty()) {
            return Response.noContent().build();
        }
        List<MapFunctionDTO> functionList = mapFunctions.stream().map(m -> transform(m)).collect(Collectors.toList());
        return Response.ok().entity(new MapFunctionListDTO(functionList)).build();
    }

    @GET
    @Path("functions/reduce")
    public Response listReduceFunctions() {
        List<ReductionFunction> reduceFunctions = getManager().listReduceFunctions();
        if (reduceFunctions == null || reduceFunctions.isEmpty()) {
            return Response.noContent().build();
        }
        List<ReduceFunctionDTO> functionList = reduceFunctions.stream().map(r -> transform(r)).collect(Collectors.toList());
        return Response.ok().entity(new ReduceFunctionListDTO(functionList)).build();
    }

    private IpServiceResponseDTO transform(IpService input) {
        IpServiceResponseDTO response = new IpServiceResponseDTO();
        response.setId(input.getId());
        response.setNodeLabel(input.getNodeLabel());
        response.setServiceName(input.getServiceName());
        response.setIpAddress(input.getIpAddress());
        response.setLocation(ResourceLocationFactory.createIpServiceLocation(String.valueOf(input.getId())));
        return response;
    }

    private AbstractEdgeResponseDTO transform(Edge edge) {
        Objects.requireNonNull(edge);
        if (edge instanceof IpServiceEdge) {
            return transform((IpServiceEdge) edge);
        }
        if (edge instanceof ChildEdge) {
            return transform((ChildEdge) edge);
        }
        if (edge instanceof ReductionKeyEdge) {
            return transform((ReductionKeyEdge) edge);
        }
        throw new IllegalArgumentException("Could not find a mapper for edge of type " + edge.getClass());
    }

    private IpServiceEdgeResponseDTO transform(IpServiceEdge edge) {
        final IpServiceEdgeResponseDTO response = new IpServiceEdgeResponseDTO();
        response.setId(edge.getId());
        response.setOperationalStatus(edge.getOperationalStatus());
        response.setLocation(ResourceLocationFactory.createBusinessServiceEdgeLocation(edge.getSource().getId(), edge.getId()));
        response.setReductionKeys(edge.getReductionKeys());
        response.setMapFunction(transform(edge.getMapFunction()));
        response.setWeight(edge.getWeight());
        response.setIpService(transform(edge.getIpService()));
        return response;
    }

    private ChildEdgeResponseDTO transform(ChildEdge edge) {
        final ChildEdgeResponseDTO response = new ChildEdgeResponseDTO();
        response.setId(edge.getId());
        response.setChildId(edge.getChild().getId());
        response.setOperationalStatus(edge.getOperationalStatus());
        response.setLocation(ResourceLocationFactory.createBusinessServiceEdgeLocation(edge.getSource().getId(), edge.getId()));
        response.setReductionKeys(edge.getReductionKeys());
        response.setMapFunction(transform(edge.getMapFunction()));
        response.setWeight(edge.getWeight());
        return response;
    }

    private ReductionKeyEdgeResponseDTO transform(ReductionKeyEdge edge) {
        final ReductionKeyEdgeResponseDTO response = new ReductionKeyEdgeResponseDTO();
        response.setId(edge.getId());
        response.setOperationalStatus(edge.getOperationalStatus());
        response.setReductionKey(edge.getReductionKey());
        response.setLocation(ResourceLocationFactory.createBusinessServiceEdgeLocation(edge.getSource().getId(), edge.getId()));
        response.setReductionKeys(edge.getReductionKeys());
        response.setMapFunction(transform(edge.getMapFunction()));
        response.setWeight(edge.getWeight());
        return response;
    }

    private MapFunction transform(MapFunctionDTO input) {
        return input.getType().fromDTO(input);
    }

    private MapFunctionDTO transform(MapFunction input) {
        MapFunctionType type = MapFunctionType.valueOf(input.getClass());
        return type.toDTO(input);
    }

    private ReduceFunctionDTO transform(ReductionFunction input) {
        ReduceFunctionType type = ReduceFunctionType.valueOf(input.getClass());
        return type.toDTO(input);
    }

    private ReductionFunction transform(ReduceFunctionDTO input) {
        return input.getType().fromDTO(input);
    }
}
