/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.concurrent;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.slf4j.MDC;

public class LogPreservingThreadFactory implements ThreadFactory {
    private final BitSet m_slotNumbers;
    private final String m_name;
    private final int m_poolSize;
    private Map m_mdc = null;
    private int m_counter = 0;

    public LogPreservingThreadFactory(String poolName, int poolSize, boolean preserveMDC) {
         m_name = poolName;
         m_poolSize = poolSize;
         // Make the bitset of thread numbers one larger so that we can 1-index it.
         // If pool size is Integer.MAX_VALUE, then the BitSet will not be used.
         m_slotNumbers = poolSize < Integer.MAX_VALUE ? new BitSet(poolSize + 1) : new BitSet(1);
         if (preserveMDC) {
        	 m_mdc = MDC.getCopyOfContextMap();
         }
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
    
    private Map getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }
    
    private void setContextMap(Map map) {
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
                Map mdc = getCopyOfContextMap();
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
                Map mdc = getCopyOfContextMap();
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
                Map mdc = getCopyOfContextMap();
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
                if (bs.get(i)) {
                    continue;
                } else {
                    bs.set(i, true);
                    return i;
                }
            }
            // We should never return zero
            return 0;
        }
    }
}