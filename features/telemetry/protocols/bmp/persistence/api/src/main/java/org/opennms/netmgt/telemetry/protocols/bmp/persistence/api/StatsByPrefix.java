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

public class StatsByPrefix {

    private Date intervalTime;

    private String peerHashId;

    private BigInteger updates;

    private BigInteger withdraws;

    private String prefix;

    private Integer prefixLen;

    public StatsByPrefix(Date intervalTime, String peerHashId, String prefix, Integer prefixLen, BigInteger withdraws, BigInteger updates) {
        this.intervalTime = intervalTime;
        this.peerHashId = peerHashId;
        this.updates = updates;
        this.withdraws = withdraws;
        this.prefix = prefix;
        this.prefixLen = prefixLen;
    }

    public Date getIntervalTime() {
        return intervalTime;
    }

    public String getPeerHashId() {
        return peerHashId;
    }

    public Long getUpdates() {
        return updates != null ? updates.longValue() : null;
    }

    public Long getWithdraws() {
        return withdraws != null ? withdraws.longValue() : null;
    }

    public String getPrefix() {
        return prefix;
    }

    public Integer getPrefixLen() {
        return prefixLen;
    }
}
