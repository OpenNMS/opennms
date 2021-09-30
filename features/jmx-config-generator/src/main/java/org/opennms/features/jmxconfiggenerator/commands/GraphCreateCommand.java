/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
