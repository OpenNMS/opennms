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

public class Stat extends Record {
    public Action action;
    public long sequence;
    public String routerHash;
    public InetAddress routerIp;
    public String peerHash;
    public InetAddress peerIp;
    public long peerAsn;
    public Instant timestamp;

    public int prefixesRejected;
    public int knownDupPrefixes;
    public int knownDupWithdraws;
    public int invalidClusterList;
    public int invalidAsPath;
    public int invalidOriginatorId;
    public int invalidAsConfed;
    public long prefixesPrePolicy;
    public long prefixesPostPolicy;

    public Stat() {
        super(Type.ROUTER);
    }

    @Override
    protected String[] fields() {
        return new String[]{
                this.action.value,
                Long.toString(this.sequence),
                this.routerHash,
                InetAddressUtils.toIpAddrString(this.routerIp),
                this.peerHash,
                InetAddressUtils.toIpAddrString(this.peerIp),
                Long.toString(this.peerAsn),
                formatTimestamp(this.timestamp),
                Integer.toString(this.prefixesRejected),
                Integer.toString(this.knownDupPrefixes),
                Integer.toString(this.knownDupWithdraws),
                Integer.toString(this.invalidClusterList),
                Integer.toString(this.invalidAsPath),
                Integer.toString(this.invalidOriginatorId),
                Integer.toString(this.invalidAsConfed),
                Long.toString(this.prefixesPrePolicy),
                Long.toString(this.prefixesPostPolicy)
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
