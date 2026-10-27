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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.opennms.netmgt.config.UserMgmtJaxbMapper;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.VersionedTable;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.OnCallScheduleTime;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.web.rest.v2.api.OnCallRolesRestApi;
import org.opennms.web.rest.v2.model.OnCallRoleDto;
import org.opennms.web.rest.v2.model.OnCallRoleRenameRequest;
import org.opennms.web.rest.v2.model.OnCallScheduleDto;
import org.opennms.web.rest.v2.model.OnCallTimeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Versioned on-call role management on the role DAO. One document per role; a PUT that carries schedules
 * replaces them. Each request is one transaction: a write takes the user-management write lock first, so the
 * If-Match check is exact, and the in-memory managers reload once it commits.
 *
 * A schedule identical to one already stored on the role is grandfathered: it skips the format validation and
 * the membership check, so a role migrated from groups.xml stays editable. Every other schedule must be
 * valid for notifd and name a member of the membership group, which is all the JSP picker allows.
 *
 * Errors are thrown, never returned, so that a rejected request rolls back whatever it had done.
 */
@Component("onCallRolesRestServiceV2")
public class OnCallRolesRestService implements OnCallRolesRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(OnCallRolesRestService.class);

    private static final String WHAT = "On-call role management";

    @Autowired
    private UserMgmtRestSupport m_support;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Autowired
    private UserGroupDao m_userGroupDao;

    @Autowired
    private UserAccountDao m_userAccountDao;

    @Override
    @Transactional(readOnly = true)
    public Response listOnCallRoles(final SecurityContext securityContext) {
        m_support.assertAdmin(securityContext, WHAT);
        m_support.requireDatabaseStore();
        try {
            final List<OnCallRole> roles = new ArrayList<>(m_onCallRoleDao.findAll());
            roles.sort(Comparator.comparing(OnCallRole::getName, String.CASE_INSENSITIVE_ORDER));
            final List<OnCallRoleDto> dtos = new ArrayList<>();
            for (final OnCallRole role : roles) {
                dtos.add(toDto(role, role.getVersion()));
            }
            return Response.ok(dtos).build();
        } catch (final Exception e) {
            throw m_support.translate("read on-call roles", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Response getOnCallRole(final SecurityContext securityContext, final Request request, final String name) {
        m_support.assertAdmin(securityContext, WHAT);
        m_support.requireDatabaseStore();
        try {
            final OnCallRole role = load(name);
            final int version = role.getVersion();
            return Response.ok(toDto(role, version)).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("read on-call role", e);
        }
    }

    @Override
    @Transactional
    public Response createOnCallRole(final SecurityContext securityContext, final OnCallRoleDto dto) {
        m_support.assertAdmin(securityContext, WHAT);
        m_support.requireDatabaseStore();
        if (dto == null || StringUtils.isBlank(dto.getName())) {
            throw m_support.badRequest("An on-call role name is required.");
        }
        final String name = dto.getName();
        final String nameProblem = OnCallScheduleValidator.validateName(name);
        if (nameProblem != null) {
            throw m_support.badRequest(nameProblem);
        }
        if (StringUtils.isBlank(dto.getMembershipGroup())) {
            throw m_support.badRequest("A membership group is required.");
        }
        if (StringUtils.isBlank(dto.getSupervisor())) {
            throw m_support.badRequest("A supervisor is required.");
        }
        try {
            m_support.lockForWrite();
            if (m_onCallRoleDao.findByName(name) != null) {
                throw m_support.conflict("On-call role " + name + " already exists.");
            }
            final OnCallRole role = new OnCallRole(name, loadGroup(dto.getMembershipGroup()), loadSupervisor(dto.getSupervisor()));
            applyDto(role, dto);
            m_onCallRoleDao.saveOrUpdate(role);
            m_onCallRoleDao.flush();
            final int version = m_support.version(VersionedTable.oncall_roles, role.getId());
            LOG.info("On-call role {} created by {}", name, UserMgmtValidation.principal(securityContext));
            return Response.status(Status.CREATED).entity(toDto(role, version)).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("create on-call role", e);
        }
    }

    @Override
    @Transactional
    public Response updateOnCallRole(final SecurityContext securityContext, final HttpHeaders headers, final String name, final OnCallRoleDto dto) {
        m_support.assertAdmin(securityContext, WHAT);
        m_support.requireDatabaseStore();
        if (dto == null) {
            throw m_support.badRequest("An on-call role body is required.");
        }
        if (dto.getName() != null && !name.equals(dto.getName())) {
            throw m_support.badRequest("The name in the body does not match the request path; use the rename endpoint to change names.");
        }
        try {
            m_support.lockForWrite();
            final OnCallRole role = load(name, headers);
            m_support.checkIfMatch(headers, VersionedTable.oncall_roles, role.getId(), "On-call role " + name);
            // fields left out of the body are preserved
            if (dto.getMembershipGroup() != null) {
                role.setMembershipGroup(loadGroup(dto.getMembershipGroup()));
            }
            if (dto.getSupervisor() != null) {
                role.setSupervisor(loadSupervisor(dto.getSupervisor()));
            }
            applyDto(role, dto);
            m_onCallRoleDao.saveOrUpdate(role);
            m_onCallRoleDao.flush();
            final int version = m_support.version(VersionedTable.oncall_roles, role.getId());
            return Response.ok(toDto(role, version)).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("update on-call role", e);
        }
    }

    @Override
    @Transactional
    public Response renameOnCallRole(final SecurityContext securityContext, final HttpHeaders headers, final String name, final OnCallRoleRenameRequest body) {
        m_support.assertAdmin(securityContext, WHAT);
        m_support.requireDatabaseStore();
        if (body == null || StringUtils.isBlank(body.getNewName())) {
            throw m_support.badRequest("A newName is required.");
        }
        final String newName = body.getNewName();
        final String nameProblem = OnCallScheduleValidator.validateName(newName);
        if (nameProblem != null) {
            throw m_support.badRequest(nameProblem);
        }
        try {
            m_support.lockForWrite();
            final OnCallRole role = load(name, headers);
            m_support.checkIfMatch(headers, VersionedTable.oncall_roles, role.getId(), "On-call role " + name);
            if (m_onCallRoleDao.findByName(newName) != null) {
                throw m_support.conflict("On-call role " + newName + " already exists.");
            }
            // destination paths refer to roles by name and are not updated, as for group renames and the JSP page
            role.setName(newName);
            m_onCallRoleDao.saveOrUpdate(role);
            m_onCallRoleDao.flush();
            final int version = m_support.version(VersionedTable.oncall_roles, role.getId());
            LOG.info("On-call role {} renamed to {} by {}", name, newName, UserMgmtValidation.principal(securityContext));
            return Response.ok(toDto(role, version)).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("rename on-call role", e);
        }
    }

    @Override
    @Transactional
    public Response deleteOnCallRole(final SecurityContext securityContext, final HttpHeaders headers, final String name) {
        m_support.assertAdmin(securityContext, WHAT);
        m_support.requireDatabaseStore();
        try {
            m_support.lockForWrite();
            final OnCallRole role = load(name, headers);
            m_support.checkIfMatch(headers, VersionedTable.oncall_roles, role.getId(), "On-call role " + name);
            m_onCallRoleDao.delete(role);
            m_onCallRoleDao.flush();
            LOG.info("On-call role {} deleted by {}", name, UserMgmtValidation.principal(securityContext));
            return Response.noContent().build();
        } catch (final Exception e) {
            throw m_support.translate("delete on-call role", e);
        }
    }

    /** @throws javax.ws.rs.WebApplicationException 404 if there is no such object */
    private OnCallRole load(final String name) {
        return load(name, null);
    }

    /** For a write: a missing object is a 412 instead of a 404 when the request carries an If-Match. */
    private OnCallRole load(final String name, final HttpHeaders headers) {
        final OnCallRole role = m_onCallRoleDao.findByName(name);
        if (role == null) {
            throw m_support.notFoundOrPreconditionFailed(headers, "On-call role " + name, "On-call role " + name + " was not found.");
        }
        return role;
    }

    private UserGroup loadGroup(final String name) {
        final UserGroup group = m_userGroupDao.findByName(name);
        if (group == null) {
            throw new UserMgmtValidationException("Unknown membership group: " + name);
        }
        return group;
    }

    private UserAccount loadSupervisor(final String username) {
        final UserAccount user = m_userAccountDao.findByUsername(username);
        if (user == null) {
            throw new UserMgmtValidationException("Unknown supervisor: " + username);
        }
        return user;
    }

    /**
     * Applies the description and the schedules; the group and the supervisor are already set. Everything is
     * validated before the role is touched, so a rejected request leaves nothing half applied.
     */
    private void applyDto(final OnCallRole role, final OnCallRoleDto dto) {
        final String owner = "On-call role \"" + role.getName() + "\"";
        final List<OnCallSchedule> schedules = dto.getSchedules() == null ? null : buildSchedules(role, owner, dto.getSchedules());
        // a description left out of the body is preserved; an empty one is stored as null, as a manager would
        if (dto.getDescription() != null) {
            role.setDescription(StringUtils.trimToNull(dto.getDescription()));
        }
        // an unchanged list is left alone, so that no row is rewritten and the version stays put
        if (schedules != null && !key(role.getSchedules()).equals(key(schedules))) {
            role.getSchedules().clear();
            role.getSchedules().addAll(schedules);
        }
    }

    private List<OnCallSchedule> buildSchedules(final OnCallRole role, final String owner, final List<OnCallScheduleDto> dtos) {
        final Set<String> usernames = new HashSet<>();
        for (final OnCallScheduleDto dto : dtos) {
            if (dto == null) {
                throw new UserMgmtValidationException(owner + ": a schedule must not be null.");
            }
            if (StringUtils.isBlank(dto.getUser())) {
                throw new UserMgmtValidationException(owner + ": a schedule needs a user.");
            }
            usernames.add(dto.getUser());
        }
        final Map<String, UserAccount> accounts = new HashMap<>();
        if (!usernames.isEmpty()) {
            for (final UserAccount account : m_userAccountDao.findByUsernames(usernames)) {
                accounts.put(account.getUsername(), account);
            }
        }
        final List<Object> stored = key(role.getSchedules());
        final Set<String> members = role.getMembershipGroup().getMembers().stream()
                .map(UserAccount::getUsername).collect(Collectors.toSet());

        final List<OnCallSchedule> schedules = new ArrayList<>();
        for (final OnCallScheduleDto raw : dtos) {
            final OnCallScheduleDto dto = normalized(raw);
            final UserAccount user = accounts.get(dto.getUser());
            if (user == null) {
                throw new UserMgmtValidationException("Unknown user in a schedule: " + dto.getUser());
            }
            // validated and membership-checked unless it is one of the role's stored schedules
            if (!stored.contains(key(dto))) {
                OnCallScheduleValidator.validate(dto);
                if (!members.contains(dto.getUser())) {
                    throw new UserMgmtValidationException("Schedule user " + dto.getUser() + " is not a member of the group "
                            + role.getMembershipGroup().getName() + ".");
                }
            }
            final OnCallSchedule schedule = new OnCallSchedule(user, OnCallSchedule.Type.valueOf(dto.getType()));
            for (final OnCallTimeDto time : Objects.requireNonNullElse(dto.getTimes(), List.<OnCallTimeDto>of())) {
                final OnCallScheduleTime entity = new OnCallScheduleTime(
                        UserMgmtValidation.checkLength(owner, "schedule day", time.getDay(), UserMgmtJaxbMapper.DAY_LENGTH),
                        UserMgmtValidation.checkLength(owner, "schedule begin time", time.getBegins(), UserMgmtJaxbMapper.TIME_LENGTH),
                        UserMgmtValidation.checkLength(owner, "schedule end time", time.getEnds(), UserMgmtJaxbMapper.TIME_LENGTH));
                entity.setTimeId(UserMgmtValidation.checkLength(owner, "schedule time id", time.getId(), UserMgmtJaxbMapper.TIME_ID_LENGTH));
                schedule.getTimes().add(entity);
            }
            schedules.add(schedule);
        }
        return schedules;
    }

    /** Incoming day and id get the usual trimToNull; the times themselves are checked exactly as sent. */
    private static OnCallScheduleDto normalized(final OnCallScheduleDto dto) {
        final OnCallScheduleDto copy = new OnCallScheduleDto();
        copy.setUser(dto.getUser());
        copy.setType(dto.getType());
        if (dto.getTimes() != null) {
            final List<OnCallTimeDto> times = new ArrayList<>();
            for (final OnCallTimeDto time : dto.getTimes()) {
                if (time == null) {
                    times.add(null);
                    continue;
                }
                final OnCallTimeDto copyTime = new OnCallTimeDto();
                copyTime.setId(StringUtils.trimToNull(time.getId()));
                copyTime.setDay(StringUtils.trimToNull(time.getDay()));
                copyTime.setBegins(time.getBegins());
                copyTime.setEnds(time.getEnds());
                times.add(copyTime);
            }
            copy.setTimes(times);
        }
        return copy;
    }

    /** A comparable form of a stored or built schedule: user, type and every time's id, day, begins and ends. */
    private static List<Object> key(final List<OnCallSchedule> schedules) {
        final List<Object> keys = new ArrayList<>();
        for (final OnCallSchedule schedule : schedules) {
            final List<Object> times = new ArrayList<>();
            for (final OnCallScheduleTime time : schedule.getTimes()) {
                times.add(Arrays.asList(time.getTimeId(), time.getDay(), time.getBegins(), time.getEnds()));
            }
            keys.add(Arrays.asList(schedule.getUser().getUsername(), schedule.getType().name(), times));
        }
        return keys;
    }

    /** The same form for a requested schedule; an unusable one (no times, a bad type) never equals a stored one. */
    private static Object key(final OnCallScheduleDto dto) {
        final List<Object> times = new ArrayList<>();
        if (dto.getTimes() != null) {
            for (final OnCallTimeDto time : dto.getTimes()) {
                times.add(time == null ? null : Arrays.asList(time.getId(), time.getDay(), time.getBegins(), time.getEnds()));
            }
        }
        return Arrays.asList(dto.getUser(), dto.getType(), times);
    }

    private static OnCallRoleDto toDto(final OnCallRole role, final int version) {
        final OnCallRoleDto dto = new OnCallRoleDto();
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setMembershipGroup(role.getMembershipGroup().getName());
        dto.setSupervisor(role.getSupervisor().getUsername());
        dto.setVersion(version);
        final List<OnCallScheduleDto> schedules = new ArrayList<>();
        for (final OnCallSchedule schedule : role.getSchedules()) {
            final OnCallScheduleDto scheduleDto = new OnCallScheduleDto();
            scheduleDto.setUser(schedule.getUser().getUsername());
            scheduleDto.setType(schedule.getType().name());
            final List<OnCallTimeDto> times = new ArrayList<>();
            for (final OnCallScheduleTime time : schedule.getTimes()) {
                final OnCallTimeDto timeDto = new OnCallTimeDto();
                timeDto.setId(time.getTimeId());
                timeDto.setDay(time.getDay());
                timeDto.setBegins(time.getBegins());
                timeDto.setEnds(time.getEnds());
                times.add(timeDto);
            }
            scheduleDto.setTimes(times);
            schedules.add(scheduleDto);
        }
        dto.setSchedules(schedules);
        return dto;
    }
}
