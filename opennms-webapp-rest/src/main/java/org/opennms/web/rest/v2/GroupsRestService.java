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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.web.api.Authentication;
import org.opennms.web.svclayer.api.GroupService;
import org.opennms.web.rest.v2.api.GroupsRestApi;
import org.opennms.web.rest.v2.model.GroupDto;
import org.opennms.web.rest.v2.model.GroupRenameRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Versioned group management on top of {@link GroupManager}: groups.xml
 * remains the system of record and hand-editing keeps working. Unlike the
 * legacy JSPs (which only hid the buttons), the Admin-group protections are
 * enforced here, server-side, and referential integrity with on-call roles is
 * handled deliberately: renames follow the roles' membership-group, deletes
 * are rejected while roles still reference the group.
 *
 * Mutations validate the full request up front and then apply it to a
 * detached copy of the stored group, so a rejected request can never leave
 * partial changes in the manager's shared in-memory state.
 */
@Component("groupsRestServiceV2")
public class GroupsRestService implements GroupsRestApi {

    // Check-then-act sequences synchronize on GroupFactory.class: user
    // mutations cascade into GroupManager (deleteUser/renameUser walk every
    // group), so a service-private lock cannot serialize the two v2 services
    // against each other.

    private static final Logger LOG = LoggerFactory.getLogger(GroupsRestService.class);

    /** The default group; the on-call role machinery references it. */
    private static final Set<String> PROTECTED_GROUPS = Set.of("Admin");

    /** Markup per the legacy controller, plus URL-path-segment safety. */
    private static final Pattern INVALID_NAME = Pattern.compile(".*[&<>\"`':/\\\\%?#\\s]+.*");

    private static final Pattern INVALID_COMMENTS = Pattern.compile(".*[&<>\"`']+.*");

    /** Same grammar as the users API. */
    private static final Pattern DUTY_SCHEDULE = Pattern.compile("^((?:Mo|Tu|We|Th|Fr|Sa|Su){1,7})(\\d{1,4})-(\\d{1,4})$");

    @Autowired
    private GroupManager m_groupManager;

    @Autowired
    private UserManager m_userManager;

    /** Handles the DB-side category authorizations on delete/rename. */
    @Autowired
    private GroupService m_groupService;

