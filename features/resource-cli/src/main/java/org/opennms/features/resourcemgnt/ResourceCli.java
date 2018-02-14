/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.resourcemgnt;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;
import org.opennms.features.resourcemgnt.commands.Command;
import org.opennms.features.resourcemgnt.commands.DeleteCommand;
import org.opennms.features.resourcemgnt.commands.ListCommand;
import org.opennms.features.resourcemgnt.commands.ShowCommand;

public class ResourceCli {

    @Argument(required = true,
              index = 0,
              metaVar = "action",
              handler = SubCommandHandler.class)
    @SubCommands({@SubCommand(name = "list", impl = ListCommand.class),
                  @SubCommand(name = "show", impl = ShowCommand.class),
                  @SubCommand(name = "delete", impl = DeleteCommand.class)})
    private Command command;

    @Option(required = false, name = "--url", usage = "URL of your OpenNMS installation")
    private String baseUrl = "http://localhost:8980/opennms";

    @Option(required = false, name = "--username", usage = "username")
    private String username = "admin";

    @Option(required = false, name = "--password", usage = "password")
    private String password = "admin";

    @Option(name = "--help", usage = "display help and exit")
    private boolean help = false;

    public static void main(final String args[]) {
        final ResourceCli resourceCli = new ResourceCli();
        resourceCli.parseArguments(args);

        try {
            resourceCli.command.execute(resourceCli);

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void parseArguments(final String args[]) {
        // Parse the arguments
        final CmdLineParser cmdLineParser = new CmdLineParser(this);

        try {
            cmdLineParser.parseArgument(args);
        } catch (final CmdLineException e) {
            System.err.println("Error: " + e.getMessage() + "\n");

            displayHelp(cmdLineParser);
            System.exit(-1);
        }

        // Display help message if "--help" was used
        if (help) {
            displayHelp(cmdLineParser);
            System.exit(0);
        }
    }

    /**
     * Displays the usage help message.
     *
     * @param cmdLineParser the parser instance to be used
     */
    private void displayHelp(final CmdLineParser cmdLineParser) {
        System.err.println("OpenNMS simple resource management tool\n");

        System.err.println("--- Usage ---\n");
        System.err.println("  Listing available resources:");
        System.err.println("    resourcecli [options] list");
        System.err.println("  Displaying details for given resource:");
        System.err.println("    resourcecli [options] show <resource>");
        System.err.println("  Deleting a resource:");
        System.err.println("    resourcecli [options] delete <resource>\n");
        System.err.println("--- Additional options ---\n");
        cmdLineParser.printUsage(System.err);
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }
}
