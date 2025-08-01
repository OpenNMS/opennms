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
package org.opennms.netmgt.provision.service;

import static org.awaitility.Awaitility.await;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.netmgt.provision.service.operations.ImportOperationsManager;
import org.opennms.netmgt.provision.service.operations.InsertOperation;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

import com.google.common.collect.Lists;

public class RequisitionAccountantTest {
    private boolean blocked = false;

    private RequisitionAccountant createRequisitionAccountant() {
        final ProvisionService provisionService = Mockito.mock(ProvisionService.class);
        final LocationAwareSnmpClient locationAwareSnmpClient = Mockito.mock(LocationAwareSnmpClient.class);
        Mockito.when(provisionService.getLocationAwareSnmpClient()).thenReturn(locationAwareSnmpClient);
        Mockito.when(provisionService.getHostnameResolver()).thenReturn(new HostnameResolver() {
            @Override
            public CompletableFuture<String> getHostnameAsync(InetAddress addr, String location) {
                return CompletableFuture.supplyAsync(()-> {
                    while (blocked) {
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return location + InetAddressUtils.str(addr);
                });
            }
        });

        final ImportOperationsManager importOperationsManager = Mockito.mock(ImportOperationsManager.class);
        Mockito.when(importOperationsManager.foundNode("node1", "node1", "MINION", "", "", "monitorKey")).thenReturn(new InsertOperation("foreignSource", "node1", "node1", "MINION", "","", provisionService, "monitorKey"));
        Mockito.when(importOperationsManager.foundNode("node2", "node2", "MINION", "", "", "monitorKey")).thenReturn(new InsertOperation("foreignSource", "node2", "node1", "MINION", "","", provisionService, "monitorKey"));
        Mockito.when(importOperationsManager.foundNode("node3", "node3", "MINION", "", "", "monitorKey")).thenReturn(new InsertOperation("foreignSource", "node3", "node1", "MINION", "","", provisionService, "monitorKey"));

        return new RequisitionAccountant(importOperationsManager, "monitorKey");
    }

    private RequisitionNode createNode(final String foreignId, final String ... ipAddresses) {
        final RequisitionNode requisitionNode = new RequisitionNode();
        requisitionNode.setLocation("MINION");
        requisitionNode.setNodeLabel(foreignId);
        requisitionNode.setForeignId(foreignId);
        requisitionNode.setBuilding("");
        requisitionNode.setCity("");

        final List<RequisitionInterface> requisitionInterfaces = new ArrayList<>();

        for(final String ipAddress : ipAddresses) {
            final RequisitionInterface requisitionInterface = new RequisitionInterface();
            requisitionInterface.setIpAddr(ipAddress);
            requisitionInterface.setManaged(true);
            requisitionInterface.setStatus(1);
            requisitionInterface.setSnmpPrimary(requisitionInterfaces.size() == 0 ? PrimaryType.PRIMARY : PrimaryType.SECONDARY);
            requisitionInterfaces.add(requisitionInterface);
        }

        requisitionNode.setInterfaces(requisitionInterfaces);

        return requisitionNode;
    }

    private Requisition createRequisition() {
        final Requisition requisition = new Requisition();
        requisition.setForeignSource("foreignSource");
        requisition.setDate(new Date());

        requisition.setNodes(Lists.newArrayList(
                createNode("node1", "10.32.29.11"),
                createNode("node2", "10.32.30.22", "10.32.29.22"),
                createNode("node3", "10.32.29.33", "10.32.30.33", "10.32.28.33")
        ));

        return requisition;
    }

    @Test
    public void testRequisitionAccountant() {
        blocked = true;

        // create requisition with three nodes, first with one, second with two, third with three interfaces
        final Requisition requisition = createRequisition();

        // create visitor
        final RequisitionAccountant requisitionAccountant = createRequisitionAccountant();

        // no DNS lookups scheduled since visit() wasn't called
        Assert.assertEquals(0, requisitionAccountant.dnsLookupsTotal());
        Assert.assertEquals(0, requisitionAccountant.dnsLookupsPending());
        Assert.assertEquals(0, requisitionAccountant.dnsLookupsCompleted());

        final CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                requisition.visit(requisitionAccountant);
            }
        });

        // wait for lookups to be scheduled
        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            return requisitionAccountant.dnsLookupsTotal() == 6;
        });

        // check for all six lookups
        Assert.assertEquals(6, requisitionAccountant.dnsLookupsTotal());
        Assert.assertEquals(6, requisitionAccountant.dnsLookupsPending());
        Assert.assertEquals(0, requisitionAccountant.dnsLookupsCompleted());

        // unblock
        blocked = false;

        // wait till lookups are completed
        await().atMost(1, TimeUnit.MINUTES).until(() -> requisitionAccountant.dnsLookupsPending() == 0);

        Assert.assertEquals(6, requisitionAccountant.dnsLookupsTotal());
        Assert.assertEquals(0, requisitionAccountant.dnsLookupsPending());
        Assert.assertEquals(6, requisitionAccountant.dnsLookupsCompleted());
    }
}
