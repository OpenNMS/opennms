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
package org.opennms.features.apilayer.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;
import org.opennms.integration.api.v1.model.Severity;
import org.opennms.integration.api.v1.model.TopologyProtocol;
import org.opennms.integration.api.v1.ticketing.Ticket.State;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;

public class ModelMappersTest {
    @Test
    public void testToSeverity() {
        for (final var from : OnmsSeverity.values()) {
            final var to = ModelMappers.toSeverity(from);
            assertEquals("the severity enum names should match (" + from.toString() + "=" + to.toString() + ")", from.toString(), to.toString());
        }
    }

    @Test
    public void testFromSeverity() {
        for (final var from : Severity.values()) {
            final var to = ModelMappers.fromSeverity(from);
            assertEquals("the severity enum names should match (" + from.toString() + "=" + to.toString() + ")", from.toString(), to.toString());
        }
    }

    @Test
    public void testToTicketState() {
        // OPA's State object is a simplified one, containing only OPEN, CANCELLED, and CLOSED
        final var noMapping = Arrays.asList(
                "CREATE_PENDING",
                "CREATE_FAILED",
                "UPDATE_PENDING",
                "UPDATE_FAILED",
                "CLOSE_PENDING",
                "CLOSE_FAILED",
                "RESOLVED",
                "RESOLVE_PENDING",
                "RESOLVE_FAILED",
                "CANCEL_PENDING",
                "CANCEL_FAILED"
        );

        for (final var from : TroubleTicketState.values()) {
            final var to = ModelMappers.toTicketState(from);
            if (noMapping.contains(from.toString())) {
                assertNull("this ticket state does not have an corresponding type in OPA", to);
            } else {
                assertEquals("the ticket state enum names should match (" + from.toString() + "=" + to.toString() + ")", from.toString(), to.toString());
            }
        }
    }

    @Test
    public void testFromTicketState() {
        for (final var from : State.values()) {
            final var to = ModelMappers.fromTicketState(from);
            assertEquals("the ticket state enum names should match (" + from.toString() + "=" + to.toString() + ")", from.toString(), to.toString());
        }
    }

    @Test
    public void testToTopologyProtocol() {
        for (final var proto : ProtocolSupported.values()) {
            final var from = OnmsTopologyProtocol.create(proto.toString());
            final var to = ModelMappers.toTopologyProtocol(from);
            assertEquals("the protocols should match (" + from.toString() + "=" + to.toString() + ")", from.toString(), to.toString());
        }
    }

    @Test
    public void testToOnmsTopologyProtocol() {
        for (final var proto : ProtocolSupported.values()) {
            final var from = TopologyProtocol.valueOf(proto.toString());
            final var to = ModelMappers.toOnmsTopologyProtocol(from);
            assertEquals("the protocols should match (" + from.toString() + "=" + to.toString() + ")", from.toString(), to.toString());
        }
    }

    @Test
    public void testAllToplogyProtocolsInProtocolSupported() {
        for (final var from : TopologyProtocol.values()) {
            if ("ALL".equals(from.toString())) {
                // ALL is a special case that is not expected to be everywhere else
                continue;
            }
            final var to = ProtocolSupported.valueOf(from.toString());
            assertEquals("the protocols should match (" + from.toString() + "=" + to.toString() + ")", from.toString(), to.toString());
        }
    }
}
