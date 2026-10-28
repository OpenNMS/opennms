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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.UserMgmtJaxbMapper;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.VersionedTable;
import org.opennms.netmgt.model.usermgmt.ContactType;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserContact;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.api.UsersRestApi;
import org.opennms.web.rest.v2.model.UserDto;
import org.opennms.web.rest.v2.model.UserPasswordRequest;
import org.opennms.web.rest.v2.model.UserRenameRequest;
import org.opennms.web.rest.v2.model.UserWriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Versioned user management on the user DAOs. Each request is one transaction: a write takes the
 * user-management write lock first, so the If-Match check is exact, and the in-memory managers reload
 * once it commits. Unlike the legacy JSPs (which only hid the buttons), the admin/rtc delete and rename
 * protections are enforced here, server-side. The password hash is never serialized.
 *
 * Errors are thrown, never returned, so that a rejected request rolls back whatever it had done.
 */
@Component("usersRestServiceV2")
public class UsersRestService implements UsersRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(UsersRestService.class);

    /** System accounts that must not be deleted or renamed. */
    private static final Set<String> PROTECTED_USERS = Set.of("admin", "rtc");

    @Autowired
    private UserMgmtRestSupport m_support;

    @Autowired
    private UserAccountDao m_userAccountDao;

    @Autowired
    private SecurityRoleDao m_securityRoleDao;

    /** For the checks on delete: the roles the user supervises, and a role named like the user. */
    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    /** For the destination path check on delete: a group named like the user. */
    @Autowired
    private UserGroupDao m_userGroupDao;

    @Override
    @Transactional(readOnly = true)
    public Response listUsers(final SecurityContext securityContext) {
        m_support.assertAdmin(securityContext, "User management");
        m_support.requireDatabaseStore();
        try {
            final List<UserDto> users = new ArrayList<>();
            for (final UserAccount user : m_userAccountDao.findAll()) {
                users.add(toDto(user, user.getVersion()));
            }
            users.sort(Comparator.comparing(UserDto::getUserId, String.CASE_INSENSITIVE_ORDER));
            return Response.ok(users).build();
        } catch (final Exception e) {
            throw m_support.translate("read users", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Response getUser(final SecurityContext securityContext, final Request request, final String userId) {
        m_support.assertAdmin(securityContext, "User management");
        m_support.requireDatabaseStore();
        try {
            final UserAccount user = load(userId);
            // the version of the row read for the body, not a second read: a newer version with an older body would let
            // an old copy pass If-Match (lazily loaded children can only be newer, which at worst gives a false 412)
            final int version = user.getVersion();
            return Response.ok(toDto(user, version)).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("read user", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Response listAvailableRoles(final SecurityContext securityContext) {
        m_support.assertAdmin(securityContext, "User management");
        m_support.requireDatabaseStore();
        try {
            final List<String> roles = m_securityRoleDao.findAll().stream()
                    .map(SecurityRole::getName)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
            return Response.ok(roles).build();
        } catch (final Exception e) {
            throw m_support.translate("read roles", e);
        }
    }

    @Override
    @Transactional
    public Response createUser(final SecurityContext securityContext, final UserWriteRequest request) {
        m_support.assertAdmin(securityContext, "User management");
        m_support.requireDatabaseStore();
        if (request == null || StringUtils.isBlank(request.getUserId())) {
            throw m_support.badRequest("A userId is required.");
        }
        final String userId = request.getUserId().trim();
        final String userIdProblem = UserMgmtValidation.validateName(userId, "userId");
        if (userIdProblem != null) {
            throw m_support.badRequest(userIdProblem);
        }
        if (StringUtils.isBlank(request.getPassword())) {
            throw m_support.badRequest("A password is required.");
        }
        try {
            m_support.lockForWrite();
            if (m_userAccountDao.findByUsername(userId) != null) {
                throw m_support.conflict("User " + userId + " already exists.");
            }
            final UserAccount user = new UserAccount(userId);
            UserMgmtValidation.checkLength(owner(userId), "user name", userId, UserMgmtJaxbMapper.USERNAME_LENGTH);
            final List<SecurityRole> roles = validateDtoFields(request, null);
            user.setPasswordHash(UserManager.hashPassword(request.getPassword()));
            user.setPasswordSalted(true);
            applyDto(user, request, roles);
            m_userAccountDao.saveOrUpdate(user);
            m_userAccountDao.flush();
            final int version = m_support.version(VersionedTable.users, user.getId());
            LOG.info("User {} created by {}", userId, UserMgmtValidation.principal(securityContext));
            return Response.status(Status.CREATED).entity(toDto(user, version)).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("create user", e);
        }
    }

    @Override
    @Transactional
    public Response updateUser(final SecurityContext securityContext, final HttpHeaders headers, final String userId, final UserDto dto) {
        m_support.assertAdmin(securityContext, "User management");
        m_support.requireDatabaseStore();
        if (dto == null) {
            throw m_support.badRequest("A user body is required.");
        }
        if (dto.getUserId() != null && !userId.equals(dto.getUserId())) {
            throw m_support.badRequest("The userId in the body does not match the request path; use the rename endpoint to change ids.");
        }
        // stripping ROLE_ADMIN from the admin account would lock every
        // administrator out of the web UI until the roles are repaired by hand
        if ("admin".equals(userId) && dto.getRoles() != null && !dto.getRoles().contains(Authentication.ROLE_ADMIN)) {
            throw m_support.badRequest("The admin user must keep the " + Authentication.ROLE_ADMIN + " role.");
        }
        try {
            m_support.lockForWrite();
            final UserAccount user = load(userId, headers);
            m_support.checkIfMatch(headers, VersionedTable.users, user.getId(), "User " + userId);
            final List<SecurityRole> roles = validateDtoFields(dto, user);
            if (roles != null && roles.stream().noneMatch(r -> Authentication.ROLE_ADMIN.equals(r.getName()))) {
                checkNotLastAdministrator(user, "lose the " + Authentication.ROLE_ADMIN + " role");
            }
            applyDto(user, dto, roles);
            m_userAccountDao.saveOrUpdate(user);
            m_userAccountDao.flush();
            final int version = m_support.version(VersionedTable.users, user.getId());
            return Response.ok(toDto(user, version)).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("update user", e);
        }
    }

    @Override
    @Transactional
    public Response setPassword(final SecurityContext securityContext, final HttpHeaders headers, final String userId, final UserPasswordRequest body) {
        m_support.assertAdmin(securityContext, "User management");
        m_support.requireDatabaseStore();
        if (body == null || StringUtils.isBlank(body.getPassword())) {
            throw m_support.badRequest("A password is required.");
        }
        try {
            m_support.lockForWrite();
            final UserAccount user = load(userId, headers);
            m_support.checkIfMatch(headers, VersionedTable.users, user.getId(), "User " + userId);
            user.setPasswordHash(UserManager.hashPassword(body.getPassword()));
            user.setPasswordSalted(true);
            m_userAccountDao.saveOrUpdate(user);
            m_userAccountDao.flush();
            final int version = m_support.version(VersionedTable.users, user.getId());
            LOG.info("Password changed for user {} by {}", userId, UserMgmtValidation.principal(securityContext));
            return Response.noContent().tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("change password", e);
        }
    }

    @Override
    @Transactional
    public Response renameUser(final SecurityContext securityContext, final HttpHeaders headers, final String userId, final UserRenameRequest body) {
        m_support.assertAdmin(securityContext, "User management");
        m_support.requireDatabaseStore();
        if (body == null || StringUtils.isBlank(body.getNewUserId())) {
            throw m_support.badRequest("A newUserId is required.");
        }
        if (PROTECTED_USERS.contains(userId)) {
            throw m_support.badRequest("The system user " + userId + " cannot be renamed.");
        }
        final String newUserId = body.getNewUserId().trim();
        final String userIdProblem = UserMgmtValidation.validateName(newUserId, "userId");
        if (userIdProblem != null) {
            throw m_support.badRequest(userIdProblem);
        }
        if (newUserId.equals(userId)) {
            throw m_support.badRequest("The newUserId is the same as the current userId.");
        }
        try {
            m_support.lockForWrite();
            final UserAccount user = load(userId, headers);
            m_support.checkIfMatch(headers, VersionedTable.users, user.getId(), "User " + userId);
            if (m_userAccountDao.findByUsername(newUserId) != null) {
                throw m_support.conflict("User " + newUserId + " already exists.");
            }
            UserMgmtValidation.checkLength(owner(newUserId), "user name", newUserId, UserMgmtJaxbMapper.USERNAME_LENGTH);
            // memberships, supervisors and schedules reference the id and follow the new name
            user.setUsername(newUserId);
            m_userAccountDao.saveOrUpdate(user);
            m_userAccountDao.flush();
            final int version = m_support.version(VersionedTable.users, user.getId());
            LOG.info("User {} renamed to {} by {}", userId, newUserId, UserMgmtValidation.principal(securityContext));
            return Response.ok(toDto(user, version)).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("rename user", e);
        }
    }

    @Override
    @Transactional
    public Response deleteUser(final SecurityContext securityContext, final HttpHeaders headers, final String userId) {
        m_support.assertAdmin(securityContext, "User management");
        m_support.requireDatabaseStore();
        if (PROTECTED_USERS.contains(userId)) {
            throw m_support.badRequest("The system user " + userId + " cannot be deleted.");
        }
        try {
            m_support.lockForWrite();
            final UserAccount user = load(userId, headers);
            m_support.checkIfMatch(headers, VersionedTable.users, user.getId(), "User " + userId);
            // the DAO strips memberships and schedules but refuses a supervisor; checking first lets the
            // message name the roles
            final List<String> supervisedRoles = m_onCallRoleDao.findBySupervisor(userId).stream()
                    .map(OnCallRole::getName)
                    .collect(Collectors.toList());
            if (!supervisedRoles.isEmpty()) {
                throw m_support.conflict("User " + userId
                        + " is the supervisor of on-call role(s) " + String.join(", ", supervisedRoles)
                        + "; assign a different supervisor first.");
            }
            checkNotLastAdministrator(user, "be deleted");
            // notifd resolves a target name as a group, then an on-call role, then a user: one of those takes the paths
            if (m_userGroupDao.findByName(userId) == null && m_onCallRoleDao.findByName(userId) == null) {
                m_support.checkNotDestinationPathTarget("User " + userId, userId);
            }
            m_userAccountDao.delete(user);
            m_userAccountDao.flush();
            LOG.info("User {} deleted by {}", userId, UserMgmtValidation.principal(securityContext));
            return Response.noContent().build();
        } catch (final Exception e) {
            throw m_support.translate("delete user", e);
        }
    }

    /**
     * Throws 409 if the user is the only one with ROLE_ADMIN. Without an administrator nobody can manage users in
     * the web UI or log in to the Karaf console until the roles are repaired in the database. Must be called under
     * the write lock, so that two requests cannot each remove a different last-but-one administrator.
     */
    private void checkNotLastAdministrator(final UserAccount user, final String change) {
        final boolean isAdministrator = user.getSecurityRoles().stream().anyMatch(r -> Authentication.ROLE_ADMIN.equals(r.getName()));
        if (isAdministrator && m_userAccountDao.findBySecurityRole(Authentication.ROLE_ADMIN).stream()
                .allMatch(u -> u.getId().equals(user.getId()))) {
            throw m_support.conflict("User " + user.getUsername() + " is the only user with the " + Authentication.ROLE_ADMIN
                    + " role and cannot " + change + "; give the role to another user first.");
        }
    }

    /** @throws javax.ws.rs.WebApplicationException 404 if there is no such object */
    private UserAccount load(final String userId) {
        return load(userId, null);
    }

    /** For a write: a missing object is a 412 instead of a 404 when the request carries an If-Match. */
    private UserAccount load(final String userId, final HttpHeaders headers) {
        final UserAccount user = m_userAccountDao.findByUsername(userId);
        if (user == null) {
            throw m_support.notFoundOrPreconditionFailed(headers, "User " + userId, "User " + userId + " was not found.");
        }
        return user;
    }

    private static UserDto toDto(final UserAccount user, final int version) {
        final UserDto dto = new UserDto();
        dto.setUserId(user.getUsername());
        dto.setVersion(version);
        dto.setFullName(user.getFullName());
        dto.setUserComments(user.getComments());
        dto.setEmail(contactInfo(user, ContactType.email));
        dto.setPagerEmail(contactInfo(user, ContactType.pagerEmail));
        dto.setWorkPhone(contactInfo(user, ContactType.workPhone));
        dto.setMobilePhone(contactInfo(user, ContactType.mobilePhone));
        dto.setHomePhone(contactInfo(user, ContactType.homePhone));
        dto.setNumericPagerService(contactServiceProvider(user, ContactType.numericPage));
        dto.setNumericPagerPin(contactInfo(user, ContactType.numericPage));
        dto.setTextPagerService(contactServiceProvider(user, ContactType.textPage));
        dto.setTextPagerPin(contactInfo(user, ContactType.textPage));
        dto.setTuiPin(user.getTuiPin());
        dto.setTimeZoneId(user.getTimeZoneId());
        dto.setDutySchedules(new ArrayList<>(user.getDutySchedules()));
        final List<String> roles = user.getSecurityRoles().stream()
                .map(SecurityRole::getName)
                .sorted()
                .collect(Collectors.toList());
        dto.setRoles(roles);
        dto.setReadOnly(roles.contains(Authentication.ROLE_READONLY));
        return dto;
    }

    /**
     * Validates every field of the request BEFORE anything is applied, so a
     * rejected request cannot leave partial state anywhere.
     *
     * @return the security roles the request names, or null if it leaves the roles alone
     */
    private List<SecurityRole> validateDtoFields(final UserDto dto, final UserAccount existing) {
        final String owner = owner(existing == null ? dto.getUserId() : existing.getUsername());
        UserMgmtValidation.checkComments(dto.getUserComments(), existing == null ? null : existing.getComments(), existing != null);
        final String timeZoneId = StringUtils.trimToNull(dto.getTimeZoneId());
        if (timeZoneId != null) {
            try {
                UserMgmtValidation.checkLength(owner, "time zone", ZoneId.of(timeZoneId).getId(), UserMgmtJaxbMapper.TIME_ZONE_ID_LENGTH);
            } catch (final java.time.DateTimeException e) {
                throw new UserMgmtValidationException("Invalid timeZoneId: " + timeZoneId);
            }
        }
        // the TUI PIN is dialled on a phone keypad
        final String tuiPin = StringUtils.trimToNull(dto.getTuiPin());
        if (tuiPin != null && !tuiPin.matches("[0-9]+")) {
            throw new UserMgmtValidationException("The TUI PIN must contain only digits.");
        }
        UserMgmtValidation.checkLength(owner, "TUI PIN", tuiPin, UserMgmtJaxbMapper.TUI_PIN_LENGTH);
        UserMgmtValidation.checkLength(owner, "full name", StringUtils.trimToNull(dto.getFullName()), UserMgmtJaxbMapper.FULL_NAME_LENGTH);
        for (final String[] field : new String[][] {
                { "email", dto.getEmail() }, { "pager email", dto.getPagerEmail() }, { "work phone", dto.getWorkPhone() },
                { "mobile phone", dto.getMobilePhone() }, { "home phone", dto.getHomePhone() },
                { "numeric pager service", dto.getNumericPagerService() }, { "numeric pager PIN", dto.getNumericPagerPin() },
                { "text pager service", dto.getTextPagerService() }, { "text pager PIN", dto.getTextPagerPin() } }) {
            UserMgmtValidation.checkLength(owner, field[0], StringUtils.trimToNull(field[1]), UserMgmtJaxbMapper.CONTACT_LENGTH);
        }
        List<SecurityRole> roles = null;
        if (dto.getRoles() != null) {
            roles = new ArrayList<>();
            for (final String name : dto.getRoles()) {
                if (StringUtils.isBlank(name)) {
                    throw new UserMgmtValidationException("Security role names must not be blank.");
                }
                final SecurityRole role = m_securityRoleDao.findByName(name);
                if (role == null) {
                    throw new UserMgmtValidationException("Unknown security role: " + name);
                }
                if (!roles.contains(role)) {
                    roles.add(role);
                }
            }
        }
        if (dto.getDutySchedules() != null) {
            // hand-edited data never makes a user uneditable: see UserMgmtValidation.validateDutySchedules
            UserMgmtValidation.validateDutySchedules(owner, dto.getDutySchedules(), existing == null ? null : existing.getDutySchedules());
        }
        return roles;
    }

    /**
     * Applies the pre-validated DTO. Only the exposed contact types are
     * touched; every other contact — XMPP, microblog, SNMP trap — and the
     * password survive untouched, so a v2 update can never corrupt
     * hand-maintained entries. List fields left out of the request body
     * arrive as null and are preserved.
     */
    private static void applyDto(final UserAccount user, final UserDto dto, final List<SecurityRole> roles) {
        // omitted (null) fields are preserved, matching the groups and
        // on-call services; an empty string clears a field
        if (dto.getFullName() != null) {
            user.setFullName(StringUtils.trimToNull(dto.getFullName()));
        }
        if (dto.getUserComments() != null && !UserMgmtValidation.commentsUnchanged(dto.getUserComments(), user.getComments())) {
            user.setComments(StringUtils.trimToNull(dto.getUserComments()));
        }
        if (dto.getTuiPin() != null) {
            user.setTuiPin(StringUtils.trimToNull(dto.getTuiPin()));
        }
        if (dto.getTimeZoneId() != null) {
            // stored as a manager round trip would write it back, so a later manager save doesn't rewrite it
            final String timeZoneId = StringUtils.trimToNull(dto.getTimeZoneId());
            user.setTimeZoneId(timeZoneId == null ? null : ZoneId.of(timeZoneId).getId());
        }
        applyContact(user, ContactType.email, dto.getEmail(), null);
        applyContact(user, ContactType.pagerEmail, dto.getPagerEmail(), null);
        applyContact(user, ContactType.workPhone, dto.getWorkPhone(), null);
        applyContact(user, ContactType.mobilePhone, dto.getMobilePhone(), null);
        applyContact(user, ContactType.homePhone, dto.getHomePhone(), null);
        // pager PIN is the contact info; pager service provider is a separate field on the same contact
        applyContact(user, ContactType.numericPage, dto.getNumericPagerPin(), dto.getNumericPagerService());
        applyContact(user, ContactType.textPage, dto.getTextPagerPin(), dto.getTextPagerService());
        UserMgmtValidation.replaceIfChanged(user.getDutySchedules(), dto.getDutySchedules());
        if (roles != null && !user.getSecurityRoles().equals(new LinkedHashSet<>(roles))) {
            user.getSecurityRoles().clear();
            user.getSecurityRoles().addAll(roles);
        }
    }

    /**
     * A null value means "preserve", an empty one "clear". A contact left with neither a value nor a
     * service provider is removed.
     */
    private static void applyContact(final UserAccount user, final ContactType type, final String info, final String serviceProvider) {
        if (info == null && serviceProvider == null) {
            return;
        }
        final UserContact contact = user.getContact(type);
        final String newInfo = info == null ? (contact == null ? null : contact.getInfo()) : StringUtils.trimToNull(info);
        final String newProvider = serviceProvider == null ? (contact == null ? null : contact.getServiceProvider())
                : StringUtils.trimToNull(serviceProvider);
        if (newInfo == null && newProvider == null) {
            user.removeContact(type);
        } else if (contact == null || !Objects.equals(newInfo, contact.getInfo())
                || !Objects.equals(newProvider, contact.getServiceProvider())) {
            user.setContact(type, newInfo, newProvider);
        }
    }

    private static String contactInfo(final UserAccount user, final ContactType type) {
        final UserContact contact = user.getContact(type);
        return contact == null || StringUtils.isEmpty(contact.getInfo()) ? null : contact.getInfo();
    }

    private static String contactServiceProvider(final UserAccount user, final ContactType type) {
        final UserContact contact = user.getContact(type);
        return contact == null || StringUtils.isEmpty(contact.getServiceProvider()) ? null : contact.getServiceProvider();
    }

    private static String owner(final String userId) {
        return "User \"" + userId + "\"";
    }

}
