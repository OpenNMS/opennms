/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.jmxconfiggenerator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.management.MBeanServerConnection;
import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.jmxconfiggenerator.graphs.GraphConfigGenerator;
import org.opennms.jmxconfiggenerator.graphs.JmxConfigReader;
import org.opennms.jmxconfiggenerator.graphs.Report;
import org.opennms.jmxconfiggenerator.helper.NameTools;
import org.opennms.jmxconfiggenerator.jmxconfig.JmxDatacollectionConfiggenerator;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class Starter {

    @Option(name = "-jmx", usage = "Generate jmx-datacollection.xml by reading JMX over RMI")
    private boolean jmx = false;

    @Option(name = "-service", usage = "Your optional service-name. Like cassandra, jboss, tomcat")
    private String serviceName = "anyservice";

    @Option(name = "-host", usage = "Hostname or IP-Adress of JMX-RMI host")
    private String hostName;

    @Option(name = "-username", usage = "Username for JMX-RMI Authentication")
    private String username;

    @Option(name = "-password", usage = "Password for JMX-RMI Authentication")
    private String password;

    @Option(name = "-port", usage = "Port of JMX-RMI service")
    private String port;

    @Option(name = "-jmxmp", usage = "Use JMXMP and not JMX-RMI")
    private boolean jmxmp = false;

//    @Option(name = "-ssl", usage = "Use SSL for the connection")
    private boolean ssl = false;

    @Option(name = "-skipDefaultVM", usage = "set to process default JavaVM Beans.")
    private boolean skipDefaultVM = false;

    @Option(name = "-runWritableMBeans", usage = "include MBeans that are read- and writable.")
    private boolean runWritableMBeans = false;

    @Option(name = "-graph", usage = "Generate snmp-graph.properties linke file to out, by reading jmx-datacollection.xml like file from input")
    private boolean graph = false;

    @Option(name = "-input", usage = "Jmx-datacolletion.xml like file to parse")
    private String inputFile;

    @Option(name = "-out", usage = "File to write generated snmp-graph.properties linke content")
    private String outFile;

    @Option(name = "-template", usage = "Template file for SnmpGraphs")
    private String templateFile;

    @Option(name = "-dictionary", usage = "Dictionary properties file for replacing attribute names and parts of this names")
    private String dictionaryFile;

    public static void main(String[] args) throws IOException {
        new Starter().doMain(args);
    }

    public void doMain(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);

        parser.setUsageWidth(80);

        try {
            parser.parseArgument(args);
            if (jmx && graph) {
                throw new CmdLineException(parser, "jmx and graph is set. Just use one at a time.");
            }
            if (!jmx && !graph) {
                throw new CmdLineException(parser, "set jmx or graph.");
            }
            if (jmx && hostName != null && port != null && outFile != null) {
                NameTools.loadInternalDictionary();
                if (dictionaryFile != null) {
                    NameTools.loadExtermalDictionary(dictionaryFile);
                }
                JmxDatacollectionConfiggenerator jmxConfigGenerator = new JmxDatacollectionConfiggenerator();
                MBeanServerConnection mBeanServerConnection = jmxConfigGenerator.createMBeanServerConnection(hostName, port, username, password, ssl, jmxmp);
                JmxDatacollectionConfig generateJmxConfigModel = jmxConfigGenerator.generateJmxConfigModel(mBeanServerConnection, serviceName, !skipDefaultVM, runWritableMBeans);
                jmxConfigGenerator.writeJmxConfigFile(generateJmxConfigModel, outFile);
                return;
            }
            if (graph && inputFile != null && outFile != null) {
                JmxConfigReader jmxToSnmpGraphConfigGen = new JmxConfigReader();
                Collection<Report> reports = jmxToSnmpGraphConfigGen.generateReportsByJmxDatacollectionConfig(inputFile);

                GraphConfigGenerator graphConfigGenerator = new GraphConfigGenerator();

                String snmpGraphConfig;
                if (templateFile != null) {
                    snmpGraphConfig = graphConfigGenerator.generateSnmpGraph(reports, templateFile);
                } else {
                    snmpGraphConfig = graphConfigGenerator.generateSnmpGraph(reports);
                }

                System.out.println(snmpGraphConfig);
                FileUtils.writeStringToFile(new File(outFile), snmpGraphConfig, "UTF-8");
                return;
            }
            throw new CmdLineException(parser, "no valid call found.");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("JmxConfigGenerator [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            // System.err.println("  Example: java -jar JmxConfigGenerator" +
            // parser.printExample(ALL));
            System.err.println("Examples:");
            System.err.println(" Generation of jmx-datacollection.xml: java -jar JmxConfigGenerator.jar -jmx -host localhost -port 7199 -out JMX-DatacollectionDummy.xml [-service cassandra] [-skipDefaultVM] [-runWritableMBeans] [-dictionary dictionary.properties]");
            System.err.println(" Generation of snmp-graph.properties: java -jar JmxConfigGenerator.jar -graph -input test.xml -out test.properies [-template graphTemplate.vm] [-service cassandra]");
        }
    }
}
