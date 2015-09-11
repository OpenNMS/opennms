/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
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

package org.opennms.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.rrd.model.AbstractDS;
import org.opennms.netmgt.rrd.model.RrdConvertUtils;
import org.opennms.netmgt.rrd.model.RrdSample;
import org.opennms.netmgt.rrd.model.v1.RRDv1;
import org.opennms.netmgt.rrd.model.v3.RRDv3;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.ValueType;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Lists;

/**
 * The Class NewtsConverter.
 * <p>The converter requires multi-ds metrics; otherwise it won't work.</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class NewtsConverter {

    /** The Constant CMD_SYNTAX. */
    private static final String CMD_SYNTAX = "newts-converter [options]";

    /** The SNMP directory. */
    private File snmpDir;

    /** The nodes list. */
    private List<Node> nodes;

    /** The Cassandra/Newts Repository. */
    private SampleRepository repository;

    /** The processed resources. */
    private int processedResources;

    /** The Newts inject batch size. */
    private int batchSize;

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        NewtsConverter converter = new NewtsConverter();
        converter.execute(args);
    }

    /**
     * Execute.
     *
     * @param args the arguments
     */
    public void execute(String[] args) {
        Options options = new Options();
        options.addOption(new Option("h", "help", false, "Print this help"));
        options.addOption(new Option("o", "onms-home", true, "OpenNMS Home Directory (defaults to /opt/opennms)"));
        options.addOption(new Option("r", "rrd-dir", true, "OpenNMS Home Directory (defaults to /var/opennms/rrd)"));
        options.addOption(new Option("R", "rrd-binary", true, "The binary path to the rrdtool command (defaults to /usr/bin/rrdtool)"));
        options.addOption(new Option("t", "threads", true, "Number of conversion threads (defaults to 5)"));

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: %s%n", e.getMessage()), options, null);
            System.exit(1);
        }

        // Processing Options

        if (cmd.hasOption('h')) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, null, options, null);
            System.exit(0);
        }

        File onmsHome = new File(cmd.hasOption('o') ? cmd.getOptionValue('o') : "/opt/opennms");
        if (!onmsHome.exists() || onmsHome.isFile()) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Directory %s doesn't exist%n", onmsHome.getAbsolutePath()), options, null);
            System.exit(1);
        }
        System.setProperty("opennms.home", onmsHome.getAbsolutePath());
        File rrdDir = new File(cmd.hasOption('r') ? cmd.getOptionValue('r') : "/var/opennms/rrd");
        if (!rrdDir.exists() || rrdDir.isFile()) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Directory %s doesn't exist%n", rrdDir.getAbsolutePath()), options, null);
            System.exit(1);
        }
        File rrdBinary = new File(cmd.hasOption('R') ? cmd.getOptionValue('R') : "/usr/bin/rrdtool");
        if (!rrdBinary.exists() || rrdBinary.isDirectory()) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: RRDtool command %s doesn't exist%n", rrdBinary.getAbsolutePath()), options, null);
            System.exit(1);
        }
        String threadsStr = cmd.hasOption('t') ? cmd.getOptionValue('t') : "5";
        int threads = -1;
        try {
            threads = Integer.parseInt(threadsStr);            
        } catch (Exception e) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Invalid number of threads: %s%n", e.getMessage()), options, null);
            System.exit(1);
        }

        // Initialize OpenNMS

        OnmsProperties.initialize();

        final String strategy = System.getProperty("org.opennms.timeseries.strategy", "rrd");
        final String host = System.getProperty("org.opennms.newts.config.hostname", "localhost");
        final String keyspace = System.getProperty("org.opennms.newts.config.keyspace", "newts");
        int ttl = Integer.parseInt(System.getProperty("org.opennms.newts.config.ttl", "31540000"));
        int port = Integer.parseInt(System.getProperty("org.opennms.newts.config.keyspace", "9042"));

        batchSize = Integer.parseInt(System.getProperty("org.opennms.newts.config.max_batch_size", "16"));

        System.out.printf("OpenNMS Home: %s\n", onmsHome);
        System.out.printf("RRD Directory: %s\n", rrdDir);
        System.out.printf("RRDtool CLI: %s\n", rrdBinary);
        System.out.printf("Timeseries Strategy: %s\n", strategy);
        System.out.printf("Conversion Threads: %d\n", threads);
        System.out.printf("Cassandra Host: %s\n", host);
        System.out.printf("Cassandra Port: %s\n", port);
        System.out.printf("Cassandra Keyspace: %s\n", keyspace);
        System.out.printf("Newts Max Batch Size: %d\n", batchSize);
        System.out.printf("Newts TTL: %d\n", ttl);

        if (!"newts".equals(strategy)) {
            System.err.println("ERROR: The configured timeseries strategy is not 'newts' on opennms.properties (org.opennms.timeseries.strategy).");
            System.err.println("       Fix it before continue");
            System.exit(1);
        }

        ClassPathXmlApplicationContext context = null;

        try {
            context = new ClassPathXmlApplicationContext(new String[] {
                    "classpath:/META-INF/opennms/applicationContext-soa.xml",
                    "classpath:/META-INF/opennms/applicationContext-newts.xml"
                    });
            repository = context.getBean(SampleRepository.class);
        } catch (Exception e) {
            e.printStackTrace(); // TODO This is not elegant, but it helps.
            System.err.printf("ERROR: Cannot connect to the Cassandra/Newts backend: %s%n", e.getMessage());
            System.err.println("       Make sure Newts is properly configured in opennms.properties.");
            System.err.println("       OpenNMS can be running while running the Newts converter.");
            System.exit(1);
        }

        nodes = new ArrayList<Node>();
        Connection conn = null;
        try {
            conn = DataSourceFactory.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace(); // TODO This is not elegant, but it helps.
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Cannot connect to the database: %s%n", e.getMessage()), options, null);
            System.exit(1);
        }
        final DBUtils db = new DBUtils(NewtsConverter.class);
        db.watch(conn);
        try {
            Statement st = conn.createStatement();
            db.watch(st);
            ResultSet rs = st.executeQuery("SELECT nodeid, nodelabel, foreignsource, foreignid from node n");
            db.watch(rs);
            while (rs.next()) {
                nodes.add(new Node(rs));
            }
        } catch (Throwable t) {
            t.printStackTrace(); // TODO This is not elegant, but it helps.
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Cannot obtain the nodes from the database: %s%n", t.getMessage()), options, null);
            System.exit(1);
        } finally {
            db.cleanUp();
        }
        System.out.printf("Found %d nodes on the database\n", nodes.size());

        // Process JRB/RRD files

        long start = System.currentTimeMillis();
        System.out.println("Starting Conversion...");

        processedResources = 0;
        snmpDir = new File(rrdDir, "snmp");
        System.out.println("Processing " + snmpDir);
        try {
            Files.walk(snmpDir.toPath())
            .filter(p -> p.toString().endsWith("ds.properties"))
            .forEach(p -> processResource(p.getParent()));
        } catch (Exception e) {
            e.printStackTrace(); // TODO This is not elegant, but it helps.
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Cannot get the RRD/JRB files: %s%n", e.getMessage()), options, null);
            System.exit(1);
        }

        Period period = new Interval(start, System.currentTimeMillis()).toPeriod();
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendSeconds().appendSuffix(" sec")
                .appendMinutes().appendSuffix(" min")
                .appendHours().appendSuffix(" hours")
                .appendDays().appendSuffix(" days")
                .printZeroNever()
                .toFormatter();
        System.out.printf("Conversion Finished. Enlapsed time %s\n", formatter.print(period));
        context.close();

        if (processedResources == 0) {
            System.err.println("ERROR: there are no multi-metric DS on your RRD directory. Newts requires that storeByGroup is enabled.\n");
            System.err.println("If storeByGroup is not enabled, you must do the following:");
            System.err.println("a) Stop OpenNMS if it is running.");
            System.err.println("b) Enable storeByGroup.");
            System.err.println("c) Start OpenNMS and give it some time to be sure that all the multi-metric RRD/JRB files were created.");
            System.err.println("   Do not panic, the old data will be merged.");
            System.err.println("d) Use the rrd-converter tool, to merge the single-metric JRB/RRD into multi-metric JRB/RRD.");
            System.err.println("   This process could take a while. Wait until it is completely finished.");
            System.err.println("e) Use the rrd-converter tool, clean the RRD/JRB directory (i.e. remove the left-over/temporary files.");
            System.err.println("f) Execute the Newts converter again.");
            System.exit(1);
        }

        System.exit(0);
    }

    /**
     * Process resource.
     *
     * @param resourcePath the resource path
     */
    private void processResource(Path resourcePath) {
        processedResources++;
        String resourceId = getResourceId(resourcePath);
        System.out.println("\nProcessing resource: " + resourceId);
        Properties ds = new Properties();
        try {
            ds.load(new FileInputStream(new File(resourcePath.toFile(), "ds.properties")));
        } catch (Exception e) {
            System.err.printf("ERROR: can't parse ds.properties from %s\n", resourcePath);
            e.printStackTrace();
            return;
        }
        Set<String> rrds = new TreeSet<String>();
        ds.values().forEach(o -> rrds.add((String)o));
        rrds.forEach(p -> {
            processResource(resourcePath, resourceId, p);
        });
    }

    /**
     * Process metric.
     *
     * @param resourcePath the resource path
     * @param resourceId the resource id
     * @param multiDsFileName the multi-ds file name
     */
    private void processResource(Path resourcePath, String resourceId, String multiDsFileName) {
        System.out.printf("   Metric-group: %s\n", multiDsFileName);
        String metricId = resourceId + ':' + multiDsFileName;
        // TODO Map<String,String> properties = getStringProperties(resourcePath);
        try {
            File metricFile = new File(resourcePath.toFile(), multiDsFileName + ".rrd");
            if (metricFile.exists()) {
                parseRrd(metricId, metricFile);
                return;
            }
            metricFile = new File(resourcePath.toFile(), multiDsFileName + ".jrb");
            if (metricFile.exists()) {
                parseJrb(metricId, metricFile);
                return;
            }
        } catch (Exception e) {
            System.err.printf("ERROR: Can't parse JRB/RRD for %s at %s\n", multiDsFileName, resourcePath);        
            e.printStackTrace(); // TODO This is not elegant, but it helps.
        }
        System.err.printf("ERROR: There are no multi-ds JRB/RRD for %s at %s\n", multiDsFileName, resourcePath);        
    }

    /**
     * Gets the string properties.
     *
     * @param resourcePath the resource path
     * @return the string properties
     */
    private Map<String,String> getStringProperties(Path resourcePath) {
        Properties properties = new Properties();
        File file = new File(resourcePath.toFile(), "strings.properties");
        if (file.exists()) {
            try {
                properties.load(new FileInputStream(file));
            } catch (IOException e) {
                System.err.printf("ERROR: Can't parse strings.properties at %s\n", resourcePath);        
                e.printStackTrace(); // TODO This is not elegant, but it helps.
            }
        }
        Map<String,String> map = new TreeMap<String,String>();
        properties.forEach((k,v) -> map.put((String)k, (String)v));
        return map;
    }

    /**
     * Parses a JRobin file.
     *
     * @param metricId the metric id
     * @param metricFile the metric file
     * @throws Exception the exception
     */
    private void parseJrb(String metricId, File metricFile) throws Exception {
        System.out.printf("    Parsing JRobin file %s\n", metricFile);
        RRDv1 rrd = RrdConvertUtils.dumpJrb(metricFile);
        injectDataToNewts(metricId, rrd.generateSamples(), rrd.getDataSources());
    }

    /**
     * Parses a RRDtool file.
     *
     * @param metricId the metric id
     * @param metricFile the metric file
     * @throws Exception the exception
     */
    private void parseRrd(String metricId, File metricFile) throws Exception {
        System.out.printf("    Parsing RRDtool file %s\n", metricFile);
        RRDv3 rrd = RrdConvertUtils.dumpRrd(metricFile);
        injectDataToNewts(metricId, rrd.generateSamples(), rrd.getDataSources());
    }

    /**
     * Inject data to newts.
     *
     * @param metricId the metric id
     * @param samples the samples
     * @param dataSources the data sources
     */
    private void injectDataToNewts(String metricId, List<RrdSample> samples, List<? extends AbstractDS> dataSources) {
        List<Sample> newtsSamples = new ArrayList<Sample>();
        for (RrdSample s : samples) {
            for (int i=0; i<dataSources.size(); i++) {
                String ds = dataSources.get(i).getName();
                MetricType type = dataSources.get(i).isCounter() ? MetricType.COUNTER : MetricType.GAUGE;
                ValueType<?> valueType = ValueType.compose(s.getValue(i), type);
                Timestamp ts = Timestamp.fromEpochMillis(s.getTimestamp());
                Resource resource = new Resource(metricId);
                newtsSamples.add(new Sample(ts, resource, ds, type, valueType));
            }
        };
        Lists.partition(newtsSamples, batchSize - 1).forEach(l -> repository.insert(l));
    }

    /**
     * Gets the resource ID for Newts.
     *
     * @param resourcePath the resource path
     * @return the resource ID
     */
    private String getResourceId(Path resourcePath) {
        String resourceDir = resourcePath.toString().replace(snmpDir.toString() + '/', "");
        final String fileRegex = "[" + File.separator + "]";
        String idStr = resourceDir.split(fileRegex)[0];
        if (!"fs".equals(idStr)) {
            int nodeId = Integer.parseInt(idStr);
            String id = nodes.stream().filter(n -> n.getId() == nodeId).findFirst().get().getFSId();
            return "snmp:fs:" + resourceDir.replace(idStr, id).replaceAll(fileRegex, ":");
        }
        return "snmp:" + resourceDir.replaceAll(fileRegex, ":");
    }
}
