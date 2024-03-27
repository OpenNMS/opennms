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
package org.opennms.web.utils;

import org.opennms.core.utils.WebSecurityUtils;

import static org.opennms.web.utils.UriInfoUtils.hasKey;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

public abstract class QueryParametersBuilder {

    private QueryParametersBuilder() {}

    public static QueryParameters buildFrom(UriInfo uriInfo) {
        return buildFrom(uriInfo.getQueryParameters());
    }

    public static QueryParameters buildFrom(MultivaluedMap<String, String> params) {
        final QueryParameters queryParameters = new QueryParameters();
        if (hasKey(params, "limit")) {
            queryParameters.setLimit(WebSecurityUtils.safeParseInt(params.getFirst("limit")));
        }
        if (hasKey(params, "offset")) {
            queryParameters.setOffset(WebSecurityUtils.safeParseInt(params.getFirst("offset")));
        }
        if (hasKey(params, "orderBy")) {
            String orderBy = WebSecurityUtils.sanitizeString(params.getFirst("orderBy").trim());
            String order = WebSecurityUtils.sanitizeString(params.getFirst("order"));
            if (order != null) {
                order = order.trim();
            }
            queryParameters.setOrder(new QueryParameters.Order(orderBy, "desc".equalsIgnoreCase(order)));
        }
        return queryParameters;
    }
}
