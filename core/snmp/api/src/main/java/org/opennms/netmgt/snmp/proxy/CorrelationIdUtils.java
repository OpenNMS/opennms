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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.snmp.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CorrelationIdUtils {
    private static final Pattern POP_INDEX_PATTERN = Pattern.compile("^(\\d+)(-(.*))?$");

    /**
     * Prepends the index to the correlation id on the given request.
     */
    public static void pushIndexToCorrelationId(WalkRequest walkRequest, int idx) {
        String effectiveCorrelationId = null;
        if (walkRequest.getCorrelationId() == null) {
            effectiveCorrelationId = Integer.toString(idx);
        } else {
            effectiveCorrelationId = String.format("%d-%s", idx, walkRequest.getCorrelationId());
        }
        walkRequest.setCorrelationId(effectiveCorrelationId);
    }

    /**
     * Retrieves the most recent (first one in the string) index from the correlation id
     * on the given response, and adds the (modified) response to the map at that index.
     *
     * The responses in the map contain the same results, but their correlation ids are modified
     * to remove index at which they were mapped.
     */
    public static void popIndexFromCollerationId(WalkResponse walkResponse, Map<Integer, List<WalkResponse>> responsesByIndex) {
        if (walkResponse.getCorrelationId() == null) {
            return;
        }
        Matcher m = POP_INDEX_PATTERN.matcher(walkResponse.getCorrelationId());
        if (m.matches()) {
            int index = Integer.valueOf(m.group(1));
            // Clone the response and update the correlation id
            WalkResponse clonedResponse = new WalkResponse(walkResponse.getResults(), m.group(3));

            // Add the cloned response to the map
            List<WalkResponse> responsesAtIndex = responsesByIndex.get(index);
            if (responsesAtIndex == null) {
                responsesAtIndex = new ArrayList<>();
                responsesByIndex.put(index, responsesAtIndex);
            }
            responsesAtIndex.add(clonedResponse);
        }
    }
}
