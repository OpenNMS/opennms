/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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