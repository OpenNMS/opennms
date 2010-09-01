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
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.service.Invoke;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.types.InvokeAtType;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.vmmgr.Invoker;
import org.opennms.netmgt.vmmgr.InvokerService;

public final class Main {
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
        } catch (Exception e) {
            LogUtils.warnf(Main.class, e, "An error occurred trying to parse the command-line.");
        }
        
        System.out.println("- using " + OPENNMS_HOME + "/etc for configuration files");
        System.setProperty("opennms.home", OPENNMS_HOME);

        configureLog4j(OPENNMS_HOME);
        
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


    private static void configureLog4j(final String homeDir) {
        File etcDir = new File(homeDir, "etc");
        
        File xmlFile = new File(etcDir, "log4j.xml");
        if (xmlFile.exists()) {
            DOMConfigurator.configureAndWatch(xmlFile.getAbsolutePath());
        } else {
            File propertiesFile = new File(etcDir, "log4j.properties");
            if (propertiesFile.exists()) {
                PropertyConfigurator.configureAndWatch(propertiesFile.getAbsolutePath());
            } else {
                System.err.println("Could not find a Log4j configuration file at "
                        + xmlFile.getAbsolutePath() + " or "
                        + propertiesFile.getAbsolutePath() + ".  Exiting.");
                System.exit(1);
            }
        }
    }
}
