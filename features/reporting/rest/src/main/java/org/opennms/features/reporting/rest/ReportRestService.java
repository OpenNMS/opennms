/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.rest;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/reports")
@Produces("application/json")
@Consumes("application/json")
public interface ReportRestService {

    @GET
    Response listReports();

    @GET
    @Path("/{id}")
    Response getReportDetails(@PathParam("id") String reportId);

    @POST
    @Path("/{id}")
    Response runReport(@PathParam("id") String reportId, Map<String, Object> inputParameters);

    @GET
    @Path("/persisted")
    Response listPersistedReports();

    @DELETE
    @Path("/persisted")
    Response deletePersistedReports();

    @DELETE
    @Path("/persisted/{id}")
    Response deletePersistedReport(@PathParam("id") final int id);

    @GET
    @Path("/scheduled")
    Response listScheduledReports();

    @DELETE
    @Path("/scheduled")
    Response deleteScheduledReports();

    @DELETE
    @Path("/scheduled/{id}")
    Response deleteScheduledReport(@PathParam("id") final String triggerName);
}
