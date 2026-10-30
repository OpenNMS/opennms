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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FixedThreadPoolsTest {

    private ThreadPoolExecutor m_pool;

    @Before
    public void setUp() {
        m_pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    }

    @After
    public void tearDown() {
        m_pool.shutdownNow();
    }

    @Test
    public void growsAndShrinksKeepingCoreAndMaximumEqual() {
        assertEquals(5, FixedThreadPools.resize(m_pool, 5));
        assertEquals(5, m_pool.getCorePoolSize());
        assertEquals(5, m_pool.getMaximumPoolSize());

        assertEquals(1, FixedThreadPools.resize(m_pool, 1));
        assertEquals(1, m_pool.getCorePoolSize());
        assertEquals(1, m_pool.getMaximumPoolSize());
    }

    @Test
    public void unchangedSizeIsANoOp() {
        assertEquals(2, FixedThreadPools.resize(m_pool, 2));
        assertEquals(2, m_pool.getCorePoolSize());
        assertEquals(2, m_pool.getMaximumPoolSize());
    }

    @Test
    public void factoryIsUpdatedBeforeGrowingAndAfterShrinking() {
        final int[] coreSizeWhenFactoryUpdated = new int[1];
        final LogPreservingThreadFactory factory = new LogPreservingThreadFactory("Test", 2) {
            @Override
            public void setPoolSize(final int poolSize) {
                coreSizeWhenFactoryUpdated[0] = m_pool.getCorePoolSize();
                super.setPoolSize(poolSize);
            }
        };

        assertEquals(5, FixedThreadPools.resize(m_pool, factory, "test pool", 5));
        assertEquals("growing: factory must see the new size before workers can be prestarted", 2, coreSizeWhenFactoryUpdated[0]);
        assertEquals(5, factory.getPoolSize());

        assertEquals(1, FixedThreadPools.resize(m_pool, factory, "test pool", 1));
        assertEquals("shrinking: factory is updated once the pool has shrunk", 1, coreSizeWhenFactoryUpdated[0]);
        assertEquals(1, factory.getPoolSize());

        // unchanged request leaves the factory alone
        coreSizeWhenFactoryUpdated[0] = -1;
        assertEquals(1, FixedThreadPools.resize(m_pool, factory, "test pool", 0));
        assertEquals(-1, coreSizeWhenFactoryUpdated[0]);
    }

    @Test
    public void sizesBelowOneAreClampedToOne() {
        assertEquals(1, FixedThreadPools.resize(m_pool, 0));
        assertEquals(1, m_pool.getCorePoolSize());
        assertEquals(1, FixedThreadPools.resize(m_pool, -7));
        assertEquals(1, m_pool.getMaximumPoolSize());
    }
}
