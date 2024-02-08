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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.opennms.core.time.YearGuesser;


public class SyslogYearCompleter {

    /**
     *  Adds the year (best guess) to a given syslog message if no year is present in the syslog message
     */
    public static void complete(SyslogMessage syslog) {
        complete(syslog, Instant.now());
    }

    /**
     * Adds the year (best guess) to a given syslog message if no year is present in the syslog message
     * Assumptions:
     * - syslog.getYear() == null, if not nothing to guess -> we won't do anything.
     */
    public static void complete(SyslogMessage syslog, Instant referenceTime) {
        if(syslog.getYear() != null) {
            return; // nothing to do, year is set already
        }

        // get a localized version of the referenceTime
        ZoneId zoneId = Optional.ofNullable(syslog.getZoneId()).orElse(ZoneId.systemDefault());
        LocalDateTime localReferenceTime = LocalDateTime.ofInstant(referenceTime, zoneId);

        // get LocalDateTime from Syslog
        LocalDateTime syslogDateTime = LocalDateTime.of(
                0, toValueOr1(syslog.getMonth()), toValueOr1(syslog.getDayOfMonth()),
                toValueOr0(syslog.getHourOfDay()), toValueOr0(syslog.getMinute()),
                toValueOr0(syslog.getSecond()), toValueOr0(syslog.getMillisecond()));
        int year = YearGuesser.guessYearForDate(syslogDateTime, localReferenceTime).getYear();
        syslog.setYear(year);
    }

    private static Integer toValueOr0(Integer integer) {
        return Optional.ofNullable(integer).orElse(0);
    }
    private static Integer toValueOr1(Integer integer) {
        return Optional.ofNullable(integer).orElse(1);
    }

}
