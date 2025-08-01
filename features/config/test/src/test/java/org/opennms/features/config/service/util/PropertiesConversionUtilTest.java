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
package org.opennms.features.config.service.util;

import static org.junit.Assert.assertEquals;
import static org.opennms.features.config.service.util.PropertiesConversionUtil.jsonToMap;
import static org.opennms.features.config.service.util.PropertiesConversionUtil.mapToJsonString;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opennms.features.config.service.api.JsonAsString;

public class PropertiesConversionUtilTest {
    @Test
    public void testRoundtrip() {
        Map<String, Object> originalMap = new HashMap<>();
        originalMap.put("key1", "value1");
        originalMap.put("key2", Boolean.TRUE);

        Map<String, Object> convertedMap = new HashMap<>(jsonToMap(mapToJsonString(originalMap).toString()));
        assertEquals(originalMap, convertedMap);
    }

    @Test
    public void nullValuesShouldBeRemovedForProperties() {
        Map<String, Object> originalMap = new HashMap<>();
        originalMap.put("key1", "value1");
        originalMap.put("key2", null);
        JsonAsString json = mapToJsonString(originalMap);
        Map<String, Object> properties = jsonToMap(json.toString());
        assertEquals(1, properties.size());
        assertEquals("value1", properties.get("key1"));
    }
}