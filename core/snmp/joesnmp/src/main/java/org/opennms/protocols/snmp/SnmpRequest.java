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
package org.opennms.protocols.snmp;

import java.io.IOException;

import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * <P>
 * Implements a way to track outstanding SNMP pdu request. The object tracks the
 * pdu, it's sending parameters, the number of times send, and the last time
 * sent.
 * </P>
 * 
 * <P>
 * The SnmpRequest implements the Runnable interface and its run method is
 * invoked by the corresponding SnmpTimer when the request effectively expires.
 * The member m_expires refers the the expiretion of the request and pdu, not to
 * the failed response.
 * </P>
 * 
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HERF="http://www.opennms.org">OpenNMS </A>
 * 
 * @version 1.1.1.1
 * 
 */
class SnmpRequest implements Runnable {
    /**
     * The session that created this request
     * 
     */
    SnmpSession m_session; // the seesion who sent it

    /**
     * The pdu transemited to the SnmpPeer.
     * 
     */
    SnmpSyntax m_pdu; // the PDU

    /**
     * The SnmpHandler to invoke for this request
     * 
     */
    SnmpHandler m_handler;

    /**
     * The number of times this request has been transmitted.
     * 
     */
    int m_timesSent; // number of times sent

    /**
     * When set the request as a whole has expired and should no longer be
     * process by any methods. It is effectively waiting for garbage collection.
     * 
     */
    boolean m_expired; // when set DO NOT PROCESS

    /**
     * Used to create an SnmpRequest object. This constructor sets the fields to
     * their default value along with the passed parameters.
     * 
     * @param session
     *            The sending session
     * @param pdu
     *            The pdu to send to the remote
     * @param handler
     *            The handler to invoke!
     * 
     */
    SnmpRequest(SnmpSession session, SnmpPduPacket pdu, SnmpHandler handler) {
        super();
        m_session = session;
        m_pdu = pdu;
        m_expired = false;
        m_timesSent = 0;
        m_handler = handler;
    }

    /**
     * Used to create an SnmpRequest object. This constructor sets the fields to
     * their default value along with the passed parameters.
     * 
     * @param session
     *            The sending session
     * @param pdu
     *            The pdu to send to the remote
     * @param handler
     *            The handler to invoke!
     * 
     */
    SnmpRequest(SnmpSession session, SnmpPduTrap pdu, SnmpHandler handler) {
        super();
        m_session = session;
        m_pdu = pdu;
        m_expired = false;
        m_timesSent = 0;
        m_handler = handler;
    }

    /**
     * Used to process the timeout of an SnmpRequest. The method is invoked by
     * the session timer object. If the request has "expired" then no processing
     * occurs. If the number of retries have exceeded the session parameters
     * then the SnmpHandler's snmpTimeoutError() method is invoked. If an error
     * occurs transmiting the pdu then the snmpInternalError() method is
     * invoked.
     * 
     * @see SnmpHandler
     * @see SnmpPduRequest
     * @see SnmpSession
     */
    @Override
    public void run() {
        if (m_expired)
            return;

        if (m_timesSent <= m_session.getPeer().getRetries()) {
            m_timesSent++;
            try {
                //
                // An SNMP Trap command should only be sent ONCE!
                //
                m_session.transmit(this);
                if (m_pdu instanceof SnmpPduPacket) {
                    if (((SnmpPduPacket) m_pdu).getCommand() != SnmpPduPacket.V2TRAP)
                        m_session.getTimer().schedule(this, m_session.getPeer().getTimeout());
                    else
                        m_expired = true;
                } else if (m_pdu instanceof SnmpPduTrap) {
                    m_expired = true;
                }
            } catch (IOException err) {
                m_expired = true;
                try {
                    m_handler.snmpInternalError(m_session, SnmpSession.ERROR_IOEXCEPTION, m_pdu);
                } catch (Exception e) {
                    // ignore
                }
            } catch (SnmpPduEncodingException err) {
                m_expired = true;
                try {
                    m_handler.snmpInternalError(m_session, SnmpSession.ERROR_ENCODING, m_pdu);
                } catch (Exception e) {
                    // ignore
                }
            } catch (AsnEncodingException err) {
                m_expired = true;
                try {
                    m_handler.snmpInternalError(m_session, SnmpSession.ERROR_ENCODING, m_pdu);
                } catch (Exception e) {
                    // ignore
                }
            }
        } else {
            m_expired = true;
            try {
                m_handler.snmpTimeoutError(m_session, m_pdu);
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
