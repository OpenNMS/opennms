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
package org.opennms.core.concurrent;

import org.slf4j.MDC;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class LogPreservingThreadFactory implements ThreadFactory {
    private final BitSet m_slotNumbers;
    private final String m_name;
    private final int m_poolSize;
    private Map<String,String> m_mdc = null;
    private int m_counter = 0;

    public LogPreservingThreadFactory(String poolName, int poolSize) {
         m_name = poolName;
         m_poolSize = poolSize;
         // Make the bitset of thread numbers one larger so that we can 1-index it.
         // If pool size is Integer.MAX_VALUE, then the BitSet will not be used.
         m_slotNumbers = poolSize < Integer.MAX_VALUE ? new BitSet(poolSize + 1) : new BitSet(1);

         m_mdc = MDC.getCopyOfContextMap();

    }

    @Override
    public Thread newThread(final Runnable r) {
        if (m_poolSize == Integer.MAX_VALUE) {
            return getIncrementingThread(r);
        } else if (m_poolSize > 1) {
            return getPooledThread(r);
        } else {
            return getSingleThread(r);
        }
    }
    
    private Map<String,String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }
    
    private void setContextMap(Map<String,String> map) {
        if (map == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(map);
        }
    }

    private Thread getIncrementingThread(final Runnable r) {
        String name = String.format("%s-Thread-%d", m_name, ++m_counter);
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> mdc = getCopyOfContextMap();
                try {
                    // Set the logging prefix if it was stored during creation
                    setContextMap(m_mdc);
                    // Run the delegate Runnable
                    r.run();
                } finally {
                    setContextMap(mdc);
                }
            }
        }, name);
    }

    private Thread getSingleThread(final Runnable r) {
        String name = String.format("%s-Thread", m_name);
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> mdc = getCopyOfContextMap();
                try {
                    // Set the logging prefix if it was stored during creation
                    setContextMap(m_mdc);
                    // Run the delegate Runnable
                    r.run();
                } finally {
                    setContextMap(mdc);
                }
            }
        }, name);
    }

    private Thread getPooledThread(final Runnable r) {
        final int threadNumber = getOpenThreadSlot(m_slotNumbers);
        String name = String.format("%s-Thread-%d-of-%d", m_name, threadNumber, m_poolSize);
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> mdc = getCopyOfContextMap();
                try {
                    try {
                        setContextMap(m_mdc);
                        r.run();
                    } finally {
                        setContextMap(mdc);
                    }
                } finally {
                    // And make sure the mark the thread as unused afterwards if
                    // the thread ever exits
                    synchronized(m_slotNumbers) {
                        m_slotNumbers.set(threadNumber, false);
                    }
                }
            }
        }, name);
    }

    private static int getOpenThreadSlot(BitSet bs) {
        synchronized(bs) {
            // Start at 1 so that we always return a positive integer
            for (int i = 1; i < bs.size(); i++) {
                if (!bs.get(i)) {
                    bs.set(i, true);
                    return i;
                }
            }
            // We should never return zero
            return 0;
        }
    }
}