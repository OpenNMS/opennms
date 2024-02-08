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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class MonitorHolderTest {
    @Test
    public void testExpiry() throws ExecutionException, InterruptedException {
        MonitorHolder holder = new MonitorHolder(1);
        ProvisionMonitor monitor1 = holder.createMonitor("test1");
        ProvisionMonitor monitor2 = holder.createMonitor("test2");
        Assert.assertEquals("It should have 2 monitors.", 2, holder.getMonitors().size());
        Thread.sleep(500L);
        holder.getMonitor(monitor1.getName());
        Assert.assertEquals("It should still have 2 monitor.", 2, holder.getMonitors().size());
        Thread.sleep(500L);
        Assert.assertEquals("test2 should be expired.",1, holder.getMonitors().size());
        Assert.assertNotNull(holder.getMonitor(monitor1.getName()));
        Assert.assertNull(holder.getMonitor(monitor2.getName()));
        Thread.sleep(1000L);
        Assert.assertEquals("Should be all expired.", 0, holder.getMonitors().size());
    }

    @Test
    public void testChangeExpiry() throws ExecutionException, InterruptedException {
        MonitorHolder holder = new MonitorHolder(1);
        holder.createMonitor("test1");
        holder.createMonitor("test2");
        Assert.assertEquals("It should have 2 monitors.", 2, holder.getMonitors().size());
        holder.createCacheWithExpireTime(2);
        Assert.assertEquals("It should still have 2 monitors.", 2, holder.getMonitors().size());
        Thread.sleep(1100L);
        Assert.assertEquals("It should still have 2 monitors. Since expiry time is 2s now.", 2, holder.getMonitors().size());
        Thread.sleep(900L);
        Assert.assertEquals("It should still have 0 now.", 0, holder.getMonitors().size());
    }

    @Test
    public void testMaxAssociatedMetrics() throws ExecutionException, InterruptedException {
        MonitorHolder holder = new MonitorHolder(1);
        String overallMetricName = "single-job-test";
        for(int i = 0; i < holder.MAX_METRIC_ASSOCIATION_SIZE ; i++){
            holder.createMonitor(overallMetricName);
            //metric names created have a timestamp format up to the second so need to wait at least a second before adding a new one
            Thread.sleep(1100L);
        }
        Assert.assertEquals("It should have " + holder.MAX_METRIC_ASSOCIATION_SIZE + " items", holder.getAssociatedMetrics().get(overallMetricName).size(), holder.MAX_METRIC_ASSOCIATION_SIZE );
        var oldest = holder.getAssociatedMetrics().get(overallMetricName).peekLast();
        holder.createMonitor(overallMetricName);
        Assert.assertEquals("It should still have " + holder.MAX_METRIC_ASSOCIATION_SIZE + " items", holder.getAssociatedMetrics().get(overallMetricName).size(), holder.MAX_METRIC_ASSOCIATION_SIZE );
        var newOldest = holder.getAssociatedMetrics().get(overallMetricName).peekLast();
        Assert.assertNotEquals("It should remove the oldest ", oldest, newOldest);

        for(int i = 0; i <= holder.MAX_METRIC_ASSOCIATION_SIZE ; i++){
            holder.createMonitor(overallMetricName);
            Thread.sleep(500L);
        }
        Assert.assertEquals("It should still have " + holder.MAX_METRIC_ASSOCIATION_SIZE + " items", holder.getAssociatedMetrics().get(overallMetricName).size(), holder.MAX_METRIC_ASSOCIATION_SIZE );

    }

    @Test
    public void testOverallMonitors() throws ExecutionException, InterruptedException {
        MonitorHolder holder = new MonitorHolder(1);
        String overallMetricName = "single-job-test";
        String overallMetricName2 = "single-job-test2";
        holder.createOverallMonitor(overallMetricName);
        holder.createOverallMonitor(overallMetricName2);
        for(int i = 0; i < holder.MAX_METRIC_ASSOCIATION_SIZE ; i++){
            holder.createMonitor(overallMetricName);
            //metric names created have a timestamp format up to the second so need to wait at least a second before adding a new one
            Thread.sleep(1100L);
        }
        for(int i = 0; i < 10; i++){
            holder.createMonitor(overallMetricName2);
            //metric names created have a timestamp format up to the second so need to wait at least a second before adding a new one
            Thread.sleep(1100L);
        }
        Assert.assertEquals("Should have 2 associated metrics",holder.getAssociatedMetrics().size(), 2);
        Assert.assertEquals("Should exists and have 10 associated metrics",holder.getAssociatedMetrics().get(overallMetricName2).size(),10);
        Assert.assertEquals("Should have 2 overall monitors",holder.getOverallMonitors().size(), 2);
        Assert.assertArrayEquals("overall keys should be the same as associated keys", holder.getAssociatedMetrics().keySet().toArray(new String[0]), holder.getOverallMonitors().keySet().toArray(new String[0]));

    }
}
