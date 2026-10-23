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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class LogPreservingThreadFactoryTest {

    private static final Runnable NOOP = () -> { };

    @Test
    public void pooledThreadNamesFollowTheCurrentPoolSize() {
        final LogPreservingThreadFactory factory = new LogPreservingThreadFactory("Test", 2);
        assertEquals("Test-Thread-1-of-2", factory.newThread(NOOP).getName());

        factory.setPoolSize(70);
        assertEquals(70, factory.getPoolSize());

        // Slots 2..70: well past the original BitSet capacity, which used to hand out slot 0
        final Set<String> names = new HashSet<>();
        for (int i = 2; i <= 70; i++) {
            names.add(factory.newThread(NOOP).getName());
        }
        assertEquals(69, names.size());
        assertTrue(names.contains("Test-Thread-2-of-70"));
        assertTrue(names.contains("Test-Thread-70-of-70"));
        assertFalse(names.stream().anyMatch(n -> n.startsWith("Test-Thread-0-")));
    }

    @Test
    public void slotIsReleasedWhenTheThreadExits() throws InterruptedException {
        final LogPreservingThreadFactory factory = new LogPreservingThreadFactory("Test", 3);
        final Thread first = factory.newThread(NOOP);
        assertEquals("Test-Thread-1-of-3", first.getName());
        first.start();
        first.join();

        assertEquals("Test-Thread-1-of-3", factory.newThread(NOOP).getName());
    }

    @Test
    public void shrinkingToOneThreadKeepsNumberedNamesUnique() {
        final LogPreservingThreadFactory factory = new LogPreservingThreadFactory("Test", 4);
        assertEquals("Test-Thread-1-of-4", factory.newThread(NOOP).getName());
        factory.setPoolSize(1);
        // The first worker is still alive, so the un-numbered single-thread name would not be unique
        assertEquals("Test-Thread-2-of-1", factory.newThread(NOOP).getName());
    }

    @Test
    public void growingFromOneThreadSwitchesToNumberedNames() {
        final LogPreservingThreadFactory factory = new LogPreservingThreadFactory("Test", 1);
        assertEquals("Test-Thread", factory.newThread(NOOP).getName());
        factory.setPoolSize(3);
        assertEquals("Test-Thread-1-of-3", factory.newThread(NOOP).getName());
        assertEquals("Test-Thread-2-of-3", factory.newThread(NOOP).getName());
    }
}
