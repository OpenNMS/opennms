/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.StringTrimAdapter;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("users.xsd")
@XmlType(propOrder={"m_userId", "m_fullName", "m_userComments", "m_password", "contacts", "dutySchedules", "roles", "m_tuiPin"})
public class User implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "user-id", required = true)
    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String m_userId;

    @XmlElement(name = "full-name")
    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String m_fullName;

    @XmlElement(name = "user-comments")
    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String m_userComments;

    @XmlElement(name = "password", required = true)
    private Password m_password;

    private List<Contact> m_contacts = new ArrayList<>();

    private List<String> m_dutySchedules = new ArrayList<>();

    private List<String> m_roles = new ArrayList<>();

    @XmlElement(name = "tui-pin")
    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String m_tuiPin;

    public User() {
    }

    public User(final String userId) {
        m_userId = userId;
    }

    public User(final String userId, final String fullName, final String userComments) {
        setUserId(userId);
        setFullName(fullName);
        setUserComments(userComments);
    }

    public String getUserId() {
        return m_userId;
    }

    public void setUserId(final String userId) {
        ConfigUtils.assertNotEmpty(userId, "user-id");
        m_userId = userId;
    }

    public Optional<String> getFullName() {
        return Optional.ofNullable(m_fullName);
    }

    public void setFullName(final String fullName) {
        m_fullName = ConfigUtils.normalizeString(fullName);
    }

    public Optional<String> getUserComments() {
        return Optional.ofNullable(m_userComments);
    }

    public void setUserComments(final String userComments) {
        m_userComments = ConfigUtils.normalizeString(userComments);
    }

    public Password getPassword() {
        return m_password;
    }

    public void setPassword(final Password password) {
        ConfigUtils.assertNotNull(password, "password");
        m_password = password;
    }

    public void setPassword(final String password, final Boolean salt) {
        m_password = new Password(password, salt);
    }

    @XmlElement(name = "contact")
    public List<Contact> getContacts() {
        return m_contacts;
    }

    public void addContact(final Contact contact) {
        m_contacts.add(contact);
    }

    public void setContacts(final List<Contact> contacts) {
        if (m_contacts == contacts) return;
        m_contacts.clear();
        if (contacts != null) m_contacts.addAll(contacts);
    }

    public void clearContacts() {
        m_contacts.clear();
    }

    @XmlElement(name = "duty-schedule")
    public List<String> getDutySchedules() {
        return m_dutySchedules;
    }

    public void setDutySchedules(final List<String> dutySchedules) {
        if (m_dutySchedules == dutySchedules) return;
        m_dutySchedules.clear();
        if (dutySchedules != null) m_dutySchedules.addAll(dutySchedules.stream().map(ConfigUtils::normalizeAndTrimString).collect(Collectors.toList()));
    }

    public void addDutySchedule(final String dutySchedule) {
        m_dutySchedules.add(dutySchedule);
    }

    public void clearDutySchedules() {
        m_dutySchedules.clear();
    }

    @XmlElement(name = "role")
    public List<String> getRoles() {
        return m_roles;
    }

    public void setRoles(final List<String> roles) {
        if (roles == m_roles) return;
        m_roles.clear();
        if (roles != null) m_roles.addAll(roles.stream().map(ConfigUtils::normalizeAndTrimString).collect(Collectors.toList()));
    }

    public void addRole(final String role) {
        m_roles.add(role);
    }

    public void clearRoles() {
        m_roles.clear();
    }

    public Optional<String> getTuiPin() {
        return Optional.ofNullable(m_tuiPin);
    }

    public void setTuiPin(final String tuiPin) {
        m_tuiPin = tuiPin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_userId, m_fullName, m_userComments,
                            m_password, m_contacts, m_dutySchedules,
                            m_roles, m_tuiPin);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof User) {
            final User temp = (User) obj;
            return Objects.equals(temp.m_userId, m_userId)
                    && Objects.equals(temp.m_fullName, m_fullName)
                    && Objects.equals(temp.m_userComments, m_userComments)
                    && Objects.equals(temp.m_password, m_password)
                    && Objects.equals(temp.m_contacts, m_contacts)
                    && Objects.equals(temp.m_dutySchedules, m_dutySchedules)
                    && Objects.equals(temp.m_roles, m_roles)
                    && Objects.equals(temp.m_tuiPin, m_tuiPin);
        }
        return false;
    }

    @Override
    public String toString() {
        return "User[userId=" + m_userId + ", fullName=" + m_fullName
                + ", userComments=" + m_userComments + ", password="
                + m_password + ", contacts=" + m_contacts
                + ", dutySchedules=" + m_dutySchedules + ", roles="
                + m_roles + ", tuiPin=" + m_tuiPin + "]";
    }

}
