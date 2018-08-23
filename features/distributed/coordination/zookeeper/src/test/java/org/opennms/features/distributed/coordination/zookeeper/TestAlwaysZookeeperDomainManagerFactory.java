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

package org.opennms.features.distributed.coordination.zookeeper;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.features.distributed.coordination.api.DomainManagerFactory;

/**
 * Tests for {@link AlwaysZookeeperDomainManagerFactory}.
 */
public class TestAlwaysZookeeperDomainManagerFactory {
    /**
     * Verifies the factory generates the correct instance type.
     */
    @Test
    public void checkInstance() {
        DomainManagerFactory managerFactory = new AlwaysZookeeperDomainManagerFactory()
                .buildWithConnectString("127.0.0.1:2181").buildWithNamespace("coordination");
        assertTrue(managerFactory.getManagerForDomain("test.domain") instanceof ZookeeperDomainManager);
    }
}
