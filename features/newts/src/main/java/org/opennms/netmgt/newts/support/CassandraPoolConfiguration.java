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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Loads the Cassandra pool configuration from system properties and exposes these as named beans.
 * Uses a value of null if no system property is set (which allows the driver to use its own defaults.)
 */
@Configuration
public class CassandraPoolConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraPoolConfiguration.class);

    @Bean(name="cassandra.pool.connections-per-host")
    public Integer getCoreConnectionsPerHost() {
        return sysPropToIntOrNull("org.opennms.newts.config.connections-per-host");
    }

    @Bean(name="cassandra.pool.max-requests-per-connection")
    public Integer getMaxRequestsPerConnection() {
        return sysPropToIntOrNull("org.opennms.newts.config.max-requests-per-connection");
    }

    private static Integer sysPropToIntOrNull(String sysProp) {
        final String val = System.getProperty(sysProp, null);
        if (val == null) {
            return null;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            LOG.error("Failed to parse value of system property {}='{}' to an integer. Using default value.", sysProp, val);
            return null;
        }
    }
}
