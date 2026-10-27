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
import java.util.Date;
import java.util.List;
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

    /** Markup characters, plus the characters that are not safe in a URL path segment. */
    private static final Pattern INVALID_NAME_CHARACTERS = Pattern.compile("[&<>\"`'/\\\\%?#]");

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
        if (!name.equals(name.trim())) {
            return "The on-call role name must not start or end with whitespace.";
        }
        if (INVALID_NAME_CHARACTERS.matcher(name).find()) {
            return "The on-call role name must not contain markup or the characters / \\ % ? #";
        }
        if (".".equals(name) || "..".equals(name)) {
            return "The on-call role name must not be a dot segment.";
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
            throw new UserMgmtValidationException(owner + field + " '" + value + "' must be a time like " + example(format) + ".");
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
