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
package org.opennms.netmgt.wsman.eventlog;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses {@code 90s}, {@code 15m}, {@code 1h}, {@code 2d}, or a bare number of milliseconds. */
public final class Durations {

    private static final Pattern DURATION = Pattern.compile("^\\s*(\\d+)\\s*([smhd]?)\\s*$", Pattern.CASE_INSENSITIVE);

    private Durations() {
    }

    public static Duration parse(String value) {
        final Matcher m = DURATION.matcher(value == null ? "" : value);
        if (!m.matches()) {
            throw new IllegalArgumentException("Not a duration: '" + value + "'");
        }
        final long n = Long.parseLong(m.group(1));
        switch (m.group(2).toLowerCase()) {
            case "s": return Duration.ofSeconds(n);
            case "m": return Duration.ofMinutes(n);
            case "h": return Duration.ofHours(n);
            case "d": return Duration.ofDays(n);
            default: return Duration.ofMillis(n);
        }
    }
}
