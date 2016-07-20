/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.netutils.internal.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.opennms.netmgt.icmp.EchoPacket;

public class PingSequence {
    private boolean timeout;
    private Throwable error;
    private EchoPacket response;
    private int sequenceNumber;

    public PingSequence(EchoPacket response) {
        this.response = response;
        this.sequenceNumber = extractSequenceNumber(this);
    }

    public PingSequence(EchoPacket request, Throwable t) {
        this.error = t;
        this.response = request;
        this.sequenceNumber = extractSequenceNumber(this);
    }

    public PingSequence(EchoPacket request, boolean timeout) {
        this.timeout = timeout;
        this.response = request;
        this.sequenceNumber = extractSequenceNumber(this);
    }

    public boolean isTimeout() {
        return timeout;
    }

    public boolean isError() {
        return error != null;
    }

    public EchoPacket getResponse() {
        return response;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isSuccess() {
        return !isTimeout() && !isError();
    }

    private static int extractSequenceNumber(PingSequence sequence) {
        try {
            return sequence.getResponse().getSequenceNumber();
        } catch (NullPointerException ex) {
            // we swallow the exception, as we bail out afterwards
        }
        // This is ugly as hell, but necessary.
        // Depending if the ping command was executed properly, either a EchoPacket response
        // (see interface Response) or a EchoPacket request (see interface Request) implementation is provided.
        // Only the EchoPacket response implementation has a getSequenceNumber() which returns a not null value.
        // The EchoPacket request implementation uses the "getId()" method which contains the "sequenceNumber".
        // As the API of the "EchoPacket" itself does not have a "SequenceIdentifier" interface, it is not possible
        // to use that API without adding unnecessary dependencies to other modules.
        // To avoid this problem in case the request was not successful, the sequenceNumber is extracted via
        // reflections from the according EchoPacket request implementations.
        try {
            Object requestId = sequence.getResponse().getClass().getMethod("getId").invoke(sequence.getResponse());
            if (requestId != null) {
                Field sequenceNumberField = requestId.getClass().getDeclaredField("m_sequenceNumber");
                if (sequenceNumberField != null) {
                    sequenceNumberField.setAccessible(true);
                    Object sequenceNumber = sequenceNumberField.get(requestId);
                    return ((Integer) sequenceNumber).intValue();
                }
            }
        } catch (NoSuchFieldException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            // we swallow the exception, as we bail out afterwards
        }
        throw new IllegalArgumentException("Cannot determine sequence number");
    }
}
