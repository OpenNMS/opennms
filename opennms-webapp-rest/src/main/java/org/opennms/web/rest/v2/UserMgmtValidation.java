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
 * Validation rules shared by the v2 user-management services: the user id and group name characters, the comments
 * markup rule (users and groups) and the free-text limit (user and group comments, on-call and security role
 * descriptions), each with its grandfathering of stored values, the duty schedules (users and groups), and the column
 * length checks. The rules for on-call role names and schedules are in {@link OnCallScheduleValidator}.
 */
final class UserMgmtValidation {

    /**
     * The characters rejected in a user id or group name, and why:
     * <ul>
     * <li>{@code & < > " ` '}: markup, as the legacy servlets rejected it;</li>
     * <li>{@code :}: it separates the user id from the password in HTTP basic authentication;</li>
     * <li>{@code / \ % ? #}: the id is a URL path segment in every per-user and per-group endpoint;</li>
     * <li>whitespace ({@code \s}, {@code \p{Z}}): it breaks the references to the id from groups, on-call roles and
     * destination paths, and names that differ only in it look alike;</li>
     * <li>control and invisible characters ({@code \p{C}}): names that differ only in them look alike.</li>
     * </ul>
     * Dot segments are rejected separately, in {@link #validateName}.
     */
    private static final Pattern INVALID_NAME = Pattern.compile("[&<>\"`':/\\\\%?#\\s\\p{Z}\\p{C}]");

    private static final Pattern INVALID_COMMENTS = Pattern.compile("[&<>\"`']");

    /** Matches the schedule strings the duty evaluation parses, e.g. MoWeFr800-1700. */
    private static final Pattern DUTY_SCHEDULE = Pattern.compile("^((?:Mo|Tu|We|Th|Fr|Sa|Su){1,7})(\\d{1,4})-(\\d{1,4})$");

    /**
     * The most characters (code points, as for the columns) in a free text: user and group comments, on-call role and
     * security role descriptions. Their columns are unbounded text, so this limit is the API's own.
     */
    static final int FREE_TEXT_LENGTH = 4096;

    private UserMgmtValidation() {
    }

    /**
     * Returns a problem description, or null when the user id or group name is acceptable.
     *
     * @param label what the value is called in the message, e.g. "userId" or "group name"
     */
    static String validateName(final String name, final String label) {
        if (INVALID_NAME.matcher(name).find()) {
            return "The " + label + " must not contain markup, whitespace, control or invisible characters, or the characters : / \\ % ? #";
        }
        if (".".equals(name) || "..".equals(name)) {
            return "The " + label + " must not be a dot segment.";
        }
        return null;
    }

    /**
     * Checks user or group comments: no markup, and at most {@link #FREE_TEXT_LENGTH} characters (see
     * {@link #checkFreeText}). Comments that are the stored ones echoed back (modulo surrounding whitespace) pass:
     * stored values that today's rules would reject are grandfathered.
     *
     * @param stored the stored comments, or null when the object is being created or has none
     */
    static void checkComments(final String owner, final String incoming, final String stored) {
        if (incoming != null && INVALID_COMMENTS.matcher(incoming).find() && !textUnchanged(incoming, stored)) {
            throw new UserMgmtValidationException("The comments must not contain any HTML markup.");
        }
        checkFreeText(owner, "comment", incoming, stored);
    }

    /**
     * Rejects a free text (comments, a description) longer than {@link #FREE_TEXT_LENGTH} once trimmed, as it would be
     * stored, unless it is the stored value echoed back (modulo surrounding whitespace): a longer stored value, written
     * before the limit, is grandfathered like markup in the comments.
     *
     * @param stored the stored value, or null when the object is being created
     */
    static void checkFreeText(final String owner, final String field, final String incoming, final String stored) {
        if (incoming != null && !textUnchanged(incoming, stored)) {
            checkLength(owner, field, StringUtils.trimToNull(incoming), FREE_TEXT_LENGTH);
        }
    }

    /** True when the incoming text is the stored one modulo surrounding whitespace; the stored value is then left untouched. */
    static boolean textUnchanged(final String incoming, final String stored) {
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
