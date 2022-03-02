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

public class Router extends Record {
    public Action action;
    public Long sequence;
    public String name;
    public String hash; // Hash of fields [ IP address, collector hash ]
    public InetAddress ipAddress;
    public String description;
    public Integer termCode;
    public String termReason;
    public String initData;
    public String termData;
    public Instant timestamp;
    public InetAddress bgpId;

    public Router() {
        super(Type.ROUTER);
    }

    @Override
    protected String[] fields() {
        return new String[]{
                this.action != null ? this.action.value : null,
                nullSafeStr(this.sequence),
                this.name,
                this.hash,
                nullSafeStr(this.ipAddress),
                this.description,
                nullSafeStr(termCode),
                this.termReason,
                this.initData,
                this.termData,
                formatTimestamp(this.timestamp),
                nullSafeStr(bgpId)
        };
    }

    public enum Action {
        FIRST("first"),
        INIT("init"),
        TERM("term");

        public final String value;

        Action(final String value) {
            this.value = value;
        }
    }
}
