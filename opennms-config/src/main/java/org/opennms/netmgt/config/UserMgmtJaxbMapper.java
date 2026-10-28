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
package org.opennms.netmgt.config;

import java.lang.reflect.Field;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.Password;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.model.usermgmt.ContactType;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.OnCallScheduleTime;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserContact;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts between the usermgmt entities and the users.xml/groups.xml JAXB model that UserManager and GroupManager
 * cache. The apply methods change an entity in place, so Hibernate updates only what changed, and they reject what
 * the tables cannot hold with a message for the user. Values are copied exactly, never trimmed.
 */
public final class UserMgmtJaxbMapper {
    private static final Logger LOG = LoggerFactory.getLogger(UserMgmtJaxbMapper.class);

    public static final int USERNAME_LENGTH = 256;
    public static final int FULL_NAME_LENGTH = 256;
    public static final int PASSWORD_LENGTH = 512;
    public static final int TUI_PIN_LENGTH = 32;
    public static final int TIME_ZONE_ID_LENGTH = 64;
    public static final int CONTACT_LENGTH = 256;
    public static final int DUTY_SCHEDULE_LENGTH = 64;
    public static final int GROUP_NAME_LENGTH = 256;
    public static final int DEFAULT_MAP_LENGTH = 256;
    public static final int ONCALL_ROLE_NAME_LENGTH = 256;
    public static final int TIME_ID_LENGTH = 64;
    public static final int DAY_LENGTH = 16;
    public static final int TIME_LENGTH = 32;

    private UserMgmtJaxbMapper() {
    }

    public static Userinfo toUserinfo(final Collection<UserAccount> users) {
        final Userinfo userinfo = new Userinfo();
        userinfo.setUsers(users.stream().map(UserMgmtJaxbMapper::toUser).collect(Collectors.toList()));
        return userinfo;
    }

    public static User toUser(final UserAccount account) {
        final User user = new User();
        user.setUserId(account.getUsername());
        setField(user, "m_fullName", account.getFullName());
        setField(user, "m_userComments", account.getComments());
        if (account.getPasswordHash() != null) {
            user.setPassword(account.getPasswordHash(), account.isPasswordSalted());
        } else {
            // UserManager reads the password of every user, so a null would stop the whole user list (and all
            // logins); an empty unsalted hash matches no password
            LOG.warn("User \"{}\" has no password. The user cannot log in until a password is set.", account.getUsername());
            user.setPassword("", false);
        }
        for (final UserContact contact : account.getContacts()) {
            final Contact xml = new Contact();
            xml.setType(contact.getType().name());
            setField(xml, "m_info", contact.getInfo());
            setField(xml, "m_serviceProvider", contact.getServiceProvider());
            user.addContact(xml);
        }
        account.getDutySchedules().forEach(user::addDutySchedule);
        account.getSecurityRoles().stream().map(SecurityRole::getName).sorted().forEach(user::addRole);
        user.setTuiPin(account.getTuiPin());
        if (account.getTimeZoneId() != null) {
            try {
                user.setTimeZoneId(ZoneId.of(account.getTimeZoneId()));
            } catch (final DateTimeException e) {
                LOG.warn("User \"{}\" has the time zone \"{}\", which Java does not know. The time zone is ignored.",
                        account.getUsername(), account.getTimeZoneId());
            }
        }
        return user;
    }

    public static Groupinfo toGroupinfo(final Collection<UserGroup> groups, final Collection<OnCallRole> roles) {
        final Groupinfo groupinfo = new Groupinfo();
        groupinfo.setGroups(groups.stream().map(UserMgmtJaxbMapper::toGroup).collect(Collectors.toList()));
        groupinfo.setRoles(roles.stream().map(UserMgmtJaxbMapper::toRole).collect(Collectors.toList()));
        return groupinfo;
    }

    public static Group toGroup(final UserGroup group) {
        final Group xml = new Group();
        setField(xml, "m_name", group.getName());
        setField(xml, "m_defaultMap", group.getDefaultMap());
        setField(xml, "m_comments", group.getComments());
        group.getMembers().forEach(m -> xml.addUser(m.getUsername()));
        group.getDutySchedules().forEach(xml::addDutySchedule);
        return xml;
    }

