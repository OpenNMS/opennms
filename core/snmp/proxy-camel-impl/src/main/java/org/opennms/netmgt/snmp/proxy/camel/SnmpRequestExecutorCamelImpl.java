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

package org.opennms.netmgt.snmp.proxy.camel;

import java.util.concurrent.CompletableFuture;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.Synchronization;
import org.opennms.netmgt.snmp.proxy.common.SnmpMultiResponseDTO;
import org.opennms.netmgt.snmp.proxy.common.SnmpRequestDTO;
import org.opennms.netmgt.snmp.proxy.common.SnmpRequestExecutor;
import org.opennms.netmgt.snmp.proxy.common.utils.LocationUtils;

/**
 * Asynchronously dispatches SNMP requests to a Camel route for remote execution. 
 *
 * @author jwhite
 */
public class SnmpRequestExecutorCamelImpl implements SnmpRequestExecutor {

    @EndpointInject(uri = "direct:executeSnmpRequest")
    private ProducerTemplate template;

    @EndpointInject(uri = "direct:executeSnmpRequest")
    private Endpoint endpoint;

    @Override
    public CompletableFuture<SnmpMultiResponseDTO> execute(SnmpRequestDTO request) {
        // Optionally override the default location (used for testing)
        if (LocationUtils.isLocationOverrideEnabled() && request.getLocation() == null) {
            request.setLocation(LocationUtils.getLocationOverride());
        }

        final CompletableFuture<SnmpMultiResponseDTO> future = new CompletableFuture<SnmpMultiResponseDTO>();
        template.asyncCallbackSendBody(endpoint, request, new Synchronization() {
            @Override
            public void onComplete(Exchange exchange) {
                final SnmpMultiResponseDTO res = exchange.getOut().getBody(SnmpMultiResponseDTO.class);
                future.complete(res);
            }
            @Override
            public void onFailure(Exchange exchange) {
                future.completeExceptionally(exchange.getException());
            }
        });
        return future;
    }

    public void setTemplate(ProducerTemplate template) {
        this.template = template;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
}
