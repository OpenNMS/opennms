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
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Record;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public class Collector extends Record {
    public Action action;
    public Long sequence;
    public String adminId;
    public String hash;
    public List<InetAddress> routers;
    public Instant timestamp;

    public Collector() {
        super(Type.COLLECTOR);
    }

    @Override
    protected String[] fields() {
        return new String[]{
                this.action.value,
                nullSafeStr(this.sequence),
                this.adminId,
                this.hash,
                this.routers != null ? Joiner.on(',').join(Iterables.transform(this.routers, InetAddressUtils::str)) : "",
                this.routers != null ? Integer.toString(this.routers.size()) : "0",
                Record.formatTimestamp(this.timestamp)
        };
    }

    public enum Action {
        STARTED("started"),
        CHANGE("change"),
        HEARTBEAT("heartbeat"),
        STOPPED("stopped");

        public final String value;

        Action(final String value) {
            this.value = value;
        }
    }
}
