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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.api.UserConfig.ContactType;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
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

/**
 * Versioned user management on top of {@link UserManager}: users.xml remains
 * the system of record and hand-editing keeps working. Unlike the legacy JSPs
 * (which only hid the buttons), the admin/rtc delete and rename protections
 * are enforced here, server-side. The password hash is never serialized.
 *
 * Mutations validate the full request up front and then apply it to a
 * detached copy of the stored user, so a rejected request can never leave
 * partial changes in the manager's shared in-memory state.
 */
@Component("usersRestServiceV2")
public class UsersRestService implements UsersRestApi {

    // Check-then-act sequences synchronize on GroupFactory.class: user
    // mutations cascade into GroupManager (deleteUser/renameUser walk every
    // group), so the v2 users and groups services must share one monitor.

    private static final Logger LOG = LoggerFactory.getLogger(UsersRestService.class);

    /** System accounts that must not be deleted or renamed. */
    private static final Set<String> PROTECTED_USERS = Set.of("admin", "rtc");

    /**
     * Rejects markup (legacy servlet rule) plus characters that break how the
     * id is used downstream: ':' (HTTP basic auth), whitespace (group
     * references), and '/', '\', '%', '?', '#' (the id is a URL path segment
     * in every per-user endpoint).
     */
    private static final Pattern INVALID_USER_ID = Pattern.compile("[&<>\"`':/\\\\%?#\\s]");

    /** Same markup characters the groups API rejects in comments. */
    private static final Pattern INVALID_COMMENTS = Pattern.compile("[&<>\"`']");

    /**
     * Day tokens + military begin-end times, e.g. MoWeFr800-1700. Overnight
     * schedules (begin after end, e.g. MoTu2000-800) are legal — the legacy
     * UI wrote them and hand-edited files contain them.
     */
    private static final Pattern DUTY_SCHEDULE = Pattern.compile("^((?:Mo|Tu|We|Th|Fr|Sa|Su){1,7})(\\d{1,4})-(\\d{1,4})$");

    @Autowired
    private UserManager m_userManager;

    /** For the on-call-role supervisor referential check on delete. */
    @Autowired
    private org.opennms.netmgt.config.GroupManager m_groupManager;

