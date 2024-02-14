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
package org.opennms.features.distributed.coordination.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Test;
import org.opennms.features.distributed.coordination.api.Role;

/**
 * Tests for {@link AbstractDomainManager}.
 */
public class AbstractDomainManagerTest {
    private static final String becomeActiveCallback = "becomeActive";
    private static final String becomeStandbyCallback = "becomeStandby";
    private static final String onRegisterCallback = "onFirstRegister";
    private static final String onDeregisterCallback = "onLastDeregister";
    private static final String domain = "test.domain";
    private static final String id = "test.id";
    private final List<String> callbacksCalled = new ArrayList<>();
    private final AtomicInteger numNotifiedActive = new AtomicInteger(0);
    private final AtomicInteger numNotifiedStandby = new AtomicInteger(0);

    /**
     * Test a typical register-role change-deregister lifecycle.
     */
    @Test
    public void testLifecycle() {
        String testId = id;
        TestDomainManager manager = new TestDomainManager(domain);

        manager.register(testId, (role, domain) -> {
            if (role == Role.ACTIVE) {
                callbacksCalled.add(becomeActiveCallback);
            } else if (role == Role.STANDBY) {
                callbacksCalled.add(becomeStandbyCallback);
            }
        });

        assertThat(manager.isAnythingRegistered(), is(equalTo(true)));
        assertThat(manager.isRegistered(testId), is(equalTo(true)));

        manager.becomeActive();
        assertSame(Role.ACTIVE, manager.getCurrentRole());

        manager.becomeStandby();
        assertSame(Role.STANDBY, manager.getCurrentRole());

        manager.deregister(testId);
        assertThat(manager.isAnythingRegistered(), is(equalTo(false)));
        assertThat(manager.isRegistered(testId), is(equalTo(false)));
        assertEquals(Arrays.asList(onRegisterCallback, becomeActiveCallback, becomeStandbyCallback,
                onDeregisterCallback), callbacksCalled);
    }

    /**
     * Test that all registrants are notified of role changes.
     */
    @Test
    public void testMultipleNotify() {
        TestDomainManager manager = new TestDomainManager(domain);
        int numToRegister = 100;

        IntStream.range(0, numToRegister).parallel().forEach(i -> manager.register(id + i,
                (role, domain) -> {
                    if (role == Role.ACTIVE) {
                        numNotifiedActive.incrementAndGet();
                    } else if (role == Role.STANDBY) {
                        numNotifiedStandby.incrementAndGet();
                    }
                }));

        assertEquals(numToRegister, manager.getRoleChangeHandlers().size());

        manager.becomeActive();
        assertEquals(numToRegister, numNotifiedActive.get());

        manager.becomeStandby();
        assertEquals(numToRegister, numNotifiedStandby.get());
    }

    private class TestDomainManager extends AbstractDomainManager {
        TestDomainManager(String domain) {
            super(domain);
        }

        @Override
        protected void onFirstRegister() {
            callbacksCalled.add(onRegisterCallback);
        }

        @Override
        protected void onLastDeregister() {
            callbacksCalled.add(onDeregisterCallback);
        }
    }
}
