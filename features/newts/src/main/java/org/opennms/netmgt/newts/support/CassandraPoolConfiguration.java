/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Loads the Cassandra pool configuration from system properties and exposes these as named beans.
 * Uses a value of null if no system property is set (which allows the driver to use it's own defaults.)
 */
@Configuration
public class CassandraPoolConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraPoolConfiguration.class);

    @Bean(name="cassandra.pool.core-connections-per-host")
    public Integer getCoreConnectionsPerHost() {
        return sysPropToIntOrNull("org.opennms.newts.config.core-connections-per-host");
    }

    @Bean(name="cassandra.pool.max-connections-per-host")
    public Integer getMaxConnectionsPerHost() {
        return sysPropToIntOrNull("org.opennms.newts.config.max-connections-per-host");
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
