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
