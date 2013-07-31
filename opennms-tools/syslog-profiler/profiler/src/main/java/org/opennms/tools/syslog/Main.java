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

package org.opennms.tools.syslog;

import java.io.File;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.service.Invoke;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.types.InvokeAtType;
import org.opennms.netmgt.vmmgr.Invoker;
import org.opennms.netmgt.vmmgr.InvokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {
    
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    
    public static String OPENNMS_HOME;

    @SuppressWarnings({ "static-access", "deprecation" })
    public static void main (final String... args) throws Exception {
        final Options options = new Options();
        options.addOption(OptionBuilder.withDescription("this help").withLongOpt("help").create("h"));
        options.addOption(OptionBuilder.hasArg().withArgName("DIRECTORY").withDescription("OpenNMS home directory").withLongOpt("opennms-home").create("o"));

        final CommandLineParser parser = new GnuParser();
        try {
            final CommandLine line = parser.parse(options, args);
            if (line.hasOption("help")) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("syslog-profiler", options, true);
                System.exit(1);
            }
            if (line.hasOption("opennms-home")) {
                OPENNMS_HOME = line.getOptionValue("opennms-home");
            } else {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("syslog-profiler", "You must specify your OpenNMS home.", options, null);
                System.exit(1);
            }
        } catch (Throwable e) {
            LOG.warn("An error occurred trying to parse the command-line.", e);
        }
        
        System.out.println("- using " + OPENNMS_HOME + "/etc for configuration files");
        System.setProperty("opennms.home", OPENNMS_HOME);


        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        List<Invoke> invokes = new ArrayList<Invoke>();
        invokes.add((Invoke)CastorUtils.unmarshal(Invoke.class, new StringReader("<invoke at=\"start\" pass=\"0\" method=\"init\"/>")));
        invokes.add((Invoke)CastorUtils.unmarshal(Invoke.class, new StringReader("<invoke at=\"start\" pass=\"1\" method=\"start\"/>")));
        invokes.add((Invoke)CastorUtils.unmarshal(Invoke.class, new StringReader("<invoke at=\"status\" pass=\"0\" method=\"status\"/>")));
        invokes.add((Invoke)CastorUtils.unmarshal(Invoke.class, new StringReader("<invoke at=\"stop\" pass=\"0\" method=\"stop\"/>")));

        List<Service> services = new ArrayList<Service>();

        Invoker invoker = new Invoker();
        invoker.setServer(server);
        invoker.setAtType(InvokeAtType.START);
        for (final Service s : Invoker.getDefaultServiceConfigFactory().getServices()) {
            if (s.getName().contains("Eventd") || s.getName().contains("Syslogd")) {
                services.add(s);
            }
        }
        List<InvokerService> invokerServices = InvokerService.createServiceList(services.toArray(new Service[0]));
        System.err.println(invokerServices);
        invoker.setServices(invokerServices);
        invoker.instantiateClasses();

        Thread.sleep(10000);
    }


}
