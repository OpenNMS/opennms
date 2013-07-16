/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.features.jmxconfiggenerator.graphs.GraphConfigGenerator;
import org.opennms.features.jmxconfiggenerator.graphs.JmxConfigReader;
import org.opennms.features.jmxconfiggenerator.graphs.Report;
import org.opennms.features.jmxconfiggenerator.jmxconfig.JmxDatacollectionConfiggenerator;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class Starter {

    private static Logger logger = LoggerFactory.getLogger(Starter.class);
    
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
    
    // @Option(name = "-ssl", usage = "Use SSL for the connection")
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
    
    @Option(name = "-url", usage = "JMX URL Usage: <hostname>:<port> OR service:jmx:<protocol>:<sap> OR service:jmx:remoting-jmx://<hostname>:<port>")
    private String url;
	
    private Map<String, String> dictionary = new HashMap<String, String>();

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
            } else if (!jmx && !graph) {
                throw new CmdLineException(parser, "set jmx or graph.");
            }

            dictionary = loadInternalDictionary();
            if (dictionaryFile != null) {
                dictionary = loadExternalDictionary(dictionaryFile);
            }

            if (jmx) {
                JMXConnector jmxConnector = null;
                JmxDatacollectionConfiggenerator jmxConfigGenerator = new JmxDatacollectionConfiggenerator();
                
                JMXServiceURL jmxServiceURL = null;
                if (hostName != null && port != null && outFile != null) {
                    jmxServiceURL = jmxConfigGenerator.getJmxServiceURL(jmxmp, hostName, port);
                } else if (url != null && outFile != null) {
                    jmxServiceURL = new JMXServiceURL(url);
                } else {
                    throw new CmdLineException(parser, "no valid call found.");
                }

                jmxConnector = jmxConfigGenerator.getJmxConnector(username, password, jmxServiceURL);
                MBeanServerConnection mBeanServerConnection = jmxConfigGenerator.createMBeanServerConnection(jmxConnector);
                JmxDatacollectionConfig generateJmxConfigModel = jmxConfigGenerator.generateJmxConfigModel(mBeanServerConnection, serviceName, !skipDefaultVM, runWritableMBeans, dictionary);
                jmxConfigGenerator.writeJmxConfigFile(generateJmxConfigModel, outFile);

                if (jmxConnector != null) {
                    logger.debug("closing connection");
                    jmxConnector.close();
                    logger.debug("connection closed");
                }

                return;

            } else if (graph) {
                if (inputFile != null && outFile != null) {

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
                } else {
                    throw new CmdLineException(parser, "no valid call found.");
                }
            } else {
                throw new CmdLineException(parser, "no valid call found.");
            }
        } catch (Exception e) {
            logger.error("An exception occured", e);
            System.err.println("JmxConfigGenerator [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            // System.err.println("  Example: java -jar JmxConfigGenerator" +
            // parser.printExample(ALL));
            System.err.println("Examples:");
            System.err
                    .println(" Generation of jmx-datacollection.xml: java -jar JmxConfigGenerator.jar -jmx -host localhost -port 7199 -out JMX-DatacollectionDummy.xml [-service cassandra] [-skipDefaultVM] [-runWritableMBeans] [-dictionary dictionary.properties]");
            System.err.println(" Generation of snmp-graph.properties: java -jar JmxConfigGenerator.jar -graph -input test.xml -out test.properies [-template graphTemplate.vm] [-service cassandra]");
        }
    }

    // TODO make this private!
    public static Map<String, String> loadInternalDictionary() {
        Map<String, String> internalDictionary = new HashMap<String, String>();
        Properties properties = new Properties();
        try {
            BufferedInputStream stream = new BufferedInputStream(Starter.class.getClassLoader().getResourceAsStream("dictionary.properties"));
            properties.load(stream);
            stream.close();
        } catch (IOException ex) {
            logger.error("Load dictionary entries from internal properties files error: '{}'", ex.getMessage());
        }
        logger.info("Loaded '{}' internal dictionary entries", properties.size());
        for (Object key : properties.keySet()) {
            internalDictionary.put(key.toString(), properties.get(key).toString());
        }
        logger.info("Dictionary entries loaded: '{}'", internalDictionary.size());
        return internalDictionary;
    }
    
    private Map<String, String> loadExternalDictionary(String dictionaryFile) {
        Map<String, String> externalDictionary = new HashMap<String, String>();
        Properties properties = new Properties();
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(dictionaryFile));
            properties.load(stream);
            stream.close();
        } catch (FileNotFoundException ex) {
            logger.error("'{}'", ex.getMessage());
        } catch (IOException ex) {
            logger.error("'{}'", ex.getMessage());
        }
        logger.info("Loaded '{}' external dictionary entries from '{}'", properties.size(), dictionaryFile);
        for (Object key : properties.keySet()) {
            externalDictionary.put(key.toString(), properties.get(key).toString());
        }
        logger.info("Dictionary entries loaded: '{}'", externalDictionary.size());
        return externalDictionary;
	}
}
