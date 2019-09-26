/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

public abstract class UriInfoUtils {

    private UriInfoUtils() {}

    public static boolean hasKey(final UriInfo uriInfo, final String key) {
        final MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        return hasKey(params, key);
    }

    public static boolean hasKey(final MultivaluedMap<String, String> params, String key) {
        return params.containsKey(key) && params.getFirst(key) != null && !"".equals(params.getFirst(key).trim());
    }

    public static String getValue(UriInfo uriInfo, String key, String defaultValue) {
        if (hasKey(uriInfo, key)) {
            return uriInfo.getQueryParameters().getFirst(key).trim();
        }
        return defaultValue;
    }

    public static List<String> getValues(UriInfo uriInfo, String key) {
        return getValues(uriInfo, key, Collections.emptyList());
    }

    public static List<String> getValues(UriInfo uriInfo, String key, List<String> defaultValue) {
        if (hasKey(uriInfo, key)) {
            final Set<String> uniqueValues = uriInfo.getQueryParameters().get(key)
                    .stream().map(g -> g != null ? g.trim() : g)
                    .filter(g -> g != null)
                    .collect(Collectors.toSet());
            return new ArrayList<>(uniqueValues);
        }
        return defaultValue;
    }
}
