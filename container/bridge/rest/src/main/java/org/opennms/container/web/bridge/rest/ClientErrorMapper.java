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
package org.opennms.container.web.bridge.rest;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

/**
 * Converts a {@link ClientErrorException} that has no mapper of its own.
 * CXF throws this exception type when a request matches a resource but no method of the resource.
 * <p>
 * Status 404 stays 404. For each other status, the mapper for {@link Exception} makes the response.
 */
@Provider
public class ClientErrorMapper implements ExceptionMapper<ClientErrorException> {

    @Context
    private Providers providers;

    @Override
    public Response toResponse(final ClientErrorException exception) {
        final Response response = exception.getResponse();
        if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            return Response.status(Response.Status.NOT_FOUND).entity(exception.getMessage()).build();
        }
        final ExceptionMapper<Exception> fallback = providers.getExceptionMapper(Exception.class);
        return fallback == null ? response : fallback.toResponse(exception);
    }
}