    public static Role toRole(final OnCallRole role) {
        final Role xml = new Role();
        xml.setName(role.getName());
        xml.setMembershipGroup(role.getMembershipGroup().getName());
        xml.setSupervisor(role.getSupervisor().getUsername());
        setField(xml, "m_description", role.getDescription());
        for (final OnCallSchedule schedule : role.getSchedules()) {
            xml.addSchedule(toSchedule(schedule));
        }
        return xml;
    }

    private static Schedule toSchedule(final OnCallSchedule schedule) {
        final Schedule xml = new Schedule();
        xml.setName(schedule.getUser().getUsername());
        xml.setType(schedule.getType().name());
        for (final OnCallScheduleTime time : schedule.getTimes()) {
            final Time xmlTime = new Time();
            xmlTime.setId(time.getTimeId());
            xmlTime.setDay(time.getDay());
            xmlTime.setBegins(time.getBegins());
            xmlTime.setEnds(time.getEnds());
            xml.addTime(xmlTime);
        }
        return xml;
    }

    public static void applyUser(final User source, final UserAccount target, final Map<String, SecurityRole> securityRolesByName) {
        final String owner = "User \"" + source.getUserId() + "\"";
        target.setUsername(required(owner, "user name", source.getUserId(), USERNAME_LENGTH));
        target.setFullName(checkLength(owner, "full name", source.getFullName().orElse(null), FULL_NAME_LENGTH));
        target.setComments(source.getUserComments().orElse(null));
        final Password password = source.getPassword();
        // no password, or the empty one that toUser gives a user without a password: store none
        final boolean noPassword = password == null || password.getEncryptedPassword() == null || password.getEncryptedPassword().isEmpty();
        target.setPasswordHash(noPassword ? null
                : checkLength(owner, "password", password.getEncryptedPassword(), PASSWORD_LENGTH));
        target.setPasswordSalted(!noPassword && Boolean.TRUE.equals(password.getSalt()));
        target.setTuiPin(checkLength(owner, "TUI PIN", source.getTuiPin().orElse(null), TUI_PIN_LENGTH));
        target.setTimeZoneId(checkLength(owner, "time zone",
                source.getTimeZoneId().map(ZoneId::getId).orElse(null), TIME_ZONE_ID_LENGTH));

        final Set<ContactType> seen = EnumSet.noneOf(ContactType.class);
        for (final Contact contact : source.getContacts()) {
            final ContactType type = contactType(owner, contact.getType());
            if (!seen.add(type)) {
                continue; // UserManager only ever uses the first contact of a type
            }
            target.setContact(type,
                    checkLength(owner, type + " contact", contact.getInfo().orElse(null), CONTACT_LENGTH),
                    checkLength(owner, type + " service provider", contact.getServiceProvider().orElse(null), CONTACT_LENGTH));
        }
        target.getContacts().removeIf(c -> !seen.contains(c.getType()));

        replaceIfChanged(target.getDutySchedules(), checkAll(owner, "duty schedule", source.getDutySchedules(), DUTY_SCHEDULE_LENGTH));

        final Set<SecurityRole> roles = new HashSet<>();
        for (final String name : source.getRoles()) {
            final SecurityRole role = securityRolesByName.get(name);
            if (role == null) {
                throw new IllegalArgumentException(owner + " has the unknown security role \"" + name + "\".");
            }
            roles.add(role);
        }
        if (!roles.equals(target.getSecurityRoles())) {
            target.getSecurityRoles().clear();
            target.getSecurityRoles().addAll(roles);
        }
    }

    public static void applyGroup(final Group source, final UserGroup target, final Map<String, UserAccount> usersByName) {
        final String owner = "Group \"" + source.getName() + "\"";
        target.setName(required(owner, "name", source.getName(), GROUP_NAME_LENGTH));
        target.setDefaultMap(checkLength(owner, "default map", source.getDefaultMap().orElse(null), DEFAULT_MAP_LENGTH));
        target.setComments(source.getComments().orElse(null));

        final List<UserAccount> members = new ArrayList<>();
        final Set<String> seen = new HashSet<>();
        for (final String username : source.getUsers()) {
            if (!seen.add(username)) {
                continue; // group_members holds a user once per group
            }
            final UserAccount user = usersByName.get(username);
            if (user == null) {
                throw new IllegalArgumentException(owner + " has the member \"" + username + "\", which is not a user.");
            }
            members.add(user);
        }
        replaceIfChanged(target.getMembers(), members);
        replaceIfChanged(target.getDutySchedules(), checkAll(owner, "duty schedule", source.getDutySchedules(), DUTY_SCHEDULE_LENGTH));
    }

