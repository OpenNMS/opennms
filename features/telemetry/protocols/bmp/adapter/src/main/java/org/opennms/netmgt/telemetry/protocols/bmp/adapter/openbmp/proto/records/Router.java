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
