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
package org.opennms.web.outage;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Date;

import org.junit.Test;

public class OutageUtilTest {

    @Test
    public void formatDurationUsesFullTimestampsAcrossMidnight() {
        Date firstTime = Date.from(Instant.parse("2026-03-18T23:59:59Z"));
        Date lastTime = Date.from(Instant.parse("2026-03-19T00:00:01Z"));

        assertEquals("0h0m2s", OutageUtil.formatDuration(firstTime, lastTime));
    }

    @Test
    public void formatDurationIncludesDaysWhenElapsedTimeExceedsOneDay() {
        Date firstTime = Date.from(Instant.parse("2026-03-18T00:00:00Z"));
        Date lastTime = Date.from(Instant.parse("2026-03-19T01:02:03Z"));

        assertEquals("1 day, 1h2m3s", OutageUtil.formatDuration(firstTime, lastTime));
    }

    @Test
    public void formatDurationIncludesDaysWhenElapsedTimeExceedsTwoDays() {
        Date firstTime = Date.from(Instant.parse("2026-03-18T00:00:00Z"));
        Date lastTime = Date.from(Instant.parse("2026-03-20T01:02:03Z"));

        assertEquals("2 days, 1h2m3s", OutageUtil.formatDuration(firstTime, lastTime));
    }

    @Test
    public void formatDurationReturnsEmptyStringWhenInputIsNull() {
        Date now = Date.from(Instant.parse("2026-03-18T00:00:00Z"));

        assertEquals("", OutageUtil.formatDuration(null, now));
        assertEquals("", OutageUtil.formatDuration(now, null));
    }

    @Test
    public void formatDurationPreservesNegativeSignWhenLastTimeIsBeforeFirstTime() {
        Date firstTime = Date.from(Instant.parse("2026-03-19T00:00:01Z"));
        Date lastTime = Date.from(Instant.parse("2026-03-18T23:59:59Z"));

        assertEquals("0h0m-2s", OutageUtil.formatDuration(firstTime, lastTime));
    }
}
