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
package org.opennms.web.rest.v2.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class LazyKarafBridgeTest {

    @Test
    public void missingFrameworkClassesLeaveTheContainerUnavailable() {
        final AtomicInteger attempts = new AtomicInteger();
        final LazyKarafBridge bridge = new LazyKarafBridge(() -> {
            attempts.incrementAndGet();
            throw new NoClassDefFoundError("org/osgi/framework/BundleContext");
        });

        assertFalse(bridge.isAvailable());
        assertTrue(bridge.installedKars().isEmpty());
        try {
            bridge.installFeatures(Collections.singleton("feature"));
            fail("expected the operation to be refused");
        } catch (final KarafBridge.KarafOperationException expected) {
            // container unavailable
        }
        assertEquals(1, attempts.get());
    }

    @Test
    public void theRealBridgeIsCreatedOnFirstUseOnly() {
        final AtomicInteger attempts = new AtomicInteger();
        final FakeKarafBridge fake = new FakeKarafBridge();
        fake.installedKars.add("lab-plugin");
        final LazyKarafBridge bridge = new LazyKarafBridge(() -> {
            attempts.incrementAndGet();
            return fake;
        });

        assertEquals(0, attempts.get());
        assertTrue(bridge.isAvailable());
        assertEquals(Collections.singletonList("lab-plugin"), bridge.installedKars());
        assertEquals(1, attempts.get());
    }
}
