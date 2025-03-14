/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.newts.converter;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.UnsignedLong;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.jrobin.core.RrdException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.newts.support.NewtsUtils;
import org.opennms.netmgt.rrd.model.AbstractDS;
import org.opennms.netmgt.rrd.model.AbstractRRA;
import org.opennms.netmgt.rrd.model.AbstractRRD;
import org.opennms.netmgt.rrd.model.Row;
import org.opennms.netmgt.rrd.model.RrdConvertUtils;
import org.opennms.newts.api.Counter;
import org.opennms.newts.api.Gauge;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.ValueType;
import org.opennms.newts.api.search.Indexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Class NewtsConverter.
 *
 * The converter requires multi-ds metrics; otherwise it won't work.
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @author Dustin Frisch <dustin@opennms.org>
 */
public class NewtsConverter implements AutoCloseable {
    private final static Logger LOG = LoggerFactory.getLogger(NewtsConverter.class);

    /** The Constant CMD_SYNTAX. */
    private final static String CMD_SYNTAX = "newts-converter [options]";

    private final static Timestamp EPOCH = Timestamp.fromEpochMillis(0);
    private final static ValueType<?> ZERO = ValueType.compose(0, MetricType.GAUGE);

    private enum StorageStrategy {
        STORE_BY_METRIC,
        STORE_BY_GROUP,
    }

    private enum StorageTool {
        RRDTOOL,
        JROBIN,
    }

    private static class ForeignId {
        private final String foreignSource;
        private final String foreignId;

        public ForeignId(final String foreignSource,
                         final String foreignId) {
            this.foreignSource = foreignSource;
            this.foreignId = foreignId;
        }
    }

    private final ClassPathXmlApplicationContext context;

    private final Path onmsHome;
    private final Path rrdDir;
    private final Path rrdBinary;
    private final StorageStrategy storageStrategy;
    private final StorageTool storageTool;

    /**
     * Mapping form node ID to foreign ID.
     **/
    private final Map<Integer, ForeignId> foreignIds = Maps.newHashMap();

    /** The Cassandra/Newts Repository. */
    private final SampleRepository repository;

    /** The Cassandra/Newts Indexer. */
    private final Indexer indexer;

    /** The processed resources. */
    private static AtomicLong processedMetrics = new AtomicLong(0);
    private static AtomicLong processedSamples = new AtomicLong(0);

    /** The Newts inject batch size. */
    private int batchSize;

    private final ExecutorService executor;

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(final String... args) {
        final long start;
        try (final NewtsConverter converter = new NewtsConverter(args)) {
            start = System.currentTimeMillis();

            converter.execute();

        } catch (final NewtsConverterError e) {
            LOG.error(e.getMessage(), e);
            System.exit(1);
            throw null;
        }

        final Period period = new Interval(start, System.currentTimeMillis()).toPeriod();
        final PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays().appendSuffix(" days ")
                .appendHours().appendSuffix(" hours ")
                .appendMinutes().appendSuffix(" min ")
                .appendSeconds().appendSuffix(" sec ")
                .printZeroNever()
                .toFormatter();

        LOG.info("Conversion Finished: metrics: {}, samples: {}, time: {}", processedMetrics, processedSamples, formatter.print(period));

        System.exit(0);
    }

    private NewtsConverter(final String... args) {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Print this help");
        options.addOption(helpOption);

        final Option opennmsHomeOption = new Option("o", "onms-home", true, "OpenNMS Home Directory (defaults to /opt/opennms)");
        options.addOption(opennmsHomeOption);

        final Option rrdPathOption = new Option("r", "rrd-dir", true, "The path to the RRD data (defaults to ONMS-HOME/share/rrd)");
        options.addOption(rrdPathOption);

        final Option rrdToolOption = new Option("t", "rrd-tool", true, "Whether to use rrdtool or JRobin (defaults to use rrdtool)");
        options.addOption(rrdToolOption);

        final Option rrdBinaryOption = new Option("T", "rrd-binary", true, "The binary path to the rrdtool command (defaults to /usr/bin/rrdtool, only used if rrd-tool is set)");
        options.addOption(rrdBinaryOption);

        final Option storeByGroupOption = new Option("s", "storage-strategy", true, "Whether store by group was enabled or not");
        storeByGroupOption.setRequired(true);
        options.addOption(storeByGroupOption);

        final Option threadsOption = new Option("n", "threads", true, "Number of conversion threads (defaults to number of CPUs)");
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
                      : this.onmsHome.resolve("share").resolve("rrd");
        if (!Files.exists(this.rrdDir) || !Files.isDirectory(this.rrdDir)) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Directory %s doesn't exist%n", this.rrdDir.toAbsolutePath()), options, null);
            System.exit(1);
            throw null;
        }

