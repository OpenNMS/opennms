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
