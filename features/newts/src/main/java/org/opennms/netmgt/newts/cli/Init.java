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
package org.opennms.netmgt.newts.cli;

import java.util.ServiceLoader;

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.newts.cassandra.Schema;
import org.opennms.newts.cassandra.SchemaManager;

import javax.inject.Named;

public class Init implements Command {

    private static final ServiceLoader<Schema> s_schemas = ServiceLoader.load(Schema.class);

    @Option(name="-h", aliases={ "--help" }, help=true)
    boolean showHelp = false;

    @Option(name="-p", aliases={ "--print-only" }, usage="Prints the CQL statements instead of executing them.")
    boolean printOnly = false;

    @Option(name="-r", aliases={ "--replication-factor" }, usage="Sets the replication factor to use when creating the keyspace.")
    int replicationFactor = SchemaManager.DEFAULT_REPLICATION_FACTOR;

    @Override
    public void execute() throws Exception {
        if (showHelp) {
            System.out.println("Usage: $OPENNMS_HOME/bin/newts init");
            CmdLineParser parser = new CmdLineParser(new Init());
            parser.printUsage(System.out);
            return;
        }

        String datacenter = System.getProperty("org.opennms.newts.config.datacenter", "datacenter1");
        String keyspace = System.getProperty("org.opennms.newts.config.keyspace", "newts");
        String hostname = System.getProperty("org.opennms.newts.config.hostname", "localhost");
        int port = SystemProperties.getInteger("org.opennms.newts.config.port", 9042);
        String username = System.getProperty("org.opennms.newts.config.username");
        String password = System.getProperty("org.opennms.newts.config.password");
        boolean ssl = Boolean.getBoolean("org.opennms.newts.config.ssl");
        String driverSettingsFile = System.getProperty("org.opennms.newts.config.driver-settings-file");

        if (!Strings.isNullOrEmpty(driverSettingsFile)) {
            System.out.printf("Initializing the '%s' keyspace on %s:%d%n", keyspace, hostname, port);
        } else {
            System.out.printf("Initializing the '%s' keyspace with driver settings from: %s%n", keyspace, driverSettingsFile);
        }
        try (SchemaManager m = new SchemaManager(datacenter, keyspace,
                hostname, port, username, password, ssl, driverSettingsFile)) {
            m.setReplicationFactor(replicationFactor);
            for (Schema s : s_schemas) {
                m.create(s, true, printOnly);
            }
        }

        if (!printOnly) {
            System.out.println("The keyspace was successfully created.");
        }
    }
}
