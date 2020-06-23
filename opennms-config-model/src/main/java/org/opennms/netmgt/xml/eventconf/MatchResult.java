/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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
