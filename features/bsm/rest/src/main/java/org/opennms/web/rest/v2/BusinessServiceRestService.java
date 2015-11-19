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

import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.dao.bsm.BusinessServiceDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.bsm.BusinessService;
import org.opennms.netmgt.model.bsm.BusinessServiceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("business-services")
@Transactional
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
public class BusinessServiceRestService extends AbstractDaoRestService<BusinessService, Long> {

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Autowired
    private BusinessServiceDao businessServiceDao;

    @Override
    protected OnmsDao<BusinessService, Long> getDao() {
        return businessServiceDao;
    }

    @Override
    protected Class<BusinessService> getDaoClass() {
        return BusinessService.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder() {
        return new CriteriaBuilder(BusinessService.class);
    }

    @Override
    protected JaxbListWrapper<BusinessService> createListWrapper(Collection<BusinessService> services) {
        return new BusinessServiceList(services);
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
