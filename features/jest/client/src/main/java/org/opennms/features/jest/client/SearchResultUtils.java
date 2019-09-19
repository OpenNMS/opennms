/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.jest.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.searchbox.core.SearchResult;

public class SearchResultUtils {
    private static final String[] PATH_TO_TOTAL = new String[]{"hits", "total", "value"};

    public static long getTotal(SearchResult result) {
        Long total = -1L;
        JsonElement obj = getPath(result.getJsonObject(), PATH_TO_TOTAL);
        if (obj != null) total = obj.getAsLong();
        return total;
    }

    public static JsonElement getPath(JsonObject jo, String[] path) {
        JsonElement retval = null;
        if (jo != null) {
            JsonElement obj = jo;
            for (String component : path) {
                if (obj == null) break;
                obj = ((JsonObject) obj).get(component);
            }
            retval = obj;
        }
        return retval;
    }

}
