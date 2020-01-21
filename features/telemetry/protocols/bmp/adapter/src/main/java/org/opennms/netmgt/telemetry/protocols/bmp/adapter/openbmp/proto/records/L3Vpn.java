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

public class L3Vpn extends Record {
    public String action;
    public long sequence;
    public String hash; // Hash of fields [ prefix, prefix length, rd admin id, rd number, peer hash, path_id, 1 if has label(s) ]
    public String routerHash;
    public InetAddress routerIp;
    public String baseAttrHash;
    public String peerHash;
    public InetAddress peerIp;
    public long peerAsn;
    public Instant timestamp;
    public InetAddress prefix;
    public int prefixLength;
    public boolean ipv4;
    public String origin;
    public String asPath;
    public int asPathCount;
    public long originAs;
    public InetAddress nextHop;
    public int med;
    public int localPref;
    public String aggregator;
    public String communityList;
    public String extCommunityList;
    public String clusterList;
    public boolean atomicAgg;
    public boolean nextHopIpv4;
    public InetAddress originatorId;
    public int pathId;
    public String labels;
    public boolean prePolicy;
    public boolean adjIn;
    public String routeDistinguisher;
    public int routeDistinguisherType;
    public String largeCommunityList;

    public L3Vpn() {
        super(Type.L3VPN);
    }

    @Override
    protected String[] fields() {
        return new String[]{
                this.action,
                Long.toString(this.sequence),
                this.hash,
                this.routerHash,
                InetAddressUtils.toIpAddrString(this.routerIp),
                this.baseAttrHash,
                this.peerHash,
                InetAddressUtils.toIpAddrString(this.peerIp),
                Long.toString(this.peerAsn),
                formatTimestamp(this.timestamp),
                InetAddressUtils.toIpAddrString(this.prefix),
                Integer.toString(this.prefixLength),
                Boolean.toString(this.ipv4),
                this.origin,
                this.asPath,
                Integer.toString(this.asPathCount),
                Long.toString(this.originAs),
                InetAddressUtils.toIpAddrString(this.nextHop),
                Integer.toString(this.med),
                Integer.toString(this.localPref),
                this.aggregator,
                this.communityList,
                this.extCommunityList,
                this.clusterList,
                Boolean.toString(this.atomicAgg),
                Boolean.toString(this.nextHopIpv4),
                InetAddressUtils.toIpAddrString(this.originatorId),
                Integer.toString(this.pathId),
                this.labels,
                Boolean.toString(this.prePolicy),
                Boolean.toString(this.adjIn),
                this.routeDistinguisher,
                Integer.toString(this.routeDistinguisherType),
                this.largeCommunityList
        };
    }
}
