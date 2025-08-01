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

public class Peer extends Record {
    public Action action;
    public Long sequence;
    public String hash; // Hash of fields [ remote/peer ip, peer RD, router hash ]
    public String routerHash;
    public String name;
    public InetAddress remoteBgpId;
    public InetAddress routerIp;
    public Instant timestamp;
    public Long remoteAsn;
    public InetAddress remoteIp;
    public String peerRd;
    public Integer remotePort;
    public Long localAsn;
    public InetAddress localIp;
    public Integer localPort;
    public InetAddress localBgpId;
    public String infoData;
    public String advertisedCapabilities;
    public String receivedCapabilities;
    public Long remoteHolddown;
    public Long advertisedHolddown;
    public Integer bmpReason;
    public Integer bgpErrorCode;
    public Integer bgpErrorSubcode;
    public String errorText;
    public boolean l3vpn;
    public boolean prePolicy;
    public boolean ipv4;
    public boolean locRib;
    public boolean locRibFiltered;
    public String tableName;

    public Peer() {
        super(Type.PEER);
    }

    @Override
    protected String[] fields() {
        return new String[]{
                this.action != null ? this.action.value : null,
                nullSafeStr(this.sequence),
                this.hash,
                this.routerHash,
                this.name,
                nullSafeStr(this.remoteBgpId),
                nullSafeStr(this.routerIp),
                formatTimestamp(this.timestamp),
                nullSafeStr(this.remoteAsn),
                nullSafeStr(remoteIp),
                this.peerRd,
                nullSafeStr(this.remotePort),
                nullSafeStr(this.localAsn),
                nullSafeStr(this.localIp),
                nullSafeStr(this.localPort),
                nullSafeStr(this.localBgpId),
                this.infoData,
                this.advertisedCapabilities,
                this.receivedCapabilities,
                nullSafeStr(this.remoteHolddown),
                nullSafeStr(this.advertisedHolddown),
                nullSafeStr(this.bmpReason),
                nullSafeStr(this.bgpErrorCode),
                nullSafeStr(this.bgpErrorSubcode),
                this.errorText,
                boolAsInt(this.l3vpn),
                boolAsInt(this.prePolicy),
                boolAsInt(this.ipv4),
                boolAsInt(this.locRib),
                boolAsInt(this.locRibFiltered),
                this.tableName,
        };
    }

    public enum Action {
        FIRST("first"),
        UP("up"),
        DOWN("down");

        public final String value;

        Action(final String value) {
            this.value = value;
        }
    }
}
