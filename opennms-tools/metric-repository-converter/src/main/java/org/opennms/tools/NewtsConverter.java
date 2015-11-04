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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.BooleanUtils;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.newts.support.NewtsUtils;
import org.opennms.netmgt.rrd.model.AbstractDS;
import org.opennms.netmgt.rrd.model.AbstractRRD;
import org.opennms.netmgt.rrd.model.RrdConvertUtils;
import org.opennms.netmgt.rrd.model.RrdSample;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.ValueType;
import org.opennms.newts.api.search.Indexer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Lists;

/**
 * The Class NewtsConverter.
 *
 * The converter requires multi-ds metrics; otherwise it won't work.
 *
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class NewtsConverter {

    /** The Constant CMD_SYNTAX. */
    private static final String CMD_SYNTAX = "newts-converter [options]";

    private static final Timestamp EPOCH = Timestamp.fromEpochMillis(0);
    private static final ValueType<?> ZERO = ValueType.compose(0, MetricType.GAUGE);

    private final Path onmsHome;
    private final Path rrdDir;
    private final Path rrdBinary;
    private final int threads;
    private final boolean storeByGroup;
    private final boolean rrdTool;

    /**
     * Mapping form node ID to foreign ID.
     **/
    private final Map<Integer, String> foreignIds = Maps.newHashMap();

    /** The Cassandra/Newts Repository. */
    private final SampleRepository repository;

    /** The Cassandra/Newts Indexer. */
    private final Indexer indexer;

    /** The processed resources. */
    private int processedMetrics;
    private int processedSamples;

    /** The Newts inject batch size. */
    private int batchSize;

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(final String... args) {
        final NewtsConverter converter = new NewtsConverter(args);
        converter.execute();
    }

    private NewtsConverter(final String... args) {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Print this help");
        options.addOption(helpOption);

        final Option opennmsHomeOption = new Option("o", "onms-home", true, "OpenNMS Home Directory (defaults to /opt/opennms)");
        options.addOption(opennmsHomeOption);

        final Option rrdPathOption = new Option("r", "rrd-dir", true, "The path to the RRD data (defaults to /var/opennms/rrd)");
        options.addOption(rrdPathOption);

        final Option rrdToolOption = new Option("t", "rrd-tool", true, "Whether to use rrdtool or JRobin (defaults to use rrdtool)");
        options.addOption(rrdToolOption);

        final Option rrdBinaryOption = new Option("T",
                                                  "rrd-binary",
                                                  true,
                                                  "The binary path to the rrdtool command (defaults to /usr/bin/rrdtool, only used if rrd-tool is set)");
        options.addOption(rrdBinaryOption);

        final Option storeByGroupOption = new Option("s", "store-by-group", true, "Whether store by group was enabled or not");
        storeByGroupOption.setRequired(true);
        options.addOption(storeByGroupOption);

        final Option threadsOption = new Option("n", "threads", true, "Number of conversion threads (defaults to 5)");
        options.addOption(threadsOption);

        final CommandLineParser parser = new PosixParser();

        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args);

        } catch (ParseException e) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: %s%n", e.getMessage()), options, null);
            System.exit(1);
            throw null;
        }

        // Processing Options
        if (cmd.hasOption('h')) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, null, options, null);
            System.exit(0);
        }

        this.onmsHome = cmd.hasOption('o')
                        ? Paths.get(cmd.getOptionValue('o'))
                        : Paths.get("/opt/opennms");
        if (!Files.exists(this.onmsHome) || !Files.isDirectory(this.onmsHome)) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Directory %s doesn't exist%n", this.onmsHome.toAbsolutePath()), options, null);
            System.exit(1);
            throw null;
        }
        System.setProperty("opennms.home", onmsHome.toAbsolutePath().toString());

        this.rrdDir = cmd.hasOption('r')
                      ? Paths.get(cmd.getOptionValue('r'))
                      : Paths.get("/var/opennms/rrd");
        if (!Files.exists(this.rrdDir) || !Files.isDirectory(this.rrdDir)) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Directory %s doesn't exist%n", this.rrdDir.toAbsolutePath()), options, null);
            System.exit(1);
            throw null;
        }

        try {
            storeByGroup = BooleanUtils.toBooleanObject(cmd.getOptionValue('s'));

        } catch (NullPointerException e) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Invalid value for storeByGroup%n"), options, null);
            System.exit(1);
            throw null;
        }

        try {
            rrdTool = cmd.hasOption('t')
                      ? BooleanUtils.toBooleanObject(cmd.getOptionValue('t'))
                      : true;

        } catch (NullPointerException e) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Invalid value for rrd-tool%n"), options, null);
            System.exit(1);
            throw null;
        }

        this.rrdBinary = cmd.hasOption('T')
                         ? Paths.get(cmd.getOptionValue('T'))
                         : Paths.get("/usr/bin/rrdtool");
        if (!Files.exists(this.rrdBinary) || !Files.isExecutable(this.rrdBinary)) {
            new HelpFormatter().printHelp(80,
                                          CMD_SYNTAX,
                                          String.format("ERROR: RRDtool command %s doesn't exist%n", this.rrdBinary.toAbsolutePath()),
                                          options,
                                          null);
            System.exit(1);
            throw null;
        }


        try {
            this.threads = cmd.hasOption('n')
                           ? Integer.parseInt(cmd.getOptionValue('n'))
                           : 5;
        } catch (Exception e) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Invalid number of threads: %s%n", e.getMessage()), options, null);
            System.exit(1);
            throw null;
        }

        // Initialize OpenNMS
        OnmsProperties.initialize();

        final String strategy = System.getProperty("org.opennms.timeseries.strategy", "rrd");
        final String host = System.getProperty("org.opennms.newts.config.hostname", "localhost");
        final String keyspace = System.getProperty("org.opennms.newts.config.keyspace", "newts");
        int ttl = Integer.parseInt(System.getProperty("org.opennms.newts.config.ttl", "31540000"));
        int port = Integer.parseInt(System.getProperty("org.opennms.newts.config.port", "9042"));

        batchSize = Integer.parseInt(System.getProperty("org.opennms.newts.config.max_batch_size", "16"));

        System.out.printf("OpenNMS Home: %s\n", onmsHome);
        System.out.printf("RRD Directory: %s\n", rrdDir);
        System.out.printf("Use RRDtool Tool: %s\n", rrdTool);
        System.out.printf("RRDtool CLI: %s\n", rrdBinary);
        System.out.printf("StoreByGroup: %s\n", storeByGroup);
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
            throw null;
        }

        try {
            final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                    "classpath:/META-INF/opennms/applicationContext-soa.xml",
                    "classpath:/META-INF/opennms/applicationContext-newts.xml"
            });

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    context.close();
                }
            });

            this.repository = context.getBean(SampleRepository.class);
            this.indexer = context.getBean(Indexer.class);

        } catch (final Exception e) {
            System.err.printf("ERROR: Cannot connect to the Cassandra/Newts backend: %s%n", e.getMessage());
            System.err.println("       Make sure Newts is properly configured in opennms.properties.");
            System.err.println("       There is no need to stop OpenNMS to execute this tool.");
            System.exit(1);
            throw null;
        }
    }

    public void execute() {
        // Initialize node ID to foreign ID mapping
        try (final Connection conn = DataSourceFactory.getInstance().getConnection();
             final Statement st = conn.createStatement();
             final ResultSet rs = st.executeQuery("SELECT nodeid, foreignsource, foreignid from node n")) {
            while (rs.next()) {
                foreignIds.put(rs.getInt("nodeid"),
                               String.format("%s:%s",
                                             rs.getString("foreignsource"),
                                             rs.getString("foreignid")));
            }

        } catch (final Exception e) {
            System.err.println("ERROR: Failed to connect to database: " + e.getMessage());
            System.exit(1);
            throw null;
        }

        System.out.printf("Found %d nodes on the database\n", foreignIds.size());

        // Process JRB/RRD files
        final long start = System.currentTimeMillis();
        System.out.println("Starting Conversion...");
        System.out.println("Processing " + rrdDir);

        processedMetrics = 0;
        processedSamples = 0;

        this.processStoreByGroupResources(this.rrdDir.resolve("response"));

        this.processStringsProperties(this.rrdDir.resolve("snmp"));
        if (storeByGroup) {
            this.processStoreByGroupResources(this.rrdDir.resolve("snmp"));
        } else {
            this.processStoreByMetricResources(this.rrdDir.resolve("snmp"));
        }

        final Period period = new Interval(start, System.currentTimeMillis()).toPeriod();
        final PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendSeconds().appendSuffix(" sec")
                .appendMinutes().appendSuffix(" min")
                .appendHours().appendSuffix(" hours")
                .appendDays().appendSuffix(" days")
                .printZeroNever()
                .toFormatter();
        System.out.printf("\nConversion Finished.\nMetrics updated: %d\nEnlapsed time %s\n", processedMetrics, formatter.print(period));

        System.exit(0);
    }

    private void processStoreByGroupResources(final Path path) {
        try {
            // Find and process all resource folders containing a 'ds.properties' file
            Files.walk(path)
                 .filter(p -> p.endsWith("ds.properties"))
                 .forEach(p -> processStoreByGroupResource(p.getParent()));

        } catch (Exception e) {
            System.err.println("ERROR: Error while reading RRD files: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
            throw null;
        }
    }

    private void processStoreByGroupResource(final Path path) {
        // Load the 'ds.properties' for the current path
        final Properties ds = new Properties();
        try (final BufferedReader r = Files.newBufferedReader(path.resolve("ds.properties"))) {
            ds.load(r);

        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }

        // Get all groups declared in the ds.properties and process the RRD files
        Sets.newHashSet(Iterables.transform(ds.values(), Object::toString))
            .forEach(group -> {
                processResource(path,
                                group,
                                group);
            });
    }

    private void processStoreByMetricResources(final Path path) {
        try {
            // Find an process all '.meta' files and the according RRD files
            Files.walk(path)
                 .filter(p -> p.getFileName().toString().endsWith(".meta"))
                 .forEach(p -> this.processStoreByMetricResource(p));

        } catch (Exception e) {
            System.err.println("ERROR: Error while reading RRD files: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
            throw null;
        }
    }

    private void processStoreByMetricResource(final Path metaPath) {
        // Use the containing directory as resource path
        final Path path = metaPath.getParent();

        // Extract the metric name from the file name
        final String metric = FilenameUtils.removeExtension(metaPath.getFileName().toString());

        // Load the '.meta' file to get the group name
        final Properties meta = new Properties();
        try (final BufferedReader r = Files.newBufferedReader(path.resolve(String.format("%s.meta", metric)))) {
            meta.load(r);

        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }

        final String group = meta.getProperty("GROUP");

        // Process the resource
        this.processResource(path,
                             metric,
                             group);
    }

    /**
     * Process metric.
     *
     * @param resourceDir the path where the resource file lives in
     * @param fileName the RRD file name without extension
     * @param group the group name
     */
    private void processResource(final Path resourceDir,
                                 final String fileName,
                                 final String group) {
        final ResourcePath resourcePath = buildResourcePath(resourceDir);

        // Load and interpolate the RRD file
        final AbstractRRD rrd;
        try {
            if (this.rrdTool) {
                rrd = RrdConvertUtils.dumpRrd(resourceDir.resolve(fileName + ".rrd").toFile());

            } else {
                rrd = RrdConvertUtils.dumpJrb(resourceDir.resolve(fileName + ".jrb").toFile());
            }

        } catch (final Exception e) {
            System.err.printf("ERROR: Can't parse JRB/RRD %s.(rrd/jrb): %s\n", resourceDir.resolve(fileName), e.getMessage());
            e.printStackTrace();
            System.exit(1);
            throw null;
        }

        // Inject the samples from the RRD file to NewTS
        this.injectSamplesToNewts(resourcePath,
                                  group,
                                  rrd.getDataSources(),
                                  rrd.generateSamples());

    }

    private void injectSamplesToNewts(final ResourcePath resourcePath,
                                      final String group,
                                      final List<? extends AbstractDS> dataSources,
                                      final List<RrdSample> samples) {
        // Create a resource ID from the resource path
        final String groupId = NewtsUtils.toResourceId(ResourcePath.get(resourcePath, group));

        // Insert the samples in batches
        List<Sample> batch = Lists.newArrayListWithCapacity(this.batchSize);
        for (RrdSample s : samples) {
            for (int i = 0; i < dataSources.size(); i++) {
                final Timestamp timestamp = Timestamp.fromEpochMillis(s.getTimestamp());

                final Map<String, String> attributes = Maps.newHashMap();
                NewtsUtils.addIndicesToAttributes(resourcePath, attributes);
                final Resource resource = new Resource(groupId,
                                                       Optional.of(attributes));

                final String metric = dataSources.get(i).getName();

                final MetricType type = dataSources.get(i).isCounter()
                                        ? MetricType.COUNTER
                                        : MetricType.GAUGE;
                final ValueType<?> valueType = ValueType.compose(s.getValue(i), type);

                final Sample sample = new Sample(timestamp, resource, metric, type, valueType);

                batch.add(sample);

                if (batch.size() >= this.batchSize) {
                    repository.insert(batch, true);
                    batch = Lists.newArrayListWithCapacity(this.batchSize);
                }
            }
        }

        repository.insert(batch, true);

        this.processedMetrics += dataSources.size();
        this.processedSamples += dataSources.size() * samples.size();

        System.out.println("Stats: " + this.processedMetrics + " / " + this.processedSamples);
    }

    private void processStringsProperties(final Path path) {
        try {
            // Find an process all 'strings.properties' files
            Files.walk(path)
                 .filter(p -> p.endsWith("strings.properties"))
                 .forEach(p -> {
                     final Properties properties = new Properties();
                     try (final BufferedReader r = Files.newBufferedReader(p)) {
                         properties.load(r);

                     } catch (final IOException e) {
                         throw Throwables.propagate(e);
                     }

                     this.injectStringPropertiesToNewts(buildResourcePath(p.getParent()),
                                                        Maps.fromProperties(properties));
                 });

        } catch (Exception e) {
            System.err.println("ERROR: Error while reading RRD files: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
            throw null;
        }
    }

    private void injectStringPropertiesToNewts(final ResourcePath resourcePath,
                                               final Map<String, String> stringProperties) {
        final Resource resource = new Resource(NewtsUtils.toResourceId(resourcePath),
                                               Optional.of(stringProperties));

        final Sample sample = new Sample(EPOCH,
                                         resource,
                                         "strings",
                                         MetricType.GAUGE,
                                         ZERO);

        indexer.update(Lists.newArrayList(sample));
    }

    private ResourcePath buildResourcePath(final Path resourceDir) {
        final ResourcePath resourcePath;
        final Path relativeResourceDir = this.rrdDir.relativize(resourceDir);

        // Transform store-by-id path into store-by-foreign-source path
        if (relativeResourceDir.startsWith(Paths.get("snmp")) &&
            !relativeResourceDir.startsWith(Paths.get("snmp", "fs"))) {

            // The part after snmp/ is considered the node ID
            final int nodeId = Integer.valueOf(relativeResourceDir.getName(1).toString());

            resourcePath = ResourcePath.get("snmp", "fs", foreignIds.get(nodeId));

        } else {
            resourcePath = ResourcePath.get(relativeResourceDir);
        }
        return resourcePath;
    }
}
