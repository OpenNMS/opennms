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
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

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
 */
@Component("usersRestServiceV2")
public class UsersRestService implements UsersRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(UsersRestService.class);

    /** System accounts that must not be deleted or renamed. */
    private static final Set<String> PROTECTED_USERS = Set.of("admin", "rtc");

    /** Mirrors the legacy servlets' markup check on user ids. */
    private static final Pattern INVALID_USER_ID = Pattern.compile(".*[&<>\"`']+.*");

    @Autowired
    private UserManager m_userManager;

    @Override
    public Response listUsers(final SecurityContext securityContext) {
        assertAdmin(securityContext);
        try {
            final List<UserDto> users = new ArrayList<>();
            for (final User user : m_userManager.getUsers().values()) {
                users.add(toDto(user));
            }
            users.sort(Comparator.comparing(UserDto::getUserId, String.CASE_INSENSITIVE_ORDER));
            return Response.ok(users).build();
        } catch (final Exception e) {
            return serverError("Can't read users: %s", e);
        }
    }

    @Override
    public Response getUser(final SecurityContext securityContext, final String userId) {
        assertAdmin(securityContext);
        try {
            final User user = m_userManager.getUser(userId);
            if (user == null) {
                return Response.status(Status.NOT_FOUND).entity("User " + userId + " was not found.").build();
            }
            return Response.ok(toDto(user)).build();
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
        if (request == null || isBlank(request.getUserId())) {
            return Response.status(Status.BAD_REQUEST).entity("A user-id is required.").build();
        }
        final String userId = request.getUserId().trim();
        if (INVALID_USER_ID.matcher(userId).matches()) {
            return Response.status(Status.BAD_REQUEST).entity("The user-id must not contain any HTML markup.").build();
        }
        if (isBlank(request.getPassword())) {
            return Response.status(Status.BAD_REQUEST).entity("A password is required.").build();
        }
        try {
            if (m_userManager.hasUser(userId)) {
                return Response.status(Status.BAD_REQUEST).entity("User " + userId + " already exists.").build();
            }
            final User user = new User();
            user.setUserId(userId);
            user.setPassword(m_userManager.encryptedPassword(request.getPassword(), true), Boolean.TRUE);
            applyDto(user, request);
            m_userManager.saveUser(userId, user);
            LOG.info("User {} created by {}", userId, securityContext.getUserPrincipal() == null ? "?" : securityContext.getUserPrincipal().getName());
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
        try {
            final User user = m_userManager.getUser(userId);
            if (user == null) {
                return Response.status(Status.NOT_FOUND).entity("User " + userId + " was not found.").build();
            }
            applyDto(user, dto);
            m_userManager.saveUser(userId, user);
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't update user: %s", e);
        }
    }

    @Override
    public Response setPassword(final SecurityContext securityContext, final String userId, final UserPasswordRequest request) {
        assertAdmin(securityContext);
        if (request == null || isBlank(request.getPassword())) {
            return Response.status(Status.BAD_REQUEST).entity("A password is required.").build();
        }
        try {
            final User user = m_userManager.getUser(userId);
            if (user == null) {
                return Response.status(Status.NOT_FOUND).entity("User " + userId + " was not found.").build();
            }
            user.setPassword(m_userManager.encryptedPassword(request.getPassword(), true), Boolean.TRUE);
            m_userManager.saveUser(userId, user);
            LOG.info("Password changed for user {} by {}", userId, securityContext.getUserPrincipal() == null ? "?" : securityContext.getUserPrincipal().getName());
            return Response.noContent().build();
        } catch (final Exception e) {
            return serverError("Can't change password: %s", e);
        }
    }

    @Override
    public Response renameUser(final SecurityContext securityContext, final String userId, final UserRenameRequest request) {
        assertAdmin(securityContext);
        if (request == null || isBlank(request.getNewUserId())) {
            return Response.status(Status.BAD_REQUEST).entity("A new-user-id is required.").build();
        }
        if (PROTECTED_USERS.contains(userId)) {
            return Response.status(Status.BAD_REQUEST).entity("The system user " + userId + " cannot be renamed.").build();
        }
        final String newUserId = request.getNewUserId().trim();
        if (INVALID_USER_ID.matcher(newUserId).matches()) {
            return Response.status(Status.BAD_REQUEST).entity("The user-id must not contain any HTML markup.").build();
        }
        try {
            if (!m_userManager.hasUser(userId)) {
                return Response.status(Status.NOT_FOUND).entity("User " + userId + " was not found.").build();
            }
            if (m_userManager.hasUser(newUserId)) {
                return Response.status(Status.BAD_REQUEST).entity("User " + newUserId + " already exists.").build();
            }
            m_userManager.renameUser(userId, newUserId);
            LOG.info("User {} renamed to {} by {}", userId, newUserId, securityContext.getUserPrincipal() == null ? "?" : securityContext.getUserPrincipal().getName());
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
            if (!m_userManager.hasUser(userId)) {
                return Response.status(Status.NOT_FOUND).entity("User " + userId + " was not found.").build();
            }
            m_userManager.deleteUser(userId);
            LOG.info("User {} deleted by {}", userId, securityContext.getUserPrincipal() == null ? "?" : securityContext.getUserPrincipal().getName());
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
        dto.setTuiPin(user.getTuiPin().orElse(null));
        dto.setTimeZoneId(user.getTimeZoneId().map(Objects::toString).orElse(null));
        dto.setDutySchedules(new ArrayList<>(user.getDutySchedules()));
        dto.setRoles(new ArrayList<>(user.getRoles()));
        dto.setReadOnly(user.getRoles().contains(Authentication.ROLE_READONLY));
        return dto;
    }

    /**
     * Applies the DTO onto the JAXB user. Only the exposed contact types
     * (email, pagerEmail) are touched; every other contact — XMPP, microblog,
     * phones, paging services — and the password survive untouched, so a v2
     * update can never corrupt hand-maintained users.xml entries.
     */
    private void applyDto(final User user, final UserDto dto) {
        user.setFullName(trimToNull(dto.getFullName()));
        user.setUserComments(trimToNull(dto.getUserComments()));
        user.setTuiPin(trimToNull(dto.getTuiPin()));
        final String timeZoneId = trimToNull(dto.getTimeZoneId());
        if (timeZoneId == null) {
            user.setTimeZoneId((java.time.ZoneId) null);
        } else {
            try {
                user.setTimeZoneId(timeZoneId);
            } catch (final RuntimeException e) {
                throw new IllegalArgumentException("Invalid time-zone-id: " + timeZoneId);
            }
        }
        setContact(user, ContactType.email, dto.getEmail());
        setContact(user, ContactType.pagerEmail, dto.getPagerEmail());
        if (dto.getDutySchedules() != null) {
            user.setDutySchedules(new ArrayList<>(dto.getDutySchedules()));
        }
        if (dto.getRoles() != null) {
            for (final String role : dto.getRoles()) {
                if (!Authentication.isValidRole(role)) {
                    throw new IllegalArgumentException("Unknown security role: " + role);
                }
            }
            user.setRoles(new ArrayList<>(dto.getRoles()));
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

    private static void setContact(final User user, final ContactType type, final String value) {
        final Optional<Contact> existing = user.getContacts().stream()
                .filter(c -> type.name().equals(c.getType()))
                .findFirst();
        final String trimmed = trimToNull(value);
        if (existing.isPresent()) {
            existing.get().setInfo(trimmed == null ? "" : trimmed);
        } else if (trimmed != null) {
            final Contact contact = new Contact(type.name());
            contact.setInfo(trimmed);
            user.getContacts().add(contact);
        }
    }

    private static void assertAdmin(final SecurityContext securityContext) {
        if (securityContext == null || !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw new javax.ws.rs.WebApplicationException(
                    Response.status(Status.FORBIDDEN).entity("User management requires the admin role.").build());
        }
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
