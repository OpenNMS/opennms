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
package org.opennms.netmgt.provision.server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class SimpleServerRunnable implements Runnable {
    final CountDownLatch m_startingLatch = new CountDownLatch(1);
    final CountDownLatch m_stoppingLatch = new CountDownLatch(1);

    public void awaitStartup() throws InterruptedException {
        m_startingLatch.await(5, TimeUnit.SECONDS);
    }

    public void awaitShutdown() throws InterruptedException {
        m_stoppingLatch.await(5, TimeUnit.SECONDS);
    }

    protected void ready() {
        m_startingLatch.countDown();
    }

    protected void finished() {
        m_stoppingLatch.countDown();
    }
}