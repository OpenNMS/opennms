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

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class ProvisioningITCase {
    @Autowired
    private Provisioner m_provisioner;

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
        final CountDownLatch latch = new CountDownLatch(5);
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                latch.countDown();
            }
        };
        m_scheduledExecutor.execute(runnable);
        m_scanExecutor.execute(runnable);
        m_importExecutor.execute(runnable);
        m_writeExecutor.execute(runnable);
        m_provisioner.getNewSuspectExecutor().execute(runnable);
        latch.await(5, TimeUnit.MINUTES);
    }

    protected void waitForImport() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(4);
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                latch.countDown();
            }
        };
        m_scanExecutor.execute(runnable);
        m_importExecutor.execute(runnable);
        m_writeExecutor.execute(runnable);
        m_provisioner.getNewSuspectExecutor().execute(runnable);
        latch.await(5, TimeUnit.MINUTES);
    }

    protected CountDownLatch anticipateEvents(final int numberToMatch, final String... ueis) {
        final CountDownLatch eventReceived = new CountDownLatch(numberToMatch);
        m_eventSubscriber.addEventListener(new EventListener() {
            @Override public void onEvent(final IEvent e) {
                eventReceived.countDown();
            }

            @Override public String getName() {
                return "Provisioning Test Case";
            }
        }, Arrays.asList(ueis));
        return eventReceived;
    }
}
