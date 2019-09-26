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

package org.opennms.plugins.elasticsearch.rest.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create an {@link IndexStrategy} from a string.
 * Should help using {@link IndexStrategy} objects while creating objects from blueprint.xml files.
 */
public class IndexStrategyFactory {

    private static final Logger LOG = LoggerFactory.getLogger(IndexStrategyFactory.class);

    private static final IndexStrategy DEFAULT_INDEX = IndexStrategy.MONTHLY;

    public static IndexStrategy createIndexStrategy(String input) {
        for (IndexStrategy strategy : IndexStrategy.values()) {
            if (strategy.name().equalsIgnoreCase(input)) {
                LOG.debug("Using strategy {}", strategy);
                return strategy;
            }
        }
        LOG.debug("No strategy found for key {}, falling back to {}", input, DEFAULT_INDEX);
        return DEFAULT_INDEX;
    }
}
