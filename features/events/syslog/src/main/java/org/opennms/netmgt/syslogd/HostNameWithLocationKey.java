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
package org.opennms.netmgt.syslogd;

import java.util.Objects;

public class HostNameWithLocationKey {

    private final String hostName;

    private final String location;

    public HostNameWithLocationKey(String hostName, String location) {
        this.hostName = hostName;
        this.location = location;
    }

    public String getHostName() {
        return hostName;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostNameWithLocationKey that = (HostNameWithLocationKey) o;
        return Objects.equals(hostName, that.hostName) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostName, location);
    }
}
