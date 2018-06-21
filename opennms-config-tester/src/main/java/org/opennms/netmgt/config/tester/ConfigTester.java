/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.tester;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.tester.checks.ConfigCheckValidationException;
import org.opennms.netmgt.config.tester.checks.PropertiesFileChecker;
import org.opennms.netmgt.config.tester.checks.XMLFileChecker;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ConfigTester implements ApplicationContextAware {

	private Logger log = Logger.getLogger(ConfigTester.class.getName());

	private ApplicationContext m_context;
	private Map<String, String> m_configs;

	public Map<String, String> getConfigs() {
		return m_configs;
	}

	public void setConfigs(Map<String, String> configs) {
		m_configs = configs;
	}

	public ApplicationContext getApplicationContext() {
		return m_context;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		m_context = context;
	}

	public void testConfig(String name, boolean ignoreUnknown) {
		checkConfigNameValid(name, ignoreUnknown);
		String beanName = m_configs.get(name);
		if("directory".equalsIgnoreCase(beanName)){
			checkDirectoryForUnKnownFiles(name);
		} else if ("unknown".equalsIgnoreCase(beanName)){
			checkFileForSyntax(name);
		} else {
			m_context.getBean(beanName);
		}
	}

	private void checkDirectoryForUnKnownFiles(String name) {
		Path directory = Paths.get(ConfigFileConstants.getFilePathString(), name);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.{properties,xml}")) {
			for (Path entry : stream) {
				checkFileForSyntax(entry);
			}
		} catch (IOException e) {
			throw new ConfigCheckValidationException(e);
		}
	}

	private void checkFileForSyntax(String fileName) {
		File file;
		try {
			file = ConfigFileConstants.getConfigFileByName(fileName);
			checkFileForSyntax(file.toPath());
		} catch (IOException e) {
			throw new ConfigCheckValidationException(e);
		}
	}

	private void checkFileForSyntax(Path file) {
		if(file.toString().endsWith("properties")){
            PropertiesFileChecker.checkFile(file).forSyntax();
		} else if(file.toString().endsWith("xml")){
			XMLFileChecker.checkFile(file).forSyntax();
		} else {
			log.warning("No FileChecker for file found: " + file.toAbsolutePath().toString());
		}
	}

	private void checkConfigNameValid(String name, boolean ignoreUnknown) {
		if (!m_configs.containsKey(name)) {
			if (ignoreUnknown) {
				System.err.println("Unknown configuration: " + name + "... skipping.");
			} else {
				throw new IllegalArgumentException("config '" + name + "' is not a known config name");
			}
		}
	}

	public static void main(String[] argv) {

        FilterDaoFactory.setInstance(new ConfigTesterFilterDao());
        DataSourceFactory.setInstance(new ConfigTesterDataSource());
		ConfigTester tester = BeanUtils.getBean("configTesterContext", "configTester", ConfigTester.class);

		final CommandLineParser parser = new PosixParser();

		final Options options = new Options();
		options.addOption("h", "help",           false, "print this help and exit");
		options.addOption("a", "all",         	 false, "check all supported configuration files");
		options.addOption("l", "list",   		 false, "list supported configuration files and exit");
		options.addOption("v", "verbose", 		 false, "list each configuration file as it is tested");
		options.addOption("i", "ignore-unknown", false, "ignore unknown configuration files and continue processing");

		final CommandLine line;
		try {
			line = parser.parse(options, argv, false);
		} catch (ParseException e) {
			System.err.println("Invalid usage: " + e.getMessage());
			System.err.println("Run 'config-tester -h' for help.");
			System.exit(1);
			
			return; // not reached; here to eliminate warning on line being uninitialized
		}

		final boolean ignoreUnknown = line.hasOption("i");

		if ((line.hasOption('l') || line.hasOption('h') || line.hasOption('a'))) {
			if (line.getArgList().size() > 0) {
				System.err.println("Invalid usage: No arguments allowed when using the '-a', '-h', or '-l' options.");
				System.err.println("Run 'config-tester -h' for help.");
				System.exit(1);
			}
		} else {
			if (line.getArgs().length == 0) {
				System.err.println("Invalid usage: too few arguments.  Use the '-h' option for help.");
				System.exit(1);
			}
		}
		
		boolean verbose = line.hasOption('v');

		DataSourceFactory.setInstance(new ConfigTesterDataSource());

		if (line.hasOption('l')) {
			System.out.println("Supported configuration files: ");
			for (String configFile : tester.getConfigs().keySet()) {
				System.out.println("    " + configFile);
			}
			System.out.println("Note: not all OpenNMS configuration files are currently supported.");
		} else if (line.hasOption('h')) {
			 final HelpFormatter formatter = new HelpFormatter();
			 formatter.printHelp("config-tester -a\nOR: config-tester [config files]\nOR: config-tester -l\nOR: config-tester -h", options);
		} else if (line.hasOption('a')) {
			for (String configFile : tester.getConfigs().keySet()) {
				tester.testConfig(configFile, verbose, ignoreUnknown);
			}
		} else {
			for (String configFile : line.getArgs()) {
				tester.testConfig(configFile, verbose, ignoreUnknown);
			}
		}
	}

	private void testConfig(String configFile, boolean verbose, boolean ignoreUnknown) {
		if (verbose) {
			System.out.print("Testing " + configFile + " ... ");
		}
		
		long start = System.currentTimeMillis();
		testConfig(configFile, ignoreUnknown);
		long end = System.currentTimeMillis();
		
		if (verbose) {
			System.out.println("OK (" + (((float) (end - start)) / 1000) + "s)");
		}
	}
}
