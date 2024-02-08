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

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class RedirectHelper {

    public static URI getRedirectUri(final UriInfo uriInfo, final Object... pathComponents) {
        if (pathComponents != null && pathComponents.length == 0) {
            final URI requestUri = uriInfo.getRequestUri();
            try {
                return new URI(requestUri.getScheme(), requestUri.getUserInfo(), requestUri.getHost(), requestUri.getPort(), requestUri.getPath().replaceAll("/$", ""), null, null);
            } catch (final URISyntaxException e) {
                return requestUri;
            }
        } else {
            UriBuilder builder = uriInfo.getRequestUriBuilder();
            for (final Object component : pathComponents) {
                if (component != null) {
                    builder = builder.path(component.toString());
                }
            }
            return builder.build();
        }
    }
}
