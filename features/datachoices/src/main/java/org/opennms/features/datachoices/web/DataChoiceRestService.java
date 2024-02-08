/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

import org.opennms.features.datachoices.internal.usagestatistics.UsageStatisticsReportDTO;
import org.opennms.features.datachoices.internal.usagestatistics.UsageStatisticsStatusDTO;
import org.opennms.features.datachoices.internal.userdatacollection.UserDataCollectionFormData;
import org.opennms.features.datachoices.internal.userdatacollection.UserDataCollectionStatusDTO;

@Path("/datachoices")
public interface DataChoiceRestService {
    @GET
    @Produces(value={MediaType.APPLICATION_JSON})
    UsageStatisticsReportDTO getUsageStatistics() throws ServletException, IOException;

    @GET
    @Path("/status")
    @Produces(value={MediaType.APPLICATION_JSON})
    Response getStatus() throws ServletException, IOException;

    @POST
    @Path("/status")
    @Consumes({MediaType.APPLICATION_JSON})
    Response setStatus(@Context HttpServletRequest request, UsageStatisticsStatusDTO dto) throws ServletException, IOException;

    @GET
    @Path("/meta")
    @Produces(value={MediaType.APPLICATION_JSON})
    Response getMetadata() throws ServletException, IOException;

    @GET
    @Path("/userdatacollection/status")
    @Produces(value={MediaType.APPLICATION_JSON})
    Response getUserDataCollectionStatus() throws ServletException, IOException;

    @POST
    @Path("/userdatacollection/status")
    @Consumes({MediaType.APPLICATION_JSON})
    Response setUserDataCollectionStatus(@Context HttpServletRequest request, UserDataCollectionStatusDTO dto) throws ServletException, IOException;

    @POST
    @Path("/userdatacollection/submit")
    @Consumes({MediaType.APPLICATION_JSON})
    Response submitUserDataCollectionData(@Context HttpServletRequest request, UserDataCollectionFormData data) throws ServletException, IOException;
}