    @Override
    public Response listGroups(final SecurityContext securityContext) {
        assertAdmin(securityContext);
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                final List<GroupDto> groups = new ArrayList<>();
                for (final Group group : m_groupManager.getGroups().values()) {
                    groups.add(toDto(group));
                }
                groups.sort(Comparator.comparing(GroupDto::getName, String.CASE_INSENSITIVE_ORDER));
                return Response.ok(groups).build();
            }
        } catch (final Exception e) {
            return serverError("Can't read groups: %s", e);
        }
    }

    @Override
    public Response getGroup(final SecurityContext securityContext, final String name) {
        assertAdmin(securityContext);
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                final Group group = m_groupManager.getGroup(name);
                if (group == null) {
                    return Response.status(Status.NOT_FOUND).entity("Group " + name + " was not found.").build();
                }
                return Response.ok(toDto(group)).build();
            }
        } catch (final Exception e) {
            return serverError("Can't read group: %s", e);
        }
    }

    @Override
    public Response createGroup(final SecurityContext securityContext, final GroupDto dto) {
        assertAdmin(securityContext);
        if (dto == null || isBlank(dto.getName())) {
            return Response.status(Status.BAD_REQUEST).entity("A group name is required.").build();
        }
        final String name = dto.getName().trim();
        final String nameProblem = validateName(name);
        if (nameProblem != null) {
            return Response.status(Status.BAD_REQUEST).entity(nameProblem).build();
        }
        try {
            validateDtoFields(dto, null);
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                if (m_groupManager.hasGroup(name)) {
                    return Response.status(Status.BAD_REQUEST).entity("Group " + name + " already exists.").build();
                }
                final Group group = new Group();
                group.setName(name);
                applyDto(group, dto);
                m_groupManager.saveGroup(name, group);
            }
            LOG.info("Group {} created by {}", name, principal(securityContext));
            return Response.status(Status.CREATED).build();
        } catch (final Exception e) {
            return serverError("Can't create group: %s", e);
        }
    }

    @Override
    public Response updateGroup(final SecurityContext securityContext, final String name, final GroupDto dto) {
        assertAdmin(securityContext);
        if (dto == null) {
            return Response.status(Status.BAD_REQUEST).entity("A group body is required.").build();
        }
        if (dto.getName() != null && !name.equals(dto.getName())) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("The name in the body does not match the request path; use the rename endpoint to change names.").build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                final Group existing = m_groupManager.getGroup(name);
                if (existing == null) {
                    return Response.status(Status.NOT_FOUND).entity("Group " + name + " was not found.").build();
                }
                validateDtoFields(dto, existing);
                final Group updated = copyOf(existing);
                applyDto(updated, dto);
                m_groupManager.saveGroup(name, updated);
            }
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't update group: %s", e);
        }
    }

    @Override
    public Response renameGroup(final SecurityContext securityContext, final String name, final GroupRenameRequest request) {
        assertAdmin(securityContext);
        if (request == null || isBlank(request.getNewName())) {
            return Response.status(Status.BAD_REQUEST).entity("A new-name is required.").build();
        }
        if (PROTECTED_GROUPS.contains(name)) {
            return Response.status(Status.BAD_REQUEST).entity("The system group " + name + " cannot be renamed.").build();
        }
        final String newName = request.getNewName().trim();
        final String nameProblem = validateName(newName);
        if (nameProblem != null) {
            return Response.status(Status.BAD_REQUEST).entity(nameProblem).build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                if (!m_groupManager.hasGroup(name)) {
                    return Response.status(Status.NOT_FOUND).entity("Group " + name + " was not found.").build();
                }
                if (m_groupManager.hasGroup(newName)) {
                    return Response.status(Status.BAD_REQUEST).entity("Group " + newName + " already exists.").build();
                }
                // Pre-point the on-call roles at the new name in memory, then
                // let the rename's single save persist groups AND roles
                // together (GroupManager.renameGroup ends in saveGroups, which
                // serializes both). GroupService.renameGroup also migrates the
                // DB category authorizations, as the legacy page did.
                final List<Role> repointedRoles = new ArrayList<>();
                for (final Role role : m_groupManager.getRoles()) {
                    if (name.equals(role.getMembershipGroup())) {
                        role.setMembershipGroup(newName);
                        repointedRoles.add(role);
                    }
                }
                try {
                    m_groupService.renameGroup(name, newName);
                } catch (final Exception e) {
                    for (final Role role : repointedRoles) {
                        role.setMembershipGroup(name);
                    }
                    throw e;
                }
            }
            LOG.info("Group {} renamed to {} by {}", name, newName, principal(securityContext));
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't rename group: %s", e);
        }
    }

    @Override
    public Response deleteGroup(final SecurityContext securityContext, final String name) {
        assertAdmin(securityContext);
        if (PROTECTED_GROUPS.contains(name)) {
            return Response.status(Status.BAD_REQUEST).entity("The system group " + name + " cannot be deleted.").build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                if (!m_groupManager.hasGroup(name)) {
                    return Response.status(Status.NOT_FOUND).entity("Group " + name + " was not found.").build();
                }
                final List<String> referencingRoles = m_groupManager.getRoles().stream()
                        .filter(role -> name.equals(role.getMembershipGroup()))
                        .map(Role::getName)
                        .sorted()
                        .collect(Collectors.toList());
                if (!referencingRoles.isEmpty()) {
                    return Response.status(Status.BAD_REQUEST)
                            .entity("Group " + name + " is the membership group of on-call role(s) " + String.join(", ", referencingRoles)
                                    + "; delete or reassign those roles first.").build();
                }
                // GroupService.deleteGroup also clears the DB category
                // authorizations; without that, a future group reusing the
                // name would silently inherit the old authorizations
                m_groupService.deleteGroup(name);
            }
            LOG.info("Group {} deleted by {}", name, principal(securityContext));
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't delete group: %s", e);
        }
    }

    private static GroupDto toDto(final Group group) {
        final GroupDto dto = new GroupDto();
        dto.setName(group.getName());
        dto.setComments(group.getComments().orElse(null));
        dto.setUsers(new ArrayList<>(group.getUsers()));
        dto.setDutySchedules(new ArrayList<>(group.getDutySchedules()));
        return dto;
    }

    /** Detached copy so mutations never touch the manager's live object. */
    private static Group copyOf(final Group group) {
        final Group copy = new Group();
        copy.setName(group.getName());
        group.getDefaultMap().ifPresent(copy::setDefaultMap);
        group.getComments().ifPresent(copy::setComments);
        copy.setUsers(new ArrayList<>(group.getUsers()));
        copy.setDutySchedules(new ArrayList<>(group.getDutySchedules()));
        return copy;
    }

    /**
     * Validates every field of the request BEFORE anything is applied, so a
     * rejected request cannot leave partial state anywhere.
     */
    private void validateDtoFields(final GroupDto dto, final Group existing) throws Exception {
        if (dto.getComments() != null && INVALID_COMMENTS.matcher(dto.getComments()).matches()) {
            throw new IllegalArgumentException("The comments must not contain any HTML markup.");
        }
        if (dto.getUsers() != null) {
            final Set<String> seen = new LinkedHashSet<>();
            for (final String user : dto.getUsers()) {
                if (isBlank(user)) {
                    throw new IllegalArgumentException("Group members must not be blank.");
                }
                if (!seen.add(user)) {
                    throw new IllegalArgumentException("Duplicate group member: " + user);
                }
                if (!m_userManager.hasUser(user)) {
                    throw new IllegalArgumentException("Unknown user: " + user);
                }
            }
        }
        if (dto.getDutySchedules() != null) {
            // strings already stored on the record are preserved as-is so
            // hand-edited files never make a group uneditable; only entries
            // new to this request must pass validation
            final Set<String> preExisting = existing == null
                    ? Set.of() : new LinkedHashSet<>(existing.getDutySchedules());
            for (final String schedule : dto.getDutySchedules()) {
                if (!preExisting.contains(schedule)) {
                    validateDutySchedule(schedule);
                }
            }
        }
    }

    /**
     * Applies the pre-validated DTO. default-map is not exposed by the API and
     * survives untouched; list fields left out of the request body arrive as
     * null and are preserved. The user list order is kept exactly as sent —
     * it drives the notification escalation order.
     */
    private static void applyDto(final Group group, final GroupDto dto) {
        if (dto.getComments() != null) {
            group.setComments(trimToNull(dto.getComments()));
        }
        if (dto.getUsers() != null) {
            group.setUsers(new ArrayList<>(dto.getUsers()));
        }
        if (dto.getDutySchedules() != null) {
            group.setDutySchedules(new ArrayList<>(dto.getDutySchedules()));
        }
    }

    /** Returns a problem description, or null when the group name is acceptable. */
    private static String validateName(final String name) {
        if (INVALID_NAME.matcher(name).matches()) {
            return "The group name must not contain markup, whitespace, or the characters : / \\ % ? #";
        }
        if (".".equals(name) || "..".equals(name)) {
            return "The group name must not be a dot segment.";
        }
        return null;
    }

    private static void validateDutySchedule(final String schedule) {
        final Matcher matcher = schedule == null ? null : DUTY_SCHEDULE.matcher(schedule);
        if (matcher == null || !matcher.matches()) {
            throw new IllegalArgumentException("Invalid duty schedule '" + schedule + "': expected day tokens followed by begin-end military times, e.g. MoWeFr800-1700");
        }
        final int begin = Integer.parseInt(matcher.group(2));
        final int end = Integer.parseInt(matcher.group(3));
        if (begin > 2359 || end > 2359 || begin % 100 > 59 || end % 100 > 59) {
            throw new IllegalArgumentException("Invalid duty schedule '" + schedule + "': times must be military clock values between 0 and 2359");
        }
        // DutySchedule.isInSchedule compares within one calendar day, so an
        // overnight range can never match and would silently disable the
        // schedule; require two rows (e.g. MoTu2000-2359 + TuWe0-800) instead
        if (begin > end) {
            throw new IllegalArgumentException("Invalid duty schedule '" + schedule + "': the begin time must not be after the end time; split overnight coverage into two schedules");
        }
    }

    private static void assertAdmin(final SecurityContext securityContext) {
        if (securityContext == null || !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw new javax.ws.rs.WebApplicationException(
                    Response.status(Status.FORBIDDEN).entity("Group management requires the admin role.").build());
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
