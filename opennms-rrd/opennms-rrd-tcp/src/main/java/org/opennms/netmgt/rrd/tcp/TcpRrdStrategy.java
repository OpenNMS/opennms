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

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Category;
import org.opennms.core.utils.StringUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.rrd.tcp.PerformanceDataProtos.PerformanceDataReading;
import org.opennms.netmgt.rrd.tcp.PerformanceDataProtos.PerformanceDataReadings;

/**
 * Provides a TCP socket-based implementation of RrdStrategy that pushes update commands
 * out in a simple serialized format.
 */
public class TcpRrdStrategy implements RrdStrategy<TcpRrdStrategy.RrdDefinition,TcpRrdStrategy.RrdOutputSocket> {
    public static class RrdDefinition {
        private final String m_creator, m_directory, m_rrdName;
        private final int m_step;

        public RrdDefinition(
                String creator,
                String directory,
                String rrdName,
                int step
        ) {
            m_creator = creator;
            m_directory = directory;
            m_rrdName = rrdName;
            m_step = step;
        }

        public String getPath() {
            return m_directory + File.separator + m_rrdName;
        };
    }

    public class RrdOutputSocket {
        // private final RrdDefinition m_def;
        private final String m_filename;
        private final PerformanceDataProtos.PerformanceDataReadings.Builder m_messages; 

        /*
        public RrdOutputSocket(RrdDefinition def) throws Exception {
            m_socket = new Socket(InetAddress.getByName("127.0.0.1"), TCP_PORT);
            m_def = def;
        }
         */

        public RrdOutputSocket(String filename) throws Exception {
            m_filename = filename;
            m_messages = PerformanceDataProtos.PerformanceDataReadings.newBuilder();
        }

        public String getPath() {
            return m_filename; // m_def.getPath();
        };

        public void addData(String owner, String data) {
            Long timestamp = parseRrdTimestamp(data);
            List<Double> values = parseRrdValues(data);
            m_messages.addMessage(PerformanceDataReading.newBuilder()
                    .setPath(m_filename)
                    .setOwner(owner)
                    .setTimestamp(timestamp).
                    addAllValue(values)
            );
        }

        public void writeData() throws Exception {
            Socket socket = null;
            try {
                socket = new Socket(InetAddress.getByName(m_host), m_port);
                OutputStream out = socket.getOutputStream();
                m_messages.build().writeTo(out);
                // out = new FileOutputStream(new File("/tmp/testdata.protobuf"));
                // m_messages.build().writeTo(out);
                out.flush();
            } catch (Throwable e) {
                ThreadCategory.getInstance(this.getClass()).warn("Error when trying to open connection to " + m_host + ":" + m_port + ", dropping " + m_messages.getMessageCount() + " performance messages: " + e.getMessage());
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        };

        private Long parseRrdTimestamp(String data) {
            if (data.startsWith("N:")) {
                return System.currentTimeMillis();
            } else {
                String timestamp = data.split(":")[0];
                // RRD timestamps are in seconds, we want to return milliseconds
                return Long.valueOf(timestamp) * 1000;
            }
        }

        private List<Double> parseRrdValues(String data) {
            List<Double> retval = new ArrayList<Double>();
            String[] values = data.split(":");
            // Skip index zero, that's the timestamp
            for (int i = 1; i < values.length; i++) {
                // Parse the RRD value for "unknown"
                if ("U".equals(values[i])) {
                    retval.add(Double.NaN);
                } else {
                    retval.add(new Double(values[i]));
                }
            }
            return retval;
        }
    }

    private String m_host = null;

    public String getHost() {
        return m_host;
    }

    public void setConfigurationProperties(Properties configurationParameters) {
        // Do nothing
    }

    public void setHost(String host) {
        this.m_host = host;
    }

    private int m_port = 0;

    public int getPort() {
        return m_port;
    }

    public void setPort(int port) {
        this.m_port = port;
    }

    public String getDefaultFileExtension() {
        return "";
    }

    public RrdDefinition createDefinition(String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws Exception {
        return new RrdDefinition(creator, directory, rrdName, step);
    }

    public void createFile(RrdDefinition rrdDef) throws Exception {
    }

    public RrdOutputSocket openFile(String fileName) throws Exception {
        return new RrdOutputSocket(fileName);
    }

    public void updateFile(RrdOutputSocket rrd, String owner, String data) throws Exception {
        rrd.addData(owner, data);
    }

    public void closeFile(RrdOutputSocket rrd) throws Exception {
        rrd.writeData();
    }

    public Double fetchLastValue(String rrdFile, String ds, int interval) throws NumberFormatException {
        return Double.NaN;
    }

    public Double fetchLastValue(String rrdFile, String ds, String consolidationFunction, int interval) throws NumberFormatException {
        return Double.NaN;
    }

    public Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) throws NumberFormatException {
        return Double.NaN;
    }

    public InputStream createGraph(String command, File workDir) throws IOException {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    public RrdGraphDetails createGraphReturnDetails(String command, File workDir) throws IOException {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    public int getGraphLeftOffset() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    public int getGraphRightOffset() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    public int getGraphTopOffsetWithText() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    public String getStats() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        // Do nothing; this implementation simply sends data to an external source and has not control
        // over when data is persisted to disk.
    }
}
