package org.opennms.netmgt.newts;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.core.logging.Logging;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Used to write samples to the {@link org.opennms.newts.api.SampleRepository}.
 *
 * @author jwhite
 */
public class NewtsWriter {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsWriter.class);

    @Autowired
    private SampleRepository m_sampleRepository;

    private final int m_maxBatchSize;

    private final int m_maxBatchDelayInMs;

    // Batch the samples up for insertion
    private List<Sample> sampleBatch = Lists.newLinkedList();

    // Limit the amount of the time the samples spend in the batch
    private long insertBatchAfterTimeMillis = 0;

    // Used to trigger a batch insert if the samples have been sitting in the
    // batch for >= max_batch_delay
    private Timer m_batchTimer = null;

    @Inject
    public NewtsWriter(@Named("newts.max_batch_size") Integer maxBatchSize, @Named("newts.max_batch_delay") Integer maxBatchDelayInMs)  {
        Preconditions.checkArgument(maxBatchSize > 0, "max_batch_size must be strictly positive");
        Preconditions.checkArgument(maxBatchDelayInMs >= 0, "max_batch_delay must be positive");

        m_maxBatchSize = maxBatchSize;
        m_maxBatchDelayInMs = maxBatchDelayInMs;

        LOG.debug("Using max_batch_size: {} and max_batch_delay: {}", maxBatchSize, m_maxBatchDelayInMs);
    }

    public synchronized void insert(Collection<Sample> samples) {
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
            insert(ImmutableList.copyOf(it));
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
                insert(Collections.emptyList());
            }
        }, 0, m_maxBatchDelayInMs);
    }
}

