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

import org.opennms.newts.cassandra.CassandraSession;
import org.opennms.newts.cassandra.CassandraSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates the shared Cassandra session, applying the optional pool settings from
 * system properties. A value of null is used if no system property is set (which
 * allows the driver to use its own defaults).
 *
 * The pool settings used to be exposed as their own (possibly null) named beans
 * injected into the session's constructor, but as of Spring 5 a null bean no
 * longer qualifies as an autowire candidate for a required constructor argument,
 * so the session is created here instead.
 */
@Configuration
public class CassandraPoolConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraPoolConfiguration.class);

    @Bean(name="cassandraSession")
    public CassandraSession cassandraSession(
            @Qualifier("cassandra.datacenter") final String datacenter,
            @Qualifier("cassandra.keyspace") final String keyspace,
            @Qualifier("cassandra.hostname") final String hostname,
            @Qualifier("cassandra.port") final Integer port,
            @Qualifier("cassandra.compression") final String compression,
            @Qualifier("cassandra.username") final String username,
            @Qualifier("cassandra.password") final String password,
            @Qualifier("cassandra.ssl") final Boolean ssl,
            @Qualifier("cassandra.driver-settings-file") final String driverSettingsFile) {
        return new CassandraSessionImpl(datacenter, keyspace, hostname, port, compression,
                username, password, ssl,
                sysPropToIntOrNull("org.opennms.newts.config.connections-per-host"),
                sysPropToIntOrNull("org.opennms.newts.config.max-requests-per-connection"),
                driverSettingsFile);
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
