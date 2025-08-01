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
    
    public SnmpSyntax getResponseValue(final int index) {
        final var vb = getResponseVarBind(index);
        return vb == null ? null : vb.getValue();
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
