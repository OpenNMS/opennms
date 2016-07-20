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

import java.net.InetAddress;
import java.util.Objects;

import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.PingResponseCallback;

public class PingServiceResponseCallback implements PingResponseCallback {

    private final PingResult result;
    private final PingService.Callback uiCallback;

    PingServiceResponseCallback(PingRequest request, PingService.Callback uiCallback) {
        this.result = new PingResult(Objects.requireNonNull(request));
        this.uiCallback = Objects.requireNonNull(uiCallback);
        uiCallback.onUpdate(result);
    }

    @Override
    public void handleResponse(InetAddress address, EchoPacket response) {
        result.addSequence(new PingSequence(response));
        notifyUI();
    }

    @Override
    public void handleTimeout(InetAddress address, EchoPacket request) {
        PingSequence sequence = new PingSequence(request, true);
        // Somehow the timeout is also invoked if an error occurred.
        // We do not want the sequence to occur twice, on error.
        if (!result.hasSequence(sequence.getSequenceNumber())) {
            result.addSequence(sequence);
            notifyUI();
        }
    }

    @Override
    public void handleError(InetAddress address, EchoPacket request, Throwable t) {
        result.addSequence(new PingSequence(request, t));
        notifyUI();
    }

    protected boolean isDone() {
        return result.isComplete();
    }

    protected void notifyUI() {
        uiCallback.onUpdate(result);
    }
}
