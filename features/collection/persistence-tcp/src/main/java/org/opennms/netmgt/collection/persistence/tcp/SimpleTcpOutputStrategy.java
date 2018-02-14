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

import java.util.List;

import org.opennms.netmgt.rrd.tcp.RrdOutputSocket;

/**
 * Provides a simple non-queued implementation of TcpOutputStrategy
 * that pushes data to the stream one reading at a time.
 * <p>
 * The receiver of this strategy is not defined in any way. This is just a fire
 * and forget strategy. There is no way to read data back into opennms.
 * </p>
 */
public class SimpleTcpOutputStrategy implements TcpOutputStrategy {

    private String m_host = null;

    /**
     * <p>getHost</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHost() {
        return m_host;
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

    /** {@inheritDoc} */
    @Override
    public void updateData(String path, String owner, Long timestamp, List<Double> dblValues, List<String> strValues) throws Exception {
        RrdOutputSocket socket = new RrdOutputSocket(m_host, m_port);
        socket.addData(path, owner, timestamp, dblValues, strValues);
        socket.writeData();
    }
}
