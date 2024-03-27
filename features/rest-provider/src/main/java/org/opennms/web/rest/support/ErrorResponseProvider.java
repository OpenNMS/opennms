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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provider handles all exceptions which are not handled by any other provider.
 * This is required to ensure that rest requests return a 500 instead of the html error page from OpenNMS.
 *
 * @author mvrueden
 */
@Provider
public class ErrorResponseProvider implements ExceptionMapper<Exception> {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorResponseProvider.class);

    @Override
    public Response toResponse(Exception exception) {
        // Ensure Exception is logged. See HZN-1458
        LOG.error("An exception occurred while processing a rest request in an OSGi Rest Service: {}", exception.getMessage(), exception);

        // if there is an optional exception message, we add it to the response
        if (exception.getMessage() != null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(exception.getMessage()).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
