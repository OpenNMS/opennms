/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Test;
import org.opennms.core.tasks.TaskCoordinator;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.MonitoringSystemDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class ProvisionerTest {

    @After
    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void canTriggerNewSuspectScanWhenMonitoringSystemIsNotFound() throws InterruptedException {
        // Build a provisioner that overrides the 'createNewSuspectScan' call and saves the arguments from the last call
        final AtomicReference<InetAddress> ipAddressRef = new AtomicReference<>();
        final AtomicReference<String> foreignSourceRef = new AtomicReference<>();
        final AtomicReference<String> locationRef = new AtomicReference<>();
        final Provisioner provisioner = new Provisioner() {
            @Override
            public NewSuspectScan createNewSuspectScan(InetAddress ipAddress, String foreignSource, String location) {
                ipAddressRef.set(ipAddress);
                foreignSourceRef.set(foreignSource);
                locationRef.set(location);
                return mock(NewSuspectScan.class, RETURNS_DEEP_STUBS);
            }
        };

        // Mock the necessary facilities
        ProvisionService provisionService = mock(ProvisionService.class);
        when(provisionService.isDiscoveryEnabled()).thenReturn(true);
        provisioner.setProvisionService(provisionService);

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        provisioner.setScheduledExecutor(scheduledExecutorService);

        MonitoringSystemDao monitoringSystemDao = mock(MonitoringSystemDao.class);
        provisioner.setMonitoringSystemDao(monitoringSystemDao);

        // Create the newSuspect event
        Event newSuspectEvent = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "test")
                .setInterface(InetAddressUtils.ONE_TWENTY_SEVEN)
                // Reference a dist poller (or monitoring system) which does not exist
                // The MonitoringSystemDao we provide is a mock anyways, so it'll always return null
                .setDistPoller("non-existent")
                .getEvent();

        // Trigger the newSuspect and wait for the runnable to complete
        provisioner.handleNewSuspectEvent(newSuspectEvent);
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1, TimeUnit.MINUTES);

        // Make sure we tried to lookup the monitoring system from the given id
        verify(monitoringSystemDao, times(1)).get("non-existent");

        // Validate the arguments passed to createNewSuspectScan call
        assertThat(ipAddressRef.get(), equalTo(InetAddressUtils.ONE_TWENTY_SEVEN));
        assertThat(foreignSourceRef.get(), equalTo(null));
        assertThat(locationRef.get(), equalTo(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID));
    }
}
