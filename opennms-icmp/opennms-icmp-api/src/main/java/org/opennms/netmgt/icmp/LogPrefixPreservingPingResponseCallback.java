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
package org.opennms.netmgt.icmp;

import java.net.InetAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public class LogPrefixPreservingPingResponseCallback implements PingResponseCallback {
    private static final Logger LOG = LoggerFactory.getLogger(LogPrefixPreservingPingResponseCallback.class);
	
    private final PingResponseCallback m_cb;
    private final Map<String, String> m_mdc = getCopyOfContextMap();
    
    public LogPrefixPreservingPingResponseCallback(PingResponseCallback cb) {
        m_cb = cb;
    }
    
    private static Map<String, String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }
    
    private static void setContextMap(Map<String, String> map) {
        if (map == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(map);
        }
    }

    @Override
    public void handleError(InetAddress address, EchoPacket request, Throwable t) {
    	
    	Map<String, String> mdc = getCopyOfContextMap();
        try {
            setContextMap(m_mdc);
            m_cb.handleError(address, request, t);
        } finally {
            setContextMap(mdc);
        }
    }

    @Override
    public void handleResponse(InetAddress address, EchoPacket response) {
    	Map<String, String> mdc = getCopyOfContextMap();
        try {
            setContextMap(m_mdc);
            m_cb.handleResponse(address, response);
        } finally {
            setContextMap(mdc);
        }
    }

    @Override
    public void handleTimeout(InetAddress address, EchoPacket request) {
    	Map<String, String> mdc = getCopyOfContextMap();
        try {
            setContextMap(m_mdc);
            m_cb.handleTimeout(address, request);
        } finally {
            setContextMap(mdc);
        }
    }
}