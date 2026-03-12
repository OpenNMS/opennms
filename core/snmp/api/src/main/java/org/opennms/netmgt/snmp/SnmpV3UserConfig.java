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
package org.opennms.netmgt.snmp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration object that wraps SNMPv3 user credentials for distribution
 * to Minion instances via the Twin API. This replaces the REST-based
 * SNMPv3 user config fetching that previously required OPENNMS_HTTP connectivity.
 */
@XmlRootElement(name = "snmp-v3-user-config")
@XmlAccessorType(XmlAccessType.NONE)
public class SnmpV3UserConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TWIN_KEY = "snmp-v3-users";

    @XmlElementWrapper(name = "snmp-v3-users")
    @XmlElement(name = "snmp-v3-user")
    private List<SnmpV3User> users = new ArrayList<>();

    public SnmpV3UserConfig() {
    }

    public SnmpV3UserConfig(List<SnmpV3User> users) {
        this.users = users != null ? new ArrayList<>(users) : new ArrayList<>();
    }

    public List<SnmpV3User> getUsers() {
        return users;
    }

    public void setUsers(List<SnmpV3User> users) {
        this.users = users != null ? users : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SnmpV3UserConfig)) return false;
        SnmpV3UserConfig that = (SnmpV3UserConfig) o;
        return Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }

    @Override
    public String toString() {
        return "SnmpV3UserConfig{" +
                "users=" + users +
                '}';
    }
}
