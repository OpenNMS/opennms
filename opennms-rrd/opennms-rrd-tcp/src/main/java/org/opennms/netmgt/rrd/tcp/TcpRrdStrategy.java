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
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;

/**
 * Provides a TCP socket-based implementation of RrdStrategy that pushes update commands
 * out in a simple serialized format.
 *
 * @author ranger
 * @version $Id: $
 */
public class TcpRrdStrategy implements RrdStrategy<TcpRrdStrategy.RrdDefinition,TcpRrdStrategy.RrdOutputSocketWithFilename> {
    public static class RrdDefinition {
        private final String m_directory, m_rrdName;
        public RrdDefinition(
                String directory,
                String rrdName
        ) {
            m_directory = directory;
            m_rrdName = rrdName;
        }

        public String getPath() {
            return m_directory + File.separator + m_rrdName;
        };
    }

    public static class RrdOutputSocketWithFilename {
        private final RrdOutputSocket m_socket;
        private final String m_filename;

        public RrdOutputSocketWithFilename(RrdOutputSocket socket, String filename) {
            m_socket = socket;
            m_filename = filename;
        }

        public RrdOutputSocket getSocket() {
            return m_socket;
        }

        public String getFilename() {
            return m_filename;
        }
    }

    private String m_host = null;

    /**
     * <p>getHost</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHost() {
        return m_host;
    }

    /** {@inheritDoc} */
    public void setConfigurationProperties(Properties configurationParameters) {
        // Do nothing
    }

    /**
     * <p>setHost</p>
     *
     * @param host a {@link java.lang.String} object.
     */
    public void setHost(String host) {
        this.m_host = host;
    }

    private int m_port = 0;

    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    public int getPort() {
        return m_port;
    }

    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    public void setPort(int port) {
        this.m_port = port;
    }

    /**
     * <p>getDefaultFileExtension</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultFileExtension() {
        return "";
    }

    /** {@inheritDoc} */
    public RrdDefinition createDefinition(String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws Exception {
        return new RrdDefinition(directory, rrdName);
    }

    /**
     * <p>createFile</p>
     *
     * @param rrdDef a {@link org.opennms.netmgt.rrd.tcp.TcpRrdStrategy.RrdDefinition} object.
     * @throws java.lang.Exception if any.
     */
    public void createFile(RrdDefinition rrdDef) throws Exception {
        // Do nothing
    }

    /** {@inheritDoc} */
    public RrdOutputSocketWithFilename openFile(String fileName) throws Exception {
        return new RrdOutputSocketWithFilename(new RrdOutputSocket(m_host, m_port), fileName);
    }

    /** {@inheritDoc} */
    public void updateFile(RrdOutputSocketWithFilename rrd, String owner, String data) throws Exception {
        rrd.getSocket().addData(rrd.getFilename(), owner, data);
    }

    /**
     * <p>closeFile</p>
     *
     * @param rrd a {@link org.opennms.netmgt.rrd.tcp.TcpRrdStrategy.RrdOutputSocketWithFilename} object.
     * @throws java.lang.Exception if any.
     */
    public void closeFile(RrdOutputSocketWithFilename rrd) throws Exception {
        rrd.getSocket().writeData();
    }

    /** {@inheritDoc} */
    public Double fetchLastValue(String rrdFile, String ds, int interval) throws NumberFormatException {
        return Double.NaN;
    }

    /** {@inheritDoc} */
    public Double fetchLastValue(String rrdFile, String ds, String consolidationFunction, int interval) throws NumberFormatException {
        return Double.NaN;
    }

    /** {@inheritDoc} */
    public Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) throws NumberFormatException {
        return Double.NaN;
    }

    /** {@inheritDoc} */
    public InputStream createGraph(String command, File workDir) throws IOException {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    /** {@inheritDoc} */
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir) throws IOException {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    /**
     * <p>getGraphLeftOffset</p>
     *
     * @return a int.
     */
    public int getGraphLeftOffset() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    /**
     * <p>getGraphRightOffset</p>
     *
     * @return a int.
     */
    public int getGraphRightOffset() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @return a int.
     */
    public int getGraphTopOffsetWithText() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    /**
     * <p>getStats</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStats() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support graphing.");
    }

    /** {@inheritDoc} */
    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        // Do nothing; this implementation simply sends data to an external source and has not control
        // over when data is persisted to disk.
    }
}
