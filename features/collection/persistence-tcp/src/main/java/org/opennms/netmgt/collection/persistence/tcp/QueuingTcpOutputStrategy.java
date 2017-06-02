/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.persistence.tcp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.rrd.tcp.RrdOutputSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a queued implementation of TcpOutputStrategy that pushes update
 * commands in multiple readings at a time.
 * <p>
 * The receiver of this strategy is not defined in any way. This is just a fire
 * and forget strategy. There is no way to read data back into opennms.
 * </p>
 */
public class QueuingTcpOutputStrategy implements TcpOutputStrategy {
    private static final long SLEEP_TIME = Long.getLong("org.opennms.netmgt.persistence.tcp.queuingTcpSleepTime", 1000);
    private static final long OFFER_WAIT_TIME = Long.getLong("org.opennms.netmgt.persistence.tcp.queuingTcpOfferWaitTime", 500);
    private static final boolean LOGGING = Boolean.getBoolean("org.opennms.netmgt.persistence.tcp.queuingTcpLogging");
    private static final long LOGGING_INTERVAL = Long.getLong("org.opennms.netmgt.persistence.tcp.queuingTcpLoggingInterval", 300000);
    private static final Logger LOG = LoggerFactory.getLogger(QueuingTcpOutputStrategy.class);

    private final BlockingQueue<PerformanceDataReading> m_queue;
    private int m_skippedReadings = 0;
    private int m_totalOffers = 0;
    private int m_goodOffers = 0;
    private int m_badOffers = 0;

    private static class PerformanceDataReading {
        private String m_filename;
        private String m_owner;
        private Long m_timestamp;
        private List<Double> m_dblValues;
        private List<String> m_strValues;
        public PerformanceDataReading(String filename, String owner, Long timestamp, List<Double> dblValues, List<String> strValues) {
            m_filename = filename;
            m_owner = owner;
            m_timestamp = timestamp;
            m_dblValues = dblValues;
            m_strValues = strValues;
        }
        public String getFilename() {
            return m_filename;
        }
        public String getOwner() {
            return m_owner;
        }
        public Long getTimestamp() {
            return m_timestamp;
        }
        public List<Double> getDblValues() {
            return m_dblValues;
        }
        public List<String> getStrValues() {
            return m_strValues;
        }
    }

    private static class ConsumerThread extends Thread {
        private final BlockingQueue<PerformanceDataReading> m_myQueue;
        private final SimpleTcpOutputStrategy m_strategy;
        private long m_queueChecks = 0;
        private long m_queueDrains = 0;
        private long m_sentReadings = 0;

