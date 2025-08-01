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
package org.opennms.netmgt.xml.eventconf;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class MatchResult {
    public static final MatchResult FALSE = MatchResult.of(false);
    public static final MatchResult TRUE = MatchResult.of(true);

    private final boolean matched;
    private final Map<String, String> eventParameters;

    private MatchResult(boolean matched) {
        this.matched = matched;
        this.eventParameters = Collections.emptyMap();
    }

    private MatchResult(boolean matched, Map<String, String> eventParameters) {
        this.matched = matched;
        this.eventParameters = Collections.unmodifiableMap(new LinkedHashMap<>(eventParameters));
    }

    public static MatchResult of(boolean matches) {
        return new MatchResult(matches);
    }

    public static MatchResult of(boolean matches, Map<String, String> eventParameters) {
        return new MatchResult(matches, eventParameters);
    }

    public MatchResult and(MatchResult other) {
        // If either results are false, then re-use that instance
        if (!matched) {
            return this;
        }
        if (!other.matched) {
            return other;
        }
        // Both results are truthy, let's merge the event parameters
        final Map<String, String> allEventParameters = new HashMap<>();
        allEventParameters.putAll(eventParameters);
        allEventParameters.putAll(other.eventParameters);
        return MatchResult.of(true, allEventParameters);
    }

    public boolean matched() {
        return matched;
    }

    public Map<String, String> getEventParameters() {
        return eventParameters;
    }
}
