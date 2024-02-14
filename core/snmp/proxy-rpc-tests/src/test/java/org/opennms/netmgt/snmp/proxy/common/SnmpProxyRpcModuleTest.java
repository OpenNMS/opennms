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
package org.opennms.netmgt.snmp.proxy.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.snmp.SnmpObjId;
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

    @Test      // See NMS-12818
    public void testBehaviorWhenSnmpValueIsNullArray() throws InterruptedException, ExecutionException {
        // Mock the strategy class
        System.setProperty("org.opennms.snmp.strategyClass", MockSnmpStrategy.class.getName());
        // Reset first call.
        MockSnmpStrategy.setFirstCall(true);
        // Add some random oids.
        SnmpRequestDTO request = new SnmpRequestDTO();
        SnmpObjId snmpObjId1 = SnmpObjId.get(".1.3.6.1.2.1.1.2.0");
        SnmpObjId snmpObjId2 = SnmpObjId.get(".1.3.6.1.2.1.1.2.1");
        SnmpGetRequestDTO get1 = new SnmpGetRequestDTO();
        List<SnmpObjId> snmpObjIds = new ArrayList<>();
        snmpObjIds.add(snmpObjId1);
        snmpObjIds.add(snmpObjId2);
        get1.setOids(snmpObjIds);
        request.getGetRequests().add(get1);

        // MockSnmpStrategy returns the null array
        SnmpValue[] retvalues = { null };
        completedFuture = new CompletableFuture<>();
        completedFuture.complete(retvalues);

        // Now "execute" the request and verify that resulting future doesn't throw exception.
        CompletableFuture<SnmpMultiResponseDTO> future = SnmpProxyRpcModule.INSTANCE.execute(request);
        try {
            future.get();
        } catch (ExecutionException e) {
            fail();
        }
    }
}
