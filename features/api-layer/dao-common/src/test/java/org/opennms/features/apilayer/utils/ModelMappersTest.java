/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
