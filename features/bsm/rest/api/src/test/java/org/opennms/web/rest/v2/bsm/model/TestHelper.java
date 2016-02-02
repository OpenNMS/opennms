/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.bsm.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestHelper {

    public static MapFunctionDTO createMapFunctionDTO(MapFunctionType type, String[]... pairs) {
        MapFunctionDTO mapFunctionDTO = new MapFunctionDTO();
        mapFunctionDTO.setType(type);
        mapFunctionDTO.setProperties(buildMap(pairs));
        return mapFunctionDTO;
    }

    public static ReduceFunctionDTO createReduceFunctionDTO(ReduceFunctionType type, String[]... pairs) {
        ReduceFunctionDTO reduceFunctionDTO = new ReduceFunctionDTO();
        reduceFunctionDTO.setType(type);
        reduceFunctionDTO.setProperties(buildMap(pairs));
        return reduceFunctionDTO;
    }

    private static Map<String, String> buildMap(String[][] pairs) {
        if (pairs != null) {
            Map<String, String> map = new HashMap<>();
            for(String[] eachPair : pairs) {
                map.put(Objects.requireNonNull(eachPair[0]), Objects.requireNonNull(eachPair[1]));
            }
            return map;
        }
        return null;
    }

}
