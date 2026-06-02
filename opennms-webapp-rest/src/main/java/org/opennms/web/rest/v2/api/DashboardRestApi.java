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
package org.opennms.web.rest.v2.api;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API for the configurable system-wide dashboard layout (NMS-19851).
 *
 * A single JSON layout document is stored for the whole system. The document is
 * opaque to the backend (the UI owns its shape); it is persisted verbatim.
 */
@Path("dashboard")
@Tag(name = "Dashboard", description = "System-wide dashboard layout API V2")
public interface DashboardRestApi {

    @GET
    @Path("system")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get the system-wide dashboard layout.",
            description = "Returns the stored system-wide dashboard layout document, or 404 if none has been saved yet.",
            operationId = "getSystemDashboardLayout"
    )
    Response getSystemLayout();

    @PUT
    @Path("system")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Save the system-wide dashboard layout.",
            description = "Replaces the stored system-wide dashboard layout document.",
            operationId = "updateSystemDashboardLayout"
    )
    Response updateSystemLayout(Map<String, Object> layout);
}