    public static void applyRole(final Role source, final OnCallRole target, final Map<String, UserGroup> groupsByName,
                                 final Map<String, UserAccount> usersByName) {
        final String owner = "On-call role \"" + source.getName() + "\"";
        target.setName(required(owner, "name", source.getName(), ONCALL_ROLE_NAME_LENGTH));
        target.setDescription(source.getDescription().orElse(null));
        final UserGroup group = groupsByName.get(source.getMembershipGroup());
        if (group == null) {
            throw new IllegalArgumentException(owner + " has the membership group \"" + source.getMembershipGroup() + "\", which is not a group.");
        }
        target.setMembershipGroup(group);
        final UserAccount supervisor = usersByName.get(source.getSupervisor());
        if (supervisor == null) {
            throw new IllegalArgumentException(owner + " has the supervisor \"" + source.getSupervisor() + "\", which is not a user.");
        }
        target.setSupervisor(supervisor);

        // rebuild the schedules only if they changed: a rebuild deletes and inserts all of their rows
        final List<Schedule> current = target.getSchedules().stream().map(UserMgmtJaxbMapper::toSchedule).collect(Collectors.toList());
        if (current.equals(source.getSchedules())) {
            return;
        }
        final List<OnCallSchedule> schedules = new ArrayList<>();
        for (final Schedule schedule : source.getSchedules()) {
            final UserAccount user = usersByName.get(schedule.getName());
            if (user == null) {
                throw new IllegalArgumentException(owner + " has a schedule for \"" + schedule.getName() + "\", which is not a user.");
            }
            final OnCallSchedule entity = new OnCallSchedule(user, scheduleType(owner, schedule.getType()));
            for (final Time time : schedule.getTimes()) {
                final OnCallScheduleTime entityTime = new OnCallScheduleTime(
                        checkLength(owner, "schedule day", time.getDay().orElse(null), DAY_LENGTH),
                        required(owner, "schedule begin time", time.getBegins(), TIME_LENGTH),
                        required(owner, "schedule end time", time.getEnds(), TIME_LENGTH));
                entityTime.setTimeId(checkLength(owner, "schedule time id", time.getId().orElse(null), TIME_ID_LENGTH));
                entity.getTimes().add(entityTime);
            }
            schedules.add(entity);
        }
        target.getSchedules().clear();
        target.getSchedules().addAll(schedules);
    }

    /**
     * Sets a JAXB field directly, the way JAXB does when it reads users.xml and groups.xml. Some setters change the
     * value (User.setUserComments and Role.setDescription turn an empty text into null, the Group setters trim it), so
     * using them on every load would change the stored text, and the next save would write the change back.
     */
    private static void setField(final Object target, final String field, final Object value) {
        try {
            final Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot set " + target.getClass().getSimpleName() + "." + field, e);
        }
    }

    /**
     * @return the value, if it fits a column of max characters (counted in code points, as PostgreSQL does)
     * @throws IllegalArgumentException if it does not fit
     */
    public static String checkLength(final String owner, final String field, final String value, final int max) {
        if (value != null && value.codePointCount(0, value.length()) > max) {
            throw new IllegalArgumentException(owner + ": the " + field + " is longer than " + max + " characters.");
        }
        return value;
    }

    private static String required(final String owner, final String field, final String value, final int max) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(owner + ": the " + field + " is missing.");
        }
        return checkLength(owner, field, value, max);
    }

    private static List<String> checkAll(final String owner, final String field, final List<String> values, final int max) {
        values.forEach(v -> checkLength(owner, field, v, max));
        return values;
    }

    /** Changes the list only if it differs, so Hibernate does not rewrite unchanged collection rows. */
    private static <T> void replaceIfChanged(final List<T> target, final List<T> values) {
        if (!target.equals(values)) {
            target.clear();
            target.addAll(values);
        }
    }

    private static ContactType contactType(final String owner, final String type) {
        try {
            return ContactType.valueOf(type);
        } catch (final IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(owner + " has a contact of the unknown type \"" + type + "\".");
        }
    }

    private static OnCallSchedule.Type scheduleType(final String owner, final String type) {
        try {
            return OnCallSchedule.Type.valueOf(type);
        } catch (final IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(owner + " has a schedule of the unknown type \"" + type + "\".");
        }
    }
}
