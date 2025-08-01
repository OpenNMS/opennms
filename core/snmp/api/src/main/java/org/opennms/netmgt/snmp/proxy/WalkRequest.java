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
package org.opennms.netmgt.snmp.proxy;

import java.util.Objects;

import org.opennms.netmgt.snmp.SnmpObjId;

public class WalkRequest {

    private final SnmpObjId baseOid;
    private int maxRepetitions = 1;
    private SnmpObjId instance = null;
    private String correlationId = null;

    public WalkRequest(SnmpObjId baseOid) {
        this.baseOid = Objects.requireNonNull(baseOid);
    }

    public SnmpObjId getBaseOid() {
        return baseOid;
    }

    public void setMaxRepetitions(int maxRepetitions) {
        this.maxRepetitions = maxRepetitions;
    }

    public int getMaxRepetitions() {
        return maxRepetitions;
    }

    public void setInstance(SnmpObjId instance) {
        this.instance = instance;
    }

    public SnmpObjId getInstance() {
        return instance;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public String toString() {
        return String.format("WalkRequest[baseOid=%s, correlationId=%s, maxRepetitions=%d, instance=%s]",
                baseOid, correlationId, maxRepetitions, instance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseOid, correlationId, maxRepetitions, instance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final WalkRequest other = (WalkRequest) obj;
        return Objects.equals(this.baseOid, other.baseOid)
                && Objects.equals(this.correlationId, other.correlationId)
                && Objects.equals(this.maxRepetitions, other.maxRepetitions)
                && Objects.equals(this.instance, other.instance);
    }
}
