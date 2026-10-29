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
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.VersionedTable;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.opennms.netmgt.model.usermgmt.SecurityRoles;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.web.rest.v2.api.SecurityRolesRestApi;
import org.opennms.web.rest.v2.model.SecurityRoleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Versioned security role management on the role DAO, one transaction per request like the users and
 * groups services. Built-in roles can be read but not changed or deleted; a custom role can only be
 * deleted once no user holds it, so a delete never silently revokes anything.
 *
 * Errors are thrown, never returned, so that a rejected request rolls back whatever it had done.
 */
@Component("securityRolesRestServiceV2")
public class SecurityRolesRestService implements SecurityRolesRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityRolesRestService.class);

    private static final Pattern ROLE_NAME = Pattern.compile("^ROLE_[A-Z0-9_]+$");

    /** The width of the name column. */
    private static final int NAME_LENGTH = 128;

    @Autowired
    private UserMgmtRestSupport m_support;

    @Autowired
    private SecurityRoleDao m_securityRoleDao;

    @Autowired
    private UserAccountDao m_userAccountDao;

    @Override
    @Transactional(readOnly = true)
    public Response listSecurityRoles(final SecurityContext securityContext) {
        m_support.assertAdmin(securityContext, "Security role management");
        m_support.requireDatabaseStore();
        try {
            final Map<String, Integer> counts = m_securityRoleDao.countUsersByRole();
            final List<SecurityRole> roles = new ArrayList<>(m_securityRoleDao.findAll());
            // the built-ins first, then each group by name
            roles.sort(Comparator.comparing((SecurityRole role) -> !role.isBuiltin()).thenComparing(SecurityRole::getName));
            final List<SecurityRoleDto> dtos = new ArrayList<>();
            for (final SecurityRole role : roles) {
                dtos.add(toDto(role, counts.getOrDefault(role.getName(), 0), role.getVersion()));
            }
            return Response.ok(dtos).build();
        } catch (final Exception e) {
            throw m_support.translate("read security roles", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Response getSecurityRole(final SecurityContext securityContext, final String pathName) {
        m_support.assertAdmin(securityContext, "Security role management");
        m_support.requireDatabaseStore();
        try {
            final SecurityRole role = load(normalizePathName(pathName));
            final int version = role.getVersion();
            return Response.ok(toDto(role, null, version)).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("read security role", e);
        }
    }

    @Override
    @Transactional
    public Response createSecurityRole(final SecurityContext securityContext, final SecurityRoleDto dto) {
        m_support.assertAdmin(securityContext, "Security role management");
        m_support.requireDatabaseStore();
        if (dto == null || StringUtils.isBlank(dto.getName())) {
            throw m_support.badRequest("A security role name is required.");
        }
        final String name = normalizeName(dto.getName());
        final String nameProblem = validateName(name);
        if (nameProblem != null) {
            throw m_support.badRequest(nameProblem);
        }
        try {
            m_support.lockForWrite();
            if (m_securityRoleDao.findByName(name) != null) {
                throw m_support.conflict("Security role " + name + " already exists.");
            }
            UserMgmtValidation.checkFreeText(owner(name), "description", dto.getDescription(), null);
            final SecurityRole role = new SecurityRole(name, StringUtils.trimToNull(dto.getDescription()));
            m_securityRoleDao.saveOrUpdate(role);
            m_support.flushAndCheckConstraints();
            final int version = m_support.version(VersionedTable.security_roles, role.getId());
            LOG.info("Security role {} created by {}", name, UserMgmtValidation.principal(securityContext));
            return Response.status(Status.CREATED).entity(toDto(role, null, version)).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("create security role", e);
        }
    }

    @Override
    @Transactional
    public Response updateSecurityRole(final SecurityContext securityContext, final HttpHeaders headers, final String pathName, final SecurityRoleDto dto) {
        m_support.assertAdmin(securityContext, "Security role management");
        m_support.requireDatabaseStore();
        // the path takes the name as the POST does: /security-roles/auditor is ROLE_AUDITOR
        final String name = normalizePathName(pathName);
        if (dto == null) {
            throw m_support.badRequest("A security role body is required.");
        }
        if (dto.getName() != null && (StringUtils.isBlank(dto.getName()) || !name.equals(normalizeName(dto.getName())))) {
            throw m_support.badRequest("The name in the body does not match the request path; security roles cannot be renamed.");
        }
        try {
            m_support.lockForWrite();
            final SecurityRole role = load(name, headers);
            m_support.checkIfMatch(headers, VersionedTable.security_roles, role.getId(), "Security role " + name);
            if (role.isBuiltin()) {
                throw m_support.badRequest("The description of the built-in security role " + name + " cannot be changed.");
            }
            // a stored description longer than the limit is grandfathered while it is sent back unchanged
            UserMgmtValidation.checkFreeText(owner(name), "description", dto.getDescription(), role.getDescription());
            // a description left out of the body is preserved; an empty one clears it
            if (dto.getDescription() != null && !UserMgmtValidation.textUnchanged(dto.getDescription(), role.getDescription())) {
                role.setDescription(StringUtils.trimToNull(dto.getDescription()));
            }
            m_securityRoleDao.saveOrUpdate(role);
            m_support.flushAndCheckConstraints();
            final int version = m_support.version(VersionedTable.security_roles, role.getId());
            return Response.ok(toDto(role, null, version)).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("update security role", e);
        }
    }

    @Override
    @Transactional
    public Response deleteSecurityRole(final SecurityContext securityContext, final HttpHeaders headers, final String pathName) {
        m_support.assertAdmin(securityContext, "Security role management");
        m_support.requireDatabaseStore();
        final String name = normalizePathName(pathName);
        try {
            m_support.lockForWrite();
            final SecurityRole role = load(name, headers);
            m_support.checkIfMatch(headers, VersionedTable.security_roles, role.getId(), "Security role " + name);
            if (role.isBuiltin()) {
                throw m_support.badRequest("The built-in security role " + name + " cannot be deleted.");
            }
            // refuse rather than revoke: taking a role away from users is a decision for the caller
            final List<String> holders = m_userAccountDao.findBySecurityRole(name).stream()
                    .map(UserAccount::getUsername)
                    .sorted()
                    .collect(Collectors.toList());
            if (!holders.isEmpty()) {
                throw m_support.conflict("Security role " + name + " is held by user(s) " + String.join(", ", holders)
                        + "; remove it from them first.");
            }
            m_securityRoleDao.delete(role);
            m_support.flushAndCheckConstraints();
            LOG.info("Security role {} deleted by {}", name, UserMgmtValidation.principal(securityContext));
            return Response.noContent().build();
        } catch (final Exception e) {
            throw m_support.translate("delete security role", e);
        }
    }

    /** @throws javax.ws.rs.WebApplicationException 404 if there is no such object */
    private SecurityRole load(final String name) {
        return load(name, null);
    }

    /** For a write: a missing object is a 412 instead of a 404 when the request carries an If-Match. */
    private SecurityRole load(final String name, final HttpHeaders headers) {
        final SecurityRole role = m_securityRoleDao.findByName(name);
        if (role == null) {
            throw m_support.notFoundOrPreconditionFailed(headers, "Security role " + name, "Security role " + name + " was not found.");
        }
        return role;
    }

    /** A blank path name is left as it is: it names no role, so the lookup answers 404. */
    private static String normalizePathName(final String name) {
        return StringUtils.isBlank(name) ? name : normalizeName(name);
    }

    /** A name that already starts with ROLE_ is only upper-cased; any other gets the prefix. */
    private static String normalizeName(final String name) {
        final String upper = name.trim().toUpperCase(Locale.ROOT);
        return upper.startsWith(SecurityRoles.ROLE_PREFIX) ? upper : SecurityRoles.toRoleName(name.trim());
    }

    /** Returns a problem description, or null when the role name is acceptable. */
    private static String validateName(final String name) {
        if (name.length() > NAME_LENGTH) {
            return "The security role name must not be longer than " + NAME_LENGTH + " characters.";
        }
        if (!ROLE_NAME.matcher(name).matches()) {
            return "The security role name must be ROLE_ followed by letters, digits or underscores.";
        }
        return null;
    }

    private static String owner(final String name) {
        return "Security role \"" + name + "\"";
    }

    /** {@code userCount} is null, and left out of the JSON, for a single role: see {@link SecurityRoleDto}. */
    private static SecurityRoleDto toDto(final SecurityRole role, final Integer userCount, final int version) {
        final SecurityRoleDto dto = new SecurityRoleDto();
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setBuiltin(role.isBuiltin());
        dto.setUserCount(userCount);
        dto.setVersion(version);
        return dto;
    }
}
