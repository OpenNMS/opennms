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
