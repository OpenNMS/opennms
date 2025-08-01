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
package org.opennms.features.jest.client.index;

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
