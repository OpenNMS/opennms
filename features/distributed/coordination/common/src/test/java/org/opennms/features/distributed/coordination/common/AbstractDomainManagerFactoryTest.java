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

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.opennms.features.distributed.coordination.api.DomainManager;
import org.opennms.features.distributed.coordination.api.DomainManagerFactory;

/**
 * Tests for {@link AbstractDomainManagerFactory}.
 */
public class AbstractDomainManagerFactoryTest {
    /**
     * Tests the caching of domain managers.
     */
    @Test
    public void testCaching() {
        DomainManagerFactory managerFactory = new TestFactory();
        String testDomain = "test.domain";
        // The same manager should be returned since it should be cached after the first get
        assertSame(managerFactory.getManagerForDomain(testDomain), managerFactory.getManagerForDomain(testDomain));
    }

    /**
     * Tests that a new manager is returned for each domain.
     */
    @Test
    public void testDifferentManagers() {
        DomainManagerFactory managerFactory = new TestFactory();
        // Every separate domain must have a different domain manager
        assertNotSame(managerFactory.getManagerForDomain("domain1"), managerFactory.getManagerForDomain("domain2"));
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
