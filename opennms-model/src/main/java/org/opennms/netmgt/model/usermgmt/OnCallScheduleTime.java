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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * One interval of an {@link OnCallSchedule}: a {@code <time>} of groups.xml. The strings are stored as written
 * there; their format depends on the schedule's type ({@code dd-MMM-yyyy HH:mm:ss} for {@code specific},
 * {@code HH:mm:ss} otherwise) and {@code BasicSchedule} parses them.
 */
@Entity
@Table(name = "oncall_schedule_times")
public class OnCallScheduleTime implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oncall_schedule_times_seq")
    @SequenceGenerator(name = "oncall_schedule_times_seq", sequenceName = "oncall_schedule_times_id_seq", allocationSize = 1)
    private Integer id;

    /** The optional {@code id} attribute of the {@code <time>} element. */
    @Column(name = "time_id", length = 64)
    private String timeId;

    /** The weekday or day of month for {@code weekly} and {@code monthly} schedules, else null. */
    @Column(length = 16)
    private String day;

    @Column(nullable = false, length = 32)
    private String begins;

    @Column(nullable = false, length = 32)
    private String ends;

    public OnCallScheduleTime() {
    }

    public OnCallScheduleTime(final String day, final String begins, final String ends) {
        this.day = day;
        this.begins = begins;
        this.ends = ends;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getTimeId() {
        return timeId;
    }

    public void setTimeId(final String timeId) {
        this.timeId = timeId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(final String day) {
        this.day = day;
    }

    public String getBegins() {
        return begins;
    }

    public void setBegins(final String begins) {
        this.begins = begins;
    }

    public String getEnds() {
        return ends;
    }

    public void setEnds(final String ends) {
        this.ends = ends;
    }
}
