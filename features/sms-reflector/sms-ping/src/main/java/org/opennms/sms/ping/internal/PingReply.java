//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Mar 05: Cleaned up some ICMP related code.
// 2002 Nov 13: Added response time stats for ICMP requests.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//
// Reply.java,v 1.1.1.1 2001/11/11 17:34:37 ben Exp

package org.opennms.sms.ping.internal;

import org.opennms.protocols.rt.ResponseWithId;
import org.opennms.sms.ping.PingRequestId;
import org.smslib.InboundMessage;



/**
 * <p>
 * This class is use to encapsulate an ICMP reply that conforms to the
 * {@link ICMPEchoPacket packet}class. The reply must be of type ICMP Echo Reply and be
 * the correct length.
 * </p>
 *
 * <p>
 * When constructed by the <code>create</code> method the returned reply
 * encapsulates the sender's address and the received packet as final,
 * non-mutable values for the instance.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @version $Id: $
 */
public final class PingReply implements ResponseWithId<PingRequestId> {
    /**
     * The sender's address.
     */
    private final PingRequestId m_requestId;

    /**
     * The received packet.
     */
    private final InboundMessage m_packet;

	private long m_receiveTimestamp;

    /**
     * Constructs a new reply with the packet as the contents
     * of the reply.
     *
     * @param pkt
     *            The received packet.
     * @param receiveTime a long.
     */
    public PingReply(InboundMessage pkt, long receiveTime) {
        m_packet = pkt;
        m_requestId = new PingRequestId(pkt.getOriginator());
        m_receiveTimestamp = receiveTime;
    }

    /**
     * <p>getRequestId</p>
     *
     * @return a {@link org.opennms.sms.ping.PingRequestId} object.
     */
    public PingRequestId getRequestId() {
        return m_requestId;
    }
    
    /**
     * <p>getPacket</p>
     *
     * @return a {@link org.smslib.InboundMessage} object.
     */
    public InboundMessage getPacket() {
        return m_packet;
    }
    
    /**
     * <p>setReceiveTimestamp</p>
     *
     * @param millis a long.
     */
    public void setReceiveTimestamp(long millis){
    	m_receiveTimestamp = millis;
    }
    
    /**
     * <p>getReceiveTimestamp</p>
     *
     * @return a long.
     */
    public long getReceiveTimestamp(){
    	return m_receiveTimestamp;
    }

}
