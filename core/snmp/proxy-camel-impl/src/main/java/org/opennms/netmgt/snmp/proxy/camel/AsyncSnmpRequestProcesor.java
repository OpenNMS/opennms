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

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.snmp.proxy.common.SnmpMultiResponseDTO;
import org.opennms.netmgt.snmp.proxy.common.SnmpRequestDTO;
import org.opennms.netmgt.snmp.proxy.common.SnmpRequestExecutor;

/**
 * Asynchronously executes SNMP requests.
 *
 * @author jwhite
 */
public class AsyncSnmpRequestProcesor implements AsyncProcessor {

    private SnmpRequestExecutor snmpRequestExecutor;

    @Override
    public void process(Exchange exchange) throws Exception {
        final SnmpRequestDTO requestDTO = JaxbUtils.unmarshal(SnmpRequestDTO.class,
                exchange.getIn().getBody(String.class));
        final SnmpMultiResponseDTO responseDTO = snmpRequestExecutor.execute(requestDTO).get();
        exchange.getOut().setBody(JaxbUtils.marshal(responseDTO), String.class);
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        final SnmpRequestDTO requestDTO = JaxbUtils.unmarshal(SnmpRequestDTO.class,
                exchange.getIn().getBody(String.class));
        final CompletableFuture<SnmpMultiResponseDTO> future = snmpRequestExecutor.execute(requestDTO);
        future.whenComplete((res, ex) -> {
            try {
                if (ex != null) {
                    exchange.setException(ex);
                    exchange.getOut().setFault(true);
                } else {
                    try {
                        JaxbUtils.marshal(res);
                    } catch (Throwable t) {
                        // The first attempt may fail, but subsequent attempts should always work
                    }
                    exchange.getOut().setBody(JaxbUtils.marshal(res), String.class);
                }
            } finally {
                callback.done(false);
            }
        });
        return false;
    }

    public void setSnmpRequestExecutor(SnmpRequestExecutor snmpRequestExecutor) {
        this.snmpRequestExecutor = snmpRequestExecutor;
    }
}
