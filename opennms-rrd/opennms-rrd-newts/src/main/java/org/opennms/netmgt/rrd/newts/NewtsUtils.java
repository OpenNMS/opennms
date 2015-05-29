package org.opennms.netmgt.rrd.newts;

import java.io.File;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.ValueType;
import org.opennms.newts.cassandra.CassandraSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Utilities for converting from the world of RRD to Newts.
 *
 * @author jwhite
 */
public class NewtsUtils {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsUtils.class);

    private static final Pattern UPDATE_STRING_PATTERN = Pattern.compile("^(\\d+):(.*)");

    public static final String HOSTNAME_PROPERTY = "org.opennms.newts.config.hostname";

    public static final String KEYSPACE_PROPERTY = "org.opennms.newts.config.keyspace";

    public static final String PORT_PROPERTY = "org.opennms.newts.config.port";

    public static final String TTL_PROPERTY = "org.opennms.newts.config.ttl";
    
    public static final String DEFAULT_HOSTNAME = "localhost";

    public static final String DEFAULT_KEYSPACE = "newts";

    public static final String DEFAULT_PORT = "9043";

    public static final String DEFAULT_TTL = "" + 86400 * 365;

    private static final String CASSANDRA_COMPRESSION = "NONE";

    public static CassandraSession getCassrandraSession() {
        String hostname = System.getProperty(HOSTNAME_PROPERTY, DEFAULT_HOSTNAME);
        int port = Integer.valueOf(System.getProperty(PORT_PROPERTY, DEFAULT_PORT));
        String keyspace = System.getProperty(KEYSPACE_PROPERTY, DEFAULT_KEYSPACE);

        LOG.info("Using hostname {}, port {} and keyspace {}.",
                hostname, port, keyspace);

        return new CassandraSession(keyspace, hostname, port, CASSANDRA_COMPRESSION);
    }

    /**
     * Converts a 'rrd update' command string to a list of samples.
     *
     * This function requires the RRD database definition in order to determine
     * the associated resource id, metric names and metric types.
     *
     */
    protected static List<Sample> getSamplesFromRrdUpdateString(RrdDef def, String data, Map<String, String> attributes) {
        Preconditions.checkNotNull(def, "database definition");
        final List<Sample> samples = Lists.newLinkedList();

        // Initial sanitation
        if(Strings.isNullOrEmpty(data)) {
            return samples;
        }

        final Matcher m = UPDATE_STRING_PATTERN.matcher(data);
        if (!m.matches()) {
            LOG.error("Invalid update string '{}'. Returning empty sample set.", data);
            return samples;
        }

        // Parse the timestamp
        Timestamp timestamp;
        try {
            timestamp = Timestamp.fromEpochSeconds(Long.valueOf(m.group(1)));
        } catch (NumberFormatException e) {
            LOG.error("Invalid timestamp in update string '{}'. Returning empty sample set.", data, e);
            return samples;
        }

        // Split up the values
        final String[] values = m.group(2).split(":");

        // We should have as many values as we have data sources
        final List<RrdDs> rrdDataSources = def.getDataSources();
        if (values.length != rrdDataSources.size()) {
            LOG.error("The number of values {} does not match the number of datasources {}. Returning empty sample set for data: {}",
                    values.length, rrdDataSources.size(), data);
            return samples;
        }

        // Convert the values to samples
        final Iterator<RrdDs> dsIterator = rrdDataSources.iterator();
        for (final String value : values) {
            final RrdDs ds = dsIterator.next();

            Double val;
            if ("U".equalsIgnoreCase(value)) {
                val = Double.NaN;
            } else {
                try {
                    val = Double.valueOf(value);
                } catch (NumberFormatException e) {
                    LOG.error("Invalid value '{}' in update string. Skipping samples for {}.", value, ds.getName(), e);
                    continue;
                }
            }

            samples.add(
                new Sample(
                    timestamp,
                    def.getResource(),
                    ds.getName(),
                    ds.getMetricType(),
                    ValueType.compose(val, ds.getMetricType()),
                    attributes
                )
            );
        }

        return samples;
    }

    /**
     * Converts a path to a file on disk into a resource id.
     *
     * i.e. /opt/opennms/share/rrd/snmp/1/loadavg1.newts would map to snmp:1:loadavg1 
     *
     */
    protected static Resource getResourceFromPath(String path) {
        final String prefix = Paths.get(System.getProperty("opennms.home"), "share", "rrd").toString();

        // Initial sanitation
        if(Strings.isNullOrEmpty(path)
                || !path.startsWith(prefix)
                || !path.endsWith(NewtsRrdStrategy.FILE_EXTENSION)) {
            throw new IllegalArgumentException("Invalid path: '" + path + "'");
        }

        // Remove the prefix from the path
        path = path.substring(path.indexOf(prefix) + prefix.length(), path.length());

        // Split the remaining path into components
        File f = new File(path);
        List<String> pathElements = Lists.newLinkedList();
        do {
            if (pathElements.size() == 0) {
                // Remove the extension from the first element
                pathElements.add(FilenameUtils.removeExtension(f.getName()));
            } else {
                // Add all others as-is
                pathElements.add(f.getName());
            }
            f = f.getParentFile();
        } while (f.getParentFile() != null);

        // Build the resource id from the path components
        StringBuffer sb = new StringBuffer();
        for (String el : Lists.reverse(pathElements)) {
            if (sb.length() != 0) {
                sb.append(":");
            }
            // Remove any existing separators from the components
            sb.append(el.replace(":", ""));
        }

        return new Resource(sb.toString());
    }

    /**
     * Maps the RRD data-source type to one of the Newts metric types
     */
    protected static MetricType getMetricTypeFromRrdDataSource(RrdDataSource ds) {
        if ("COUNTER".equalsIgnoreCase(ds.getType())) {
            return MetricType.COUNTER;
        } else if ("GAUGE".equalsIgnoreCase(ds.getType())) {
            return MetricType.GAUGE;
        } else if ("COUNTER".equalsIgnoreCase(ds.getType())) {
            return MetricType.COUNTER;
        } else {
            return MetricType.ABSOLUTE;
        }
    }
}
