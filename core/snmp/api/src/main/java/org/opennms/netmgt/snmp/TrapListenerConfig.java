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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "trap-listener-config")
@XmlAccessorType(XmlAccessType.NONE)
public class TrapListenerConfig {

    public static final String TWIN_KEY = "trapd.listener.config";

    @XmlElementWrapper(name = "snmp-v3-users")
    @XmlElement(name = "snmp-v3-user")
    private List<SnmpV3User> snmpV3Users= new ArrayList<>();

    public List<SnmpV3User> getSnmpV3Users() {
        return this.snmpV3Users;
    }

    public void setSnmpV3Users(final List<SnmpV3User> snmpV3Users) {
        this.snmpV3Users = snmpV3Users;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TrapListenerConfig)) {
            return false;
        }
        final TrapListenerConfig that = (TrapListenerConfig) o;
        return Objects.equals(this.snmpV3Users, that.snmpV3Users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.snmpV3Users);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TrapListenerConfig.class.getSimpleName() + "[", "]")
                .add("snmpV3Users=" + snmpV3Users)
                .toString();
    }
}
