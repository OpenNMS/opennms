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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.distributed.coordination.api.DomainManager;
import org.opennms.features.distributed.coordination.api.DomainManagerFactory;

/**
 * Tests for {@link AbstractDomainManagerFactory}.
 */
public class TestAbstractDomainManagerFactory {
    /**
     * Tests the caching of domain managers.
     */
    @Test
    public void testCaching() {
        DomainManagerFactory managerFactory = new TestFactory();
        String testDomain = "test.domain";
        // The same manager should be returned since it should be cached after the first get
        Assert.assertSame(managerFactory.getManagerForDomain(testDomain),
                managerFactory.getManagerForDomain(testDomain));
    }

    /**
     * Tests that a new manager is returned for each domain.
     */
    @Test
    public void testDifferentManagers() {
        DomainManagerFactory managerFactory = new TestFactory();
        // Every separate domain must have a different domain manager
        Assert.assertNotSame(managerFactory.getManagerForDomain("domain1"), managerFactory.getManagerForDomain(
                "domain2"));
    }

    private static class TestFactory extends AbstractDomainManagerFactory {
        @Override
        protected DomainManager createManagerForDomain(String domain) {
            return new TestManager(domain);
        }
    }

    private static class TestManager extends AbstractDomainManager {
        TestManager(String domain) {
            super(domain);
        }

        @Override
        protected void onFirstRegister() {
        }

        @Override
        protected void onLastDeregister() {
        }
    }
}
