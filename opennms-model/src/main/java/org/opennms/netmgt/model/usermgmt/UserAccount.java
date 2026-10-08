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
package org.opennms.netmgt.model.usermgmt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * An OpenNMS user: a {@code <user>} of users.xml. Named apart from {@code OnmsUser}, the REST/XML
 * representation, which stays the API contract.
 */
@Entity
@Table(name = "users")
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name = "users_seq", sequenceName = "users_id_seq", allocationSize = 1)
    private Integer id;

    /** The login name; case-sensitive, as in users.xml. */
    @Column(nullable = false, unique = true, length = 256)
    private String username;

    @Column(name = "full_name", length = 256)
    private String fullName;

    @Column(columnDefinition = "text")
    private String comments;

    /** The stored password: a hash when {@link #passwordSalted}, otherwise the legacy unsalted MD5 hex. */
    @Column(name = "password_hash", length = 512)
    private String passwordHash;

    @Column(name = "password_salted", nullable = false)
    private boolean passwordSalted;

    @Column(name = "tui_pin", length = 32)
    private String tuiPin;

    /** A {@link java.time.ZoneId} id, or null for the server's time zone. */
    @Column(name = "time_zone_id", length = 64)
    private String timeZoneId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false)
    @OrderBy("id")
    private List<UserContact> contacts = new ArrayList<>();

    /** Duty schedules in the users.xml format ({@code MoTuWe800-1700}), parsed by {@code DutySchedule}. */
    @ElementCollection
    @CollectionTable(name = "user_duty_schedules", joinColumns = @JoinColumn(name = "user_id"))
    @OrderColumn(name = "position")
    @Column(name = "schedule", nullable = false, length = 64)
    private List<String> dutySchedules = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "user_security_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<SecurityRole> securityRoles = new LinkedHashSet<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time", nullable = false)
    private Date createdTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    private Date lastModified;

    public UserAccount() {
    }

    public UserAccount(final String username) {
        this.username = username;
    }

    @PrePersist
    void onCreate() {
        final Date now = new Date();
        if (createdTime == null) {
            createdTime = now;
        }
        lastModified = now;
    }

    @PreUpdate
    void onUpdate() {
        lastModified = new Date();
    }

    /** @return the contact of that type, or null if the user has none */
    public UserContact getContact(final ContactType type) {
        for (final UserContact contact : contacts) {
            if (contact.getType() == type) {
                return contact;
            }
        }
        return null;
    }

    /**
     * Sets the contact of that type. An existing contact is updated in place, which keeps its row and id;
     * there is at most one per type ({@code uk_user_contacts_user_type}).
     */
    public UserContact setContact(final ContactType type, final String info, final String serviceProvider) {
        UserContact contact = getContact(type);
        if (contact == null) {
            contact = new UserContact(type, info, serviceProvider);
            contacts.add(contact);
        } else {
            contact.setInfo(info);
            contact.setServiceProvider(serviceProvider);
        }
        return contact;
    }

    /** @return true if the user had a contact of that type */
    public boolean removeContact(final ContactType type) {
        return contacts.removeIf(c -> c.getType() == type);
    }

    public boolean hasSecurityRole(final String roleName) {
        return securityRoles.stream().anyMatch(r -> roleName.equals(r.getName()));
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isPasswordSalted() {
        return passwordSalted;
    }

    public void setPasswordSalted(final boolean passwordSalted) {
        this.passwordSalted = passwordSalted;
    }

    public String getTuiPin() {
        return tuiPin;
    }

    public void setTuiPin(final String tuiPin) {
        this.tuiPin = tuiPin;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(final String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public List<UserContact> getContacts() {
        return contacts;
    }

    /** Replaces the contents, not the list: Hibernate rejects swapping out an orphan-removal collection. */
    public void setContacts(final List<UserContact> contacts) {
        if (contacts != this.contacts) {
            this.contacts.clear();
            this.contacts.addAll(contacts);
        }
    }

    public List<String> getDutySchedules() {
        return dutySchedules;
    }

    public void setDutySchedules(final List<String> dutySchedules) {
        this.dutySchedules = dutySchedules;
    }

    public Set<SecurityRole> getSecurityRoles() {
        return securityRoles;
    }

    public void setSecurityRoles(final Set<SecurityRole> securityRoles) {
        this.securityRoles = securityRoles;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(final Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "UserAccount[" + username + "]";
    }
}
