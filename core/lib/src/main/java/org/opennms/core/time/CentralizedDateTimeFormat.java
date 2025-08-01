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
package org.opennms.core.time;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a centralized point to format date times consistently across OpenNMS.
 * Class is thread safe
 */
public class CentralizedDateTimeFormat {

    public final static String SYSTEM_PROPERTY_DATE_FORMAT = "org.opennms.ui.datettimeformat";
    public final static String SESSION_PROPERTY_TIMEZONE_ID = "org.opennms.ui.timezoneid";
    public final static String DEFAULT_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ssxxx";

    public static final Logger LOG = LoggerFactory.getLogger(CentralizedDateTimeFormat.class);

    private final DateTimeFormatter formatter;

    public CentralizedDateTimeFormat() {
        this.formatter = createFormatter();
    }

    private DateTimeFormatter createFormatter() {
        String format = getFormatPattern();
        DateTimeFormatter formatter;

        try {
            formatter = DateTimeFormatter.ofPattern(format);
        } catch (IllegalArgumentException e) {
            LOG.warn(String.format("Can not use System Property %s=%s as dateformat, will fall back to default." +
                                " Please see also java.time.format.DateTimeFormatter for the correct syntax",
                    SYSTEM_PROPERTY_DATE_FORMAT,
                    format),
                e);

            formatter = getDefaultFormatter();
        }

        return formatter;
    }

    public String getFormatPattern() {
        String format = System.getProperty(SYSTEM_PROPERTY_DATE_FORMAT);

        if (format == null) {
            format = DEFAULT_FORMAT_PATTERN;
        }

        return format;
    }

    private DateTimeFormatter getDefaultFormatter() {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_DATE)
                .appendLiteral('T')
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .appendOffsetId().toFormatter();
    }

    public String format(Instant instant, ZoneId timeZoneId) {
        if (instant == null) {
            return null;
        }

        if (timeZoneId == null) {
            timeZoneId = ZoneId.systemDefault();
        }

        return formatter.withZone(timeZoneId).format(instant);
    }

    public String format(Date date, ZoneId timeZoneId) {
        if (date == null) {
            return null;
        }

        return format(date.toInstant(), timeZoneId);
    }
}
