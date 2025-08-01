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
package org.opennms.netmgt.telemetry.protocols.bmp.persistence.api;

import java.math.BigInteger;
import java.util.Date;

public class StatsPeerRib {

    private Date intervalTime;

    private String peerHashId;

    private BigInteger v4prefixes;

    private BigInteger v6prefixes;

    public StatsPeerRib(Date intervalTime, String peerHashId, BigInteger v4prefixes, BigInteger v6prefixes) {
        this.intervalTime = intervalTime;
        this.peerHashId = peerHashId;
        this.v4prefixes = v4prefixes;
        this.v6prefixes = v6prefixes;
    }

    public Date getIntervalTime() {
        return intervalTime;
    }

    public String getPeerHashId() {
        return peerHashId;
    }

    public Integer getV4prefixes() {
        return v4prefixes != null ? v4prefixes.intValue() : null;
    }

    public Integer getV6prefixes() {
        return v6prefixes != null ? v6prefixes.intValue() : null;
    }
}
