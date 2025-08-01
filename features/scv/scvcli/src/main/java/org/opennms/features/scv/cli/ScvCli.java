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
import org.opennms.features.scv.utils.ScvUtils;
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
    private String keystore = "scv.jce";

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
            String keystoreType = lookupKeyStoreType();
            secureCredentialsVault = new JCEKSSecureCredentialsVault(this.keystore, this.password, keystoreType);
        }

        return secureCredentialsVault;
    }

    private  String lookupKeyStoreType() {
        String keyStoreType = SecureCredentialsVault.KeyStoreType.JCEKS.toString();
        try {

            // Try to get the OpenNMS home directory from the environment variable
            String opennmsHome = System.getenv("OPENNMS_HOME");
            // Get the keystore type from SCV properties, if specified
            Properties scvProps = ScvUtils.loadScvProperties(opennmsHome);
            keyStoreType = scvProps.getProperty(ScvUtils.SCV_KEYSTORE_TYPE_PROPERTY);

            // If the password is not set, try to get it from SCV properties
            if (this.password == null || this.password.isEmpty()) {
                password = scvProps.getProperty(JCEKSSecureCredentialsVault.KEYSTORE_KEY_PROPERTY);
            }
        } catch (Exception e) {
            LOG.error("WARNING: unable to load properties files");
        }

        return keyStoreType;
    }

    private static String lookupDefaultPassword() {
        Properties properties = new Properties();
        try{
            String passowrd = lookupPasswordFromProperties();
            if (passowrd != null && !passowrd.isEmpty()) {
                return passowrd;
            }
            properties.load(ScvCli.class.getResourceAsStream("/scvcli.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getProperty(DEFAULT_PASSWORD_PROPERTY);
    }

    /**
     * Loads the keystore password from the OpenNMS properties.
     * <p>
     * This method looks for a property with key {@code org.opennms.features.scv.jceks.key} in the
     * OpenNMS properties configurations.
     * </p>
     *
     * @return the keystore password from the properties file.
     */
    private static String lookupPasswordFromProperties(){
        String keyStoreKey = null;
        // Try to get the OpenNMS home directory from the environment variable
        String opennmsHome = System.getenv("OPENNMS_HOME");
        Properties scvProps = ScvUtils.loadScvProperties(opennmsHome);
        keyStoreKey = scvProps.getProperty(ScvUtils.KEYSTORE_KEY_PROPERTY);
        return keyStoreKey;

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
