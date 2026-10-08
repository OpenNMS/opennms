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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * When one user is on call for an {@link OnCallRole}: a {@code <schedule>} of a groups.xml role, whose
 * {@code name} attribute is the user.
 */
@Entity
@Table(name = "oncall_schedules")
public class OnCallSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The schedule types of groups.xml; the constant names are what is stored. */
    public enum Type {
        specific,
        daily,
        weekly,
        monthly
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oncall_schedules_seq")
    @SequenceGenerator(name = "oncall_schedules_seq", sequenceName = "oncall_schedules_id_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 16)
    private Type type;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "schedule_id", nullable = false)
    @OrderColumn(name = "position")
    private List<OnCallScheduleTime> times = new ArrayList<>();

    public OnCallSchedule() {
    }

    public OnCallSchedule(final UserAccount user, final Type type) {
        this.user = user;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(final UserAccount user) {
        this.user = user;
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public List<OnCallScheduleTime> getTimes() {
        return times;
    }

    /** Replaces the contents, not the list: Hibernate rejects swapping out an orphan-removal collection. */
    public void setTimes(final List<OnCallScheduleTime> times) {
        if (times != this.times) {
            this.times.clear();
            this.times.addAll(times);
        }
    }
}
