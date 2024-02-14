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
package org.opennms.netmgt.provision.service.operations;

import static org.awaitility.Awaitility.await;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.service.HostnameResolver;
import org.opennms.netmgt.provision.service.ProvisionService;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

public class SaveOrUpdateOperationTest {
    private boolean blocked = true;

    private ProvisionService createProvisioningService() {
        final ProvisionService provisionService = Mockito.mock(ProvisionService.class);
        final LocationAwareSnmpClient locationAwareSnmpClient = Mockito.mock(LocationAwareSnmpClient.class);
        Mockito.when(provisionService.getLocationAwareSnmpClient()).thenReturn(locationAwareSnmpClient);
        Mockito.when(provisionService.getHostnameResolver()).thenReturn(new HostnameResolver() {
            @Override
            public CompletableFuture<String> getHostnameAsync(InetAddress addr, String location) {
                return CompletableFuture.supplyAsync(() -> {
                    while (blocked) {
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return location + "-" + InetAddressUtils.str(addr);
                });
            }
        });

        return provisionService;
    }

    @Test
    public void testParallelNameResolution() throws InterruptedException {
        blocked = true;

        final ProvisionService provisionService = createProvisioningService();
        final Set<CompletableFuture<Void>> completableFutures = new HashSet<>();

        // insert first node with three interfaces
        final SaveOrUpdateOperation insertOperation1 = new InsertOperation("foreignSource", "foreignId1", "nodeLabel1", "MINION", "", "", provisionService, "monitorKey");
        insertOperation1.foundInterface(InetAddressUtils.addr("10.32.29.12"), "Interface #1", PrimaryType.PRIMARY, true, 1, completableFutures);
        insertOperation1.foundInterface(InetAddressUtils.addr("10.32.29.13"), "Interface #2", PrimaryType.SECONDARY, true, 1, completableFutures);
        insertOperation1.foundInterface(InetAddressUtils.addr("10.32.29.14"), "Interface #3", PrimaryType.SECONDARY, true, 1, completableFutures);

        // insert second node with two interfaces
        final SaveOrUpdateOperation insertOperation2 = new InsertOperation("foreignSource", "foreignId2", "nodeLabel2", "MINION", "", "", provisionService, "monitorKey");
        insertOperation2.foundInterface(InetAddressUtils.addr("10.32.30.101"), "Interface #1", PrimaryType.PRIMARY, true, 1, completableFutures);
        insertOperation2.foundInterface(InetAddressUtils.addr("10.32.30.102"), "Interface #2", PrimaryType.SECONDARY, true, 1, completableFutures);

        // blocked, so five pending lookups
        Assert.assertEquals(5, completableFutures.size());
        Assert.assertEquals(5, completableFutures.stream().filter(f -> !f.isDone()).count());

        // unblock
        blocked = false;

        // wait, till all lookups are completed
        await().atMost(1, TimeUnit.MINUTES).until(() -> completableFutures.stream().filter(f -> f.isDone()).count() == 5);

        // all done
        Assert.assertEquals(5, completableFutures.size());

        // check that the correct hostnames are set
        Assert.assertEquals("MINION-10.32.29.12", insertOperation1.getNode().getIpInterfaceByIpAddress("10.32.29.12").getIpHostName());
        Assert.assertEquals("MINION-10.32.29.13", insertOperation1.getNode().getIpInterfaceByIpAddress("10.32.29.13").getIpHostName());
        Assert.assertEquals("MINION-10.32.29.14", insertOperation1.getNode().getIpInterfaceByIpAddress("10.32.29.14").getIpHostName());
        Assert.assertEquals("MINION-10.32.30.101", insertOperation2.getNode().getIpInterfaceByIpAddress("10.32.30.101").getIpHostName());
        Assert.assertEquals("MINION-10.32.30.102", insertOperation2.getNode().getIpInterfaceByIpAddress("10.32.30.102").getIpHostName());
    }

    @Test
    public void testNameResolutionFlag() {
        blocked = false;
        final ProvisionService provisionService = createProvisioningService();
        final Set<CompletableFuture<Void>> completableFutures = new HashSet<>();

        Assert.assertEquals(0, completableFutures.size());

        // insert a node with one interface
        final SaveOrUpdateOperation insertOperation1 = new InsertOperation("foreignSource", "foreignId1", "nodeLabel1", "MINION", "", "", provisionService, "monitorKey");
        insertOperation1.foundInterface(InetAddressUtils.addr("10.32.29.12"), "Interface #1", PrimaryType.PRIMARY, true, 1, completableFutures);
        Assert.assertEquals(1, completableFutures.size());

        // insert a node with interface without address
        final SaveOrUpdateOperation insertOperation2 = new InsertOperation("foreignSource", "foreignId2", "nodeLabel2", "MINION", "", "", provisionService, "monitorKey");
        insertOperation2.foundInterface(null, "Interface #1", PrimaryType.PRIMARY, true, 1, completableFutures);
        Assert.assertEquals(1, completableFutures.size());

        // set system property to false
        System.setProperty("org.opennms.provisiond.reverseResolveRequisitionIpInterfaceHostnames", "false");

        // insert another node with one interface
        final SaveOrUpdateOperation insertOperation3 = new InsertOperation("foreignSource", "foreignId3", "nodeLabel3", "MINION", "", "", provisionService, "monitorKey");
        insertOperation3.foundInterface(InetAddressUtils.addr("10.32.29.32"), "Interface #1", PrimaryType.PRIMARY, true, 1, completableFutures);
        Assert.assertEquals(1, completableFutures.size());

        await().atMost(1, TimeUnit.MINUTES).until(() -> completableFutures.stream().filter(f -> f.isDone()).count() == 1);

        // check for the right hostname
        Assert.assertEquals("MINION-10.32.29.12", insertOperation1.getNode().getPrimaryInterface().getIpHostName());
        Assert.assertEquals(0, insertOperation2.getNode().getIpInterfaces().size());
        Assert.assertEquals(null, insertOperation3.getNode().getPrimaryInterface().getIpHostName());
    }
}
