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
package org.opennms.netmgt.config.groups;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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

@XmlRootElement(name = "group")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("groups.xsd")
@XmlType(propOrder={"m_name", "m_defaultMap", "m_comments", "users", "dutySchedules"})
public class Group implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "name", required = true)
    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String m_name;

    @XmlElement(name = "default-map")
    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String m_defaultMap;

    @XmlElement(name = "comments")
    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String m_comments;

    private List<String> m_users = new ArrayList<>();

    private List<String> m_dutySchedules = new ArrayList<>();

    public Group() {
    }

    public Group(final String name, final String... users) {
        setName(name);
        setUsers(Arrays.asList(users));
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(ConfigUtils.normalizeAndTrimString(name), "name");
    }

    public Optional<String> getDefaultMap() {
        return Optional.ofNullable(m_defaultMap);
    }

    public void setDefaultMap(final String defaultMap) {
        m_defaultMap = ConfigUtils.normalizeAndTrimString(defaultMap);
    }

    public Optional<String> getComments() {
        return Optional.ofNullable(m_comments);
    }

    public void setComments(final String comments) {
        m_comments = ConfigUtils.normalizeAndTrimString(comments);
    }

    @XmlElement(name = "user")
    public List<String> getUsers() {
        return m_users;
    }

    public void setUsers(final List<String> users) {
        if (users == m_users) return;
        m_users.clear();
        if (users != null) m_users.addAll(users.stream().map(ConfigUtils::normalizeAndTrimString).collect(Collectors.toList()));
    }

    public void addUser(final String user) {
        m_users.add(ConfigUtils.normalizeAndTrimString(user));
    }

    public boolean removeUser(final String user) {
        return m_users.remove(user);
    }

    public void clearUsers() {
        m_users.clear();
    }

    @XmlElement(name = "duty-schedule")
    public List<String> getDutySchedules() {
        return m_dutySchedules;
    }

    public void setDutySchedules(final List<String> dutySchedules) {
        if (dutySchedules == m_dutySchedules) return;
        m_dutySchedules.clear();
        if (dutySchedules != null) m_dutySchedules.addAll(dutySchedules.stream().map(ConfigUtils::normalizeAndTrimString).collect(Collectors.toList()));
    }

    public void addDutySchedule(final String dutySchedule) {
        m_dutySchedules.add(ConfigUtils.normalizeAndTrimString(dutySchedule));
    }

    public void clearDutySchedules() {
        m_dutySchedules.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_defaultMap, 
                            m_comments, 
                            m_users, 
                            m_dutySchedules);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Group) {
            final Group that = (Group)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_defaultMap, that.m_defaultMap)
                    && Objects.equals(this.m_comments, that.m_comments)
                    && Objects.equals(this.m_users, that.m_users)
                    && Objects.equals(this.m_dutySchedules, that.m_dutySchedules);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Group [name=" + m_name + ", defaultMap=" + m_defaultMap
                + ", comments=" + m_comments + ", users=" + m_users
                + ", dutySchedules=" + m_dutySchedules + "]";
    }

}