    @Override
    public Response listUsers(final SecurityContext securityContext) {
        assertAdmin(securityContext);
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                final List<UserDto> users = new ArrayList<>();
                for (final User user : m_userManager.getUsers().values()) {
                    users.add(toDto(user));
                }
                users.sort(Comparator.comparing(UserDto::getUserId, String.CASE_INSENSITIVE_ORDER));
                return Response.ok(users).build();
            }
        } catch (final Exception e) {
            return serverError("Can't read users: %s", e);
        }
    }

    @Override
    public Response getUser(final SecurityContext securityContext, final String userId) {
        assertAdmin(securityContext);
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                final User user = m_userManager.getUser(userId);
                if (user == null) {
                    return Response.status(Status.NOT_FOUND).entity("User " + userId + " was not found.").build();
                }
                return Response.ok(toDto(user)).build();
            }
        } catch (final Exception e) {
            return serverError("Can't read user: %s", e);
        }
    }

    @Override
    public Response listAvailableRoles(final SecurityContext securityContext) {
        assertAdmin(securityContext);
        final List<String> roles = new ArrayList<>(Authentication.getAvailableRoles());
        roles.sort(String.CASE_INSENSITIVE_ORDER);
        return Response.ok(roles).build();
    }

    @Override
    public Response createUser(final SecurityContext securityContext, final UserWriteRequest request) {
        assertAdmin(securityContext);
        if (request == null || StringUtils.isBlank(request.getUserId())) {
            return Response.status(Status.BAD_REQUEST).entity("A userId is required.").build();
        }
        final String userId = request.getUserId().trim();
        final String userIdProblem = validateUserId(userId);
        if (userIdProblem != null) {
            return Response.status(Status.BAD_REQUEST).entity(userIdProblem).build();
        }
        if (StringUtils.isBlank(request.getPassword())) {
            return Response.status(Status.BAD_REQUEST).entity("A password is required.").build();
        }
        try {
            validateDtoFields(request, null);
        } catch (final IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                if (m_userManager.hasUser(userId)) {
                    return Response.status(Status.BAD_REQUEST).entity("User " + userId + " already exists.").build();
                }
                final User user = new User();
                user.setUserId(userId);
                user.setPassword(m_userManager.encryptedPassword(request.getPassword(), true), Boolean.TRUE);
                applyDto(user, request);
                try {
                    m_userManager.saveUser(userId, user);
                } catch (final Exception e) {
                    rollbackPhantomUser(userId);
                    throw e;
                }
            }
            LOG.info("User {} created by {}", userId, principal(securityContext));
            return Response.status(Status.CREATED).build();
        } catch (final Exception e) {
            return serverError("Can't create user: %s", e);
        }
    }

    @Override
    public Response updateUser(final SecurityContext securityContext, final String userId, final UserDto dto) {
        assertAdmin(securityContext);
        if (dto == null) {
            return Response.status(Status.BAD_REQUEST).entity("A user body is required.").build();
        }
        if (dto.getUserId() != null && !userId.equals(dto.getUserId())) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("The userId in the body does not match the request path; use the rename endpoint to change ids.").build();
        }
        // stripping ROLE_ADMIN from the admin account would lock every
        // administrator out of the web UI until users.xml is hand-edited
        if ("admin".equals(userId) && dto.getRoles() != null && !dto.getRoles().contains(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("The admin user must keep the " + Authentication.ROLE_ADMIN + " role.").build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                final User existing = m_userManager.getUser(userId);
                if (existing == null) {
                    return Response.status(Status.NOT_FOUND).entity("User " + userId + " was not found.").build();
                }
                try {
                    validateDtoFields(dto, existing);
                } catch (final IllegalArgumentException e) {
                    return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
                }
                final User updated = copyOf(existing);
                applyDto(updated, dto);
                try {
                    m_userManager.saveUser(userId, updated);
                } catch (final Exception e) {
                    restorePreviousUser(userId, existing);
                    throw e;
                }
            }
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't update user: %s", e);
        }
    }

    @Override
    public Response setPassword(final SecurityContext securityContext, final String userId, final UserPasswordRequest request) {
        assertAdmin(securityContext);
        if (request == null || StringUtils.isBlank(request.getPassword())) {
            return Response.status(Status.BAD_REQUEST).entity("A password is required.").build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                final User existing = m_userManager.getUser(userId);
                if (existing == null) {
                    return Response.status(Status.NOT_FOUND).entity("User " + userId + " was not found.").build();
                }
                final User updated = copyOf(existing);
                updated.setPassword(m_userManager.encryptedPassword(request.getPassword(), true), Boolean.TRUE);
                try {
                    m_userManager.saveUser(userId, updated);
                } catch (final Exception e) {
                    // without this, the new hash stays live in memory after the
                    // 500 and the user can log in with the "failed" password
                    restorePreviousUser(userId, existing);
                    throw e;
                }
            }
            LOG.info("Password changed for user {} by {}", userId, principal(securityContext));
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't change password: %s", e);
        }
    }

    @Override
    public Response renameUser(final SecurityContext securityContext, final String userId, final UserRenameRequest request) {
        assertAdmin(securityContext);
        if (request == null || StringUtils.isBlank(request.getNewUserId())) {
            return Response.status(Status.BAD_REQUEST).entity("A newUserId is required.").build();
        }
        if (PROTECTED_USERS.contains(userId)) {
            return Response.status(Status.BAD_REQUEST).entity("The system user " + userId + " cannot be renamed.").build();
        }
        final String newUserId = request.getNewUserId().trim();
        final String userIdProblem = validateUserId(newUserId);
        if (userIdProblem != null) {
            return Response.status(Status.BAD_REQUEST).entity(userIdProblem).build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                if (!m_userManager.hasUser(userId)) {
                    return Response.status(Status.NOT_FOUND).entity("User " + userId + " was not found.").build();
                }
                if (m_userManager.hasUser(newUserId)) {
                    return Response.status(Status.BAD_REQUEST).entity("User " + newUserId + " already exists.").build();
                }
                m_userManager.renameUser(userId, newUserId);
            }
            LOG.info("User {} renamed to {} by {}", userId, newUserId, principal(securityContext));
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't rename user: %s", e);
        }
    }

    @Override
    public Response deleteUser(final SecurityContext securityContext, final String userId) {
        assertAdmin(securityContext);
        if (PROTECTED_USERS.contains(userId)) {
            return Response.status(Status.BAD_REQUEST).entity("The system user " + userId + " cannot be deleted.").build();
        }
        try {
            synchronized (org.opennms.netmgt.config.GroupFactory.class) {
                if (!m_userManager.hasUser(userId)) {
                    return Response.status(Status.NOT_FOUND).entity("User " + userId + " was not found.").build();
                }
                // GroupManager.deleteUser strips memberships and schedules but
                // leaves role supervisors dangling, silently killing the
                // supervisor fallback for those rotas
                final List<String> supervisedRoles = new ArrayList<>();
                for (final org.opennms.netmgt.config.groups.Role role : m_groupManager.getRoles()) {
                    if (userId.equals(role.getSupervisor())) {
                        supervisedRoles.add(role.getName());
                    }
                }
                if (!supervisedRoles.isEmpty()) {
                    return Response.status(Status.BAD_REQUEST).entity("User " + userId
                            + " is the supervisor of on-call role(s) " + String.join(", ", supervisedRoles)
                            + "; assign a different supervisor first.").build();
                }
                m_userManager.deleteUser(userId);
            }
            LOG.info("User {} deleted by {}", userId, principal(securityContext));
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't delete user: %s", e);
        }
    }

    private UserDto toDto(final User user) {
        final UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setFullName(user.getFullName().orElse(null));
        dto.setUserComments(user.getUserComments().orElse(null));
        dto.setEmail(contactInfo(user, ContactType.email));
        dto.setPagerEmail(contactInfo(user, ContactType.pagerEmail));
        dto.setWorkPhone(contactInfo(user, ContactType.workPhone));
        dto.setMobilePhone(contactInfo(user, ContactType.mobilePhone));
        dto.setHomePhone(contactInfo(user, ContactType.homePhone));
        dto.setNumericPagerService(contactServiceProvider(user, ContactType.numericPage));
        dto.setNumericPagerPin(contactInfo(user, ContactType.numericPage));
        dto.setTextPagerService(contactServiceProvider(user, ContactType.textPage));
        dto.setTextPagerPin(contactInfo(user, ContactType.textPage));
        dto.setTuiPin(user.getTuiPin().orElse(null));
        dto.setTimeZoneId(user.getTimeZoneId().map(Objects::toString).orElse(null));
        dto.setDutySchedules(new ArrayList<>(user.getDutySchedules()));
        dto.setRoles(new ArrayList<>(user.getRoles()));
        dto.setReadOnly(user.getRoles().contains(Authentication.ROLE_READONLY));
        return dto;
    }

    /** Detached copy so mutations never touch the manager's live object. */
    private static User copyOf(final User user) {
        // Round-trip through JAXB (field access — User is @XmlAccessorType(NONE)
        // with field annotations, Contact is FIELD) so the deep copy bypasses the
        // HTML-sanitizing setters. setFullName / Contact.setInfo run a
        // non-idempotent Encode.forHtml, so a setter-based copy would escape an
        // already-stored value one layer deeper on every write — a password
        // change or any unrelated edit would mangle full name and contacts.
        return JaxbUtils.unmarshal(User.class, JaxbUtils.marshal(user));
    }

    /**
     * Validates every field of the request BEFORE anything is applied, so a
     * rejected request cannot leave partial state anywhere.
     */
    private static void validateDtoFields(final UserDto dto, final User existing) {
        if (dto.getUserComments() != null && INVALID_COMMENTS.matcher(dto.getUserComments()).find()
                && (existing == null || !dto.getUserComments().equals(existing.getUserComments().orElse(null)))) {
            throw new IllegalArgumentException("The comments must not contain any HTML markup.");
        }
        final String timeZoneId = StringUtils.trimToNull(dto.getTimeZoneId());
        if (timeZoneId != null) {
            try {
                java.time.ZoneId.of(timeZoneId);
            } catch (final RuntimeException e) {
                throw new IllegalArgumentException("Invalid timeZoneId: " + timeZoneId);
            }
        }
        // users.xsd constrains tui-pin to [0-9]+; an invalid value would pass
        // into the in-memory map and then fail the schema-validating marshal,
        // leaving the map poisoned (isUpdateNeeded stays false) until restart
        final String tuiPin = StringUtils.trimToNull(dto.getTuiPin());
        if (tuiPin != null && !tuiPin.matches("[0-9]+")) {
            throw new IllegalArgumentException("The TUI PIN must contain only digits.");
        }
        if (dto.getRoles() != null) {
            for (final String role : dto.getRoles()) {
                if (!Authentication.isValidRole(role)) {
                    throw new IllegalArgumentException("Unknown security role: " + role);
                }
            }
        }
        if (dto.getDutySchedules() != null) {
            // strings already stored on the record are preserved as-is so
            // hand-edited files never make a user uneditable; only entries
            // new to this request must pass validation
            final Set<String> preExisting = existing == null
                    ? Set.of() : new java.util.LinkedHashSet<>(existing.getDutySchedules());
            for (final String schedule : dto.getDutySchedules()) {
                if (!preExisting.contains(schedule)) {
                    validateDutySchedule(schedule);
                }
            }
        }
    }

    /**
     * Applies the pre-validated DTO. Only the exposed contact types (email,
     * pagerEmail) are touched; every other contact — XMPP, microblog, phones,
     * paging services — and the password survive untouched, so a v2 update
     * can never corrupt hand-maintained users.xml entries. List fields left
     * out of the request body arrive as null and are preserved.
     */
    private static void applyDto(final User user, final UserDto dto) {
        // omitted (null) fields are preserved, matching the groups and
        // on-call services; an empty string clears a field
        if (dto.getFullName() != null) {
            // setFullName runs a non-idempotent HTML sanitizer; only write when the
            // value actually changed so an unrelated edit can't re-escape the
            // already-stored value one layer deeper
            final String incoming = StringUtils.trimToNull(dto.getFullName());
            if (!Objects.equals(incoming, user.getFullName().orElse(null))) {
                user.setFullName(incoming);
            }
        }
        if (dto.getUserComments() != null) {
            user.setUserComments(StringUtils.trimToNull(dto.getUserComments()));
        }
        if (dto.getTuiPin() != null) {
            user.setTuiPin(StringUtils.trimToNull(dto.getTuiPin()));
        }
        if (dto.getTimeZoneId() != null) {
            final String timeZoneId = StringUtils.trimToNull(dto.getTimeZoneId());
            if (timeZoneId == null) {
                user.setTimeZoneId((java.time.ZoneId) null);
            } else {
                user.setTimeZoneId(timeZoneId);
            }
        }
        applyContactInfoIfChanged(user, ContactType.email, dto.getEmail());
        applyContactInfoIfChanged(user, ContactType.pagerEmail, dto.getPagerEmail());
        applyContactInfoIfChanged(user, ContactType.workPhone, dto.getWorkPhone());
        applyContactInfoIfChanged(user, ContactType.mobilePhone, dto.getMobilePhone());
        applyContactInfoIfChanged(user, ContactType.homePhone, dto.getHomePhone());
        // pager PIN is the contact info; pager service provider is a separate
        // (also sanitizing) field on the same contact
        applyContactInfoIfChanged(user, ContactType.numericPage, dto.getNumericPagerPin());
        applyContactServiceProviderIfChanged(user, ContactType.numericPage, dto.getNumericPagerService());
        applyContactInfoIfChanged(user, ContactType.textPage, dto.getTextPagerPin());
        applyContactServiceProviderIfChanged(user, ContactType.textPage, dto.getTextPagerService());
        if (dto.getDutySchedules() != null) {
            user.setDutySchedules(new ArrayList<>(dto.getDutySchedules()));
        }
        if (dto.getRoles() != null) {
            user.setRoles(new ArrayList<>(dto.getRoles()));
        }
    }

    /**
     * A failed save leaves the new user in UserManager's in-memory map
     * (_writeUser puts before _saveCurrent), which would 400 every retry as
     * "already exists". Best effort: remove the phantom again.
     */
    private void rollbackPhantomUser(final String userId) {
        try {
            if (m_userManager.hasUser(userId)) {
                m_userManager.deleteUser(userId);
            }
        } catch (final Exception rollbackFailure) {
            LOG.warn("Could not roll back partially created user {}", userId, rollbackFailure);
        }
    }

    // saveUser mutates the in-memory map before marshalling, so a failed save
    // leaves the new state live in memory while users.xml keeps the old one —
    // and isUpdateNeeded() won't notice. Re-save the previous state to bring
    // memory and file back in line.
    private void restorePreviousUser(final String userId, final User previous) {
        try {
            m_userManager.saveUser(userId, previous);
        } catch (final Exception rollbackFailure) {
            LOG.warn("Could not restore the previous state of user {} after a failed save; "
                    + "in-memory state may diverge from users.xml until restart", userId, rollbackFailure);
        }
    }

    /** Returns a problem description, or null when the user id is acceptable. */
    private static String validateUserId(final String userId) {
        if (INVALID_USER_ID.matcher(userId).find()) {
            return "The userId must not contain markup, whitespace, or the characters : / \\ % ? #";
        }
        if (".".equals(userId) || "..".equals(userId)) {
            return "The userId must not be a dot segment.";
        }
        return null;
    }

    /**
     * Duty schedules are stored as strings like MoWeFr800-1700 and parsed with
     * unchecked exceptions all over notifd/group scheduling — an invalid string
     * saved here would break duty evaluation at runtime.
     */
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

    private static String contactInfo(final User user, final ContactType type) {
        return user.getContacts().stream()
                .filter(c -> type.name().equals(c.getType()))
                .findFirst()
                .flatMap(Contact::getInfo)
                .filter(info -> !info.isEmpty())
                .orElse(null);
    }

    private static String contactServiceProvider(final User user, final ContactType type) {
        return user.getContacts().stream()
                .filter(c -> type.name().equals(c.getType()))
                .findFirst()
                .flatMap(Contact::getServiceProvider)
                .filter(sp -> !sp.isEmpty())
                .orElse(null);
    }

    // A null DTO value means "preserve"; and because Contact.setInfo /
    // setServiceProvider run a non-idempotent HTML sanitizer, only write when the
    // value actually changed so an unrelated edit can't re-escape a stored value.
    private static void applyContactInfoIfChanged(final User user, final ContactType type, final String dtoValue) {
        if (dtoValue == null) {
            return;
        }
        final String incoming = StringUtils.trimToNull(dtoValue);
        if (!Objects.equals(incoming, contactInfo(user, type))) {
            setContact(user, type, incoming);
        }
    }

    private static void applyContactServiceProviderIfChanged(final User user, final ContactType type, final String dtoValue) {
        if (dtoValue == null) {
            return;
        }
        final String incoming = StringUtils.trimToNull(dtoValue);
        if (!Objects.equals(incoming, contactServiceProvider(user, type))) {
            setContactServiceProvider(user, type, incoming);
        }
    }

    private static void setContact(final User user, final ContactType type, final String value) {
        final Optional<Contact> existing = user.getContacts().stream()
                .filter(c -> type.name().equals(c.getType()))
                .findFirst();
        final String trimmed = StringUtils.trimToNull(value);
        if (existing.isPresent()) {
            existing.get().setInfo(trimmed == null ? "" : trimmed);
        } else if (trimmed != null) {
            final Contact contact = new Contact(type.name());
            contact.setInfo(trimmed);
            user.getContacts().add(contact);
        }
    }

    private static void setContactServiceProvider(final User user, final ContactType type, final String value) {
        final Optional<Contact> existing = user.getContacts().stream()
                .filter(c -> type.name().equals(c.getType()))
                .findFirst();
        final String trimmed = StringUtils.trimToNull(value);
        if (existing.isPresent()) {
            existing.get().setServiceProvider(trimmed == null ? "" : trimmed);
        } else if (trimmed != null) {
            final Contact contact = new Contact(type.name());
            contact.setServiceProvider(trimmed);
            user.getContacts().add(contact);
        }
    }

    private static void assertAdmin(final SecurityContext securityContext) {
        if (securityContext == null || !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw new javax.ws.rs.WebApplicationException(
                    Response.status(Status.FORBIDDEN).entity("User management requires the admin role.").build());
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

}
