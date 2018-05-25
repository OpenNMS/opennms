/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.kafka.offset;

public class HostAndPort {

    private String host;
    private int port;
    private static String[] hostAndPortArray;

    public final static HostAndPort fromString(String hostWithPort) {
        hostWithPort = hostWithPort.replaceAll("\\s+", "");
        hostAndPortArray = hostWithPort.split(",");
        return getHostAndPort(hostAndPortArray[0]);
    }

    public static HostAndPort getNextHostAndPort(HostAndPort hostAndPort) {
        String hostWithPort = hostAndPort.getHost() + ":" + hostAndPort.getPort();
        if (hostAndPortArray.length == 0) {
            return null;
        } else {
            hostWithPort = getNextHost(hostWithPort);
        }
        if (hostWithPort == null) {
            return null;
        }
        return getHostAndPort(hostWithPort);
    }

    private static HostAndPort getHostAndPort(String hostWithPort) {
        int i = hostWithPort.lastIndexOf(":");
        if (i < 0 || (hostWithPort.length() == i)) {
            return null;
        }
        String[] hostWithPortArray = { hostWithPort.substring(0, i), hostWithPort.substring(i + 1) };
        HostAndPort hostAndPort = new HostAndPort();
        hostAndPort.setHost(hostWithPortArray[0]);
        hostAndPort.setPort(Integer.parseInt(hostWithPortArray[1]));
        return hostAndPort;
    }

    private static String getNextHost(String hostWithPort) {
        int len = hostAndPortArray.length;
        for (int i = 0; i < len; i++) {
            if (hostWithPort.equals(hostAndPortArray[i]) && i != (len - 1)) {
                return hostAndPortArray[i + 1];
            }
        }
        if (hostWithPort.equals(hostAndPortArray[len - 1])) {
            return hostAndPortArray[0];
        }
        return null;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "HostAndPort [host=" + host + ", port=" + port + "]";
    }

}