        public ConsumerThread(final SimpleTcpOutputStrategy strategy, final BlockingQueue<PerformanceDataReading> queue) {
            m_strategy = strategy;
            m_myQueue = queue;
            this.setName(this.getClass().getSimpleName());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    boolean drain = false;
                    long sentReadings = 0;
                    Collection<PerformanceDataReading> sendMe = new ArrayList<PerformanceDataReading>();
                    if (m_myQueue.drainTo(sendMe) > 0) {
                        drain = true;
                        sentReadings = sendMe.size();
                        RrdOutputSocket socket = new RrdOutputSocket(m_strategy.getHost(), m_strategy.getPort());
                        for (PerformanceDataReading reading : sendMe) {
                            socket.addData(reading.getFilename(), reading.getOwner(), reading.getTimestamp(), reading.getDblValues(), reading.getStrValues());
                        }
                        socket.writeData();
                    } else {
                        Thread.sleep(SLEEP_TIME);
                    }
                    if (LOGGING) {
                        countDrainStats(drain, sentReadings);
                    }
                }
            } catch (InterruptedException e) {
                LOG.warn("InterruptedException caught in QueuingTcpOutputStrategy$ConsumerThread, closing thread");
            } catch (Throwable e) {
                LOG.error("Unexpected exception caught in QueuingTcpOutputStrategy$ConsumerThread, closing thread", e);
            }
        }

        public void countDrainStats(boolean drain, long readings) {
            m_queueChecks++;
            if (drain) {
                m_queueDrains++;
                m_sentReadings += readings;
            }
        }
        public void clearDrainStats() {
            m_queueChecks = 0;
            m_queueDrains = 0;
            m_sentReadings = 0;
        }
        public long getQueueChecks() {
            return m_queueChecks;
        }
        public long getQueueDrains() {
            return m_queueDrains;
        }
        public long getSentReadings() {
            return m_sentReadings;
        }
    }

    private static class LogThread extends Thread {
        private final BlockingQueue<PerformanceDataReading> m_myQueue;
        private final QueuingTcpOutputStrategy m_strategy;
        private final ConsumerThread m_consumer;
        public LogThread(final QueuingTcpOutputStrategy strategy, final ConsumerThread consumer, final BlockingQueue<PerformanceDataReading> queue) {
            m_strategy = strategy;
            m_myQueue = queue;
            m_consumer = consumer;
            this.setName(this.getClass().getSimpleName());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    long totalOffers = m_strategy.getTotalOffers();
                    long badOffers = m_strategy.getBadOffers();
                    long goodOffers = m_strategy.getGoodOffers();
                    long queueChecks = m_consumer.getQueueChecks();
                    long queueDrains = m_consumer.getQueueDrains();
                    long queueSize = m_myQueue.size();
                    long queueRemaining = m_myQueue.remainingCapacity();
                    long sentReadings = m_consumer.getSentReadings();
                    LOG.info("Queue offers: " + totalOffers + " total, " + goodOffers + " good, " + badOffers + " bad; queue drains: " + queueChecks + " checks, " + queueDrains + " drains, " + sentReadings + " readings; queue state: " + queueSize + " elements, " + queueRemaining + " remaining capacity");
                    m_strategy.clearOfferStats();
                    m_consumer.clearDrainStats();
                    Thread.sleep(LOGGING_INTERVAL);
                }
            } catch (InterruptedException e) {
                LOG.warn("InterruptedException caught in QueuingTcpOutputStrategy$ConsumerThread, closing thread");
            } catch (Throwable e) {
                LOG.error("Unexpected exception caught in QueuingTcpOutputStrategy$ConsumerThread, closing thread", e);
            }
        }
    }

    /**
     * <p>Constructor for QueuingTcpOutputStrategy.</p>
     *
     * @param delegate a {@link org.opennms.netmgt.rrd.tcp.SimpleTcpOutputStrategy} object.
     */
    public QueuingTcpOutputStrategy(SimpleTcpOutputStrategy delegate, int queueSize) {
        m_queue = new LinkedBlockingQueue<PerformanceDataReading>(queueSize);
        ConsumerThread consumerThread = new ConsumerThread(delegate, m_queue);
        consumerThread.start();
        if (LOGGING) {
            LogThread logThread = new LogThread(this, consumerThread, m_queue);
            logThread.start();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateData(String path, String owner, Long timestamp, List<Double> dblValues, List<String> strValues) throws Exception {
        boolean offerGood = false;
        if (m_queue.offer(new PerformanceDataReading(path, owner, timestamp, dblValues, strValues), OFFER_WAIT_TIME, TimeUnit.MILLISECONDS)) {
            offerGood = true;
            if (m_skippedReadings > 0) {
                LOG.warn("Skipped {} performance data message(s) because of queue overflow", m_skippedReadings);
                m_skippedReadings = 0;
            }
        } else {
            m_skippedReadings++;
        }
        if (LOGGING) {
            countOfferStats(offerGood);
        }
    }

    public void countOfferStats(boolean goodOffer) {
        m_totalOffers++;
        if (goodOffer) {
            m_goodOffers++;
        } else {
            m_badOffers++;
        }
    }
    public void clearOfferStats() {
        m_totalOffers = 0;
        m_goodOffers = 0;
        m_badOffers = 0;
    }
    public long getTotalOffers() {
        return m_totalOffers;
    }
    public long getGoodOffers() {
        return m_goodOffers;
    }
    public long getBadOffers() {
        return m_badOffers;
    }
}
