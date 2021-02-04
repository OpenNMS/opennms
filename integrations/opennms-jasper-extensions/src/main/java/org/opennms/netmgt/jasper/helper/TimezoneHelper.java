/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimezoneHelper {
	private static final Logger LOG = LoggerFactory.getLogger(TimezoneHelper.class);

	private static SimpleDateFormat getFormatter(final ZoneId zoneId) {
		LOG.debug("getFormatter zoneId= {}", zoneId);
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (zoneId != null) {
			sdf.setTimeZone(TimeZone.getTimeZone(zoneId));
		}
		return sdf;
	}

	private static String getDay(final Date date, final ZoneId zoneId) {
		LOG.debug("getDay date={}, zoneId={}", date, zoneId);
		return TimezoneHelper.getFormatter(zoneId).format(date);
	}

	private static Date rezoneDate(final Date date, final ZoneId zoneId) throws ParseException {
		LOG.debug("rezoneDate date={}, zoneId={}", date, zoneId);
		final String dayString = TimezoneHelper.getDay(date, ZoneId.systemDefault());
		final Date rezonedDate = TimezoneHelper.getFormatter(zoneId).parse(dayString);
		LOG.debug("rezoneDate dayString={}, rezonedDate={}", dayString, rezonedDate);
		return rezonedDate;
	}

	public static long getRezonedEpoch(final Date incomingDate, final ZoneId zoneId) throws ParseException {
		LOG.debug("getRezonedEpoch incomingDate={}, zoneId={}", incomingDate, zoneId);
		return TimezoneHelper.rezoneDate(incomingDate, zoneId).getTime();
	}

	public static String formatDate(final Date date, final ZoneId zoneId, final String pattern) throws ParseException {
		final Date rezoned = TimezoneHelper.rezoneDate(date, zoneId);
		final SimpleDateFormat format = new SimpleDateFormat(pattern);
		format.setTimeZone(TimeZone.getTimeZone(zoneId));
		return format.format(rezoned);
	}

	public static String now(final ZoneId zoneId, final String pattern) throws ParseException {
		final Date now = new Date();
		final SimpleDateFormat format = new SimpleDateFormat(pattern);
		format.setTimeZone(TimeZone.getTimeZone(zoneId));
		return format.format(now);
	}

    public static String getUtcOffset(final ZoneId zoneId, final Date referenceDate) {
        if (zoneId == null || referenceDate == null) {
            return "";
        }
        final ZoneOffset zoneOffset = referenceDate.toInstant().atZone(zoneId).getOffset();
        final String zoneOffsetString = zoneOffset.toString();
        final String utcOffset = String.format("UTC%s", zoneOffsetString.equals("Z") ? "+00:00" : zoneOffsetString);
        return utcOffset;
    }
}
