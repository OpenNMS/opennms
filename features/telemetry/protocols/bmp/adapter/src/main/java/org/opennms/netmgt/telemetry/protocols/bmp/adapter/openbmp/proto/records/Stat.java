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
package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records;

import java.net.InetAddress;
import java.time.Instant;

import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Record;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;

public class Stat extends Record {
    public Action action;
    public Long sequence;
    public String routerHash;
    public InetAddress routerIp;
    public String peerHash;
    public InetAddress peerIp;
    public Long peerAsn;
    public Instant timestamp;

    public Integer prefixesRejected;
    public Integer knownDupPrefixes;
    public Integer knownDupWithdraws;
    public Integer invalidClusterList;
    public Integer invalidAsPath;
    public Integer invalidOriginatorId;
    public Integer invalidAsConfed;
    public Long prefixesPrePolicy;
    public Long prefixesPostPolicy;

    public Stat() {
        super(Type.BMP_STAT);
    }

    @Override
    protected String[] fields() {
        return new String[]{
                this.action.value,
                nullSafeStr(this.sequence),
                this.routerHash,
                nullSafeStr(this.routerIp),
                this.peerHash,
                nullSafeStr(this.peerIp),
                nullSafeStr(this.peerAsn),
                formatTimestamp(this.timestamp),
                nullSafeStr(this.prefixesRejected),
                nullSafeStr(this.knownDupPrefixes),
                nullSafeStr(this.knownDupWithdraws),
                nullSafeStr(this.invalidClusterList),
                nullSafeStr(this.invalidAsPath),
                nullSafeStr(this.invalidOriginatorId),
                nullSafeStr(this.invalidAsConfed),
                nullSafeStr(this.prefixesPrePolicy),
                nullSafeStr(this.prefixesPostPolicy)
        };
    }

    public enum Action {
        ADD("add");

        public final String value;

        Action(final String value) {
            this.value = value;
        }
    }
}
