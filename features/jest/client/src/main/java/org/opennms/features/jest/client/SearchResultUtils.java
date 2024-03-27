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
