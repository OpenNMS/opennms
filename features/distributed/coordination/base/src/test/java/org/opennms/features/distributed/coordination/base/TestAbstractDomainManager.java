/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.distributed.coordination.base;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.distributed.coordination.api.Role;

/**
 * Tests for {@link AbstractDomainManager}.
 */
public class TestAbstractDomainManager {
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

        manager.register(testId, domain -> callbacksCalled.add(becomeActiveCallback),
                domain -> callbacksCalled.add(becomeStandbyCallback));
        Assert.assertTrue(manager.isAnythingRegistered());
        Assert.assertTrue(manager.isRegistered(testId));

        manager.becomeActive();
        Assert.assertSame(Role.ACTIVE, manager.getCurrentRole());

        manager.becomeStandby();
        Assert.assertSame(Role.STANDBY, manager.getCurrentRole());

        manager.deregister(testId);
        Assert.assertFalse(manager.isAnythingRegistered());
        Assert.assertFalse(manager.isRegistered(testId));
        Assert.assertEquals(Arrays.asList(onRegisterCallback, becomeActiveCallback, becomeStandbyCallback,
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
                domain -> numNotifiedActive.incrementAndGet(), domain -> numNotifiedStandby.incrementAndGet()));

        assertEquals(numToRegister, manager.getRoleChangeHandlers().size());

        manager.becomeActive();
        Assert.assertEquals(numToRegister, numNotifiedActive.get());

        manager.becomeStandby();
        Assert.assertEquals(numToRegister, numNotifiedStandby.get());
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
