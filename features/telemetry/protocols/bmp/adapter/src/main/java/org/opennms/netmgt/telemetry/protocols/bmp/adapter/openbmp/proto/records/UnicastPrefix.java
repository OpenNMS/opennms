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

public class UnicastPrefix extends Record {
    public Action action;
    public Long sequence;
    public String hash; // Hash of fields [ prefix, prefix length, peer hash, path_id, 1 if has label(s) ]
    public String routerHash;

    public InetAddress routerIp;
    public String baseAttrHash;
    public String peerHash;
    public InetAddress peerIp;
    public Long peerAsn;
    public Instant timestamp;
    public InetAddress prefix;
    public Integer length;
    public boolean ipv4;
    public String origin;
    public String asPath;
    public Integer asPathCount;
    public Long originAs;
    public InetAddress nextHop;
    public Long med;
    public Long localPref;
    public String aggregator;
    public String communityList;
    public String extCommunityList;
    public String clusterList;
    public boolean atomicAgg;
    public boolean nextHopIpv4;
    public String originatorId;
    public Long pathId;
    public String labels;
    public boolean prePolicy;
    public boolean adjIn;
    public String largeCommunityList;

    public UnicastPrefix() {
        super(Type.UNICAST_PREFIX);
    }

    @Override
    protected String[] fields() {
        return new String[]{
                this.action != null ? this.action.value : null,
                nullSafeStr(this.sequence),
                this.hash,
                this.routerHash,
                nullSafeStr(this.routerIp),
                this.baseAttrHash,
                this.peerHash,
                nullSafeStr(this.peerIp),
                nullSafeStr(this.peerAsn),
                formatTimestamp(this.timestamp),
                nullSafeStr(this.prefix),
                nullSafeStr(this.length),
                boolAsInt(this.ipv4),
                this.origin,
                this.asPath,
                nullSafeStr(this.asPathCount),
                nullSafeStr(this.originAs),
                nullSafeStr(this.nextHop),
                nullSafeStr(this.med),
                nullSafeStr(this.localPref),
                this.aggregator,
                this.communityList,
                this.extCommunityList,
                this.clusterList,
                boolAsInt(this.atomicAgg),
                boolAsInt(this.nextHopIpv4),
                this.originatorId,
                nullSafeStr(this.pathId),
                this.labels,
                boolAsInt(this.prePolicy),
                boolAsInt(this.adjIn),
                this.largeCommunityList
        };
    }

    public enum Action {
        ADD("add"),
        DELETE("del");

        public final String value;

        Action(final String value) {
            this.value = value;
        }
    }
}
