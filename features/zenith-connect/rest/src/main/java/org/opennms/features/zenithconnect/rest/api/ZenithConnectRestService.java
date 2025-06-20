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
package org.opennms.features.zenithconnect.rest.api;

import org.opennms.features.zenithconnect.persistence.api.ZenithConnectRegistration;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectRegistrations;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

@Path("/zenith-connect")
public interface ZenithConnectRestService {
    /**
     * Get Zenith Connect registration info
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("registrations")
    Response getRegistrations();

    @POST
    @Path("registrations")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    Response addRegistration(ZenithConnectRegistration registration);

    @PUT
    @Path("registrations/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    Response updateRegistration(@PathParam("id") String id, ZenithConnectRegistration registration);

    /**
     * Delete registrations with the given id or ids.
     * @param ids comma separated list of ids (String)
     */
    @DELETE
    @Path("registrations/{id}")
    Response deleteRegistration(@PathParam("id") String id);
}
