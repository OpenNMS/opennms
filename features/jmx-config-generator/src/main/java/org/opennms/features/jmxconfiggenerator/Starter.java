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
package org.opennms.features.jmxconfiggenerator;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;
import org.opennms.features.jmxconfiggenerator.commands.CmdRunException;
import org.opennms.features.jmxconfiggenerator.commands.Command;
import org.opennms.features.jmxconfiggenerator.commands.GraphCreateCommand;
import org.opennms.features.jmxconfiggenerator.commands.JmxConfigCreateCommand;
import org.opennms.features.jmxconfiggenerator.commands.QueryCommand;
import org.opennms.features.jmxconfiggenerator.log.ConsoleLogAdapter;

import java.io.PrintStream;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class Starter extends Command {

    @Argument(
            handler=SubCommandHandler.class,
            usage = "These are the supported commands. Type JmxConfigGenerator <command> --help for more details.",
            metaVar = "<command>",
            required=false)
    @SubCommands({
            @SubCommand(name="query", impl=QueryCommand.class),
            @SubCommand(name="generate-conf", impl=JmxConfigCreateCommand.class),
            @SubCommand(name="generate-graph", impl= GraphCreateCommand.class)})
    private Command cmd;

    public static void main(String[] args) {
        Starter starter = new Starter();
        CmdLineParser parser = new CmdLineParser(starter, ParserProperties.defaults().withUsageWidth(120));
        try {
            parser.parseArgument(args);
            if (starter.cmd == null) { // no command specified, we have to use StarterCommand
                starter.cmd = starter;
            }
            starter.cmd.run(parser);
        } catch (CmdLineException e) {
            starter.LOG.error(e.getMessage());
            starter.LOG.error("");
            starter.cmd.printUsage();
            System.exit(1);
        } catch (CmdRunException ex) {
            handleException(ex, starter.LOG);
            System.exit(2);
        } catch (Exception e) {
            handleException(e, starter.LOG);
            System.exit(3);
        }
    }

    protected static void handleException(Exception ex, ConsoleLogAdapter logAdapter) {
        logAdapter.error("An unexpected error occurred.");
        logAdapter.error(ex.getMessage());
        if (logAdapter.isDebugEnabled()) {
            ex.printStackTrace(new PrintStream(logAdapter.getErrorOutputStream()));
        }
        if (ex.getCause() != null) {
            logAdapter.error("{}: {}", ex.getCause(), ex.getCause().getMessage());
            if (logAdapter.isDebugEnabled()) {
                ex.getCause().printStackTrace(new PrintStream(logAdapter.getErrorOutputStream()));
            }
        }
    }

    @Override
    protected void execute() throws CmdRunException, CmdLineException {
        // we print help, if we are Starter and no options are setj
        if (!isHelp() && cmd == this) {
            printUsage();
        }
    }

    @Override
    protected void validate(CmdLineParser parser) throws CmdLineException {
        // we do not need to do anything here
    }

    @Override
    protected String getDescription() {
        return "JmxConfigGenerator <command> [options...] [arguments...]";
    }

    @Override
    public void printUsage() {
        super.printUsage();
        LOG.info("");
        LOG.info("Examples: ");
        LOG.info(" Querying: java-jar JmxConfigGenerator.jar query --host localhost --port 7199 [--ids-only] [--show-domains] [--ignore <filter criteria>] [--include-values] <filter criteria>");
        LOG.info(" Generation of jmx-datacollection.xml: java -jar JmxConfigGenerator.jar generate-conf --host localhost --port 7199 --output JMX-DatacollectionDummy.xml [--service cassandra] [--skipDefaultVM] [--dictionary dictionary.properties]");
        LOG.info(" Generation of snmp-graph.properties: java -jar JmxConfigGenerator.jar generate-graph --input test.xml --output test.properties [--template graphTemplate.vm]");
    }
}
