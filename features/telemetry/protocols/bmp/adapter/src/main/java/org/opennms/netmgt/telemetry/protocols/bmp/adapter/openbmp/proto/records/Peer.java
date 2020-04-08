/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
