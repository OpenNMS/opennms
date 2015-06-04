package org.opennms.netmgt.rrd.newts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

/**
 * This is an attempt to use Newts as a data store using the
 * current RrdStrategy interface.
 *
 * The motivation is twofold:
 *  1) Start persisting OpenNMS metrics into Newts in order to identify
 *     any gaps with the current feature set, or issues with their
 *     current implementation.
 *
 *  2) Help identify changes we need to make to OpenNMS' persistence,
 *     resource naming, and metric retrieval code in order to fully
 *     support Newts as a data store.
 *
 * @author jwhite
 */
public class NewtsRrdStrategy implements RrdStrategy<RrdDef, RrdDb> {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsRrdStrategy.class);

    public static final int TTL = Integer.getInteger("org.opennms.newts.config.ttl", 31536000);

    protected static final String FILE_EXTENSION = ".newts";

    /////////
    // Newts
    /////////

    @Autowired
    private NewtsPersistor m_persistor;

    @Autowired
    private SampleRepository m_sampleRepository;

    private Thread m_persistorThread = null;

    // Number of seconds to keep samples for
    private int m_ttl;

    // Keep track of the definitions used by file system path
    private final Map<String, RrdDef> m_defByPath = Maps.newConcurrentMap();

    // Keep track of the attributes set by file system path
    private final Map<String, Map<String, String>> m_attrsByPath = Maps.newConcurrentMap();

    @Override
    public synchronized void setConfigurationProperties(Properties props) {
        m_ttl = Integer.valueOf((String)props.getOrDefault(NewtsUtils.TTL_PROPERTY, NewtsUtils.DEFAULT_TTL));

        LOG.info("Using ttl {}", m_ttl);

        if (m_persistorThread != null) {
            LOG.warn("Persistor already started.");
        } else {
            LOG.info("Starting persistor thread.");

            m_persistorThread = new Thread(m_persistor);
            m_persistorThread.setName("NewtsPersistor");
            m_persistorThread.start();
        }
    }

    private void maybeStartPersistorThread() {
        if (m_persistorThread != null) {
            return;
        }
        setConfigurationProperties(System.getProperties());
    }

    /////////
    // Persistence
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
        final List<Sample> samples = NewtsUtils.getSamplesFromRrdUpdateString(def, data, attributes);
        LOG.debug("Adding {} samples to the queue.", samples.size());
        if (!m_persistor.getQueue().offer(samples)) {
            LOG.warn("The queue rejected {} samples. These will be lost.", samples.size());
        }

        maybeStartPersistorThread();
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
        final Results<Sample> samples = m_sampleRepository.select(resource, Optional.of(start), Optional.of(end));

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

    @Override
    public int getGraphLeftOffset() {
        return 0;
    }

    @Override
    public int getGraphRightOffset() {
        return 0;
    }

    @Override
    public int getGraphTopOffsetWithText() {
        return 0;
    }

    /////////
    // Misc.
    /////////

    @Override
    public String getDefaultFileExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        // pass
    }

    /**
     * This implementation does not track any stats.
     */
    @Override
    public String getStats() {
        return "";
    }
}
