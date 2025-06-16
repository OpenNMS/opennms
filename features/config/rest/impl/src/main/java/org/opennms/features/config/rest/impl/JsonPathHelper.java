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
package org.opennms.features.config.rest.impl;

import java.util.List;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

public class JsonPathHelper {

    private static final Configuration AS_PATH_LIST = Configuration.builder()
            .options(Option.AS_PATH_LIST).build();
    
    public static String get(String data, String path) {
        JsonPath jsonPath = JsonPath.compile(path);

        //it's Ok to read multiple nodes, it could be useful so there is no assertThereIsExactlyOnePath() here
        
        return JsonPath.parse(data).read(jsonPath).toString();
    }

    public static String update(String data, String path, String newPartContent) {
        JsonPath jsonPath = JsonPath.compile(path);
        assertThereIsExactlyOnePath(data, jsonPath);

        Object newContentObject = Configuration.defaultConfiguration().jsonProvider().parse(newPartContent);
        return JsonPath.parse(data).set(jsonPath, newContentObject).jsonString();
    }

    public static String append(String data, String path, String newPartContent) {
        JsonPath jsonPath = JsonPath.compile(path);
        assertThereIsExactlyOnePath(data, jsonPath);

        Object newContentObject = Configuration.defaultConfiguration().jsonProvider().parse(newPartContent);
        return JsonPath.parse(data).add(jsonPath, newContentObject).jsonString();
    }

    public static String insertOrUpdateNode(String data, String parent, String nodeName, String newPartContent) {
        JsonPath jsonPath = JsonPath.compile(parent);
        assertThereIsExactlyOnePath(data, jsonPath);

        Object newContentObject = Configuration.defaultConfiguration().jsonProvider().parse(newPartContent);
        return JsonPath.parse(data).put(parent, nodeName, newContentObject).jsonString();
    }

    public static String delete(String data, String path) {
        JsonPath jsonPath = JsonPath.compile(path);
        assertThereIsExactlyOnePath(data, jsonPath);
        return JsonPath.parse(data).delete(jsonPath).jsonString();
    }

    private static int count(String data, JsonPath jsonPath) {
        List<String> paths = JsonPath.using(AS_PATH_LIST).parse(data).read(jsonPath);
        return paths.size();
    }

    private static void assertThereIsExactlyOnePath(String data, JsonPath jsonPath) {
        if (count(data, jsonPath) != 1) {
            throw new IllegalArgumentException("Path must resolve to a single element");
        }
    }
}
