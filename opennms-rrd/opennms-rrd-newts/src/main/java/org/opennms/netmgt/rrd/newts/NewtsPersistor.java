package org.opennms.netmgt.rrd.newts;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.opennms.core.logging.Logging;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

/**
 * The NewtsPersistor is responsible for persisting samples gathered
 * by the NewtsRrdStrategy.
 *
 * Samples are queued globally and persisted in batches
 * using a Nagle inspired algorithm.
 *
 * @author jwhite
 */
public class NewtsPersistor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsPersistor.class);

    private static final boolean NO_DELAY = false;

    private static final long DELAY_IN_MS = 250;

    private static final long DELAY_AFTER_FAILURE_IN_MS = 5 * 1000;

    private static final int QUEUE_CAPACITY = 2048;

    private final LinkedBlockingQueue<Collection<Sample>> m_queue = Queues.newLinkedBlockingQueue(QUEUE_CAPACITY);

    @Autowired
    private SampleRepository m_sampleRepository ;

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
                    // We use an immutable list, since the collection of samples may continue
                    // to be referenced by one or more sample processors after insert() returns
                    final Builder<Sample> builder = new ImmutableList.Builder<Sample>();
                    samples.stream().forEach(builder::addAll);
                    flattenedSamples = builder.build();

                    LOG.debug("Inserting {} samples", flattenedSamples.size());
                    m_sampleRepository.insert(flattenedSamples);

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
            LOG.error("Interrupted.", e);
        }
    }

    public BlockingQueue<Collection<Sample>> getQueue() {
        return m_queue;
    }
}
