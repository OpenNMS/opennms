/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.proxy.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

public class SnmpProxyRpcModuleTest {

    @After
    public void tearDown() {
        SnmpUtils.setStrategyResolver(null);
    }

    public static CompletableFuture<SnmpValue[]> completedFuture = CompletableFuture
            .completedFuture(new SnmpValue[] {});
    public static CompletableFuture<SnmpValue[]> failedFuture = new CompletableFuture<>();
  
    @Test
    public void testBehaviorWhenOneGetFails() throws InterruptedException, ExecutionException {
        // Mock the strategy class
        System.setProperty("org.opennms.snmp.strategyClass", MockSnmpStrategy.class.getName());
        // Create a basic request with two gets
        // (the agent and the OIDs don't matter here since we're mocking)
        SnmpRequestDTO request = new SnmpRequestDTO();
        SnmpGetRequestDTO get1 = new SnmpGetRequestDTO();
        SnmpGetRequestDTO get2 = new SnmpGetRequestDTO();
        request.getGetRequests().add(get1);
        request.getGetRequests().add(get2);

        // MockSnmpStrategy returns the completed future the first time
        // and the failed future subsequently
        failedFuture.completeExceptionally(new IllegalStateException("Oups"));

        // Now "execute" the request and verify that the resulting
        // future fails with an ExecutionException
        CompletableFuture<SnmpMultiResponseDTO> future = SnmpProxyRpcModule.INSTANCE.execute(request);
        try {
            future.get();
            fail("did not throw!");
        } catch (ExecutionException e) {
            assertEquals("Oups", e.getCause().getMessage());
        }
    }
}
