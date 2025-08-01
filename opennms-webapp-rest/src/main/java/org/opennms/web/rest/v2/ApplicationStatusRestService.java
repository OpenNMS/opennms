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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "PerspectivePoller", description = "Perspective Poller API")
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
    @Operation(summary = "Get a Status of a specified application", description = "Get a Status of a specified application", operationId = "ApplicationStatusRestServiceGetStatusByApplicationId")
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
    @Operation(summary = "Get a Status of a specified application by monitoringServiceId", description = "Get a Status of a specified application by monitoringServiceId", operationId = "ApplicationStatusRestServiceGetStatusByMonitoringServiceId")
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
