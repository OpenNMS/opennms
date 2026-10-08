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
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A group of users: a {@code <group>} of groups.xml. Named apart from {@code OnmsGroup}, the REST/XML
 * representation, and from {@code org.opennms.netmgt.config.GroupDao}.
 */
@Entity
@Table(name = "groups")
public class UserGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "groups_seq")
    @SequenceGenerator(name = "groups_seq", sequenceName = "groups_id_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false, unique = true, length = 256)
    private String name;

    @Column(name = "default_map", length = 256)
    private String defaultMap;

    @Column(columnDefinition = "text")
    private String comments;

    /**
     * The members in order; notifications to the group go out in this order. A user is listed at most once.
     * <p>
     * Remove a user from this list rather than relying on the {@code group_members} delete cascade alone:
     * a row deleted behind Hibernate's back leaves a gap in {@code position}, which loads as a null member.
     * {@code UserAccountDao.delete} does this for you.
     */
    @ManyToMany
    @JoinTable(name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @OrderColumn(name = "position")
    private List<UserAccount> members = new ArrayList<>();

    /** Duty schedules in the groups.xml format ({@code MoTuWe800-1700}), parsed by {@code DutySchedule}. */
    @ElementCollection
    @CollectionTable(name = "group_duty_schedules", joinColumns = @JoinColumn(name = "group_id"))
    @OrderColumn(name = "position")
    @Column(name = "schedule", nullable = false, length = 64)
    private List<String> dutySchedules = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time", nullable = false)
    private Date createdTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    private Date lastModified;

    public UserGroup() {
    }

    public UserGroup(final String name) {
        this.name = name;
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

    public boolean hasMember(final String username) {
        return members.stream().anyMatch(u -> username.equals(u.getUsername()));
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

    public String getDefaultMap() {
        return defaultMap;
    }

    public void setDefaultMap(final String defaultMap) {
        this.defaultMap = defaultMap;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public List<UserAccount> getMembers() {
        return members;
    }

    public void setMembers(final List<UserAccount> members) {
        this.members = members;
    }

    public List<String> getDutySchedules() {
        return dutySchedules;
    }

    public void setDutySchedules(final List<String> dutySchedules) {
        this.dutySchedules = dutySchedules;
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
        return "UserGroup[" + name + "]";
    }
}
