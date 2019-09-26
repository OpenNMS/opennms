/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategyFactory;

public class IndexStrategyFactoryTest {
    @Test
    public void verifyInitialization() {
        // Verify initialization for each IndexStrategy (case matches)
        for (IndexStrategy eachValue : IndexStrategy.values()) {
            assertEquals(eachValue, IndexStrategyFactory.createIndexStrategy(eachValue.name()));
        }

        // Verify Initialization for each IndexStrategy (case does not match)
        // See HZN-1240 for more details
        for (IndexStrategy eachValue : IndexStrategy.values()) {
            assertEquals(eachValue, IndexStrategyFactory.createIndexStrategy(eachValue.name().toLowerCase()));
        }
    }

}