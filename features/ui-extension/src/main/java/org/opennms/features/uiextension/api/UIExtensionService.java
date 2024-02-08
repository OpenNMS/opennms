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
package org.opennms.features.uiextension.api;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.opennms.integration.api.v1.ui.UIExtension;

@Path("/plugins")
public interface UIExtensionService {
    @GET
    @Produces(value = {MediaType.APPLICATION_JSON})
    List<UIExtension> listPlugins();

    @GET
    @Path("/ui-extension/module/{id}")
    @Produces(value = {"application/javascript"})
    String getExtensionJSFile(@PathParam("id") String id, @QueryParam("path") String resourcePath) throws IOException;

    @GET
    @Path("/ui-extension/css/{id}")
    @Produces(value = {"text/css"})
    String getExtensionCSSFile(@PathParam("id") String id) throws IOException;
}
