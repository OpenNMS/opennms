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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.opennms.core.utils.OwnedInterval;
import org.opennms.core.utils.OwnedIntervalSequence;
import org.opennms.core.utils.Owner;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.api.OnCallRolesRestApi;
import org.opennms.web.rest.v2.model.OnCallCalendarDto;
import org.opennms.web.rest.v2.model.OnCallCalendarDto.CalendarDayDto;
import org.opennms.web.rest.v2.model.OnCallCalendarDto.CalendarEntryDto;
import org.opennms.web.rest.v2.model.OnCallRoleDto;
import org.opennms.web.rest.v2.model.OnCallRoleDto.OnCallScheduleDto;
import org.opennms.web.rest.v2.model.OnCallRoleDto.OnCallTimeDto;
import org.opennms.web.rest.v2.model.OnCallRoleRenameRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Versioned on-call role management on top of {@link GroupManager}: the roles
 * section of groups.xml remains the system of record and hand-editing keeps
 * working. Schedule entries already stored on a role round-trip untouched;
 * entries new to a request are validated against the schema types and the
 * legacy editor's rules (member of the membership group, start before end).
 * The calendar endpoint reuses the exact interval resolution notifd uses, so
 * what the page shows is what notifd will do.
 */
@Component("onCallRolesRestServiceV2")
public class OnCallRolesRestService implements OnCallRolesRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(OnCallRolesRestService.class);

    // Check-then-act sequences synchronize on GroupFactory.class: roles live
    // in groups.xml, and user/group mutations cascade through GroupManager,
    // so all v2 services touching it share one monitor.

    /** Markup per the legacy servlets, plus URL-path-segment safety. */
    private static final Pattern INVALID_NAME = Pattern.compile("[&<>\"`':/\\\\%?#\\s]");

    private static final Set<String> SCHEDULE_TYPES = Set.of("specific", "daily", "weekly", "monthly");

    private static final Set<String> WEEKDAYS = Set.of("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday");

    /** groups.xsd time formats; Locale.ROOT matches the legacy writers. */
    private static final String DATE_TIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    private static final String TIME_FORMAT = "HH:mm:ss";

    @Autowired
    private GroupManager m_groupManager;

    @Autowired
    private UserManager m_userManager;

    @Override
    public Response listRoles(final SecurityContext securityContext) {
        assertAdmin(securityContext);
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                // getRole()/getRoles() serve the in-memory cache without checking file
                // freshness; update() reloads groups.xml if it was edited by hand.
                m_groupManager.update();
                final List<OnCallRoleDto> roles = new ArrayList<>();
                for (final Role role : m_groupManager.getRoles()) {
                    roles.add(toDto(role, false));
                }
                roles.sort(Comparator.comparing(OnCallRoleDto::getName, String.CASE_INSENSITIVE_ORDER));
                return Response.ok(roles).build();
            }
        } catch (final Exception e) {
            return serverError("Can't read on-call roles: %s", e);
        }
    }

    @Override
    public Response getRole(final SecurityContext securityContext, final String name) {
        assertAdmin(securityContext);
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                m_groupManager.update();
                final Role role = m_groupManager.getRole(name);
                if (role == null) {
                    return Response.status(Status.NOT_FOUND).entity("On-call role " + name + " was not found.").build();
                }
                return Response.ok(toDto(role, true)).build();
            }
        } catch (final Exception e) {
            return serverError("Can't read on-call role: %s", e);
        }
    }

    @Override
    public Response getCalendar(final SecurityContext securityContext, final String name, final Integer year, final Integer month) {
        assertAdmin(securityContext);
        if (year == null || month == null || month < 1 || month > 12 || year < 2000 || year > 2100) {
            return Response.status(Status.BAD_REQUEST).entity("year (2000-2100) and month (1-12) are required.").build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                m_groupManager.update();
                if (m_groupManager.getRole(name) == null) {
                    return Response.status(Status.NOT_FOUND).entity("On-call role " + name + " was not found.").build();
                }
                final OnCallCalendarDto calendar = new OnCallCalendarDto();
                calendar.setRole(name);
                calendar.setYear(year);
                calendar.setMonth(month);
                final YearMonth yearMonth = YearMonth.of(year, month);
                final ZoneId zone = ZoneId.systemDefault();
                calendar.setTimeZone(zone.getId());
                for (int dayOfMonth = 1; dayOfMonth <= yearMonth.lengthOfMonth(); dayOfMonth++) {
                    final LocalDate date = yearMonth.atDay(dayOfMonth);
                    final Date dayStart = Date.from(date.atStartOfDay(zone).toInstant());
                    final Date dayEnd = Date.from(date.plusDays(1).atStartOfDay(zone).toInstant());
                    final CalendarDayDto dayDto = new CalendarDayDto();
                    dayDto.setDate(date.toString());
                    final OwnedIntervalSequence intervals = m_groupManager.getRoleScheduleEntries(name, dayStart, dayEnd);
                    for (final java.util.Iterator<OwnedInterval> it = intervals.iterator(); it.hasNext();) {
                        final OwnedInterval interval = it.next();
                        final CalendarEntryDto entry = new CalendarEntryDto();
                        entry.setStart(interval.getStart().getTime());
                        entry.setEnd(interval.getEnd().getTime());
                        boolean supervisor = false;
                        final Set<String> users = new LinkedHashSet<>();
                        for (final Owner owner : interval.getOwners()) {
                            users.add(owner.getUser());
                            supervisor |= owner.isSupervisor();
                        }
                        entry.setUsers(new ArrayList<>(users));
                        entry.setSupervisor(supervisor);
                        dayDto.getEntries().add(entry);
                    }
                    calendar.getDays().add(dayDto);
                }
                return Response.ok(calendar).build();
            }
        } catch (final Exception e) {
            return serverError("Can't compute the on-call calendar: %s", e);
        }
    }

    @Override
    public Response createRole(final SecurityContext securityContext, final OnCallRoleDto dto) {
        assertAdmin(securityContext);
        if (dto == null || isBlank(dto.getName())) {
            return Response.status(Status.BAD_REQUEST).entity("A role name is required.").build();
        }
        final String name = dto.getName().trim();
        final String nameProblem = validateName(name);
        if (nameProblem != null) {
            return Response.status(Status.BAD_REQUEST).entity(nameProblem).build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                m_groupManager.update();
                if (m_groupManager.getRole(name) != null) {
                    return Response.status(Status.BAD_REQUEST).entity("On-call role " + name + " already exists.").build();
                }
                validateDtoFields(dto, null);
                final Role role = new Role();
                role.setName(name);
                applyDto(role, dto);
                m_groupManager.saveRole(role);
            }
            LOG.info("On-call role {} created by {}", name, principal(securityContext));
            return Response.status(Status.CREATED).build();
        } catch (final Exception e) {
            return serverError("Can't create on-call role: %s", e);
        }
    }

    @Override
    public Response updateRole(final SecurityContext securityContext, final String name, final OnCallRoleDto dto) {
        assertAdmin(securityContext);
        if (dto == null) {
            return Response.status(Status.BAD_REQUEST).entity("A role body is required.").build();
        }
        if (dto.getName() != null && !name.equals(dto.getName())) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("The name in the body does not match the request path; use the rename endpoint to change names.").build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                m_groupManager.update();
                final Role existing = m_groupManager.getRole(name);
                if (existing == null) {
                    return Response.status(Status.NOT_FOUND).entity("On-call role " + name + " was not found.").build();
                }
                validateDtoFields(dto, existing);
                final Role updated = copyOf(existing);
                applyDto(updated, dto);
                m_groupManager.saveRole(updated);
            }
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't update on-call role: %s", e);
        }
    }

    @Override
    public Response renameRole(final SecurityContext securityContext, final String name, final OnCallRoleRenameRequest request) {
        assertAdmin(securityContext);
        if (request == null || isBlank(request.getNewName())) {
            return Response.status(Status.BAD_REQUEST).entity("A new-name is required.").build();
        }
        final String newName = request.getNewName().trim();
        final String nameProblem = validateName(newName);
        if (nameProblem != null) {
            return Response.status(Status.BAD_REQUEST).entity(nameProblem).build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                m_groupManager.update();
                final Role existing = m_groupManager.getRole(name);
                if (existing == null) {
                    return Response.status(Status.NOT_FOUND).entity("On-call role " + name + " was not found.").build();
                }
                if (m_groupManager.getRole(newName) != null) {
                    return Response.status(Status.BAD_REQUEST).entity("On-call role " + newName + " already exists.").build();
                }
                // save the copy under the new name FIRST so a failure between
                // the two writes leaves both roles present, never neither
                final Role renamed = copyOf(existing);
                renamed.setName(newName);
                m_groupManager.saveRole(renamed);
                m_groupManager.deleteRole(name);
            }
            LOG.info("On-call role {} renamed to {} by {}", name, newName, principal(securityContext));
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't rename on-call role: %s", e);
        }
    }

    @Override
    public Response deleteRole(final SecurityContext securityContext, final String name) {
        assertAdmin(securityContext);
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                m_groupManager.update();
                if (m_groupManager.getRole(name) == null) {
                    return Response.status(Status.NOT_FOUND).entity("On-call role " + name + " was not found.").build();
                }
                m_groupManager.deleteRole(name);
            }
            LOG.info("On-call role {} deleted by {}", name, principal(securityContext));
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't delete on-call role: %s", e);
        }
    }

    private OnCallRoleDto toDto(final Role role, final boolean includeSchedules) throws Exception {
        final OnCallRoleDto dto = new OnCallRoleDto();
        dto.setName(role.getName());
        dto.setMembershipGroup(role.getMembershipGroup());
        dto.setSupervisor(role.getSupervisor());
        dto.setDescription(role.getDescription().orElse(null));
        // hand-edited day values the runtime can't resolve (e.g. day="mon")
        // make schedule evaluation throw; one bad role must not 500 the list
        List<String> onCallUsers = List.of();
        try {
            final String[] onCall = m_userManager.getUsersScheduledForRole(role.getName(), new Date());
            onCallUsers = onCall == null ? List.of() : List.of(onCall);
        } catch (final Exception e) {
            LOG.warn("Can't evaluate the schedule of on-call role {}: {}", role.getName(), e.toString());
            // an unevaluable schedule must be distinguishable from an idle
            // one: notifd cannot resolve this rota either
            dto.setScheduleError("The schedule cannot be evaluated; check this role's entries in groups.xml.");
        }
        dto.setCurrentlyOnCall(onCallUsers);
        if (includeSchedules) {
            final List<OnCallScheduleDto> schedules = new ArrayList<>();
            for (final Schedule schedule : role.getSchedules()) {
                schedules.add(toScheduleDto(schedule));
            }
            dto.setSchedules(schedules);
        }
        return dto;
    }

    private static OnCallScheduleDto toScheduleDto(final Schedule schedule) {
        final OnCallScheduleDto dto = new OnCallScheduleDto();
        dto.setUser(schedule.getName());
        dto.setType(schedule.getType());
        final List<OnCallTimeDto> times = new ArrayList<>();
        for (final Time time : schedule.getTimes()) {
            final OnCallTimeDto timeDto = new OnCallTimeDto();
            timeDto.setId(time.getId().orElse(null));
            timeDto.setDay(time.getDay().orElse(null));
            timeDto.setBegins(time.getBegins());
            timeDto.setEnds(time.getEnds());
            times.add(timeDto);
        }
        dto.setTimes(times);
        return dto;
    }

    /** Detached deep copy so mutations never touch the manager's live object. */
    private static Role copyOf(final Role role) {
        final Role copy = new Role();
        copy.setName(role.getName());
        copy.setMembershipGroup(role.getMembershipGroup());
        copy.setSupervisor(role.getSupervisor());
        role.getDescription().ifPresent(copy::setDescription);
        for (final Schedule schedule : role.getSchedules()) {
            copy.getSchedules().add(copyOf(schedule));
        }
        return copy;
    }

    private static Schedule copyOf(final Schedule schedule) {
        final Schedule copy = new Schedule();
        copy.setName(schedule.getName());
        copy.setType(schedule.getType());
        for (final Time time : schedule.getTimes()) {
            final Time timeCopy = new Time();
            time.getId().ifPresent(timeCopy::setId);
            time.getDay().ifPresent(timeCopy::setDay);
            timeCopy.setBegins(time.getBegins());
            timeCopy.setEnds(time.getEnds());
            copy.getTimes().add(timeCopy);
        }
        return copy;
    }

    /**
     * Validates everything BEFORE anything is applied. Schedules already
     * stored on the role (matched by canonical form) round-trip untouched so
     * hand-edited files never make a role uneditable; schedules new to the
     * request must pass the schema and legacy-editor rules. On create, and
     * whenever supervisor/membership-group are being set, those must exist.
     */
    private void validateDtoFields(final OnCallRoleDto dto, final Role existing) throws Exception {
        // on create both identity fields are required; on update each is
        // validated independently so one can be changed without the other
        final String supervisor = trimToNull(dto.getSupervisor());
        if (existing == null || dto.getSupervisor() != null) {
            if (supervisor == null || !m_userManager.hasUser(supervisor)) {
                throw new IllegalArgumentException("A supervisor is required and must be an existing user.");
            }
        }
        final String membershipGroup = trimToNull(dto.getMembershipGroup());
        if (existing == null || dto.getMembershipGroup() != null) {
            if (membershipGroup == null || !m_groupManager.hasGroup(membershipGroup)) {
                throw new IllegalArgumentException("A membership-group is required and must be an existing group.");
            }
        }
        // A membership-group change can invalidate schedules that were valid
        // under the old group, so the "already on the role" exemption must not
        // apply to it, and schedules left unchanged still have to be re-checked.
        final boolean membershipChanging = existing != null && membershipGroup != null
                && !membershipGroup.equals(existing.getMembershipGroup());
        final String scheduleGroup = membershipGroup != null
                ? membershipGroup
                : existing == null ? null : existing.getMembershipGroup();
        final Group group = scheduleGroup == null ? null : m_groupManager.getGroup(scheduleGroup);
        final Set<String> members = group == null ? Set.of() : new LinkedHashSet<>(group.getUsers());
        if (dto.getSchedules() != null) {
            final Set<String> preExisting = (existing == null || membershipChanging) ? Set.of()
                    : existing.getSchedules().stream().map(OnCallRolesRestService::canonical).collect(Collectors.toSet());
            for (final OnCallScheduleDto schedule : dto.getSchedules()) {
                if (!preExisting.contains(canonical(schedule))) {
                    validateSchedule(schedule, members);
                }
            }
        } else if (membershipChanging) {
            // membership group changed without resubmitting schedules: the
            // retained schedules' users must still belong to the new group, or
            // notifd would keep paging users who are no longer members while the
            // new group's members get nothing
            for (final Schedule schedule : existing.getSchedules()) {
                final String user = trimToNull(schedule.getName());
                if (user != null && !members.contains(user)) {
                    throw new IllegalArgumentException("Schedule user " + user
                            + " is not a member of the new membership group " + membershipGroup
                            + "; update or remove the schedule before changing the group.");
                }
            }
        }
    }

    private void validateSchedule(final OnCallScheduleDto schedule, final Set<String> members) {
        final String user = trimToNull(schedule.getUser());
        if (user == null) {
            throw new IllegalArgumentException("Every schedule requires a user.");
        }
        if (!members.contains(user)) {
            throw new IllegalArgumentException("Schedule user " + user + " is not a member of the role's membership group.");
        }
        final String type = trimToNull(schedule.getType());
        if (type == null || !SCHEDULE_TYPES.contains(type)) {
            throw new IllegalArgumentException("Schedule type must be one of " + SCHEDULE_TYPES + ".");
        }
        if (schedule.getTimes() == null || schedule.getTimes().isEmpty()) {
            throw new IllegalArgumentException("Every schedule requires at least one time entry.");
        }
        for (final OnCallTimeDto time : schedule.getTimes()) {
            validateTime(type, time);
        }
    }

    /**
     * Validates AND canonicalizes: begins/ends are rewritten into the exact
     * zero-padded form the runtime dispatches on (setOutCalTime switches on
     * string length 20/8 and parses with Locale.ROOT), and weekly days are
     * lowercased. Accepting anything looser would store entries notifd
     * silently ignores.
     */
    private void validateTime(final String type, final OnCallTimeDto time) {
        final String day = trimToNull(time.getDay());
        switch (type) {
            case "specific":
                if (day != null) {
                    throw new IllegalArgumentException("A specific schedule must not carry a day.");
                }
                final Date begins = parseDate(DATE_TIME_FORMAT, time.getBegins());
                final Date ends = parseDate(DATE_TIME_FORMAT, time.getEnds());
                if (!begins.before(ends)) {
                    throw new IllegalArgumentException("The start time must be before the end time.");
                }
                time.setBegins(strictFormat(DATE_TIME_FORMAT, Locale.ROOT).format(begins));
                time.setEnds(strictFormat(DATE_TIME_FORMAT, Locale.ROOT).format(ends));
                return;
            case "daily":
                if (day != null) {
                    throw new IllegalArgumentException("A daily schedule must not carry a day.");
                }
                break;
            case "weekly":
                if (day == null || !WEEKDAYS.contains(day.toLowerCase(Locale.ROOT))) {
                    throw new IllegalArgumentException("A weekly schedule requires a weekday name as its day.");
                }
                time.setDay(day.toLowerCase(Locale.ROOT));
                break;
            case "monthly":
                if (day == null || !day.matches("0?[1-9]|[1-2][0-9]|3[0-1]")) {
                    throw new IllegalArgumentException("A monthly schedule requires a day of month (1-31).");
                }
                // the groups.xsd day pattern forbids leading zeros
                time.setDay(String.valueOf(Integer.parseInt(day)));
                break;
            default:
                throw new IllegalArgumentException("Unsupported schedule type: " + type);
        }
        final Date start = parseDate(TIME_FORMAT, time.getBegins());
        final Date end = parseDate(TIME_FORMAT, time.getEnds());
        if (!start.before(end)) {
            throw new IllegalArgumentException("The start time must be before the end time.");
        }
        time.setBegins(strictFormat(TIME_FORMAT, Locale.ROOT).format(start));
        time.setEnds(strictFormat(TIME_FORMAT, Locale.ROOT).format(end));
    }

    private static Date parseDate(final String format, final String value) {
        if (value == null) {
            throw new IllegalArgumentException("Schedule times require begins and ends values.");
        }
        try {
            return strictFormat(format, Locale.ROOT).parse(value.trim());
        } catch (final ParseException e) {
            throw new IllegalArgumentException("Invalid schedule time '" + value + "': expected the format " + format);
        }
    }

    private static SimpleDateFormat strictFormat(final String format, final Locale locale) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale);
        dateFormat.setLenient(false);
        return dateFormat;
    }

    /** Canonical form used to recognize schedules that were already stored. */
    private static String canonical(final Schedule schedule) {
        return canonical(schedule.getName(), schedule.getType(),
                schedule.getTimes().stream()
                        .map(t -> (t.getDay().orElse("") + "|" + t.getBegins() + "|" + t.getEnds()))
                        .collect(Collectors.toList()));
    }

    private static String canonical(final OnCallScheduleDto schedule) {
        return canonical(schedule.getUser(), schedule.getType(),
                schedule.getTimes() == null ? List.of()
                        : schedule.getTimes().stream()
                                .map(t -> ((t.getDay() == null ? "" : t.getDay()) + "|" + t.getBegins() + "|" + t.getEnds()))
                                .collect(Collectors.toList()));
    }

    private static String canonical(final String user, final String type, final List<String> times) {
        return user + "//" + type + "//" + String.join(";;", times);
    }

    private void applyDto(final Role role, final OnCallRoleDto dto) {
        if (dto.getSupervisor() != null) {
            role.setSupervisor(dto.getSupervisor().trim());
        }
        if (dto.getMembershipGroup() != null) {
            role.setMembershipGroup(dto.getMembershipGroup().trim());
        }
        if (dto.getDescription() != null) {
            role.setDescription(trimToNull(dto.getDescription()));
        }
        if (dto.getSchedules() != null) {
            role.getSchedules().clear();
            for (final OnCallScheduleDto scheduleDto : dto.getSchedules()) {
                final Schedule schedule = new Schedule();
                schedule.setName(scheduleDto.getUser().trim());
                schedule.setType(scheduleDto.getType().trim());
                for (final OnCallTimeDto timeDto : scheduleDto.getTimes()) {
                    final Time time = new Time();
                    if (trimToNull(timeDto.getId()) != null) {
                        time.setId(timeDto.getId().trim());
                    }
                    if (trimToNull(timeDto.getDay()) != null) {
                        time.setDay(timeDto.getDay().trim());
                    }
                    time.setBegins(timeDto.getBegins().trim());
                    time.setEnds(timeDto.getEnds().trim());
                    schedule.getTimes().add(time);
                }
                role.getSchedules().add(schedule);
            }
        }
    }

    private static String validateName(final String name) {
        if (INVALID_NAME.matcher(name).find()) {
            return "The role name must not contain markup, whitespace, or the characters : / \\ % ? #";
        }
        if (".".equals(name) || "..".equals(name)) {
            return "The role name must not be a dot segment.";
        }
        return null;
    }

    private static void assertAdmin(final SecurityContext securityContext) {
        if (securityContext == null || !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw new javax.ws.rs.WebApplicationException(
                    Response.status(Status.FORBIDDEN).entity("On-call role management requires the admin role.").build());
        }
    }

    private static String principal(final SecurityContext securityContext) {
        return securityContext.getUserPrincipal() == null ? "?" : securityContext.getUserPrincipal().getName();
    }

    private Response serverError(final String format, final Exception e) {
        if (e instanceof IllegalArgumentException) {
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        LOG.error(String.format(format, e.getMessage()), e);
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(String.format(format, e.getMessage())).build();
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
