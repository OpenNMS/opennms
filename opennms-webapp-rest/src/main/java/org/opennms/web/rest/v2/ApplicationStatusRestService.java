/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.support.ApplicationStatusUtil;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("perspectivepoller")
@Transactional
public class ApplicationStatusRestService {

    @Autowired
    private OutageDao outageDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @GET
    @Path("{applicationId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response applicationStatus(@PathParam("applicationId") final Integer applicationId, @QueryParam("start") Long start, @QueryParam("end") Long end) {
        if (end == null) {
            end = new Date().getTime();
        }

        if (start == null) {
            start = end - 86400000;
        }

        final OnmsApplication onmsApplication = applicationDao.get(applicationId);
        if (onmsApplication == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Collection<OnmsOutage> statusChanges = outageDao.getStatusChangesForApplicationIdBetween(new Date(start), new Date(end), applicationId);
        return Response.ok(ApplicationStatusUtil.buildApplicationStatus(onmsApplication, statusChanges, start, end)).build();
    }

    @GET
    @Path("{applicationId}/{monitoredServiceId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response applicationServiceStatus(@PathParam("applicationId") final Integer applicationId, @PathParam("monitoredServiceId") final Integer monitoredServiceId, @QueryParam("start") Long start, @QueryParam("end") Long end) {
        if (end == null) {
            end = new Date().getTime();
        }

        if (start == null) {
            start = end - 86400000;
        }

        final OnmsApplication onmsApplication = applicationDao.get(applicationId);
        if (onmsApplication == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Collection<OnmsOutage> statusChanges = outageDao.getStatusChangesForApplicationIdBetween(new Date(start), new Date(end), applicationId);
        return Response.ok(ApplicationStatusUtil.buildApplicationServiceStatus(monitoredServiceDao, onmsApplication, monitoredServiceId, statusChanges, start, end)).build();
    }
}
