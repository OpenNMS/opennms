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
package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.Test;

public class SyslogYearCompleterTest {

    @Test
    public void shouldGuessTheYearRight() {
        test("0000-02-02T10:10:10", "2019-02-02T10:10:10", 2019);
        test("0000-01-01T01:01:10", "2019-12-31T23:59:10", 2020);
        test("0000-12-31T23:59:10", "2020-01-01T01:01:10", 2019);
        test("0000-07-01T01:01:10", "2019-06-30T23:59:10", 2019);
        test("0000-06-30T23:59:10", "2019-07-01T01:01:10", 2019);
        test("1970-06-30T23:59:10", "2019-07-01T01:01:10", 2019);
    }

    @Test
    public void shouldUseTheYearWhichWasGiven() {
        test("2000-04-04T10:10:10", "2019-04-04T10:10:10", 2000);
        test("2000-01-01T01:01:10", "2019-12-31T23:59:10", 2000);
        test("2000-12-31T23:59:10", "2020-01-01T01:01:10", 2000);
        test("2000-07-01T01:01:10", "2019-06-30T23:59:10", 2000);
        test("2000-06-30T23:59:10", "2019-07-01T01:01:10", 2000);
    }

    private void test(final String syslogDate, final String referenceDate, final Integer expectedYear) {
        LocalDateTime syslogDateTime = LocalDateTime.parse(syslogDate);
        SyslogMessage syslog = new SyslogMessage();
        if (syslogDateTime.getYear() > 0 &&  syslogDateTime.getYear() != 1970) {
            syslog.setYear(syslogDateTime.getYear());
        }
        syslog.setMonth(syslogDateTime.getMonth().getValue());
        syslog.setDayOfMonth(syslogDateTime.getDayOfMonth());
        syslog.setHourOfDay(syslogDateTime.getHour());
        syslog.setMinute(syslogDateTime.getMinute());
        Instant referenceDateTime = LocalDateTime.parse(referenceDate).atZone(ZoneId.systemDefault()).toInstant();
        SyslogYearCompleter.complete(syslog, referenceDateTime);
        assertEquals(expectedYear, syslog.getYear());
    }
}
