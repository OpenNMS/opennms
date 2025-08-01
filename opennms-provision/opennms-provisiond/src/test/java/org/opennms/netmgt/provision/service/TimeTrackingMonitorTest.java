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

import com.codahale.metrics.MetricRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.provision.service.operations.DeleteOperation;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

public class TimeTrackingMonitorTest {

    @Test
    public void testAll() {
        MetricRegistry registry = new MetricRegistry();
        TimeTrackingMonitor monitor = new TimeTrackingMonitor("test", registry);
        DeleteOperation dummyOperation = new DeleteOperation(0, null, null, null);
        Resource dummyResource = new PathResource(".");
        NodeScan dummyNodeScan = new NodeScan(0, null, null, null, null, null, null, null, null, monitor, null);
        monitor.start();

        monitor.beginAuditNodes();
        monitor.finishAuditNodes();
        Assert.assertEquals(1, monitor.getAuditTimer().getCount());

        monitor.beginImporting();
        monitor.finishImporting();
        Assert.assertEquals(1, monitor.getImportTimer().getCount());

        monitor.beginScanEvent(dummyOperation);
        monitor.finishScanEvent(dummyOperation);
        Assert.assertEquals(1, monitor.getScanEventTimer().getCount());

        monitor.beginPersisting(dummyOperation);
        monitor.finishPersisting(dummyOperation);
        Assert.assertEquals(1, monitor.getPersistingTimer().getCount());

        monitor.beginLoadingResource(dummyResource);
        monitor.finishLoadingResource(dummyResource, 10);
        Assert.assertEquals(1, monitor.getLoadingTimer().getCount());
        Assert.assertEquals(10, monitor.getNodeCount());

        monitor.beginScanning(dummyNodeScan);
        Assert.assertTrue(monitor.getCurrentNodes().containsKey(dummyNodeScan));
        monitor.finishScanning(dummyNodeScan);
        Assert.assertEquals(1, monitor.getScanningTimer().getCount());
        Assert.assertEquals(0, monitor.getCurrentNodes().size());

        monitor.finish();
        Assert.assertTrue(monitor.getEndTime().after(monitor.getStartTime()));
    }
}
