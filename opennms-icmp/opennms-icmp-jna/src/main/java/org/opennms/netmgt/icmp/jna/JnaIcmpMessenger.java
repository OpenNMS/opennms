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
package org.opennms.netmgt.icmp.jna;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.IcmpMessengerIOException;
import org.opennms.core.tracker.Messenger;
import org.opennms.core.tracker.ReplyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author brozow
 */
public class JnaIcmpMessenger implements Messenger<JnaPingRequest, JnaPingReply>, PingReplyListener {
	
	
	private static final Logger LOG = LoggerFactory
			.getLogger(JnaIcmpMessenger.class);
	
	private V4Pinger m_v4;
	private V6Pinger m_v6;
    private ReplyHandler<JnaPingReply> m_callback = null;

	public JnaIcmpMessenger(final int pingerId) throws Exception {
	    Throwable error = null;
	    try {
	        m_v4 = new V4Pinger(pingerId);
	        m_v4.addPingReplyListener(this);
	    } catch (final Throwable t) {
	        LOG.debug("Unable to initialize IPv4 Pinger.", t);
	        error = t;
	        m_v4 = null;
	    }
	    
	    try {
	        m_v6 = new V6Pinger(pingerId);
	        m_v6.addPingReplyListener(this);
	    } catch (final Throwable t) {
	        LOG.debug("Unable to initialize IPv6 Pinger.", t);
	        if (error == null) error = t;
	        m_v6 = null;
	    }
	    
	    if (m_v4 == null && m_v6 == null) {
	        final IcmpMessengerIOException exception = new IcmpMessengerIOException("IPv4 and IPv6 are not available.", error);
	        LOG.warn("Unable to initialize JNA ICMP messenger", exception);
	        throw exception;
	    }
	}
	
    public boolean isV4Available() {
        if (m_v4 != null) {
            return true;
        }
        return false;
    }

    public boolean isV6Available() {
        if (m_v6 != null) {
            return true;
        }
        return false;
    }

        @Override
	public void sendRequest(final JnaPingRequest request) {
		request.send(m_v4, m_v6);
	}

        @Override
	public void start(ReplyHandler<JnaPingReply> callback) {
        m_callback = callback;
        if (m_v4 != null) m_v4.start();
        if (m_v6 != null) m_v6.start();
	}

        @Override
	public void onPingReply(final InetAddress address, final EchoPacket packet) {
        m_callback.handleReply(new JnaPingReply(address, packet));
	}

        public void setTrafficClass(int tc) throws IOException {
            if (m_v4 != null) m_v4.getPingSocket().setTrafficClass(tc);
            if (m_v6 != null) m_v6.getPingSocket().setTrafficClass(tc);
        }

        public void setAllowFragmentation(boolean allow) throws IOException {
            if (m_v4 != null) m_v4.getPingSocket().allowFragmentation(allow);
            if (m_v6 != null) m_v6.getPingSocket().allowFragmentation(allow);
        }

}
