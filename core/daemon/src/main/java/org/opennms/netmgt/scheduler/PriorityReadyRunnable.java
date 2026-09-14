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
package org.opennms.netmgt.scheduler;

import java.util.concurrent.Delayed;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public interface PriorityReadyRunnable extends ReadyRunnable, Delayed {
    void setPriority(int priority);
    int getPriority();
    String getInfo();

    void setDelayUntil(long timestampMs);
    long getDelayUntil();

    default void applyBackoffDelay(long baseMs, long maxMs) {
        int p = getPriority();
        if (p <= 0) {
            setDelayUntil(0);
            return;
        }
        long delay = Math.min(maxMs, baseMs * (1L << Math.min(p - 1, 10)));
        long jitter = (long) (delay * 0.1 * (ThreadLocalRandom.current().nextDouble() * 2 - 1));
        setDelayUntil(System.currentTimeMillis() + delay + jitter);
    }

    @Override
    default long getDelay(TimeUnit unit) {
        long remaining = getDelayUntil() - System.currentTimeMillis();
        return unit.convert(Math.max(0, remaining), TimeUnit.MILLISECONDS);
    }

    @Override
    default int compareTo(Delayed other) {
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS),
                           other.getDelay(TimeUnit.MILLISECONDS));
    }
}
