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
package org.opennms.features.situationfeedback.rest;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.opennms.features.situationfeedback.api.AlarmFeedback;

@Path("/situation-feedback")
public interface SituationFeedbackRestService {

    @GET
    @Path("/tags")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getTags(@QueryParam("prefix") String prefix);

    @GET
    @Path("/{situationId : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<AlarmFeedback> getFeedback(@PathParam("situationId") int situationId);

    @POST
    @Path("/{situationId : \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void setFeedback(@PathParam("situationId") int situationId, List<AlarmFeedback> feedback);
}
