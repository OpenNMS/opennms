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
package org.opennms.features.kafka.producer.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos.TopologyRef.Protocol;
import org.opennms.integration.api.v1.model.TopologyProtocol;

public class OpennmsModelProtosTest {
    @Test
    public void testTopologyProtocolToProtobuf() {
        for (final var from : TopologyProtocol.values()) {
            if (from == TopologyProtocol.ALL) {
                // ALL is a special case that we don't expect to be on both sides, skip it
                continue;
            }

            var to = OpennmsModelProtos.TopologyRef.Protocol.valueOf(from.toString());
            assertEquals("expect protocols to match, but " +from.toString() + "!=" +to.toString() + ")", from.toString(), to.toString());
        }
    }

    @Test
    public void testProtobufToTopologyProtocol() {
        for (final var from : OpennmsModelProtos.TopologyRef.Protocol.values()) {
            if (from == Protocol.UNRECOGNIZED) {
                // UNRECOGNIZED is a special case that we don't expect to be on both sides, skip it
                continue;
            }
            final var to = TopologyProtocol.valueOf(from.toString());
            assertEquals("expect protocols to match, but " +from.toString() + "!=" +to.toString() + ")", from.toString(), to.toString());
        }
    }
}
