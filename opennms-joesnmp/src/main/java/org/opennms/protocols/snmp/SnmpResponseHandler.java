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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.protocols.snmp;


/**
 * The SNMP handler used to receive responses from individual sessions. When a
 * response is received that matches a system object identifier request the
 * session is notified.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class SnmpResponseHandler implements SnmpHandler {
    
    boolean m_error = true;
    
    /**
     * The returned object identifier
     */
    private SnmpPduPacket m_response = null;

    /**
     * {@inheritDoc}
     *
     * The method that handles a returned packet from the remote agent.
     */
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
     * {@inheritDoc}
     *
     * This method is invoked when an internal error occurs on the SNMP session.
     */
    public void snmpInternalError(SnmpSession sess, int err, SnmpSyntax obj) {
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     *
     * This method is invoked when the session fails to receive a response to a
     * particular packet.
     */
    public void snmpTimeoutError(SnmpSession sess, SnmpSyntax pkt) {
        synchronized (this) {
            notifyAll();
        }
    }
    
    /**
     * <p>getResponse</p>
     *
     * @return a {@link org.opennms.protocols.snmp.SnmpPduPacket} object.
     */
    public SnmpPduPacket getResponse() {
        return m_response;
    }

    /**
     * Returns the recovered snmp system object identifier, if any. If one was
     * not returned then a null value is returned to the caller.
     *
     * @return a {@link org.opennms.protocols.snmp.SnmpVarBind} object.
     */
    public SnmpVarBind getFirstResponseVarBind() {
        return getResponseVarBind(0);
    }
    
    /**
     * <p>getFirstResponseValue</p>
     *
     * @return a {@link org.opennms.protocols.snmp.SnmpSyntax} object.
     */
    public SnmpSyntax getFirstResponseValue() {
        return getResponseValue(0);
    }
    
    /**
     * <p>getFirstResponseString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFirstResponseString() {
        return getResponseString(0);
    }
    
    /**
     * <p>getResponseValue</p>
     *
     * @param index a int.
     * @return a {@link org.opennms.protocols.snmp.SnmpSyntax} object.
     */
    public SnmpSyntax getResponseValue(int index) {
        return getResponseVarBind(index).getValue();
    }
    
    /**
     * <p>getResponseString</p>
     *
     * @param index a int.
     * @return a {@link java.lang.String} object.
     */
    public String getResponseString(int index) {
        SnmpSyntax val = getResponseValue(index);
        return (val == null ? null : val.toString());
    }
    
    /**
     * <p>getResponseVarBind</p>
     *
     * @param index a int.
     * @return a {@link org.opennms.protocols.snmp.SnmpVarBind} object.
     */
    public SnmpVarBind getResponseVarBind(int index) {
        return (getResponse() == null ? null : getResponse().getVarBindAt(index));
    }
    
    /**
     * <p>getResponseVarBindCount</p>
     *
     * @return a int.
     */
    public int getResponseVarBindCount() {
        return (getResponse() == null ? 0 : getResponse().getLength());
    }
}
