/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.daemon.DaemonConfigService;
import org.opennms.netmgt.daemon.DaemonDTO;
import org.opennms.netmgt.daemon.DaemonReloadStateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("daemonRestService")
@Path("daemons")
public class DaemonRestService {

    @Autowired
    private DaemonConfigService configService;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public DaemonDTO[] getDaemons(@Context final UriInfo uriInfo) {
        return this.configService.getDaemons();
    }

    @POST
    @Path("/reload/{daemonName}")
    public Response reloadDaemonByName(@PathParam("daemonName") String daemonName) {
        if(this.configService.reloadDaemon(daemonName)){
            return Response.noContent().build();
        }
        // TODO zottel correct status code or use another one ?!
        return Response.status(428).build();
    }

    @GET
    @Path("/checkReloadState/{daemonName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public DaemonReloadStateDTO getDaemonReloadState(@PathParam("daemonName") String daemonName) {
        return this.configService.getDaemonReloadState(daemonName);
    }
}