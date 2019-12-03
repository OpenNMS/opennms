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
