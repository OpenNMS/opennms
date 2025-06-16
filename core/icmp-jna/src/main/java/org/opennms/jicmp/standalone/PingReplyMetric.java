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
package org.opennms.jicmp.standalone;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * PingReplyMetric
 *
 * @author brozow
 */
public class PingReplyMetric extends Metric implements PingReplyListener {
    
    CountDownLatch m_latch;
    int m_count;
    long m_interval;
    
    public PingReplyMetric(int count, long interval) {
        m_latch = new CountDownLatch(count);
        m_count = count;
        m_interval = interval;
    }

    @Override
    public void onPingReply(InetAddress address, PingReply reply) {
        try {
            update(reply.getElapsedTimeNanos());
        } finally {
            m_latch.countDown();
        }
    }

    public void await() throws InterruptedException {
        m_latch.await(m_interval*m_count + 1000, TimeUnit.MILLISECONDS);
    }

}
