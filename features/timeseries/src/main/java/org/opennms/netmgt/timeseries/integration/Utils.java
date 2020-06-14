/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.integration;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.netmgt.measurements.api.FetchResults;

public class Utils {

    public static Map<String, String> asMap (Collection<Tag> tags) {
        Map<String, String> map = new HashMap<>();
        for (Tag tag : tags) {
            map.put(tag.getKey(), tag.getValue());
        }
        return map;
    }

    // TODO: Patrick: remove later
    public static boolean equals(final FetchResults results1, final FetchResults results2) {
        if (results1 == results2) return true;
        if (results2 == null || results1.getClass() != results2.getClass()) return false;
        FetchResults that = results2;
        return Objects.equals(results1.getMetadata(), that.getMetadata()) &&
                Objects.equals(results1.getConstants(), that.getConstants()) &&
                Objects.equals(results1.getStep(), that.getStep()) &&
                Arrays.equals(results1.getTimestamps(), results2.getTimestamps()) &&
                results1.getColumns().size() == results2.getColumns().size() &&
                results1.getColumns().entrySet().stream()
                        .map(e -> Arrays.equals(e.getValue(), that.getColumns().get(e.getKey())))
                        .reduce(Boolean.TRUE, Boolean::logicalAnd);
    }
}
