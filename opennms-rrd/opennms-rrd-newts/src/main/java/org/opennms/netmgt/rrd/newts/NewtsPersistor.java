package org.opennms.netmgt.rrd.newts;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.opennms.core.logging.Logging;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleProcessor;
import org.opennms.newts.api.SampleProcessorService;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.cassandra.CassandraSession;
import org.opennms.newts.cassandra.search.CassandraIndexer;
import org.opennms.newts.cassandra.search.CassandraIndexerSampleProcessor;
import org.opennms.newts.cassandra.search.GuavaResourceMetadataCache;
import org.opennms.newts.cassandra.search.ResourceMetadataCache;
import org.opennms.newts.persistence.cassandra.CassandraSampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * The NewtsPersistor is responsible for persisting samples gathered
 * by the NewtsRrdStrategy.
 *
 * This is not ideal and we should find a way of using the sample-storage-newts features from
 * Minion project instead.
 *
 * @author jwhite
 */
public class NewtsPersistor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsPersistor.class);

    private static final boolean NO_DELAY = false;

    private static final long DELAY_IN_MS = 250;

    // Ideally this value would correspond to the number of unique resource this system
    // may process. However, this comes at the cost of storing a ResourceMetadata object
    // for every one of these.
    private static final long MAX_CACHE_ENTRIES = 4096;

    private static final int SAMPLE_PROCESSOR_MAX_THREADS = 4;

    private static final long DELAY_AFTER_FAILURE_IN_MS = 5 * 1000;

    private final int m_ttl;

    private final LinkedBlockingQueue<Collection<Sample>> m_queue;

    private final MetricRegistry m_registry = new MetricRegistry();

    private final ResourceMetadataCache m_cache = new GuavaResourceMetadataCache(MAX_CACHE_ENTRIES, m_registry);

    private CassandraSession m_session = null;

    private SampleRepository m_sampleRepository = null;

    private CassandraIndexer m_indexer = null;

    public NewtsPersistor(int ttl, LinkedBlockingQueue<Collection<Sample>> queue) {
        m_ttl = ttl;
        m_queue = queue;
    }

    @Override
    public void run() {
        // We'd expect the logs from this thread to be in collectd.log
        Logging.putPrefix("collectd");

        try {
            final List<Collection<Sample>> samples = Lists.newArrayList();
            ImmutableList<Sample> flattenedSamples;

            while(true) {
                samples.clear();
                flattenedSamples = null;

                // Block and wait for an element
                samples.add(m_queue.take());
                try {
                    if (!NO_DELAY) {
                        // We only have a single sample, if there are no other samples
                        // pending on the queue, then sleep for short delay before
                        // checking again and initiating the insert
                        if (m_queue.size() == 0) {
                            Thread.sleep(DELAY_IN_MS);
                        }
                    }

                    // Grab all of the remaining samples on the queue
                    m_queue.drainTo(samples);

                    // Flatten the samples into an immutable list
                    // We use an immutable list, since the the collection
                    // of samples may continue to be referenced by
                    // one or more sample processors after insert() returns
                    final Builder<Sample> builder = new ImmutableList.Builder<Sample>();
                    samples.stream().forEach(builder::addAll);
                    flattenedSamples = builder.build();

                    LOG.debug("Inserting {} samples", flattenedSamples.size());
                    getSampleRepository().insert(flattenedSamples);

                    if (LOG.isDebugEnabled()) {
                        String uniqueResourceIds = flattenedSamples.stream()
                            .map(s -> s.getResource().getId())
                            .distinct()
                            .collect(Collectors.joining(", "));
                        LOG.debug("Successfully inserted samples for resources with ids {}", uniqueResourceIds);
                    }
                } catch (Throwable t) {
                    if (flattenedSamples != null) {
                        LOG.error("Failed to insert the samples. Adding them back to the end of the queue.", t);

                        if (m_queue.offer(flattenedSamples)) {
                            LOG.debug("Succesfully restored the samples in the queue.");
                        } else {
                            LOG.error("Failed to restore the samples in the queue. Current size: {}", m_queue.size());
                        }
                    }

                    // Rest before trying again
                    Thread.sleep(DELAY_AFTER_FAILURE_IN_MS);
                }
            }
        } catch (InterruptedException e) {
            LOG.warn("Interrupted.", e);
        }
    }

    private synchronized CassandraSession getSession() {
        if (m_session == null) {
            m_session = NewtsUtils.getCassrandraSession();
        }
        return m_session;
    }

    private synchronized CassandraIndexer getIndexer() {
        if (m_indexer == null) {
            m_indexer = new CassandraIndexer(getSession(), m_ttl, m_cache, m_registry);
        }
        return m_indexer;
    }

    public synchronized SampleRepository getSampleRepository() {
        if (m_sampleRepository == null) {
            // Index the samples
            Set<SampleProcessor> sampleProcessors = Sets.newHashSet(new CassandraIndexerSampleProcessor(getIndexer()));
            SampleProcessorService processors = new SampleProcessorService(SAMPLE_PROCESSOR_MAX_THREADS, sampleProcessors);

            // Sample repositories are used for reading/writing
            m_sampleRepository = new CassandraSampleRepository(getSession(), m_ttl, m_registry, processors);
        }
        return m_sampleRepository;
    }
}
