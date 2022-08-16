/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.config.rest.impl;

import java.util.List;
import java.util.Random;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

public class JsonPathHelper {

    private static final Configuration AS_PATH_LIST = Configuration.builder()
            .options(Option.AS_PATH_LIST).build();
    
    public static String get(String data, String path) {
        JsonPath jsonPath = JsonPath.compile(path);

        //it's ok to read multiple nodes, it could be useful 
        //assertThereIsExactlyOnePath(data, jsonPath);
        
        return JsonPath.parse(data).read(jsonPath).toString();
    }

    public static String update(String data, String path, String newPartContent) {
        JsonPath jsonPath = JsonPath.compile(path);
        assertThereIsExactlyOnePath(data, jsonPath);
        
        //the object under specified path will be replaced with providet text without checking if the text is valid JSON 
        //for that on the first step the specified node will be replaced by unique number, and then this number will be replaced 
        //withh provided text
        Long unique = new Random().nextLong();
        while (data.contains(unique.toString())) {
            //just to be sure we do not overwrite come existing data
            unique = new Random().nextLong();
        }
        String newJson = JsonPath.parse(data).set(jsonPath, unique).jsonString();
        return newJson.replace(unique.toString(), newPartContent);
    }

    public static String delete(String data, String path) {
        JsonPath jsonPath = JsonPath.compile(path);
        assertThereIsExactlyOnePath(data, jsonPath);
        return JsonPath.parse(data).delete(jsonPath).jsonString();
    }
    
    private static void assertThereIsExactlyOnePath(String data, JsonPath jsonPath) {
        List<String> paths = JsonPath.using(AS_PATH_LIST).parse(data).read(jsonPath);
        if (paths.size() != 1) {
            throw new IllegalArgumentException("There must be exactly one path in JSON");
        }
    }
}
