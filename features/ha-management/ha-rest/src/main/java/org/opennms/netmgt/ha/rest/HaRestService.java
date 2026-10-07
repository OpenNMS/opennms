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
package org.opennms.netmgt.ha.rest;

import org.opennms.netmgt.ha.HaConfiguration;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * HA management REST API, registered through the OSGi JAX-RS whiteboard
 * (see {@code OSGI-INF/blueprint/blueprint.xml}; served under
 * {@code /opennms/rest/ha} by the container bridge).
 *
 * <p>Authorization is enforced by the webapp's Spring Security
 * intercept-url rules ({@code applicationContext-spring-security.xml}):
 * config writes, failover, and the sync endpoints require {@code ROLE_ADMIN}.
 * {@code @RolesAllowed} is NOT honored on this path — do not add it.
 */
@Path("/ha")
public interface HaRestService {

    /** Current state of all HA instances, from ha_instance_status. */
    @GET
    @Path("status")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response getStatus();

    /** Current HA configuration of this instance. */
    @GET
    @Path("config")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response getConfig();

    /** Replaces the on-disk configuration and applies it immediately.
     * Immutable fields (enabled, instance-id, role, mode) reject with 400. */
    @PUT
    @Path("config")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response updateConfig(@Context SecurityContext securityContext, HaConfiguration newCfg);

    /** Initiates graceful failover; valid only while this instance is ACTIVE
     * and in coordinator mode. */
    @POST
    @Path("failover")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response initiateFailover(@Context SecurityContext securityContext);

    /** Config-sync manifest: one line per in-scope file under etc/, formatted
     * {@code <sha256> <size> <relative-path>}. Consumed by the standby's
     * {@code HaConfigSyncer}. */
    @GET
    @Path("sync/manifest")
    @Produces(MediaType.TEXT_PLAIN)
    Response getSyncManifest();

    /** Raw bytes of one manifest file. Paths are validated against the etc
     * root and the exclusion rules. */
    @GET
    @Path("sync/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response getSyncFile(@QueryParam("root") String root, @QueryParam("f") String relativePath);
}
