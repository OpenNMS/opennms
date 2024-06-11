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
package org.opennms.features.jmxconfiggenerator.commands;

import com.google.common.io.ByteStreams;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.features.jmxconfiggenerator.graphs.GraphConfigGenerator;
import org.opennms.features.jmxconfiggenerator.graphs.JmxConfigReader;
import org.opennms.features.jmxconfiggenerator.graphs.Report;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Implements the "create-graph" command functionality.
 */
public class GraphCreateCommand extends Command {

    @Option(name = "--input",
            usage = "Jmx-datacolletion.xml file to use as input to generate the graph properties file",
            metaVar = "<jmx-datacollection.xml>")
    private String inputFile;

    @Option(name = "--output",
            usage = "File name for the generated graph properties file.",
            required = false,
            metaVar = "<file>")
    private String outFile;

    @Option(name = "--template",
            usage = "Template file using Apache Velocity template engine to be used to generate the graph properties. Use --print-template to print the default template",
            required = false,
            metaVar = "<file>")
    private String templateFile;

    @Option(name = "--print-template", usage = "Prints the default template.", required = false)
    private boolean printTemplate;

    @Override
    protected void execute() throws CmdRunException, CmdLineException {
        if (printTemplate) {
            LOG.info(getClass().getResourceAsStream("/graphTemplate.vm"));
            return;
        }

        try {
            JmxConfigReader jmxToSnmpGraphConfigGen = new JmxConfigReader(LOG);
            Collection<Report> reports = jmxToSnmpGraphConfigGen.generateReportsByJmxDatacollectionConfig(inputFile);

            GraphConfigGenerator graphConfigGenerator = new GraphConfigGenerator(LOG);
            String snmpGraphConfig = graphConfigGenerator.generateSnmpGraph(reports, templateFile);

            LOG.info(snmpGraphConfig);
            ByteStreams.copy(new ByteArrayInputStream(snmpGraphConfig.getBytes()), new FileOutputStream(outFile));
        } catch (IOException ioex) {
            throw new CmdRunException(ioex);
        }
    }

    @Override
    protected String getDescription() {
        return "Generates the snmp-graph.properties file.";
    }

    @Override
    protected void validate(CmdLineParser parser) throws CmdLineException {
        if (printTemplate) {
            return;
        }
        if (inputFile == null) {
            throw new CmdLineException(getParser(), "You have not specified an input file.");
        }
        if (!Files.exists(Paths.get(inputFile))) {
            throw new CmdLineException(getParser(), "You have specified an input file which does not exist.");
        }
        if (outFile == null) {
            LOG.warn("No output file name defined using: snmp-graph.properties");
            outFile = "snmp-graph.properties";
        }
        if (Files.exists(Paths.get(outFile))) {
            throw new CmdLineException("The specified outfile already exists.");
        }
        if (templateFile != null && !Files.exists(Paths.get(templateFile))) {
            throw new CmdLineException("The specified template file does not exist.");
        }
    }
}
