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
package org.opennms.features.scv.cli;

import java.io.IOException;
import java.util.Properties;
import java.util.function.Function;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.cli.commands.GetCommand;
import org.opennms.features.scv.cli.commands.ListCommand;
import org.opennms.features.scv.cli.commands.SetCommand;
import org.opennms.features.scv.cli.commands.DeleteCommand;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScvCli {

    public static final Logger LOG = LoggerFactory.getLogger(ScvCli.class);
    private final static String DEFAULT_PASSWORD_PROPERTY = "org.opennms.features.scv.cli.password";

    @Argument(required = true,
            index = 0,
            metaVar = "ACTION",
            handler = SubCommandHandler.class)
    @SubCommands({@SubCommand(name = "list", impl = ListCommand.class),
            @SubCommand(name = "set", impl = SetCommand.class),
            @SubCommand(name = "get", impl = GetCommand.class),
            @SubCommand(name = "delete", impl = DeleteCommand.class)})
    private Function<ScvCli, Integer> command;

    @Option(name = "--keystore",
            aliases = {"-k"},
            required = false,
            metaVar = "KEYSTORE")
    private String keystore = lookupKeyStore();

    @Option(name = "--password",
            aliases = {"-p"},
            required = false,
            metaVar = "PASSWORD")
    private String password = lookupDefaultPassword();

    private SecureCredentialsVault secureCredentialsVault;

    private ScvCli() {
    }

    public SecureCredentialsVault getSecureCredentialsVault() {
        if (secureCredentialsVault == null) {
            secureCredentialsVault = new JCEKSSecureCredentialsVault(this.keystore, this.password);
        }

        return secureCredentialsVault;
    }

    private static String lookupKeyStore() {
        Properties properties = new Properties();
        String defaultKeyStore="scv.jce";
        try {
            properties.load(ScvCli.class.getResourceAsStream("/scvcli-filtered.properties"));
            String opennmsHome = properties.getProperty("install.dir");
            SecureCredentialsVault.loadScvProperties(opennmsHome);
        } catch (Exception e) {
            LOG.error("WARNING: unable to load properties files");
        }

        return defaultKeyStore;
    }

    private static String lookupDefaultPassword() {
        Properties properties = new Properties();
        try {
            properties.load(ScvCli.class.getResourceAsStream("/scvcli.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getProperty(DEFAULT_PASSWORD_PROPERTY);
    }

    public static void main(final String args[]) {
        final ScvCli scvCli = new ScvCli();

        scvCli.parseArguments(args);

        try {
            System.exit(scvCli.command.apply(scvCli));
        } catch (final Exception e) {
            e.printStackTrace();

            System.exit(1);
        }
    }

    public void parseArguments(final String args[]) {
        final CmdLineParser cmdLineParser = new CmdLineParser(this);

        try {
            cmdLineParser.parseArgument(args);
        } catch (final CmdLineException e) {
            System.err.println("Error: " + e.getMessage() + "\n");

            System.err.println("Usage: scvcli [--keystore KEYSTORE] [--password PASSWORD] set ALIAS USERNAME PASSWORD [--attribute key=value]...");
            System.err.println("       scvcli [--keystore KEYSTORE] [--password PASSWORD] get ALIAS");
            System.err.println("       scvcli [--keystore KEYSTORE] [--password PASSWORD] list");
            System.err.println("       scvcli [--keystore KEYSTORE] [--password PASSWORD] delete ALIAS");

            System.exit(1);
        }
    }
}
