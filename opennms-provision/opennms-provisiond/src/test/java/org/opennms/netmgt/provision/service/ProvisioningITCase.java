/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class ProvisioningITCase {
    @Autowired
    @Qualifier("scanExecutor")
    private PausibleScheduledThreadPoolExecutor m_scanExecutor;

    @Autowired
    @Qualifier("scheduledExecutor")
    private PausibleScheduledThreadPoolExecutor m_scheduledExecutor;
    
    @Autowired
    @Qualifier("importExecutor")
    private PausibleScheduledThreadPoolExecutor m_importExecutor;

    @Autowired
    @Qualifier("writeExecutor")
    private PausibleScheduledThreadPoolExecutor m_writeExecutor;

    @Autowired
    private MockEventIpcManager m_eventSubscriber;

    public PausibleScheduledThreadPoolExecutor getScanExecutor() {
        return m_scanExecutor;
    }

    public PausibleScheduledThreadPoolExecutor getScheduledExecutor() {
        return m_scheduledExecutor;
    }

    public PausibleScheduledThreadPoolExecutor getImportExecutor() {
        return m_importExecutor;
    }

    public PausibleScheduledThreadPoolExecutor getWriteExecutor() {
        return m_writeExecutor;
    }

    protected void waitForEverything() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(4);
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                latch.countDown();
            }
        };
        m_scheduledExecutor.execute(runnable);
        m_scanExecutor.execute(runnable);
        m_importExecutor.execute(runnable);
        m_writeExecutor.execute(runnable);
        latch.await(5, TimeUnit.MINUTES);
    }

    protected void waitForImport() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                latch.countDown();
            }
        };
        m_scanExecutor.execute(runnable);
        m_importExecutor.execute(runnable);
        m_writeExecutor.execute(runnable);
        latch.await(5, TimeUnit.MINUTES);
    }

    protected CountDownLatch anticipateEvents(final int numberToMatch, final String... ueis) {
        final CountDownLatch eventReceived = new CountDownLatch(numberToMatch);
        m_eventSubscriber.addEventListener(new EventListener() {
            @Override public void onEvent(final Event e) {
                eventReceived.countDown();
            }

            @Override public String getName() {
                return "Provisioning Test Case";
            }
        }, Arrays.asList(ueis));
        return eventReceived;
    }
}
