/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.snmp;


/**
 * The SNMP handler used to receive responses from individual sessions. When a
 * response is received that matches a system object identifier request the
 * session is notified.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class SnmpResponseHandler implements SnmpHandler {
    
    boolean m_error = true;
    
    /**
     * The returned object identifier
     */
    private SnmpPduPacket m_response = null;

    /**
     * The method that handles a returned packet from the remote agent.
     * 
     * @param sess
     *            The SNMP session that received the result.
     * @param command
     *            The SNMP command.
     * @param pkt
     *            The SNMP packet that was received.
     */
    @Override
    public void snmpReceivedPdu(SnmpSession sess, int command, SnmpPduPacket pkt) {
        if (pkt.getCommand() == SnmpPduPacket.RESPONSE) {
            if (((SnmpPduRequest) pkt).getErrorStatus() == SnmpPduPacket.ErrNoError) {
                m_response = pkt;
            }

            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * This method is invoked when an internal error occurs on the SNMP session.
     * 
     * @param sess
     *            The SNMP session that received the result.
     * @param err
     *            The err.
     * @param obj
     *            The syntax object.
     */
    @Override
    public void snmpInternalError(SnmpSession sess, int err, SnmpSyntax obj) {
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * This method is invoked when the session fails to receive a response to a
     * particular packet.
     * 
     * @param sess
     *            The SNMP session that received the result.
     * @param pkt
     *            The SNMP packet that was received.
     */
    @Override
    public void snmpTimeoutError(SnmpSession sess, SnmpSyntax pkt) {
        synchronized (this) {
            notifyAll();
        }
    }
    
    public SnmpPduPacket getResponse() {
        return m_response;
    }

    /**
     * Returns the recovered SNMP system object identifier, if any. If one was
     * not returned then a null value is returned to the caller.
     * 
     */
    public SnmpVarBind getFirstResponseVarBind() {
        return getResponseVarBind(0);
    }
    
    public SnmpSyntax getFirstResponseValue() {
        return getResponseValue(0);
    }
    
    public String getFirstResponseString() {
        return getResponseString(0);
    }
    
    public SnmpSyntax getResponseValue(int index) {
        return getResponseVarBind(index).getValue();
    }
    
    public String getResponseString(int index) {
        SnmpSyntax val = getResponseValue(index);
        return (val == null ? null : val.toString());
    }
    
    public SnmpVarBind getResponseVarBind(int index) {
        return (getResponse() == null ? null : getResponse().getVarBindAt(index));
    }
    
    public int getResponseVarBindCount() {
        return (getResponse() == null ? 0 : getResponse().getLength());
    }
}
