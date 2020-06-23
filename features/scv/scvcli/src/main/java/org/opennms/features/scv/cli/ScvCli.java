/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;

public class ScvCli {

    private final static String DEFAULT_PASSWORD_PROPERTY = "org.opennms.features.scv.cli.password";

    @Argument(required = true,
            index = 0,
            metaVar = "ACTION",
            handler = SubCommandHandler.class)
    @SubCommands({@SubCommand(name = "list", impl = ListCommand.class),
            @SubCommand(name = "set", impl = SetCommand.class),
            @SubCommand(name = "get", impl = GetCommand.class)})
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
            secureCredentialsVault = new JCEKSSecureCredentialsVault(this.keystore, this.password);
        }

        return secureCredentialsVault;
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

            System.exit(1);
        }
    }
}
