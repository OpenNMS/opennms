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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.opennms.netmgt.config.UserMgmtJaxbMapper;

/**
 * Validation rules shared by the v2 user and group services (and the duty-schedule rule shared with
 * nothing else): the id/name characters, the comments rule with its grandfathering, and duty schedules.
 */
final class UserMgmtValidation {

    private static final Pattern INVALID_NAME = Pattern.compile("[&<>\"`':/\\\\%?#\\s]");

    private static final Pattern INVALID_COMMENTS = Pattern.compile("[&<>\"`']");

    /** Matches the schedule strings the duty evaluation parses, e.g. MoWeFr800-1700. */
    private static final Pattern DUTY_SCHEDULE = Pattern.compile("^((?:Mo|Tu|We|Th|Fr|Sa|Su){1,7})(\\d{1,4})-(\\d{1,4})$");

    private UserMgmtValidation() {
    }

    /**
     * Returns a problem description, or null when the user id or group name is acceptable.
     *
     * @param label what the value is called in the message, e.g. "userId" or "group name"
     */
    static String validateName(final String name, final String label) {
        if (INVALID_NAME.matcher(name).find()) {
            return "The " + label + " must not contain markup, whitespace, or the characters : / \\ % ? #";
        }
        if (".".equals(name) || "..".equals(name)) {
            return "The " + label + " must not be a dot segment.";
        }
        return null;
    }

    /**
     * Rejects markup in incoming comments, unless they are the stored comments echoed back (modulo
     * surrounding whitespace): stored values that today's rules would reject are grandfathered.
     *
     * @param stored the stored comments, or null when the object is being created
     */
    static void checkComments(final String incoming, final String stored, final boolean existing) {
        if (incoming != null && INVALID_COMMENTS.matcher(incoming).find()
                && (!existing || !commentsUnchanged(incoming, stored))) {
            throw new UserMgmtValidationException("The comments must not contain any HTML markup.");
        }
    }

    /** True when the incoming comments are the stored ones modulo surrounding whitespace; the stored value is then left untouched. */
    static boolean commentsUnchanged(final String incoming, final String stored) {
        return StringUtils.equals(StringUtils.trimToNull(incoming), StringUtils.trimToNull(stored));
    }

    /**
     * Duty schedules are stored as strings like MoWeFr800-1700 and parsed with
     * unchecked exceptions all over notifd/group scheduling — an invalid string
     * saved here would break duty evaluation at runtime.
     */
    static void validateDutySchedule(final String schedule) {
        final Matcher matcher = schedule == null ? null : DUTY_SCHEDULE.matcher(schedule);
        if (matcher == null || !matcher.matches()) {
            throw new UserMgmtValidationException("Invalid duty schedule '" + schedule + "': expected day tokens followed by begin-end military times, e.g. MoWeFr800-1700");
        }
        final int begin = Integer.parseInt(matcher.group(2));
        final int end = Integer.parseInt(matcher.group(3));
        if (begin > 2359 || end > 2359 || begin % 100 > 59 || end % 100 > 59) {
            throw new UserMgmtValidationException("Invalid duty schedule '" + schedule + "': times must be military clock values between 0 and 2359");
        }
        // DutySchedule.isInSchedule compares within one calendar day, so an
        // overnight range can never match and would silently disable the
        // schedule; require two rows (e.g. MoTu2000-2359 + TuWe0-800) instead
        if (begin > end) {
            throw new UserMgmtValidationException("Invalid duty schedule '" + schedule + "': the begin time must not be after the end time; split overnight coverage into two schedules");
        }
    }

    /**
     * Validates the duty schedules of a request. Entries already stored on the record are kept as they are, so
     * that hand-edited or migrated data never makes the record uneditable; only the entries new to the request
     * must pass {@link #validateDutySchedule}. Every entry must fit its column.
     *
     * @param stored the schedules stored on the record, or null when the object is being created
     * @throws UserMgmtValidationException on a null or blank entry, an invalid new entry or one that is too long
     */
    static void validateDutySchedules(final String owner, final List<String> requested, final Collection<String> stored) {
        final Set<String> preExisting = stored == null ? Set.of() : new LinkedHashSet<>(stored);
        for (final String schedule : requested) {
            if (StringUtils.isBlank(schedule)) {
                throw new UserMgmtValidationException("Duty schedules must not be blank.");
            }
            if (!preExisting.contains(schedule)) {
                validateDutySchedule(schedule);
            }
            checkLength(owner, "duty schedule", schedule, UserMgmtJaxbMapper.DUTY_SCHEDULE_LENGTH);
        }
    }

    /**
     * Replaces the contents of a persistent list, but only if the requested list differs, so that no row is
     * rewritten and the version stays put. A null request leaves the list alone.
     */
    static <T> void replaceIfChanged(final List<T> current, final List<T> requested) {
        if (requested != null && !current.equals(requested)) {
            final List<T> copy = new ArrayList<>(requested);
            current.clear();
            current.addAll(copy);
        }
    }

    /**
     * @return the value, if it fits a column of max characters
     * @throws UserMgmtValidationException if it does not
     */
    static String checkLength(final String owner, final String field, final String value, final int max) {
        try {
            return UserMgmtJaxbMapper.checkLength(owner, field, value, max);
        } catch (final IllegalArgumentException e) {
            throw new UserMgmtValidationException(e.getMessage());
        }
    }

    static String principal(final SecurityContext securityContext) {
        return securityContext.getUserPrincipal() == null ? "?" : securityContext.getUserPrincipal().getName();
    }
}
