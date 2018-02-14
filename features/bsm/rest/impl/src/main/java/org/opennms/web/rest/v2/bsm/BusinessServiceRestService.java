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

package org.opennms.web.rest.v2.bsm;

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
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;
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
import org.opennms.web.rest.v2.bsm.model.ReduceFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.edge.AbstractEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ChildEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ChildEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.EdgeRequestDTOVisitor;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionMetaDTO;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionMetaListDTO;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionType;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionsManager;
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

        service.getEdges().forEach(eachEdge -> eachEdge.accept(new EdgeVisitor<Void>() {
            @Override
            public Void visit(IpServiceEdge edge) {
                response.getIpServices().add(transform(edge));
                return null;
            }

            @Override
            public Void visit(ReductionKeyEdge edge) {
                response.getReductionKeys().add(transform(edge));
                return null;
            }

            @Override
            public Void visit(ChildEdge edge) {
                response.getChildren().add(transform(edge));
                return null;
            }
        }));
        return Response.ok(response).build();
    }

    @POST
    public Response create(@Context final UriInfo uriInfo, final BusinessServiceRequestDTO request) {
        final BusinessService service = getManager().createBusinessService();
        service.setName(request.getName());
        service.setAttributes(request.getAttributes());
        service.setReduceFunction(transform(request.getReduceFunction()));

        request.getEdges().forEach(eachEdge -> eachEdge.accept(new EdgeRequestDTOVisitor() {
            @Override
            public void visit(IpServiceEdgeRequestDTO ipEdge) {
                service.addIpServiceEdge(
                        getManager().getIpServiceById(ipEdge.getIpServiceId()),
                        transform(ipEdge.getMapFunction()),
                        ipEdge.getWeight(),
                        ipEdge.getFriendlyName());
            }

            @Override
            public void visit(ChildEdgeRequestDTO childEdge) {
                service.addChildEdge(
                        getManager().getBusinessServiceById(childEdge.getChildId()),
                        transform(childEdge.getMapFunction()),
                        childEdge.getWeight());
            }

            @Override
            public void visit(ReductionKeyEdgeRequestDTO rkEdge) {
                service.addReductionKeyEdge(
                        rkEdge.getReductionKey(),
                        transform(rkEdge.getMapFunction()),
                        rkEdge.getWeight(),
                        rkEdge.getFriendlyName());
            }
        }));
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
        service.setIpServiceEdges(Sets.newHashSet());
        service.setChildEdges(Sets.newHashSet());

        request.getEdges().forEach(eachEdge -> eachEdge.accept(new EdgeRequestDTOVisitor() {

            @Override
            public void visit(IpServiceEdgeRequestDTO ipEdge) {
                getManager().addIpServiceEdge(service,
                        getManager().getIpServiceById(ipEdge.getIpServiceId()),
                        transform(ipEdge.getMapFunction()),
                        ipEdge.getWeight(),
                        ipEdge.getFriendlyName());
            }

            @Override
            public void visit(ChildEdgeRequestDTO childEdge) {
                getManager().addChildEdge(
                        service,
                        getManager().getBusinessServiceById(childEdge.getChildId()),
                        transform(childEdge.getMapFunction()),
                        childEdge.getWeight());
            }

            @Override
            public void visit(ReductionKeyEdgeRequestDTO rkEdge) {
                getManager().addReductionKeyEdge(
                    service,
                    rkEdge.getReductionKey(),
                    transform(rkEdge.getMapFunction()),
                    rkEdge.getWeight(),
                    rkEdge.getFriendlyName());
            }
        }));
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
        boolean changed = getManager().addIpServiceEdge(businessService, ipService, transform(edgeRequest.getMapFunction()), edgeRequest.getWeight(), edgeRequest.getFriendlyName());
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
        boolean changed = getManager().addReductionKeyEdge(businessService, edgeRequest.getReductionKey(), transform(edgeRequest.getMapFunction()), edgeRequest.getWeight(), edgeRequest.getFriendlyName());
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
       return createFunctionMetaListDTO(new FunctionsManager().getMapFunctions(), FunctionType.MapFunction);
    }

    @GET
    @Path("functions/map/{name}")
    public Response getMapFunctionMetaData(@PathParam("name") final String name) {
        FunctionMetaDTO metaData = new FunctionsManager().getMapFunctionMetaData(name);
        if (metaData == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No map function with name '" + name + "' found.").build();
        }
        return Response.ok().entity(metaData).build();
    }

    @GET
    @Path("functions/reduce")
    public Response listReduceFunctions() {
        return createFunctionMetaListDTO(new FunctionsManager().getReduceFunctions(), FunctionType.ReduceFunction);
    }

    @GET
    @Path("functions/reduce/{name}")
    public Response getReduceFunctionMetaData(@PathParam("name") final String name) {
        FunctionMetaDTO metaData = new FunctionsManager().getReduceFunctionMetaData(name);
        if (metaData == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No reduce function with name '" + name + "' found.").build();
        }
        return Response.ok().entity(metaData).build();
    }

    private Response createFunctionMetaListDTO(List<Class<?>> functions, FunctionType functionType) {
        if (functions == null || functions.isEmpty()) {
            return Response.noContent().build();
        }
        List<FunctionMetaDTO> functionList = functions.stream().map(functionMetaData -> new FunctionMetaDTO(functionMetaData, functionType)).collect(Collectors.toList());
        return Response.ok().entity(new FunctionMetaListDTO(functionList)).build();
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
        return edge.accept(new EdgeVisitor<AbstractEdgeResponseDTO>() {

            @Override
            public AbstractEdgeResponseDTO visit(IpServiceEdge edge) {
                return transform(edge);
            }

            @Override
            public AbstractEdgeResponseDTO visit(ReductionKeyEdge edge) {
                return transform(edge);
            }

            @Override
            public AbstractEdgeResponseDTO visit(ChildEdge edge) {
                return transform(edge);
            }
        });
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
        response.setFriendlyName(edge.getFriendlyName());
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
        response.setFriendlyName(edge.getFriendlyName());
        return response;
    }

    private MapFunction transform(MapFunctionDTO input) {
        return new FunctionsManager().getMapFunction(input);
    }

    private MapFunctionDTO transform(MapFunction input) {
       return new FunctionsManager().getMapFunctionDTO(input);
    }

    private ReduceFunctionDTO transform(ReductionFunction input) {
       return new FunctionsManager().getReduceFunctionDTO(input);
    }

    private ReductionFunction transform(ReduceFunctionDTO input) {
        return new FunctionsManager().getReduceFunction(input);
    }
}
