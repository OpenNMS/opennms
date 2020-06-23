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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.JMException;
import javax.management.MBeanServerConnection;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.features.jmxconfiggenerator.jmxconfig.JmxDatacollectionConfiggenerator;
import org.opennms.features.jmxconfiggenerator.jmxconfig.JmxHelper;
import org.opennms.features.jmxconfiggenerator.jmxconfig.query.MBeanServerQueryException;
import org.opennms.features.jmxconfiggenerator.log.ConsoleLogAdapter;
import org.opennms.features.jmxconfiggenerator.log.LogAdapter;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;

/**
 * Command to create the jmx-configgenerator.xml file.
 */
public class JmxConfigCreateCommand extends JmxCommand {

    private final LogAdapter LOG = new ConsoleLogAdapter();

    @Option(name = "--service", usage = "your optional service-name. Like cassandra, jboss, tomcat")
    private String serviceName = "anyservice";

    @Option(name = "--skipDefaultVM", usage = "set to process default JavaVM Beans.")
    private boolean skipDefaultVM = false;

    @Option(name = "--skipNonNumber", usage = "do not process any non-number values.")
    private boolean skipNonNumber = false;

    @Option(name = "--output", usage = "file name to write generated jmx-datacollection-config.xml", required = false, metaVar = "<file>")
    private String outFile;

    @Option(name = "--dictionary", usage = "dictionary properties file for replacing attribute names and parts of this names", metaVar = "<file>")
    private String dictionaryFile;

    @Option(name = "--print-dictionary", usage = "prints the used dictionary to the cmd line. May be used with --dictionary")
    private boolean showDictionary;

    @Argument(usage = "a list of attribute ids to to include for the generation.", metaVar = "<attribute id>", required = false)
    private List<String> ids;

    @Override
    protected void validate(CmdLineParser parser) throws CmdLineException {
        if (showDictionary) {
            return;
        }
        super.validate(parser);
        if (outFile == null) {
            throw new CmdLineException("You must specify --output <file>. See --help for more details.");
        }
        if (ids == null) {
            throw new CmdLineException("You must specify at least one <attribute id>. See --help for more details.");
        }
    }

    @Override
    protected void execute() throws CmdLineException, CmdRunException {
        if (showDictionary) {
            printDictionary();
            return;
        }
        super.execute();
    }

    @Override
    protected void execute(MBeanServerConnection mbeanServerConnection) throws IOException, MBeanServerQueryException, JMException {
        JmxDatacollectionConfiggenerator jmxConfigGenerator = new JmxDatacollectionConfiggenerator(LOG);
        Map<String, String> dictionary = loadDictionary();
        JmxDatacollectionConfig generateJmxConfigModel = jmxConfigGenerator.generateJmxConfigModel(ids, mbeanServerConnection, serviceName, !skipDefaultVM, skipNonNumber, dictionary);
        jmxConfigGenerator.writeJmxConfigFile(generateJmxConfigModel, outFile);
    }

    private void printDictionary() {
        try {
            Map<String, String> dictionary = loadDictionary();
            if (dictionaryFile != null) {
                LOG.info("Custom dictionary file '{}':", dictionaryFile);
            } else {
                LOG.info("Internal dictionary file:");
            }
            for (Map.Entry<String, String> eachEntry : dictionary.entrySet()) {
                LOG.info("{} = {}", eachEntry.getKey(), eachEntry.getValue());
            }
        } catch (IOException ioEx) {
            new CmdRunException(ioEx);
        }
    }

    private Map<String, String> loadDictionary() throws IOException {
        if (dictionaryFile != null) {
            LOG.debug("Custom dictionary file '{}' defined. Loading...", dictionaryFile);
            return loadExternalDictionary(dictionaryFile);
        } else {
            LOG.debug("No dictionary file defined. Using internal dictionary...");
            return loadInternalDictionary();
        }
    }

    @Override
    protected String getDescription() {
        return "Creates the jmxconfiguration.xml file with the given attribute ids.";
    }

    private Map<String, String> loadInternalDictionary() throws IOException {
        Map<String, String> internalDictionary = JmxHelper.loadInternalDictionary();
        LOG.info("Dictionary entries loaded: '{}'", internalDictionary.size());
        return internalDictionary;
    }

    private Map<String, String> loadExternalDictionary(String dictionaryFile) throws IOException {
        Map<String, String> externalDictionary = new HashMap<String, String>();
        Properties properties = new Properties();
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(dictionaryFile))) {
            properties.load(stream);
        }
        LOG.info("Loaded '{}' external dictionary entries from '{}'", properties.size(), dictionaryFile);
        for (final Map.Entry<?,?> entry : properties.entrySet()) {
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            externalDictionary.put(key.toString(), value == null? null : value.toString());
        }
        LOG.info("Dictionary entries loaded: '{}'", externalDictionary.size());
        return externalDictionary;
    }
}
