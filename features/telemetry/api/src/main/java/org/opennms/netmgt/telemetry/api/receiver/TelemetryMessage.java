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
package org.opennms.netmgt.telemetry.api.receiver;

import org.opennms.core.ipc.sink.api.Message;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Date;

public class TelemetryMessage implements Message {
    private final InetSocketAddress source;
    private final ByteBuffer buffer;
    private final Date receivedAt;

    public TelemetryMessage(InetSocketAddress source, ByteBuffer buffer) {
        this(source, buffer, new Date());
    }

    public TelemetryMessage(InetSocketAddress source, ByteBuffer buffer, Date receivedAt) {
        this.source = source;
        this.buffer = buffer;
        this.receivedAt = receivedAt;
    }

    public InetSocketAddress getSource() {
        return source;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public Date getReceivedAt() {
        return receivedAt;
    }
}
