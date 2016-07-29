/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.AbstractOperation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.netutils.internal.PingWindow;
import org.opennms.features.topology.netutils.internal.service.PingService;
import org.opennms.netmgt.icmp.Pinger;

import com.google.common.base.Strings;

public class PingOperation extends AbstractOperation {

    private Pinger pinger;

    public PingOperation(Pinger pinger) {
        this.pinger = Objects.requireNonNull(pinger);
    }

    @Override
    public Undoer execute(final List<VertexRef> targets, final OperationContext operationContext) {
        final VertexRef target = targets.get(0);
        final Vertex vertex = getVertexItem(operationContext, target);
        new PingWindow(vertex, new PingService(pinger)).open();
        return null;
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        if (targets.size() == 1) {
            // Only enable if we actually have something to ping
            String ipAddress = getVertexItem(operationContext, targets.get(0)).getIpAddress();
            if (!Strings.isNullOrEmpty(ipAddress)) {
                try {
                    InetAddressUtils.getInetAddress(ipAddress);
                    return true;
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean display(final List<VertexRef> targets, final OperationContext operationContext) {
        return targets != null && targets.size() > 0;
    }

    @Override
    public String getId() {
        return "ping";
    }
}
