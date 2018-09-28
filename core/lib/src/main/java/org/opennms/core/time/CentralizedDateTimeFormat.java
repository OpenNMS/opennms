/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
    public final static String DEFAULT_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssxxx";

    public static final Logger LOG = LoggerFactory.getLogger(CentralizedDateTimeFormat.class);

    private final DateTimeFormatter formatter;

    public CentralizedDateTimeFormat(){
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
                format)
                    , e);
            formatter = getDefaultFormatter();
            }
        return formatter;
    }

    public String getFormatPattern(){
        String format = System.getProperty(SYSTEM_PROPERTY_DATE_FORMAT);
        if(format == null) {
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
        if(instant == null){
            return null;
        }
        if(timeZoneId == null){
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
