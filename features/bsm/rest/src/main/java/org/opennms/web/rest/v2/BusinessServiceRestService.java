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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.model.BusinessService;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceListDTO;
import org.opennms.web.rest.v2.bsm.model.IpServiceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("business-services")
@Transactional
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
public class BusinessServiceRestService {

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Autowired
    private BusinessServiceDao businessServiceDao;

    protected OnmsDao<BusinessService, Long> getDao() {
        return businessServiceDao;
    }

    @GET
    public Response list() {
        List<BusinessService> all = getDao().findAll();
        if (all == null ||all.isEmpty()) {
            return Response.noContent().build();
        }
        List<BusinessServiceDTO> entities = transform(all);
        BusinessServiceListDTO serviceList = new BusinessServiceListDTO(entities, ResourceLocationFactory.createBusinessServiceLocation());
        return Response.ok(serviceList).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") Long id) {
        BusinessService service = getBusinessService(id);
        BusinessServiceDTO entity = transform(service);
        return Response.ok(entity).build();
    }

    @POST
    public Response create(@Context final UriInfo uriInfo, BusinessServiceDTO objectToCreate) {
        BusinessService service = transform(objectToCreate);
        Long id = getDao().save(service);
        return Response.created(RedirectHelper.getRedirectUri(uriInfo, id)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        BusinessService service = getBusinessService(id);
        getDao().delete(service);
        return Response.ok().build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") Long id, BusinessServiceDTO dto) {
        BusinessService businessService = getBusinessService(id);
        if (!businessService.getId().equals(dto.getId())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        getDao().saveOrUpdate(transform(dto));
        return Response.noContent().build();
    }

    @POST
    @Path("{id}/ip-service/{ipServiceId}")
    public Response attachIpService(@PathParam("id") final Long serviceId,
                                    @PathParam("ipServiceId") final Integer ipServiceId) {
        final BusinessService service = getBusinessService(serviceId);
        final OnmsMonitoredService monitoredService = getIpService(ipServiceId);
        // if already exists, no update
        if (service.getIpServices().contains(monitoredService)) {
            return Response.notModified().build();
        }
        // add and update
        service.addIpService(monitoredService);
        getDao().update(service);
        return Response.ok().build();
    }

    @DELETE
    @Path("{id}/ip-service/{ipServiceId}")
    public Response detachIpService(@PathParam("id") final Long serviceId,
                                    @PathParam("ipServiceId") final Integer ipServiceId) {
        final BusinessService service = getBusinessService(serviceId);
        final OnmsMonitoredService monitoredService = getIpService(ipServiceId);

        // does not exist, no update necessary
        if (!service.getIpServices().contains(monitoredService)) {
            return Response.notModified().build();
        }

        // remove and update
        service.removeIpService(monitoredService);
        businessServiceDao.update(service);
        return Response.ok().build();
    }

    private List<BusinessServiceDTO> transform(List<BusinessService> all) {
        if (all != null) {
            List<BusinessServiceDTO> transformedList = new ArrayList<>();
            for (BusinessService eachService : all) {
                BusinessServiceDTO serviceDTO = transform(eachService);
                if (serviceDTO != null) {
                    transformedList.add(serviceDTO);
                }
            }
            return transformedList;
        }
        return null;
    }

    private BusinessService transform(BusinessServiceDTO dto) throws WebApplicationException {
        BusinessService service = new BusinessService();
        service.setId(dto.getId());
        service.setName(dto.getName());
        service.setAttributes(new HashMap<>(dto.getAttributes()));
        for (IpServiceDTO eachService : dto.getIpServices()) {
            OnmsMonitoredService ipService = getIpService(Integer.valueOf(eachService.getId()));
            service.addIpService(ipService);
        }
        return service;
    }

    private BusinessServiceDTO transform(BusinessService service) {
        BusinessServiceDTO dto = new BusinessServiceDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setAttributes(new HashMap<>(service.getAttributes()));
        for (OnmsMonitoredService eachService : service.getIpServices()) {
            IpServiceDTO ipServiceDTO = transform(eachService);
            if (ipServiceDTO != null) {
                dto.addIpService(ipServiceDTO);
            }
        }
        return dto;
    }

    private IpServiceDTO transform(OnmsMonitoredService input) {
        if (input != null) {
            IpServiceDTO output = new IpServiceDTO();
            if (input.getId() != null) {
                output.setId(String.valueOf(input.getId()));
                output.setLocation(ResourceLocationFactory.createIpServiceLocation(output.getId()));
                return output;
            }
        }
        return null;
    }

    private BusinessService getBusinessService(Long serviceId) {
        final BusinessService service = getDao().get(serviceId);
        if (service == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return service;
    }

    private OnmsMonitoredService getIpService(Integer serviceId) {
        final OnmsMonitoredService monitoredService = monitoredServiceDao.get(serviceId);
        if (monitoredService == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return monitoredService;
    }

}
