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
 * 2010 Feb 22: Created this file.
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

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.tcp.PerformanceDataProtos.PerformanceDataReading;

public class RrdOutputSocket {
    // private final RrdDefinition m_def;
    private final String m_host;
    private final int m_port;
    private final PerformanceDataProtos.PerformanceDataReadings.Builder m_messages; 

    public RrdOutputSocket(String host, int port) throws Exception {
        m_host = host;
        m_port = port;
        m_messages = PerformanceDataProtos.PerformanceDataReadings.newBuilder();
    }

    public void addData(String filename, String owner, String data) {
        Long timestamp = parseRrdTimestamp(data);
        List<Double> values = parseRrdValues(data);
        m_messages.addMessage(PerformanceDataReading.newBuilder()
                .setPath(filename)
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