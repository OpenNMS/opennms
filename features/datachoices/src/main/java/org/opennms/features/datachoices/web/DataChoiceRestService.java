/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

package org.opennms.features.datachoices.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.features.datachoices.internal.UsageStatisticsReportDTO;
import org.opennms.features.datachoices.internal.UsageStatisticsStatusDTO;
import org.opennms.features.datachoices.internal.UserDataCollectionStatusDTO;

@Path("/datachoices")
public interface DataChoiceRestService {
    @GET
    @Produces(value={MediaType.APPLICATION_JSON})
    UsageStatisticsReportDTO getUsageStatistics() throws ServletException, IOException;

    @GET
    @Path("status")
    @Produces(value={MediaType.APPLICATION_JSON})
    Response getStatus() throws ServletException, IOException;

    @POST
    @Path("status")
    @Consumes({MediaType.APPLICATION_JSON})
    Response setStatus(@Context HttpServletRequest request, UsageStatisticsStatusDTO dto) throws ServletException, IOException;

    @GET
    @Path("meta")
    @Produces(value={MediaType.APPLICATION_JSON})
    Response getMetadata() throws ServletException, IOException;

    @GET
    @Path("userdatacollection")
    @Produces(value={MediaType.APPLICATION_JSON})
    Response getUserDataCollectionStatus() throws ServletException, IOException;

    @POST
    @Path("userdatacollection")
    @Consumes({MediaType.APPLICATION_JSON})
    Response setUserDataCollectionStatus(@Context HttpServletRequest request, UserDataCollectionStatusDTO dto) throws ServletException, IOException;
}
