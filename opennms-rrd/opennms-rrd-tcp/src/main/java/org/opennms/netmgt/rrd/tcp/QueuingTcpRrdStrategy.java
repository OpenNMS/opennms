/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Jun 16: Move RRD command-specific tokenizing methods here from StringUtils - jeffg@opennms.org
 * 2007 Aug 02: Organize imports. - dj@opennms.org
 * 2007 Apr 05: Java 5 generics and loops. - dj@opennms.org
 * 2007 Mar 19: Add createGraphReturnDetails and move assertion of a graph being created to JRobinRrdGraphDetails. - dj@opennms.org
 * 2007 Mar 19: Indent, add support for PRINT in graphs. - dj@opennms.org
 * 2007 Mar 02: Add support for --base and fix some log messages. - dj@opennms.org
 * 2004 Jul 08: Created this file.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.                                                            
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *      
 * For more information contact: 
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.rrd.tcp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Provides a TCP socket-based implementation of RrdStrategy that pushes update commands
 * out in a simple serialized format.
 *
 * @author ranger
 * @version $Id: $
 */
public class QueuingTcpRrdStrategy implements RrdStrategy<TcpRrdStrategy.RrdDefinition,String> {

    private final BlockingQueue<PerformanceDataReading> m_queue = new LinkedBlockingQueue<PerformanceDataReading>(50000);
    private final ConsumerThread m_consumerThread;
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
                ThreadCategory.getInstance(this.getClass()).warn("InterruptedException caught in QueuingTcpRrdStrategy$ConsumerThread, closing thread");
            } catch (Throwable e) {
                ThreadCategory.getInstance(this.getClass()).fatal("Unexpected exception caught in QueuingTcpRrdStrategy$ConsumerThread, closing thread", e);
            }
        }
    }

    /**
     * <p>Constructor for QueuingTcpRrdStrategy.</p>
     *
     * @param delegate a {@link org.opennms.netmgt.rrd.tcp.TcpRrdStrategy} object.
     */
    public QueuingTcpRrdStrategy(TcpRrdStrategy delegate) {
        m_delegate = delegate;
        m_consumerThread = new ConsumerThread(delegate, m_queue);
        m_consumerThread.start();
    }

    /** {@inheritDoc} */
    public void setConfigurationProperties(Properties configurationParameters) {
        m_delegate.setConfigurationProperties(configurationParameters);
    }

    /**
     * <p>getDefaultFileExtension</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultFileExtension() {
        return m_delegate.getDefaultFileExtension();
    }

    /** {@inheritDoc} */
    public TcpRrdStrategy.RrdDefinition createDefinition(String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws Exception {
        return new TcpRrdStrategy.RrdDefinition(directory, rrdName);
    }

    /**
     * <p>createFile</p>
     *
     * @param rrdDef a {@link org.opennms.netmgt.rrd.tcp.TcpRrdStrategy.RrdDefinition} object.
     * @throws java.lang.Exception if any.
     */
    public void createFile(TcpRrdStrategy.RrdDefinition rrdDef) throws Exception {
        // Do nothing
    }

    /** {@inheritDoc} */
    public String openFile(String fileName) throws Exception {
        return fileName;
    }

    /** {@inheritDoc} */
    public void updateFile(String fileName, String owner, String data) throws Exception {
        if (m_queue.offer(new PerformanceDataReading(fileName, owner, data), 500, TimeUnit.MILLISECONDS)) {
            if (m_skippedReadings > 0) {
                ThreadCategory.getInstance().warn("Skipped " + m_skippedReadings + " performance data message(s) because of queue overflow");
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
    public void closeFile(String rrd) throws Exception {
        // Do nothing
    }

    /** {@inheritDoc} */
    public Double fetchLastValue(String rrdFile, String ds, int interval) throws NumberFormatException {
        return m_delegate.fetchLastValue(rrdFile, ds, interval);
    }

    /** {@inheritDoc} */
    public Double fetchLastValue(String rrdFile, String ds, String consolidationFunction, int interval) throws NumberFormatException {
        return m_delegate.fetchLastValue(rrdFile, ds, consolidationFunction, interval);
    }

    /** {@inheritDoc} */
    public Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) throws NumberFormatException {
        return m_delegate.fetchLastValueInRange(rrdFile, ds, interval, range);
    }

    /** {@inheritDoc} */
    public InputStream createGraph(String command, File workDir) throws IOException {
        return m_delegate.createGraph(command, workDir);
    }

    /** {@inheritDoc} */
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir) throws IOException {
        return m_delegate.createGraphReturnDetails(command, workDir);
    }

    /**
     * <p>getGraphLeftOffset</p>
     *
     * @return a int.
     */
    public int getGraphLeftOffset() {
        return m_delegate.getGraphLeftOffset();
    }

    /**
     * <p>getGraphRightOffset</p>
     *
     * @return a int.
     */
    public int getGraphRightOffset() {
        return m_delegate.getGraphRightOffset();
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @return a int.
     */
    public int getGraphTopOffsetWithText() {
        return m_delegate.getGraphTopOffsetWithText();
    }

    /**
     * <p>getStats</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStats() {
        return m_delegate.getStats();
    }

    /** {@inheritDoc} */
    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        m_delegate.promoteEnqueuedFiles(rrdFiles);
    }
}
