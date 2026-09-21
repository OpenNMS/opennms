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
package org.opennms.web.rest.v2;

import javax.ws.rs.Path;

import org.opennms.web.rest.support.openapi.AbstractStaticOpenApiResource;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Hidden;

/** Serves the generated V2 OpenAPI document. */
@Hidden
@Component
@Path("openapi.{type:json|yaml}")
public class OpenApiResource extends AbstractStaticOpenApiResource {

    public OpenApiResource() {
        super("/openapi/openapi-v2.json");
    }
}
