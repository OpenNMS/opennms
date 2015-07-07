package org.opennms.netmgt.rrd.newts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Used to store metrics in Newts.
 *
 * Updates made via the updateFile() are converted to Newts samples
 * and pushed to the sample repository in batches.
 *
 * Resources IDs have a one-to-one correspondence with the
 * file system paths used by corresponding .rrd or .jrb archives.
 * See {@link org.opennms.netmgt.rrd.newts.NewtsUtils#getResourceIdFromPath()} for details.
 *
 * @author jwhite
 */
public class NewtsRrdStrategy implements RrdStrategy<RrdDef, RrdDb> {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsRrdStrategy.class);

    public static final int TTL = Integer.getInteger("org.opennms.newts.config.ttl", 31536000);

    public static final boolean ENABLE_HIERARCHICAL_INDEXING = false;

    protected static final String FILE_EXTENSION = ".newts";

    private final int m_maxBatchSize;

    private final int m_maxBatchDelayInMs;

    /////////
    // Newts
    /////////

    @Autowired
    private Context m_context;

    @Autowired
    private SampleRepository m_sampleRepository;

    // Keep track of the definitions used by file system path
    private final Map<String, RrdDef> m_defByPath = Maps.newConcurrentMap();

    // Keep track of the attributes set by file system path
    private final Map<String, Map<String, String>> m_attrsByPath = Maps.newConcurrentMap();

    // Batch the samples up for insertion
    private List<Sample> sampleBatch = Lists.newLinkedList();

    // Limit the amount of the time the samples spend in the batch
    private long insertBatchAfterTimeMillis = 0;

    // Used to trigger a batch insert if the samples have been sitting in the
    // batch for >= max_batch_delay
    private Timer m_batchTimer = null;

    @Inject
    public NewtsRrdStrategy(@Named("newts.max_batch_size") Integer maxBatchSize, @Named("newts.max_batch_delay") Integer maxBatchDelayInMs)  {
        Preconditions.checkArgument(maxBatchSize > 0, "max_batch_size must be strictly positive");
        Preconditions.checkArgument(maxBatchDelayInMs >= 0, "max_batch_delay must be positive");

        m_maxBatchSize = maxBatchSize;
        m_maxBatchDelayInMs = maxBatchDelayInMs;

        LOG.debug("Using max_batch_size: {} and max_batch_delay: {}", maxBatchSize, m_maxBatchDelayInMs);
    }

    /////////
    // Sample persistence
    /////////

    private synchronized void batch(Collection<Sample> samples) {
        int remainingCapacity = m_maxBatchSize - sampleBatch.size();

        if (samples.size() <= remainingCapacity) {
            // Grab all of the samples
            sampleBatch.addAll(samples);
        } else {
            // Grab as many samples as we can
            int k = 0;
            Iterator<Sample> it = samples.iterator();
            for (; k < remainingCapacity && it.hasNext(); k++) {
                sampleBatch.add(it.next());
            }

            // Insert the current batch
            insertBatch();

            // Process the remaining samples
            batch(ImmutableList.copyOf(it));
        }

        // Don't process the batch if it's empty
        if (sampleBatch.size() < 1) {
            return;
        }

        // Start the timer if needed
        maybeStartTimer();

        // Push the batch if we're ready
        if (samples.size() >= m_maxBatchSize
                || m_maxBatchDelayInMs < 1
                || System.currentTimeMillis() >= insertBatchAfterTimeMillis) {
            insertBatch();
        }
    }

    private void insertBatch() {
        // We'd expect the logs from this thread to be in collectd.log
        try {
            Logging.withPrefix("collectd", new Callable<Void>(){
                @Override
                public Void call() throws Exception {
                    try {
                        LOG.debug("Inserting {} samples", sampleBatch.size());
                        m_sampleRepository.insert(sampleBatch);
                    } catch (Throwable t) {
                        LOG.error("An error occurred while inserting the samples. They will be lost.", t);
                    }

                    if (LOG.isDebugEnabled()) {
                        String uniqueResourceIds = sampleBatch.stream()
                            .map(s -> s.getResource().getId())
                            .distinct()
                            .collect(Collectors.joining(", "));
                        LOG.debug("Successfully inserted samples for resources with ids {}", uniqueResourceIds);
                    }

                    sampleBatch = Lists.newLinkedList();
                    insertBatchAfterTimeMillis = System.currentTimeMillis() + m_maxBatchDelayInMs;
                    return null;
                }
            });
        } catch (Exception e) {
            LOG.error("Failed to insert one or more samples.", e);
        }
    }

    private void maybeStartTimer() {
        if (m_maxBatchDelayInMs < 1 || m_batchTimer != null) {
            return;
        }

        m_batchTimer = new Timer("NewtsRrdStrategy-BatchTimer");
        m_batchTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                batch(Collections.emptyList());
            }
        }, 0, m_maxBatchDelayInMs);
    }

    @Override
    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        // Push the current batch
        insertBatchAfterTimeMillis = 0;
        batch(Collections.emptyList());
    }

    /////////
    // RRD -> Newts
    /////////

    @Override
    public RrdDef createDefinition(String creator, String directory,
            String rrdName, int step, List<RrdDataSource> dataSources,
            List<String> rraList) {
        final RrdDef def = new RrdDef(directory, rrdName, dataSources);
        m_defByPath.put(def.getPath(), def);
        return def;
    }

    @Override
    public void createFile(RrdDef def, Map<String, String> metaDataAttributes) {
        // Store the attributes
        Map<String, String> attributes = metaDataAttributes;
        if (attributes == null) {
            // The map may continue to be referenced by the indexer
            // after an insert()
            attributes = Maps.newConcurrentMap();
        }

        NewtsUtils.addParentPathAttributes(def.getPath(), attributes);

        LOG.debug("Creating resource at path {} with attributes: {}", def.getPath(), attributes);
        m_attrsByPath.put(def.getPath(), attributes);
    }

    @Override
    public RrdDb openFile(String fileName) {
        return new RrdDb(fileName);
    }

    @Override
    public void closeFile(RrdDb db) {
        // pass
    }

    @Override
    public void updateFile(RrdDb db, String owner, String data) throws RrdException {
        final RrdDef def = m_defByPath.get(db.getPath());
        if (def == null) {
            LOG.error("No known definition for {}. Ignoring update: {}",
                    db.getPath(), data);
            return;
        }
        LOG.debug("Persisting data at path {} using definition: {}", db.getPath(), def);

        final Map<String, String> attributes = m_attrsByPath.get(db.getPath());
        batch(NewtsUtils.getSamplesFromRrdUpdateString(def, data, attributes));
    }

    /////////
    // Retrieval
    /////////

    @Override
    public Double fetchLastValue(String rrdFile, String ds, int interval) {
        // We need some range to fetch a value. Sensible default?
        return Double.NaN;
    }

    @Override
    public Double fetchLastValue(String rrdFile, String ds,
            String consolidationFunction, int interval) {
        // We need some range to fetch a value. Sensible default?
        return Double.NaN;
    }

    @Override
    public Double fetchLastValueInRange(String rrdFile, String ds,
            int interval, int range) throws NumberFormatException, RrdException {
        final Resource resource = new Resource(NewtsUtils.getResourceIdFromPath(rrdFile));
        final Timestamp end = Timestamp.now();
        final Timestamp start = end.minus(range, TimeUnit.SECONDS);

        LOG.debug("Selecting samples for resource {} from {} to {}", resource, start, end);

        // Grab all of the sample in the requested interval
        final Results<Sample> samples = m_sampleRepository.select(m_context, resource, Optional.of(start), Optional.of(end));

        LOG.debug("Retrieved samples: {}", samples);

        // Select the last row
        final Row<Sample> lastRow = samples.getRows().stream()
            .reduce((previous, current) -> current).orElse(null);

        // There are no rows
        if (lastRow == null) {
            return Double.NaN;
        }

        // Select the sample for the given data-source
        final Sample sample = lastRow.getElement(ds);
        if (sample == null) {
            return Double.NaN;
        }

        // Grab the value
        return sample.getValue().doubleValue();
    }

    /////
    // Graphing
    /////

    @Override
    public InputStream createGraph(String command, File workDir)
            throws IOException, RrdException {
        throw new RrdException("createGraph() unsupported.");
    }

    @Override
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir)
            throws IOException, RrdException {
        throw new RrdException("createGraph() unsupported.");
    }

    /////////
    // Misc.
    /////////

    @Override
    public String getDefaultFileExtension() {
        return FILE_EXTENSION;
    }

    /**
     * This implementation does not track any stats.
     */
    @Override
    public String getStats() {
        return "";
    }

    @Override
    public void setConfigurationProperties(Properties props) {
        // pass
    }

    @VisibleForTesting
    public void setSampleRepository(SampleRepository sampleRepository) {
        m_sampleRepository = sampleRepository;
    }
}
