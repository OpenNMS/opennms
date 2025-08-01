/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.rrd.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.rrd.tcp.PerformanceDataProtos.PerformanceDataReading;

/**
 * <p>RrdOutputSocket class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class RrdOutputSocket {
    private static final Logger LOG = LoggerFactory.getLogger(RrdOutputSocket.class);

    // private final RrdDefinition m_def;
    private final String m_host;
    private final int m_port;
    private final PerformanceDataProtos.PerformanceDataReadings.Builder m_messages;
    private int m_messageCount = 0;

    /**
     * <p>Constructor for RrdOutputSocket.</p>
     *
     * @param host a {@link java.lang.String} object.
     * @param port a int.
     */
    public RrdOutputSocket(String host, int port) {
        m_host = host;
        m_port = port;
        m_messages = PerformanceDataProtos.PerformanceDataReadings.newBuilder();
    }

    /**
     * <p>addData</p>
     *
     * @param filename a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     * @param data a {@link java.lang.String} object.
     */
    public void addData(String filename, String owner, String data) {
        Long timestamp = parseRrdTimestamp(data);
        List<Double> values = parseRrdValues(data);
        m_messages.addMessage(PerformanceDataReading.newBuilder()
                .setPath(filename)
                .setOwner(owner)
                .setTimestamp(timestamp)
                .addAllDblValue(values)
                .addAllStrValue(new ArrayList<String>())
        );
        m_messageCount++;
    }

    /**
     * <p>addData</p>
     *
     * @param filename a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     * @param dblValues a {@link java.util.List} object.
     * @param strValues a {@link java.util.List} object.
     */
    public void addData(String filename, String owner, Long timestampInSec, List<Double> dblValues, List<String> strValues) {
        m_messages.addMessage(PerformanceDataReading.newBuilder()
                .setPath(filename)
                .setOwner(owner)
                .setTimestamp(timestampInSec * 1000)
                .addAllDblValue(dblValues)
                .addAllStrValue(strValues)
        );
        m_messageCount++;
    }

    /**
     * <p>writeData</p>
     */
    public void writeData() {
        Socket socket = null;
        try {
            socket = new Socket(InetAddressUtils.addr(m_host), m_port);
            OutputStream out = socket.getOutputStream();
            m_messages.build().writeTo(out);
            // out = new FileOutputStream(new File("/tmp/testdata.protobuf"));
            // m_messages.build().writeTo(out);
            out.flush();
        } catch (Throwable e) {
            LOG.warn("Error when trying to open connection to {}:{}, dropping {} performance messages: {}", m_host, m_port, m_messageCount, e.getMessage());
        } finally {
            if (socket != null) {
                try { 
                    socket.close(); 
                } catch (IOException e) {
                    LOG.warn("IOException when closing TCP performance data socket: {}", e.getMessage());
                }
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
        List<Double> retval = new ArrayList<>();
        String[] values = data.split(":");
        // Skip index zero, that's the timestamp
        for (int i = 1; i < values.length; i++) {
            if (values[i] == null || "null".equals(values[i])) {
                // Handle null values
                retval.add(Double.NaN);
            } else if ("U".equals(values[i])) {
                // Parse the RRD value for "unknown"
                retval.add(Double.NaN);
            } else {
                retval.add(new Double(values[i]));
            }
        }
        return retval;
    }
}
