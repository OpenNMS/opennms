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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.opennms.netmgt.config.UserMgmtJaxbMapper;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.VersionedTable;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.web.rest.v2.api.GroupsRestApi;
import org.opennms.web.rest.v2.model.GroupDto;
import org.opennms.web.rest.v2.model.GroupRenameRequest;
import org.opennms.web.svclayer.api.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Versioned group management on the group DAOs. Each request is one transaction: a write takes the
 * user-management write lock first, so the If-Match check is exact, and the in-memory managers reload
 * once it commits. The category authorizations, which live in the categories table, move or clear in the
 * same transaction as the group.
 *
 * Errors are thrown, never returned, so that a rejected request rolls back whatever it had done.
 */
@Component("groupsRestServiceV2")
public class GroupsRestService implements GroupsRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(GroupsRestService.class);

    /** The default group; the on-call role machinery references it. */
    private static final Set<String> PROTECTED_GROUPS = Set.of("Admin");

    @Autowired
    private UserMgmtRestSupport m_support;

    @Autowired
    private UserGroupDao m_userGroupDao;

    @Autowired
    private UserAccountDao m_userAccountDao;

    /** For the membership-group referential check on delete. */
    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    /** Moves or clears the category authorizations, which are stored with the categories. */
    @Autowired
    private GroupService m_groupService;

    /** For validating the category names of a request. */
    @Autowired
    private CategoryDao m_categoryDao;

    @Override
    @Transactional(readOnly = true)
    public Response listGroups(final SecurityContext securityContext) {
        m_support.assertAdmin(securityContext, "Group management");
        m_support.requireDatabaseStore();
        try {
            // one pass over the categories, so that the number of statements does not grow with the groups
            final Map<String, List<String>> categoriesByGroup = new HashMap<>();
            for (final OnmsCategory category : m_categoryDao.findAll()) {
                for (final String groupName : category.getAuthorizedGroups()) {
                    categoriesByGroup.computeIfAbsent(groupName, k -> new ArrayList<>()).add(category.getName());
                }
            }
            final List<GroupDto> groups = new ArrayList<>();
            for (final UserGroup group : m_userGroupDao.findAll()) {
                groups.add(toDto(group, group.getVersion(), categoriesByGroup.getOrDefault(group.getName(), List.of())));
            }
            groups.sort(Comparator.comparing(GroupDto::getName, String.CASE_INSENSITIVE_ORDER));
            return Response.ok(groups).build();
        } catch (final Exception e) {
            throw m_support.translate("read groups", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Response listAvailableCategories(final SecurityContext securityContext) {
        m_support.assertAdmin(securityContext, "Group management");
        m_support.requireDatabaseStore();
        try {
            final List<String> categories = m_categoryDao.findAll().stream()
                    .map(OnmsCategory::getName)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
            return Response.ok(categories).build();
        } catch (final Exception e) {
            throw m_support.translate("read categories", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Response getGroup(final SecurityContext securityContext, final String name) {
        return doGetGroup(securityContext, name);
    }

    @Override
    @Transactional(readOnly = true)
    public Response getGroupByQuery(final SecurityContext securityContext, final String name) {
        m_support.assertAdmin(securityContext, "Group management");
        m_support.requireDatabaseStore();
        if (StringUtils.isBlank(name)) {
            throw m_support.badRequest("name is required");
        }
        return doGetGroup(securityContext, name);
    }

    private Response doGetGroup(final SecurityContext securityContext, final String name) {
        m_support.assertAdmin(securityContext, "Group management");
        m_support.requireDatabaseStore();
        try {
            final UserGroup group = load(name);
            final int version = group.getVersion();
            return Response.ok(toDto(group, version, authorizedCategories(group.getName()))).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("read group", e);
        }
    }

    @Override
    @Transactional
    public Response createGroup(final SecurityContext securityContext, final GroupDto dto) {
        m_support.assertAdmin(securityContext, "Group management");
        m_support.requireDatabaseStore();
        if (dto == null || StringUtils.isBlank(dto.getName())) {
            throw m_support.badRequest("A group name is required.");
        }
        final String name = dto.getName().trim();
        final String nameProblem = UserMgmtValidation.validateName(name, "group name");
        if (nameProblem != null) {
            throw m_support.badRequest(nameProblem);
        }
        checkNotReserved(name);
        try {
            m_support.lockForWrite();
            if (m_userGroupDao.findByName(name) != null) {
                throw m_support.conflict("Group " + name + " already exists.");
            }
            UserMgmtValidation.checkLength(owner(name), "group name", name, UserMgmtJaxbMapper.GROUP_NAME_LENGTH);
            final List<UserAccount> members = validateDtoFields(dto, null, name);
            final UserGroup group = new UserGroup(name);
            applyDto(group, dto, members);
            m_userGroupDao.saveOrUpdate(group);
            applyCategories(name, dto);
            m_support.flushAndCheckConstraints();
            final int version = m_support.version(VersionedTable.groups, group.getId());
            LOG.info("Group {} created by {}", name, UserMgmtValidation.principal(securityContext));
            return Response.status(Status.CREATED).entity(toDto(group, version, authorizedCategories(group.getName()))).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("create group", e);
        }
    }

    @Override
    @Transactional
    public Response updateGroup(final SecurityContext securityContext, final HttpHeaders headers, final String name, final GroupDto dto) {
        return doUpdateGroup(securityContext, headers, name, dto);
    }

    @Override
    @Transactional
    public Response updateGroupByQuery(final SecurityContext securityContext, final HttpHeaders headers, final String name, final GroupDto dto) {
        m_support.assertAdmin(securityContext, "Group management");
        m_support.requireDatabaseStore();
        if (StringUtils.isBlank(name)) {
            throw m_support.badRequest("name is required");
        }
        return doUpdateGroup(securityContext, headers, name, dto);
    }

    private Response doUpdateGroup(final SecurityContext securityContext, final HttpHeaders headers, final String name, final GroupDto dto) {
        m_support.assertAdmin(securityContext, "Group management");
        m_support.requireDatabaseStore();
        if (dto == null) {
            throw m_support.badRequest("A group body is required.");
        }
        if (dto.getName() != null && !name.equals(dto.getName())) {
            throw m_support.badRequest("The name in the body does not match the request path; use the rename endpoint to change names.");
        }
        try {
            m_support.lockForWrite();
            final UserGroup group = load(name, headers);
            m_support.checkIfMatch(headers, VersionedTable.groups, group.getId(), "Group " + name);
            final List<UserAccount> members = validateDtoFields(dto, group, name);
            applyDto(group, dto, members);
            m_userGroupDao.saveOrUpdate(group);
            applyCategories(name, dto);
            m_support.flushAndCheckConstraints();
            final int version = m_support.version(VersionedTable.groups, group.getId());
            return Response.ok(toDto(group, version, authorizedCategories(group.getName()))).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("update group", e);
        }
    }

    @Override
    @Transactional
    public Response renameGroup(final SecurityContext securityContext, final HttpHeaders headers, final String name, final GroupRenameRequest body) {
        return doRenameGroup(securityContext, headers, name, body);
    }

    @Override
    @Transactional
    public Response renameGroupByQuery(final SecurityContext securityContext, final HttpHeaders headers, final String name, final GroupRenameRequest body) {
        m_support.assertAdmin(securityContext, "Group management");
        m_support.requireDatabaseStore();
        if (StringUtils.isBlank(name)) {
            throw m_support.badRequest("name is required");
        }
        return doRenameGroup(securityContext, headers, name, body);
    }

    /** A look-alike of a protected name, such as admin, would be mistaken for the system group; existing ones are left alone. */
    private void checkNotReserved(final String name) {
        for (final String reserved : PROTECTED_GROUPS) {
            if (reserved.equalsIgnoreCase(name) && !reserved.equals(name)) {
                throw m_support.badRequest("The group name \"" + name + "\" is reserved.");
            }
        }
    }

    private Response doRenameGroup(final SecurityContext securityContext, final HttpHeaders headers, final String name, final GroupRenameRequest body) {
        m_support.assertAdmin(securityContext, "Group management");
        m_support.requireDatabaseStore();
        if (body == null || StringUtils.isBlank(body.getNewName())) {
            throw m_support.badRequest("A newName is required.");
        }
        if (PROTECTED_GROUPS.contains(name)) {
            throw m_support.badRequest("The system group " + name + " cannot be renamed.");
        }
        final String newName = body.getNewName().trim();
        final String nameProblem = UserMgmtValidation.validateName(newName, "group name");
        if (nameProblem != null) {
            throw m_support.badRequest(nameProblem);
        }
        checkNotReserved(newName);
        if (newName.equals(name)) {
            throw m_support.badRequest("The newName is the same as the current name.");
        }
        try {
            m_support.lockForWrite();
            final UserGroup group = load(name, headers);
            m_support.checkIfMatch(headers, VersionedTable.groups, group.getId(), "Group " + name);
            if (m_userGroupDao.findByName(newName) != null) {
                throw m_support.conflict("Group " + newName + " already exists.");
            }
            UserMgmtValidation.checkLength(owner(newName), "group name", newName, UserMgmtJaxbMapper.GROUP_NAME_LENGTH);
            // on-call roles reference the group row and follow the new name; the category authorizations
            // store the name, so they move here, in the same transaction
            group.setName(newName);
            m_userGroupDao.saveOrUpdate(group);
            m_groupService.moveCategoryAuthorizations(name, newName);
            m_support.flushAndCheckConstraints();
            final int version = m_support.version(VersionedTable.groups, group.getId());
            LOG.info("Group {} renamed to {} by {}", name, newName, UserMgmtValidation.principal(securityContext));
            return Response.ok(toDto(group, version, authorizedCategories(group.getName()))).tag(m_support.etag(version)).build();
        } catch (final Exception e) {
            throw m_support.translate("rename group", e);
        }
    }

    @Override
    @Transactional
    public Response deleteGroup(final SecurityContext securityContext, final HttpHeaders headers, final String name) {
        return doDeleteGroup(securityContext, headers, name);
    }

    @Override
    @Transactional
    public Response deleteGroupByQuery(final SecurityContext securityContext, final HttpHeaders headers, final String name) {
        m_support.assertAdmin(securityContext, "Group management");
        m_support.requireDatabaseStore();
        if (StringUtils.isBlank(name)) {
            throw m_support.badRequest("name is required");
        }
        return doDeleteGroup(securityContext, headers, name);
    }

    private Response doDeleteGroup(final SecurityContext securityContext, final HttpHeaders headers, final String name) {
        m_support.assertAdmin(securityContext, "Group management");
        m_support.requireDatabaseStore();
        if (PROTECTED_GROUPS.contains(name)) {
            throw m_support.badRequest("The system group " + name + " cannot be deleted.");
        }
        try {
            m_support.lockForWrite();
            final UserGroup group = load(name, headers);
            m_support.checkIfMatch(headers, VersionedTable.groups, group.getId(), "Group " + name);
            final List<String> referencingRoles = m_onCallRoleDao.findByMembershipGroup(name).stream()
                    .map(OnCallRole::getName)
                    .sorted()
                    .collect(Collectors.toList());
            if (!referencingRoles.isEmpty()) {
                throw m_support.conflict("Group " + name + " is the membership group of on-call role(s) "
                        + String.join(", ", referencingRoles) + "; delete or reassign those roles first.");
            }
            m_support.checkNotDestinationPathTarget("Group " + name, name);
            m_userGroupDao.delete(group);
            // without this, a future group reusing the name would silently inherit the old authorizations
            m_groupService.clearCategoryAuthorizations(name);
            m_support.flushAndCheckConstraints();
            LOG.info("Group {} deleted by {}", name, UserMgmtValidation.principal(securityContext));
            return Response.noContent().build();
        } catch (final Exception e) {
            throw m_support.translate("delete group", e);
        }
    }

    /** @throws javax.ws.rs.WebApplicationException 404 if there is no such object */
    private UserGroup load(final String name) {
        return load(name, null);
    }

    /** For a write: a missing object is a 412 instead of a 404 when the request carries an If-Match. */
    private UserGroup load(final String name, final HttpHeaders headers) {
        final UserGroup group = m_userGroupDao.findByName(name);
        if (group == null) {
            throw m_support.notFoundOrPreconditionFailed(headers, "Group " + name, "Group " + name + " was not found.");
        }
        return group;
    }

    /** default-map is not exposed by the API. */
    private List<String> authorizedCategories(final String groupName) {
        return m_groupService.getAuthorizedCategoriesAsString(groupName);
    }

    private static GroupDto toDto(final UserGroup group, final int version, final List<String> categories) {
        final GroupDto dto = new GroupDto();
        dto.setName(group.getName());
        dto.setVersion(version);
        dto.setComments(group.getComments());
        dto.setUsers(group.getMembers().stream().map(UserAccount::getUsername).collect(Collectors.toList()));
        dto.setDutySchedules(new ArrayList<>(group.getDutySchedules()));
        dto.setCategories(categories.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList()));
        return dto;
    }

    /**
     * Validates every field of the request BEFORE anything is applied, so a rejected request cannot
     * leave partial state anywhere.
     *
     * @param name the group's name as it is or will be stored, for the error messages
     * @return the accounts of the members the request names, in request order, or null if it leaves the members alone
     */
    private List<UserAccount> validateDtoFields(final GroupDto dto, final UserGroup existing, final String name) {
        final String owner = owner(name);
        // stored values that today's rules would reject are grandfathered, so a migrated group stays editable
        final String storedComments = existing == null ? null : existing.getComments();
        UserMgmtValidation.checkComments(owner, dto.getComments(), storedComments);
        List<UserAccount> members = null;
        if (dto.getUsers() != null) {
            final Set<String> seen = new LinkedHashSet<>();
            for (final String user : dto.getUsers()) {
                if (StringUtils.isBlank(user)) {
                    throw new UserMgmtValidationException("Group members must not be blank.");
                }
                if (!seen.add(user)) {
                    throw new UserMgmtValidationException("Duplicate group member: " + user);
                }
            }
            // the database only holds members that exist, so any name that doesn't resolve is new to the group
            final Map<String, UserAccount> accounts = new HashMap<>();
            if (!seen.isEmpty()) {
                for (final UserAccount account : m_userAccountDao.findByUsernames(seen)) {
                    accounts.put(account.getUsername(), account);
                }
            }
            members = new ArrayList<>();
            for (final String user : dto.getUsers()) {
                final UserAccount account = accounts.get(user);
                if (account == null) {
                    throw new UserMgmtValidationException("Unknown user: " + user);
                }
                members.add(account);
            }
        }
        if (dto.getDutySchedules() != null) {
            // migrated data never makes a group uneditable: see UserMgmtValidation.validateDutySchedules
            UserMgmtValidation.validateDutySchedules(owner, dto.getDutySchedules(), existing == null ? null : existing.getDutySchedules());
        }
        if (dto.getCategories() != null) {
            // the first unknown name in sorted order is the one reported
            for (final String category : dto.getCategories().stream().sorted(Comparator.nullsFirst(Comparator.<String>naturalOrder())).collect(Collectors.toList())) {
                if (category == null || m_categoryDao.findByName(category) == null) {
                    throw new UserMgmtValidationException("Unknown category: " + category);
                }
            }
        }
        return members;
    }

    /** Runs after the group row is saved; an omitted list leaves the authorizations alone, and an unchanged one writes nothing. */
    private void applyCategories(final String name, final GroupDto dto) {
        if (dto.getCategories() != null) {
            m_groupService.setAuthorizedCategories(name, dto.getCategories());
        }
    }

    /**
     * Applies the pre-validated DTO. default-map is not exposed by the API and
     * survives untouched; list fields left out of the request body arrive as
     * null and are preserved. The member order is kept exactly as sent -
     * it drives the notification escalation order.
     */
    private static void applyDto(final UserGroup group, final GroupDto dto, final List<UserAccount> members) {
        if (dto.getComments() != null && !UserMgmtValidation.textUnchanged(dto.getComments(), group.getComments())) {
            group.setComments(StringUtils.trimToNull(dto.getComments()));
        }
        // an unchanged list is left alone, so that no row is rewritten and the version stays put
        UserMgmtValidation.replaceIfChanged(group.getMembers(), members);
        UserMgmtValidation.replaceIfChanged(group.getDutySchedules(), dto.getDutySchedules());
    }

    private static String owner(final String name) {
        return "Group \"" + name + "\"";
    }
}
