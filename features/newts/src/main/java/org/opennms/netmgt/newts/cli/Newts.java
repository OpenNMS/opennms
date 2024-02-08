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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;

public class Newts {

    @Argument(handler=SubCommandHandler.class, required=true)
    @SubCommands({
        @SubCommand(name="init", impl=Init.class),
    })
    Command cmd;

    public static void main(String[] args) throws Exception {
        // NMS-8051: opennms_bootstrap.jar in versions < 18.0.0 does not load
        // all of the system properties, so we need to do this ourselves
        loadOpenNMSProperties();

        Newts cli = new Newts();
        CmdLineParser parser = new CmdLineParser(cli);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.out.println("Usage: $OPENNMS_HOME/bin/newts <command>");
            System.out.println();
            System.out.println("Supported commands are:");
            System.out.println("\tinit");
            System.out.println();
            System.out.println("Run $OPENNMS_HOME/bin/newts <command> -h for command specific options.");
            return;
        }

        cli.cmd.execute();
    }

    private static void loadOpenNMSProperties() throws FileNotFoundException, IOException {
        // Find the opennms.properties file
        File props = Paths.get(System.getProperty("opennms.home"), "etc", "opennms.properties").toFile();
        if (!props.canRead()) {
            throw new IOException("Cannot read opennms.properties file: " + props);
        }

        // Load the properties
        try (FileInputStream fis = new FileInputStream(props)) {
            Properties p = new Properties();
            p.load(fis);

            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                String propertyName = entry.getKey().toString();
                Object value = entry.getValue();
                // Only set the value of a system property if it is not already set
                if (System.getProperty(propertyName) == null && value != null) {
                    System.setProperty(propertyName, value.toString());
                }
            }
        }
    }
}
