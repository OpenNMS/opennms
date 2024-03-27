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
package org.opennms.netmgt.newts.support;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Duration;
import org.opennms.newts.cassandra.ContextConfigurations;

/**
 * Used to build the {@link org.opennms.newts.cassandra.ContextConfigurations} from the configured
 * system properties.
 *
 * @author jwhite
 */
public class ContextConfigurationFactory {

    public static ContextConfigurations getContextConfigurations() {
        return getContextConfigurations(
                System.getProperty("org.opennms.newts.config.resource_shard", "604800"),
                System.getProperty("org.opennms.newts.config.read_consistency", "ONE"),
                System.getProperty("org.opennms.newts.config.write_consistency", "ANY")
        );
    }

    public static ContextConfigurations getContextConfigurations(
            final String resourceShardStr, final String readConsistencyStr, final String writeConsistencyStr) {

        Duration resourceShard = Duration.seconds(Long.parseLong(resourceShardStr));
        ConsistencyLevel readConsistency = DefaultConsistencyLevel.valueOf(readConsistencyStr);
        ConsistencyLevel writeConsistency = DefaultConsistencyLevel.valueOf(writeConsistencyStr);

        ContextConfigurations contexts = new ContextConfigurations();
        contexts.addContextConfig(Context.DEFAULT_CONTEXT, resourceShard, readConsistency, writeConsistency);
        return contexts;
    }
}
