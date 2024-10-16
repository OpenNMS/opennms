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
package org.opennms.web.rest.support;

import javax.naming.ServiceUnavailableException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps an Exception of type {@link ServiceUnavailableException} to a Response HTTP Status of 503 (service not available).
 */
@Provider
public class ServiceUnavailableResponseProvider implements ExceptionMapper<ServiceUnavailableException> {
    @Override
    public Response toResponse(ServiceUnavailableException exception) {
        // if there is an optional exception message, we add it to the response
        if (exception.getMessage() != null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(exception.getMessage()).build();
        }
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .type(MediaType.TEXT_PLAIN)
                .entity("The service you are requesting is not available. Please try again later")
                .build();
    }
}
