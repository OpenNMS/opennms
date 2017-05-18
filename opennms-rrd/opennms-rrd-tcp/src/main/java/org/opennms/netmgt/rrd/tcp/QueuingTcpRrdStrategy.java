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

package org.opennms.netmgt.rrd.tcp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.tcp.TcpRrdStrategy.RrdDefinition;

/**
 * Provides a TCP socket-based implementation of RrdStrategy that pushes update
 * commands out in a simple serialized format.
 * <p>
 * The receiver of this strategy is not defined in any way. This is just a fire
 * and forget strategy. There is no way to read data back into opennms.
 * </p>
 * 
 * @author ranger
 * @version $Id: $
 */
public class QueuingTcpRrdStrategy implements RrdStrategy<TcpRrdStrategy.RrdDefinition,String> {
    private static final Logger LOG = LoggerFactory.getLogger(QueuingTcpRrdStrategy.class);

    private final BlockingQueue<PerformanceDataReading> m_queue;
    private final TcpRrdStrategy m_delegate;
    private int m_skippedReadings = 0;

    private static class PerformanceDataReading {
        private String m_filename;
        private String m_owner;
        private String m_data;
        public PerformanceDataReading(String filename, String owner, String data) {
            m_filename = filename;
            m_owner = owner;
            m_data = data;
        }
        public String getFilename() {
            return m_filename;
        }
        public String getOwner() {
            return m_owner;
        }
        public String getData() {
            return m_data;
        }
    }

    private static class ConsumerThread extends Thread {
        private final BlockingQueue<PerformanceDataReading> m_myQueue;
        private final TcpRrdStrategy m_strategy;
        public ConsumerThread(final TcpRrdStrategy strategy, final BlockingQueue<PerformanceDataReading> queue) {
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
                            socket.addData(reading.getFilename(), reading.getOwner(), reading.getData());
                        }
                        socket.writeData();
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException e) {
                LOG.warn("InterruptedException caught in QueuingTcpRrdStrategy$ConsumerThread, closing thread");
            } catch (Throwable e) {
                LOG.error("Unexpected exception caught in QueuingTcpRrdStrategy$ConsumerThread, closing thread", e);
            }
        }
    }

    /**
     * <p>Constructor for QueuingTcpRrdStrategy.</p>
     *
     * @param delegate a {@link org.opennms.netmgt.rrd.tcp.TcpRrdStrategy} object.
     */
    public QueuingTcpRrdStrategy(TcpRrdStrategy delegate, int queueSize) {
        m_delegate = delegate;
        m_queue = new LinkedBlockingQueue<PerformanceDataReading>(queueSize);
        ConsumerThread consumerThread = new ConsumerThread(delegate, m_queue);
        consumerThread.start();
    }

    /** {@inheritDoc} */
    @Override
    public void setConfigurationProperties(Properties configurationParameters) {
        m_delegate.setConfigurationProperties(configurationParameters);
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
    public TcpRrdStrategy.RrdDefinition createDefinition(String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws Exception {
        return new TcpRrdStrategy.RrdDefinition(directory, rrdName);
    }

    /**
     * <p>createFile</p>
     *
     * @param rrdDef a {@link RrdDefinition} object.
     * @throws java.lang.Exception if any.
     */
    @Override
	public void createFile(RrdDefinition rrdDef) throws Exception {
		// done nothing
    }

    /** {@inheritDoc} */
    @Override
    public String openFile(String fileName) throws Exception {
        return fileName;
    }

    /** {@inheritDoc} */
    @Override
    public void updateFile(String fileName, String owner, String data) throws Exception {
        if (m_queue.offer(new PerformanceDataReading(fileName, owner, data), 500, TimeUnit.MILLISECONDS)) {
            if (m_skippedReadings > 0) {
                LOG.warn("Skipped {} performance data message(s) because of queue overflow", m_skippedReadings);
                m_skippedReadings = 0;
            }
        } else {
            m_skippedReadings++;
        }
    }

    /**
     * <p>closeFile</p>
     *
     * @param rrd a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public void closeFile(String rrd) throws Exception {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public Double fetchLastValue(String rrdFile, String ds, int interval) throws NumberFormatException {
        return m_delegate.fetchLastValue(rrdFile, ds, interval);
    }

    /** {@inheritDoc} */
    @Override
    public Double fetchLastValue(String rrdFile, String ds, String consolidationFunction, int interval) throws NumberFormatException {
        return m_delegate.fetchLastValue(rrdFile, ds, consolidationFunction, interval);
    }

    /** {@inheritDoc} */
    @Override
    public Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) throws NumberFormatException {
        return m_delegate.fetchLastValueInRange(rrdFile, ds, interval, range);
    }

    /** {@inheritDoc} */
    @Override
    public InputStream createGraph(String command, File workDir) throws IOException {
        return m_delegate.createGraph(command, workDir);
    }

    /** {@inheritDoc} */
    @Override
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir) throws IOException {
        return m_delegate.createGraphReturnDetails(command, workDir);
    }

    /**
     * <p>getGraphLeftOffset</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphLeftOffset() {
        return m_delegate.getGraphLeftOffset();
    }

    /**
     * <p>getGraphRightOffset</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphRightOffset() {
        return m_delegate.getGraphRightOffset();
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphTopOffsetWithText() {
        return m_delegate.getGraphTopOffsetWithText();
    }

    /**
     * <p>getStats</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStats() {
        return m_delegate.getStats();
    }

    /** {@inheritDoc} */
    @Override
    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        m_delegate.promoteEnqueuedFiles(rrdFiles);
    }
    
}
