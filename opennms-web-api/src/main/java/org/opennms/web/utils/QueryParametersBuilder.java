/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.utils;

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
            queryParameters.setLimit(Integer.valueOf(params.getFirst("limit").trim()));
        }
        if (hasKey(params, "offset")) {
            queryParameters.setOffset(Integer.valueOf(params.getFirst("offset").trim()));
        }
        if (hasKey(params, "orderBy")) {
            String orderBy = params.getFirst("orderBy").trim();
            String order = params.getFirst("order");
            if (order != null) {
                order = order.trim();
            }
            queryParameters.setOrder(new QueryParameters.Order(orderBy, "desc".equalsIgnoreCase(order)));
        }
        return queryParameters;
    }
}
