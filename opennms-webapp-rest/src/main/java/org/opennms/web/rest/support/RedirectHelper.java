/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