        if (!cmd.hasOption('s')) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Option for storage-strategy must be spcified%n"), options, null);
            System.exit(1);
            throw null;
        }

        switch (cmd.getOptionValue('s').toLowerCase()) {
            case "storeByMetric":
            case "sbm":
            case "false":
                storageStrategy = StorageStrategy.STORE_BY_METRIC;
                break;

            case "storeByGroup":
            case "sbg":
            case "true":
                storageStrategy = StorageStrategy.STORE_BY_GROUP;
                break;

            default:
                new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Invalid value for storage-strategy%n"), options, null);
                System.exit(1);
                throw null;
        }

        if (!cmd.hasOption('t')) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Option rrd-tool must be specified%n"), options, null);
            System.exit(1);
            throw null;
        }

        switch (cmd.getOptionValue('t').toLowerCase()) {
            case "rrdtool":
            case "rrd":
            case "true":
                storageTool = StorageTool.RRDTOOL;
                break;

            case "jrobin":
            case "jrb":
            case "false":
                storageTool = StorageTool.JROBIN;
                break;

            default:
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
        System.setProperty("rrd.binary", this.rrdBinary.toString());

        final int threads;
        try {
            threads = cmd.hasOption('n')
                      ? Integer.parseInt(cmd.getOptionValue('n'))
                      : Runtime.getRuntime().availableProcessors();
            this.executor = new ForkJoinPool(threads,
                                             ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                                             null,
                                             true);

        } catch (Exception e) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: Invalid number of threads: %s%n", e.getMessage()), options, null);
            System.exit(1);
            throw null;
        }

        // Initialize OpenNMS
        OnmsProperties.initialize();

        final String host = System.getProperty("org.opennms.newts.config.hostname", "localhost");
        final String keyspace = System.getProperty("org.opennms.newts.config.keyspace", "newts");
        int ttl = Integer.parseInt(System.getProperty("org.opennms.newts.config.ttl", "31540000"));
        int port = Integer.parseInt(System.getProperty("org.opennms.newts.config.port", "9042"));

        batchSize = Integer.parseInt(System.getProperty("org.opennms.newts.config.max_batch_size", "16"));

        LOG.info("OpenNMS Home: {}", this.onmsHome);
        LOG.info("RRD Directory: {}", this.rrdDir);
        LOG.info("Use RRDtool Tool: {}", this.storageTool);
        LOG.info("RRDtool CLI: {}", this.rrdBinary);
        LOG.info("StoreByGroup: {}", this.storageStrategy);
        LOG.info("Conversion Threads: {}", threads);
        LOG.info("Cassandra Host: {}", host);
        LOG.info("Cassandra Port: {}", port);
        LOG.info("Cassandra Keyspace: {}", keyspace);
        LOG.info("Newts Max Batch Size: {}", this.batchSize);
        LOG.info("Newts TTL: {}", ttl);

        if (!"newts".equals(System.getProperty("org.opennms.timeseries.strategy", "rrd"))) {
            throw NewtsConverterError.create("The configured timeseries strategy must be 'newts' on opennms.properties (org.opennms.timeseries.strategy)");
        }

        if (!"true".equals(System.getProperty("org.opennms.rrd.storeByForeignSource", "false"))) {
            throw NewtsConverterError.create("The option storeByForeignSource must be enabled in opennms.properties (org.opennms.rrd.storeByForeignSource)");
        }

        try {
            this.context = new ClassPathXmlApplicationContext(new String[]{
                    "classpath:/META-INF/opennms/applicationContext-soa.xml",
                    "classpath:/META-INF/opennms/applicationContext-timeseries-newts.xml"
            });

            this.repository = context.getBean(SampleRepository.class);
            this.indexer = context.getBean(Indexer.class);

        } catch (final Exception e) {
            throw NewtsConverterError.create(e, "Cannot connect to the Cassandra/Newts backend: {}", e.getMessage());
        }

        // Initialize node ID to foreign ID mapping
        try (final Connection conn = DataSourceFactory.getInstance().getConnection();
             final Statement st = conn.createStatement();
             final ResultSet rs = st.executeQuery("SELECT nodeid, foreignsource, foreignid from node n")) {
            while (rs.next()) {
                foreignIds.put(rs.getInt("nodeid"),
                               new ForeignId(rs.getString("foreignsource"),
                                             rs.getString("foreignid")));
            }

        } catch (final Exception e) {
            throw NewtsConverterError.create(e, "Failed to connect to database: {}", e.getMessage());
        }

        LOG.trace("Found {} nodes on the database", foreignIds.size());
    }

    public void execute() {
        LOG.trace("Starting Conversion...");

        this.processStoreByGroupResources(this.rrdDir.resolve("response"));

        this.processStringsProperties(this.rrdDir.resolve("snmp"));

        switch (storageStrategy) {
            case STORE_BY_GROUP:
                this.processStoreByGroupResources(this.rrdDir.resolve("snmp"));
                break;

            case STORE_BY_METRIC:
                this.processStoreByMetricResources(this.rrdDir.resolve("snmp"));
                break;
        }
    }

    private void processStoreByGroupResources(final Path path) {
        try {
            // Find and process all resource folders containing a 'ds.properties' file
            Files.walk(path)
                 .filter(p -> p.endsWith("ds.properties"))
                 .forEach(p -> processStoreByGroupResource(p.getParent()));

        } catch (Exception e) {
            LOG.error("Error while reading RRD files", e);
            return;
        }
    }

    private void processStoreByGroupResource(final Path path) {
        // Load the 'ds.properties' for the current path
        final Properties ds = new Properties();
        try (final BufferedReader r = Files.newBufferedReader(path.resolve("ds.properties"))) {
            ds.load(r);

        } catch (final IOException e) {
            LOG.error("No group information found - please verify storageStrategy settings");
            return;
        }

        // Get all groups declared in the ds.properties and process the RRD files
        Sets.newHashSet(Iterables.transform(ds.values(), Object::toString))
            .forEach(group -> this.executor.execute(() -> processResource(path,
                                                                          group,
                                                                          group)));
    }

    private void processStoreByMetricResources(final Path path) {
        try {
            // Find an process all '.meta' files and the according RRD files
            Files.walk(path)
                 .filter(p -> p.getFileName().toString().endsWith(".meta"))
                 .forEach(this::processStoreByMetricResource);

        } catch (Exception e) {
            LOG.error("Error while reading RRD files", e);
            return;
        }
    }

    private void processStoreByMetricResource(final Path metaPath) {
        // Use the containing directory as resource path
        final Path path = metaPath.getParent();

        // Extract the metric name from the file name
        final String metric = FilenameUtils.removeExtension(metaPath.getFileName().toString());

        // Load the '.meta' file to get the group name
        final Properties meta = new Properties();
        try (final BufferedReader r = Files.newBufferedReader(metaPath)) {
            meta.load(r);

        } catch (final IOException e) {
            LOG.error("Failed to read .meta file: {}", metaPath, e);
            return;
        }

        final String group = meta.getProperty("GROUP");
        if (group == null) {
            LOG.warn("No group information found - please verify storageStrategy settings");
            return;
        }

        // Process the resource
        this.executor.execute(() -> this.processResource(path,
                                                         metric,
                                                         group));
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
        LOG.info("Processing resource: dir={}, file={}, group={}", resourceDir, fileName, group);

        final ResourcePath resourcePath = buildResourcePath(resourceDir);
        if (resourcePath == null) {
            return;
        }

        // Load the RRD file
        final Path file;
        switch (this.storageTool) {
            case RRDTOOL:
                file = resourceDir.resolve(fileName + ".rrd");
                break;

            case JROBIN:
                file = resourceDir.resolve(fileName + ".jrb");
                break;

            default:
                LOG.error("Unhandled storage type: {}", this.storageTool);
                return;
        }

        if (file == null || !Files.exists(file)) {
            LOG.error("File not found: {}", file);
            return;
        }

        final AbstractRRD rrd;
        try {
            switch (this.storageTool) {
                case RRDTOOL:
                    rrd = RrdConvertUtils.dumpRrd(file.toFile());
                    break;

                case JROBIN:
                    rrd = RrdConvertUtils.dumpJrb(file.toFile());
                    break;

                default:
                    rrd = null;
            }
        } catch (final IOException|RrdException e) {
            LOG.error("Can't parse JRB/RRD file: {}", file, e);
            return;
        }

        // Inject the samples from the RRD file to NewTS
        try {
            this.injectSamplesToNewts(resourcePath,
                                      group,
                                      rrd.getDataSources(),
                                      generateSamples(rrd));

        } catch (final Exception e) {
            LOG.error("Failed to convert file: {}", file, e);
            return;
        }
    }

    protected static Sample toSample(AbstractDS ds, Resource resource, Timestamp timestamp, double value) {
        final String metric = ds.getName();
        final MetricType type = ds.isCounter()
                                ? MetricType.COUNTER
                                : MetricType.GAUGE;
        final ValueType<?> valueType = ds.isCounter()
                                       ? new Counter(UnsignedLong.valueOf(BigDecimal.valueOf(value).toBigInteger()))
                                       : new Gauge(value);
        return new Sample(timestamp, resource, metric, type, valueType);
    }

    private void injectSamplesToNewts(final ResourcePath resourcePath,
                                      final String group,
                                      final List<? extends AbstractDS> dataSources,
                                      final SortedMap<Long, List<Double>> samples) {
        final ResourcePath groupPath = ResourcePath.get(resourcePath, group);

        // Create a resource ID from the resource path
        final String groupId = NewtsUtils.toResourceId(groupPath);

        // Build indexing attributes
        final Map<String, String> attributes = Maps.newHashMap();
        NewtsUtils.addIndicesToAttributes(groupPath, attributes);

        // Create the NewTS resource to insert
        final Resource resource = new Resource(groupId,
                                               Optional.of(attributes));

        // Transform the RRD samples into NewTS samples
        List<Sample> batch = new ArrayList<>(this.batchSize);
        for (final Map.Entry<Long, List<Double>> s : samples.entrySet()) {
            for (int i = 0; i < dataSources.size(); i++) {
                final double value = s.getValue().get(i);
                if (Double.isNaN(value)) {
                    continue;
                }
                final AbstractDS ds = dataSources.get(i);
                final Timestamp timestamp = Timestamp.fromEpochSeconds(s.getKey());

                try {
                    batch.add(toSample(ds, resource, timestamp, value));
                } catch (IllegalArgumentException e) {
                    // This can happen when the value is outside of the range for the expected
                    // type i.e. negative for a counter, so we silently skip these
                    continue;
                }

                if (batch.size() >= this.batchSize) {
                    this.repository.insert(batch, true);
                    this.processedSamples.getAndAdd(batch.size());

                    batch = new ArrayList<>(this.batchSize);
                }
            }
        }

        if (!batch.isEmpty()) {
            this.repository.insert(batch, true);
            this.processedSamples.getAndAdd(batch.size());
        }

        this.processedMetrics.getAndAdd(dataSources.size());

        LOG.trace("Stats: {} / {}", this.processedMetrics, this.processedSamples);
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

                     final ResourcePath resourcePath = buildResourcePath(p.getParent());
                     if (resourcePath == null) {
                         return;
                     }

                     this.injectStringPropertiesToNewts(resourcePath,
                                                        Maps.fromProperties(properties));
                 });

        } catch (Exception e) {
            LOG.error("Error while reading string.properties", e);
            return;
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
            final int nodeId = Integer.parseInt(relativeResourceDir.getName(1).toString());

            // Get the foreign source for the node
            final ForeignId foreignId = foreignIds.get(nodeId);
            if (foreignId == null) {
                return null;
            }

            // Make a store-by-foreign-source compatible path by using the found foreign ID and append the remaining path as-is
            resourcePath = ResourcePath.get(ResourcePath.get(ResourcePath.get("snmp", "fs"),
                                                             foreignId.foreignSource,
                                                             foreignId.foreignId),
                                            Iterables.transform(Iterables.skip(relativeResourceDir, 2),
                                                                Path::toString));

        } else {
            resourcePath = ResourcePath.get(Iterables.transform(relativeResourceDir,
                                                                Path::toString));
        }
        return resourcePath;
    }

    @Override
    public void close() {
        this.executor.shutdown();
        while (!this.executor.isTerminated()) {
            try {
                this.executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
            }
        }

        this.context.close();
    }

    public static SortedMap<Long, List<Double>> generateSamples(final AbstractRRD rrd) {
        // When having pdpPerRow = 1, the correlation function does not change the generated samples as the correlation
        // functions are reflexive wen applied to a single sample - `sample == cf([sample])` holds for all possible cf.
        // Therefore we use RRA with pdpPerRow = 1 which covers the longest time range regardless of its correlation
        // function. To fill up remaining samples, we only use RRA with AVERAGE correlation function.
        final NavigableSet<AbstractRRA> rras = Stream.concat(rrd.getRras()
                                                                .stream()
                                                                .filter(rra -> rra.getPdpPerRow() == 1L)
                                                                .max(Comparator.comparingInt(rra -> rra.getRows().size()))
                                                                .map(Stream::of)
                                                                .orElseGet(Stream::empty),
                                                             rrd.getRras()
                                                                .stream()
                                                                .filter(AbstractRRA::hasAverageAsCF))
                                                     .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(AbstractRRA::getPdpPerRow))));

        final SortedMap<Long, List<Double>> collected = new TreeMap<>();

        for (final AbstractRRA rra : rras) {
            NavigableMap<Long, List<Double>> samples = generateSamples(rrd, rra);

            final AbstractRRA lowerRra = rras.lower(rra);
            if (lowerRra != null) {
                final long lowerRraStart = rrd.getLastUpdate() - lowerRra.getPdpPerRow() * rrd.getStep() * lowerRra.getRows().size();
                final long rraStep = rra.getPdpPerRow() * rrd.getStep();
                samples = samples.headMap((int) Math.ceil(((double) lowerRraStart) / ((double) rraStep)) * rraStep, false);
            }

            if (samples.isEmpty()) {
                // The whole timespan of this RRA is covered by an RRA with an higher resolution - so there is nothing
                // to add from this RRA
                continue;
            }

            final AbstractRRA higherRra = rras.higher(rra);
            if (higherRra != null) {
                final long higherRraStep = higherRra.getPdpPerRow() * rrd.getStep();
                samples = samples.tailMap((int) Math.ceil(((double) samples.firstKey()) / ((double) higherRraStep)) * higherRraStep, true);
            }

            collected.putAll(samples);
        }

        return collected;
    }

    public static NavigableMap<Long, List<Double>> generateSamples(final AbstractRRD rrd, final AbstractRRA rra) {
        final long step = rra.getPdpPerRow() * rrd.getStep();
        final long start = rrd.getStartTimestamp(rra);
        final long end = rrd.getEndTimestamp(rra);

        // Initialize Values Map
        final NavigableMap<Long, List<Double>> valuesMap = new TreeMap<>();
        for (long ts = start; ts <= end; ts += step) {
            final List<Double> values = new ArrayList<>();
            for (int i = 0; i < rrd.getDataSources().size(); i++) {
                values.add(Double.NaN);
            }
            valuesMap.put(ts, values);
        }

        // Initialize Last Values
        final List<Double> lastValues = new ArrayList<>();
        for (final AbstractDS ds : rrd.getDataSources()) {
            final double v = ds.getLastDs() == null ? 0.0 : ds.getLastDs();
            lastValues.add(v - v % step);
        }

        // Set Last-Value for Counters
        for (int i = 0; i < rrd.getDataSources().size(); i++) {
            if (rrd.getDataSource(i).isCounter()) {
                valuesMap.get(end).set(i, lastValues.get(i));
            }
        }

        // Process
        // Counters must be processed in reverse order (from latest to oldest) in order to recreate the counter raw values
        // The first sample is processed separated because the lastValues must be updated after adding each sample.
        long ts = end - step;
        for (int j = rra.getRows().size() - 1; j >= 0; j--) {
            final Row row = rra.getRows().get(j);

            for (int i = 0; i < rrd.getDataSources().size(); i++) {
                if (rrd.getDataSource(i).isCounter()) {
                    if (j > 0) {
                        final Double last = lastValues.get(i);
                        final Double current = row.getValue(i).isNaN() ? 0 : row.getValue(i);

                        Double value = last - (current * step);
                        if (value < 0) { // Counter-Wrap emulation
                            value += Math.pow(2, 64);
                        }
                        lastValues.set(i, value);
                        if (!row.getValue(i).isNaN()) {
                            valuesMap.get(ts).set(i, value);
                        }
                    }
                } else {
                    if (!row.getValue(i).isNaN()) {
                        valuesMap.get(ts + step).set(i, row.getValue(i));
                    }
                }
            }

            ts -= step;
        }

        return valuesMap;
    }
}
