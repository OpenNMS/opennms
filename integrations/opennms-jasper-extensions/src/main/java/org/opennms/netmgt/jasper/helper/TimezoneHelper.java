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
