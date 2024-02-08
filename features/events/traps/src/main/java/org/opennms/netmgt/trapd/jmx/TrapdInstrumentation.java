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
package org.opennms.netmgt.trapd.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class TrapdInstrumentation {

    private static final Logger LOG = LoggerFactory.getLogger(TrapdInstrumentation.class);

    private final AtomicLong trapsReceived = new AtomicLong();
    private final AtomicLong v1TrapsReceived = new AtomicLong();
    private final AtomicLong v2cTrapsReceived = new AtomicLong();
    private final AtomicLong v3TrapsReceived = new AtomicLong();
    private final AtomicLong vUnknownTrapsReceived = new AtomicLong();
    private final AtomicLong trapsDiscarded = new AtomicLong();
    private final AtomicLong trapsErrored = new AtomicLong();

    public void incTrapsReceivedCount(String version) {
        trapsReceived.incrementAndGet();
        if ("v1".equals(version)) {
            v1TrapsReceived.incrementAndGet();
        } else if ("v2c".equals(version) || "v2".equals(version)) {
            v2cTrapsReceived.incrementAndGet();
        } else if ("v3".equals(version)) {
            v3TrapsReceived.incrementAndGet();
        } else {
            vUnknownTrapsReceived.incrementAndGet();
            LOG.warn("Received a trap with an unknown SNMP protocol version '{}'.", version);
        }
    }

    public void incDiscardCount() {
        trapsDiscarded.incrementAndGet();
    }

    public void incErrorCount() {
        trapsErrored.incrementAndGet();
    }

    public long getV1TrapsReceived() {
        return v1TrapsReceived.get();
    }

    public long getV2cTrapsReceived() {
        return v2cTrapsReceived.get();
    }

    public long getV3TrapsReceived() {
        return v3TrapsReceived.get();
    }

    public long getVUnknownTrapsReceived() {
        return vUnknownTrapsReceived.get();
    }

    public long getTrapsDiscarded() {
        return trapsDiscarded.get();
    }

    public long getTrapsErrored() {
        return trapsErrored.get();
    }

    public long getTrapsReceived() {
        return trapsReceived.get();
    }
}
