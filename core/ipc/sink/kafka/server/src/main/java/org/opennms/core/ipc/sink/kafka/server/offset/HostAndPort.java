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
package org.opennms.core.ipc.sink.kafka.server.offset;

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
