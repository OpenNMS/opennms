/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
}
