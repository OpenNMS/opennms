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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * An on-call role: a notification rota, a {@code <role>} of groups.xml. Its members come from the membership
 * group, its supervisor is notified when nobody is on call, and its schedules say who is on call when.
 * Not to be confused with {@link SecurityRole}.
 * <p>
 * The database refuses to delete the membership group or the supervisor while a role refers to them.
 */
@Entity
@Table(name = "oncall_roles")
public class OnCallRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oncall_roles_seq")
    @SequenceGenerator(name = "oncall_roles_seq", sequenceName = "oncall_roles_id_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false, unique = true, length = 256)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_group_id", nullable = false)
    private UserGroup membershipGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supervisor_user_id", nullable = false)
    private UserAccount supervisor;

    /**
     * The schedules in order. Remove a user's schedules from this list rather than relying on the
     * {@code oncall_schedules} delete cascade alone, see {@link UserGroup#getMembers()};
     * {@code UserAccountDao.delete} does this for you.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "oncall_role_id", nullable = false)
    @OrderColumn(name = "position")
    private List<OnCallSchedule> schedules = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time", nullable = false)
    private Date createdTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    private Date lastModified;

    public OnCallRole() {
    }

    public OnCallRole(final String name, final UserGroup membershipGroup, final UserAccount supervisor) {
        this.name = name;
        this.membershipGroup = membershipGroup;
        this.supervisor = supervisor;
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

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public UserGroup getMembershipGroup() {
        return membershipGroup;
    }

    public void setMembershipGroup(final UserGroup membershipGroup) {
        this.membershipGroup = membershipGroup;
    }

    public UserAccount getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(final UserAccount supervisor) {
        this.supervisor = supervisor;
    }

    public List<OnCallSchedule> getSchedules() {
        return schedules;
    }

    /** Replaces the contents, not the list: Hibernate rejects swapping out an orphan-removal collection. */
    public void setSchedules(final List<OnCallSchedule> schedules) {
        if (schedules != this.schedules) {
            this.schedules.clear();
            this.schedules.addAll(schedules);
        }
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
        return "OnCallRole[" + name + "]";
    }
}
