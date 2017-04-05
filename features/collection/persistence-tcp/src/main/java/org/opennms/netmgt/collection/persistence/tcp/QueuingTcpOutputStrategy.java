/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.List;

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
 * 
 * @author ranger
 * @version $Id: $
 */
public class QueuingTcpOutputStrategy implements TcpOutputStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(QueuingTcpOutputStrategy.class);

    private final BlockingQueue<PerformanceDataReading> m_queue;
    private final SimpleTcpOutputStrategy m_delegate;
    private int m_skippedReadings = 0;

    private static class PerformanceDataReading {
        private String m_filename;
        private String m_owner;
        private Long m_timestamp;
        private List<Double> m_dblValues;
        private List<String> m_strValues;
        private String m_data;
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
        public ConsumerThread(final SimpleTcpOutputStrategy strategy, final BlockingQueue<PerformanceDataReading> queue) {
            m_strategy = strategy;
            m_myQueue = queue;
            this.setName(this.getClass().getSimpleName());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Collection<PerformanceDataReading> sendMe = new ArrayList<PerformanceDataReading>();
                    if (m_myQueue.drainTo(sendMe) > 0) {
                        RrdOutputSocket socket = new RrdOutputSocket(m_strategy.getHost(), m_strategy.getPort());
                        for (PerformanceDataReading reading : sendMe) {
                            socket.addData(reading.getFilename(), reading.getOwner(), reading.getTimestamp(), reading.getDblValues(), reading.getStrValues());
                        }
                        socket.writeData();
                    } else {
                        Thread.sleep(1000);
                    }
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
        m_delegate = delegate;
        m_queue = new LinkedBlockingQueue<PerformanceDataReading>(queueSize);
        ConsumerThread consumerThread = new ConsumerThread(delegate, m_queue);
        consumerThread.start();
    }

    /**
     * <p>getDefaultFileExtension</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getDefaultFileExtension() {
        return m_delegate.getDefaultFileExtension();
    }

    /** {@inheritDoc} */
    @Override
    public void updateData(String path, String owner, Long timestamp, List<Double> dblValues, List<String> strValues) throws Exception {
        if (m_queue.offer(new PerformanceDataReading(path, owner, timestamp, dblValues, strValues), 500, TimeUnit.MILLISECONDS)) {
            if (m_skippedReadings > 0) {
                LOG.warn("Skipped {} performance data message(s) because of queue overflow", m_skippedReadings);
                m_skippedReadings = 0;
            }
        } else {
            m_skippedReadings++;
        }
    }
}
