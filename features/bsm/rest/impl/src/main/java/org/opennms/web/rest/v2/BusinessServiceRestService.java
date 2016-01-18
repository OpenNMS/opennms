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
import java.util.stream.Collectors;

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
import org.opennms.web.rest.api.ResourceLocationFactory;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceListDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceRequestDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.IpServiceResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("business-services")
@Transactional
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
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
        response.setIpServices(service.getIpServices().stream()
                                      .map(s -> transform(s)).collect(Collectors.toSet()));
        response.setChildServices(service.getChildServices().stream().map(BusinessService::getId).collect(Collectors.toSet()));
        response.setLocation(ResourceLocationFactory.createBusinessServiceLocation(service.getId().toString()));
        response.setParentServices(service.getParentServices().stream().map(BusinessService::getId).collect(Collectors.toSet()));
        response.setOperationalStatus(getManager().getOperationalStatusForBusinessService(service));

        return Response.ok(response).build();
    }

    @POST
    public Response create(@Context final UriInfo uriInfo, final BusinessServiceRequestDTO request) {
        final BusinessService service = getManager().createBusinessService();
        service.setName(request.getName());
        service.setAttributes(request.getAttributes());
        service.setIpServices(request.getIpServices().stream()
                                     .map(serviceId -> getManager().getIpServiceById(serviceId))
                                     .collect(Collectors.toSet()));
        service.setChildServices(request.getChildServices().stream()
                                        .map(serviceId -> getManager().getBusinessServiceById(serviceId))
                                        .collect(Collectors.toSet()));
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
        service.setIpServices(request.getIpServices().stream()
                                     .map(serviceId -> getManager().getIpServiceById(serviceId))
                                     .collect(Collectors.toSet()));
        service.setChildServices(request.getChildServices().stream()
                                        .map(serviceId -> getManager().getBusinessServiceById(serviceId))
                                        .collect(Collectors.toSet()));
        getManager().saveBusinessService(service);

        return Response.noContent().build();
    }

    @GET
    @Path("/ip-services/{ipServiceId}")
    public Response getIpService(@PathParam("ipServiceId") final Integer ipServiceId) {
        IpService ipService = getManager().getIpServiceById(ipServiceId);
        return Response.ok().entity(transform(ipService)).build();
    }

    @POST
    @Path("daemon/reload")
    public Response reload() {
        getManager().triggerDaemonReload();
        return Response.ok().build();
    }

    private IpServiceResponseDTO transform(IpService ipService) {
        final IpServiceResponseDTO response = new IpServiceResponseDTO();
        response.setId(ipService.getId());
        response.setNodeLabel(ipService.getNodeLabel());
        response.setServiceName(ipService.getServiceName());
        response.setIpAddress(ipService.getIpAddress());
        response.setOperationalStatus(getManager().getOperationalStatusForIPService(ipService));
        response.setLocation(ResourceLocationFactory.createBusinessServiceIpServiceLocation(ipService.getId()));
        return response;
    }
}
