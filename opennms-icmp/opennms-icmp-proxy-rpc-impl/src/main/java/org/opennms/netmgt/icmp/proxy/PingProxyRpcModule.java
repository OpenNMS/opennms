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

package org.opennms.netmgt.icmp.proxy;

import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.netmgt.icmp.PingerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PingProxyRpcModule extends AbstractXmlRpcModule<PingRequestDTO, PingResponseDTO>  {

    public static final String RPC_MODULE_ID = "PING";

    @Autowired
    private PingerFactory pingerFactory;

    public PingProxyRpcModule() {
        super(PingRequestDTO.class, PingResponseDTO.class);
    }

    @Override
    public CompletableFuture<PingResponseDTO> execute(PingRequestDTO request) {
        final PingResultTracker tracker = new PingResultTracker();
        try {
            pingerFactory.getInstance().ping(
                    request.getInetAddress(),
                    request.getTimeout(),
                    request.getRetries(),
                    request.getPacketSize(),
                    1,
                    tracker);
        } catch (Exception e) {
            tracker.completeExceptionally(e);
        }
        return tracker;
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    public void setPingerFactory(PingerFactory pingerFactory) {
        this.pingerFactory = pingerFactory;
    }

    @Override
    public PingResponseDTO createResponseWithException(Throwable ex) {
        return new PingResponseDTO(ex);
    }
}
