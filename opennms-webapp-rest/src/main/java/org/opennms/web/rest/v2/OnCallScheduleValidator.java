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
package org.opennms.web.rest.v2;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.opennms.netmgt.config.BasicScheduleUtils;
import org.opennms.netmgt.config.UserMgmtJaxbMapper;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.web.rest.v2.model.OnCallScheduleDto;
import org.opennms.web.rest.v2.model.OnCallTimeDto;

/**
 * The rules for the schedules and the name of an on-call role. A schedule is accepted only if notifd can
 * evaluate it, so the times are read with the same code and formats ({@link BasicScheduleUtils#isValidTime}).
 */
public final class OnCallScheduleValidator {

    /**
     * Words of letters (any script, with their combining marks), decimal digits and {@code _ - . ( )}, separated by
     * single spaces, starting with a letter or digit. This leaves out markup, the characters that are not safe in a
     * URL path segment, dot segments, and whitespace or invisible characters that would make two names look alike.
     */
    private static final Pattern VALID_NAME = Pattern.compile("[\\p{L}\\p{Nd}][\\p{L}\\p{M}\\p{Nd}_.()-]*(?: [\\p{L}\\p{M}\\p{Nd}_.()-]+)*");

    private static final Pattern DAY_OF_MONTH = Pattern.compile("^[0-9]{1,2}$");

    private OnCallScheduleValidator() {
    }

    /**
     * @throws UserMgmtValidationException naming the schedule's user and the bad field
     */
    public static void validate(final OnCallScheduleDto schedule) {
        final String owner = "On-call schedule for " + schedule.getUser() + ": ";
        final OnCallSchedule.Type type = type(owner, schedule.getType());
        final List<OnCallTimeDto> times = schedule.getTimes();
        if (times == null || times.isEmpty()) {
            // groups.xsd: <time> has minOccurs="1" under <schedule>
            throw new UserMgmtValidationException(owner + "a schedule needs at least one time.");
        }
        final String format = type == OnCallSchedule.Type.specific ? BasicScheduleUtils.FORMAT1 : BasicScheduleUtils.FORMAT2;
        for (final OnCallTimeDto time : times) {
            if (time == null) {
                throw new UserMgmtValidationException(owner + "a time must not be null.");
            }
            validateDay(owner, type, time.getDay());
            checkTime(owner, "begins", time.getBegins(), format);
            checkTime(owner, "ends", time.getEnds(), format);
            if (!parse(time.getBegins(), format).before(parse(time.getEnds(), format))) {
                throw new UserMgmtValidationException(owner + "begins '" + time.getBegins() + "' must be before ends '" + time.getEnds() + "'.");
            }
        }
    }

    /** @return a description of what is wrong with the name of an on-call role, or null if it is acceptable */
    public static String validateName(final String name) {
        if (name == null || name.isEmpty()) {
            return "An on-call role name is required.";
        }
        if (!VALID_NAME.matcher(name).matches()) {
            return "The on-call role name must start with a letter or digit and contain only letters, digits, the characters"
                    + " _ - . ( ) and single spaces between words.";
        }
        if (name.codePointCount(0, name.length()) > UserMgmtJaxbMapper.ONCALL_ROLE_NAME_LENGTH) {
            return "The on-call role name must not be longer than " + UserMgmtJaxbMapper.ONCALL_ROLE_NAME_LENGTH + " characters.";
        }
        return null;
    }

    private static OnCallSchedule.Type type(final String owner, final String type) {
        try {
            return OnCallSchedule.Type.valueOf(type);
        } catch (final IllegalArgumentException | NullPointerException e) {
            throw new UserMgmtValidationException(owner + "the type '" + type + "' must be specific, daily, weekly or monthly.");
        }
    }

    private static void validateDay(final String owner, final OnCallSchedule.Type type, final String day) {
        switch (type) {
            case specific:
            case daily:
                if (day != null) {
                    throw new UserMgmtValidationException(owner + "a " + type + " time has no day, but this one has '" + day + "'.");
                }
                break;
            case weekly:
                if (BasicScheduleUtils.getDayOfWeekIndex(day) == null) {
                    throw new UserMgmtValidationException(owner + "the day '" + day + "' of a weekly time must be a weekday name, e.g. monday.");
                }
                break;
            case monthly:
                if (day == null || !DAY_OF_MONTH.matcher(day).matches() || Integer.parseInt(day) < 1 || Integer.parseInt(day) > 31) {
                    throw new UserMgmtValidationException(owner + "the day '" + day + "' of a monthly time must be a day of the month, 1 to 31.");
                }
                break;
            default:
                throw new IllegalStateException("Unhandled schedule type " + type);
        }
    }

    private static void checkTime(final String owner, final String field, final String value, final String format) {
        if (!BasicScheduleUtils.isValidTime(value, format)) {
            if (BasicScheduleUtils.FORMAT1.equals(format) && isSkippedLocalTime(value)) {
                throw new UserMgmtValidationException(owner + field + " '" + value + "' does not exist in the server's time zone ("
                        + ZoneId.systemDefault().getId() + "): the clocks skip it on that day, for example when daylight saving time starts.");
            }
            throw new UserMgmtValidationException(owner + field + " '" + value + "' must be a time like " + example(format) + ".");
        }
    }

    /**
     * True for a well-formed specific time that the server's time zone skips, such as 02:30 on the day daylight saving
     * time starts in America/New_York. The strict parser rejects it, though its format is right.
     */
    private static boolean isSkippedLocalTime(final String value) {
        try {
            final LocalDateTime local = LocalDateTime.parse(value,
                    DateTimeFormatter.ofPattern("dd-MMM-uuuu HH:mm:ss", Locale.getDefault()).withResolverStyle(ResolverStyle.STRICT));
            return ZoneId.systemDefault().getRules().getValidOffsets(local).isEmpty();
        } catch (final DateTimeParseException e) {
            return false;
        }
    }

    private static String example(final String format) {
        return BasicScheduleUtils.FORMAT1.equals(format) ? "21-Oct-2026 08:00:00 (dd-MMM-yyyy HH:mm:ss)" : "08:00:00 (HH:mm:ss)";
    }

    /** Only called on a value that {@link BasicScheduleUtils#isValidTime} has accepted. */
    private static Date parse(final String value, final String format) {
        final SimpleDateFormat parser = new SimpleDateFormat(format);
        parser.setLenient(false);
        return parser.parse(value, new ParsePosition(0));
    }
}
