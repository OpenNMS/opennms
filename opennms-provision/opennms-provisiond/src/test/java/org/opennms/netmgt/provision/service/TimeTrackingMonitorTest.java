/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import com.codahale.metrics.MetricRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.provision.service.operations.DeleteOperation;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

public class TimeTrackingMonitorTest {

    @Test
    @SuppressWarnings("java:S2925")
    public void testAll() throws InterruptedException {
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
        Thread.sleep(1);
        monitor.finishImporting();
        Assert.assertEquals(1, monitor.getImportTimer().getCount());

        monitor.beginScanEvent(dummyOperation);
        monitor.finishScanEvent(dummyOperation);
        Assert.assertEquals(1, monitor.getScanEventTimer().getCount());

        monitor.beginPersisting(dummyOperation);
        monitor.finishPersisting(dummyOperation);
        Assert.assertEquals(1, monitor.getPersistingTimer().getCount());

        monitor.beginLoadingResource(dummyResource);
        Thread.sleep(1);
        monitor.finishLoadingResource(dummyResource, 10);
        Assert.assertEquals(1, monitor.getLoadingTimer().getCount());
        Assert.assertEquals(10, monitor.getNodeCount());

        monitor.beginScanning(dummyNodeScan);
        Assert.assertTrue(monitor.getCurrentNodes().containsKey(dummyNodeScan));
        Thread.sleep(1);
        monitor.finishScanning(dummyNodeScan);
        Assert.assertEquals(1, monitor.getScanningTimer().getCount());
        Assert.assertEquals(0, monitor.getCurrentNodes().size());

        monitor.finish();
        Assert.assertTrue(String.format("%s is not after %s!", monitor.getEndTime(), monitor.getStartTime()), monitor.getEndTime().after(monitor.getStartTime()));
    }
}
