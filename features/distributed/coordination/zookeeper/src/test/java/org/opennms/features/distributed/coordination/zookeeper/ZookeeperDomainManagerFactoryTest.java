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
package org.opennms.features.distributed.coordination.zookeeper;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.opennms.features.distributed.coordination.api.DomainManagerFactory;

/**
 * Tests for {@link ZookeeperDomainManagerFactory}.
 */
public class ZookeeperDomainManagerFactoryTest {
    /**
     * Verifies the factory generates the correct instance type.
     */
    @Test
    public void checkInstance() {
        DomainManagerFactory managerFactory = new ZookeeperDomainManagerFactory("127.0.0.1:2181",
                "coordination");
        assertThat(managerFactory.getManagerForDomain("test.domain"),
                instanceOf(ZookeeperDomainManager.class));
    }
}
