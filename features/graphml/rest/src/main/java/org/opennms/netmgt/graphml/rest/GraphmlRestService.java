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
package org.opennms.netmgt.graphml.rest;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.graphdrawing.graphml.GraphmlType;

@Path("/graphml")
@Produces(value= MediaType.APPLICATION_XML)
@Consumes(value= MediaType.APPLICATION_XML)
public interface GraphmlRestService {

    @POST
    @Path("/{graph-name}")
    Response createGraph(@PathParam("graph-name") String graphname, GraphmlType graphmlType) throws IOException;

    @DELETE
    @Path("/{graph-name}")
    Response deleteGraph(@PathParam("graph-name") String graphname) throws IOException;

    @GET
    @Path("/{graph-name}")
    Response getGraph(@PathParam("graph-name") String graphname) throws IOException;

}
