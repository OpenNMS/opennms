/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.systemreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.opennms.bootstrap.Bootstrap;
import org.opennms.core.soa.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SystemReport extends Bootstrap {
    private static final Logger LOG = LoggerFactory.getLogger(SystemReport.class);
    final static Pattern m_pattern = Pattern.compile("^-D(.*?)=(.*)$");

    public static void main(String[] args) throws Exception {
        loadDefaultProperties();
        executeClass("org.opennms.systemreport.SystemReport", "report", args, true, true);
    }

    /**
     * @param args
     */
    public static void report(String[] args) throws Exception {
        final String tempdir = System.getProperty("java.io.tmpdir");

        // pull out -D defines first
        for (final String arg : args) {
            if (arg.startsWith("-D") && arg.contains("=")) {
                final Matcher m = m_pattern.matcher(arg);
                if (m.matches()) {
                    System.setProperty(m.group(1), m.group(2));
                }
            }
        }
        if (System.getProperty("opennms.home") == null) {
            System.setProperty("opennms.home", tempdir);
        }
        if (System.getProperty("rrd.base.dir") == null) {
            System.setProperty("rrd.base.dir", tempdir);
        }
        if (System.getProperty("rrd.binary") == null) {
            System.setProperty("rrd.binary", "/usr/bin/rrdtool");
        }

        final CommandLineParser parser = new PosixParser();

        final Options options = new Options();
        options.addOption("h", "help",           false, "this help");
        options.addOption("D", "define",         true,  "define a java property");
        options.addOption("p", "list-plugins",   false, "list the available system report plugins");
        options.addOption("u", "use-plugins",    true,  "select the plugins to output");
        options.addOption("l", "list-formats",   false, "list the available output formats");
        options.addOption("f", "format",         true,  "the format to output");
        options.addOption("o", "output",         true,  "the file to write output to");
        
        final CommandLine line = parser.parse(options, args, false);
        final Set<String> plugins = new LinkedHashSet<String>();
        
        final SystemReport report = new SystemReport();

        // help
        if (line.hasOption("h")) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("system-report.sh [options]", options);
            System.exit(0);
        }

        // format and output file
        if (line.hasOption("f")) {
            report.setFormat(line.getOptionValue("f"));
        }
        if (line.hasOption("o")) {
            report.setOutput(line.getOptionValue("o"));
        }
        if (line.hasOption("u")) {
            final String value = line.getOptionValue("u");
            if (value != null) {
                for (final String s : value.split(",+")) {
                    plugins.add(s);
                }
            }
        }

        // final command
        if (line.hasOption("p")) {
            report.listPlugins();
        } else if (line.hasOption("l")) {
            report.listFormats();
        } else {
            report.writePluginData(plugins);
        }
    }

    private ServiceRegistry m_serviceRegistry;
    private String m_output = "-";
    private String m_format = "text";

    private void setOutput(final String file) {
        m_output = file;
    }

    private void setFormat(final String format) {
        m_format = format;
    }

    private void writePluginData(final Collection<String> plugins) {
        initializeSpring();

        SystemReportFormatter formatter = null;
        for (final SystemReportFormatter f : getFormatters()) {
            if (m_format.equals(f.getName())) {
                formatter = f;
                break;
            }
        }
        if (formatter == null) {
            LOG.error("Unknown format '{}'!", m_format);
            System.exit(1);
        }

        formatter.setOutput(m_output);

        OutputStream stream = null;
        if (formatter.needsOutputStream()) {
            if (m_output.equals("-")) {
                stream = System.out;
            } else {
                try {
                    final File f = new File(m_output);
                    f.delete();
                    stream = new FileOutputStream(f, false);
                } catch (final FileNotFoundException e) {
                    LOG.error("Unable to write to '{}'", m_output, e);
                    System.exit(1);
                }
            }

            if (m_output.equals("-") && !formatter.canStdout()) {
                LOG.error("{} formatter does not support writing to STDOUT!", formatter.getName());
                System.exit(1);
            }

            formatter.setOutputStream(stream);
        }

        final int pluginSize = plugins.size();
        final Map<String,SystemReportPlugin> pluginMap = new HashMap<String,SystemReportPlugin>();
        for (final SystemReportPlugin plugin : getPlugins()) {
            final String name = plugin.getName();
            if (pluginSize == 0) plugins.add(name);
            pluginMap.put(name, plugin);
        }

        try {
            formatter.begin();
            if (stream != null) stream.flush();
            for (final String pluginName : plugins) {
                final SystemReportPlugin plugin = pluginMap.get(pluginName);
                if (plugin == null) {
                    LOG.warn("No plugin named '{}' found, skipping.", pluginName);
                } else {
                    try {
                        formatter.write(plugin);
                    } catch (final Exception e) {
                        LOG.error("An error occurred calling plugin '{}'", plugin.getName(), e);
                    }
                    if (stream != null) stream.flush();
                }
            }
            formatter.end();
            if (stream != null) stream.flush();
        } catch (final Exception e) {
            LOG.error("An error occurred writing plugin data to output.", e);
            System.exit(1);
        }

        IOUtils.closeQuietly(stream);
    }

    private void listPlugins() {
        for (final SystemReportPlugin plugin : getPlugins()) {
            System.err.println(plugin.getName() + ": " + plugin.getDescription());
        }
    }

    private void listFormats() {
        for (final SystemReportFormatter formatter : getFormatters()) {
            System.err.println(formatter.getName() + ": " + formatter.getDescription());
        }
    }

    public List<SystemReportPlugin> getPlugins() {
        initializeSpring();
        final List<SystemReportPlugin> plugins = new ArrayList<SystemReportPlugin>(m_serviceRegistry.findProviders(SystemReportPlugin.class));
        Collections.sort(plugins);
        return plugins;
    }

    public List<SystemReportFormatter> getFormatters() {
        initializeSpring();
        final List<SystemReportFormatter> formatters = new ArrayList<SystemReportFormatter>(m_serviceRegistry.findProviders(SystemReportFormatter.class));
        Collections.sort(formatters);
        return formatters;
    }

    private void initializeSpring() {
        if (m_serviceRegistry == null) {
            List<String> configs = new ArrayList<String>();
            configs.add("classpath:/META-INF/opennms/applicationContext-soa.xml");
            configs.add("classpath:/META-INF/opennms/applicationContext-dao.xml");
            configs.add("classpath*:/META-INF/opennms/component-dao.xml");
            configs.add("classpath:/META-INF/opennms/applicationContext-systemReport.xml");
            final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configs.toArray(new String[0]));
            m_serviceRegistry = (ServiceRegistry) context.getBean("serviceRegistry");
        }
    }

    public void setServiceRegistry(final ServiceRegistry registry) {
        m_serviceRegistry = registry;
    }
}
