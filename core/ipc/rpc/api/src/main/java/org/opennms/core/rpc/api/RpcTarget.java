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
package org.opennms.core.rpc.api;

import java.util.Objects;

public class RpcTarget {
    private final String location;
    private final String systemId;

    public RpcTarget(String location, String systemId) {
        this.location = location;
        this.systemId = systemId;
    }

    public String getLocation() {
        return location;
    }

    public String getSystemId() {
        return systemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RpcTarget rpcTarget = (RpcTarget) o;
        return Objects.equals(location, rpcTarget.location) &&
                Objects.equals(systemId, rpcTarget.systemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, systemId);
    }

    @Override
    public String toString() {
        return "RpcTarget{" +
                "location='" + location + '\'' +
                ", systemId='" + systemId + '\'' +
                '}';
    }
}
