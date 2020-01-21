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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Record;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;

public class LsNode extends Record {
    public String action;
    public long sequence;
    public String hash; // Hash of fields [ igp router id, bgp ls id, asn, ospf area id ]
    public String baseAttrHash;
    public String routerHash;
    public InetAddress routerIp;
    public String peerHash;
    public InetAddress peerIp;
    public long peerAsn;
    public Instant timestamp;
    public String igpRouterId;
    public String routerId;
    public long routingId;
    public int lsId;
    public String mtId;
    public String ospfAreaId;
    public String isisAreaId;
    public String protocol;
    public String flags;
    public String asPath;
    public int localPref;
    public int med;
    public String nextHop;
    public String nodeName;
    public boolean prePolicy;
    public boolean adjIn;
    public String srCapabilities;

    public LsNode() {
        super(Type.LS_NODE);
    }

    @Override
    protected String[] fields() {
        return new String[]{
                this.action,
                Long.toString(this.sequence),
                this.hash,
                this.baseAttrHash,
                this.routerHash,
                InetAddressUtils.toIpAddrString(this.routerIp),
                this.peerHash,
                InetAddressUtils.toIpAddrString(this.peerIp),
                Long.toString(this.peerAsn),
                formatTimestamp(this.timestamp),
                this.igpRouterId,
                this.routerId,
                Long.toString(this.routingId),
                Integer.toString(this.lsId),
                this.mtId,
                this.ospfAreaId,
                this.isisAreaId,
                this.protocol,
                this.flags,
                this.asPath,
                Integer.toString(this.localPref),
                Integer.toString(this.med),
                this.nextHop,
                this.nodeName,
                Boolean.toString(this.prePolicy),
                Boolean.toString(this.adjIn),
                this.srCapabilities
        };
    }
}
