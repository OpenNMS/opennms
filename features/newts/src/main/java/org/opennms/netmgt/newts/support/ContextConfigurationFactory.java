/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.newts.support;

import org.opennms.newts.api.Context;
import org.opennms.newts.api.Duration;
import org.opennms.newts.cassandra.ContextConfigurations;

import com.datastax.driver.core.ConsistencyLevel;

/**
 * Used to build the {@link org.opennms.newts.cassandra.ContextConfigurations} from the configured
 * system properties.
 *
 * @author jwhite
 */
public class ContextConfigurationFactory {

    public static ContextConfigurations getContextConfigurations() {
        String resourceShardStr = System.getProperty("org.opennms.newts.config.resource_shard", "604800");
        String readConsistencyStr = System.getProperty("org.opennms.newts.config.read_consistency", "ONE");
        String writeConsistencyStr = System.getProperty("org.opennms.newts.config.write_consistency", "ANY");

        Duration resourceShard = Duration.seconds(Long.parseLong(resourceShardStr));
        ConsistencyLevel readConsistency = ConsistencyLevel.valueOf(readConsistencyStr);
        ConsistencyLevel writeConsistency = ConsistencyLevel.valueOf(writeConsistencyStr);

        ContextConfigurations contexts = new ContextConfigurations();
        contexts.addContextConfig(Context.DEFAULT_CONTEXT, resourceShard, readConsistency, writeConsistency);
        return contexts;
    }
}
