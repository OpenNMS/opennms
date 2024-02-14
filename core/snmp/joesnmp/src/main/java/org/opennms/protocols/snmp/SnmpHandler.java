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
 * <P>
 * The SnmpHandler interface is implemented by an object that wishes to receive
 * callbacks when a SNMP protocol data unit is received from an agent. In
 * addition, if an internal error occurs or an agent fails to respond then the
 * object must handle those error conditions.
 * <P>
 * 
 * <P>
 * For error conditions the pdu is recast to an SnmpSyntax object. This is
 * mainly due to the fact that the SnmpPduTrap is not derived from
 * SnmpPduPacket. Implementations of the handler class can use <EM>instanceof
 * </EM> to determine the type of PDU involved in the error.
 * </P>
 * 
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public interface SnmpHandler {
    /**
     * <P>
     * This method is invoked when a pdu is successfully returned from the peer
     * agent. The command argument is recovered from the received pdu.
     * </P>
     * 
     * @param session
     *            The SNMP session
     * @param command
     *            The PDU command
     * @param pdu
     *            The SNMP pdu
     * 
     */
    void snmpReceivedPdu(SnmpSession session, int command, SnmpPduPacket pdu);

    /**
     * <P>
     * This method is invoked when an internal error occurs for the session. To
     * determine the exact error the err parameter should be compared with all
     * the error conditions defined in the SnmpSession class.
     * </P>
     * 
     * @param session
     *            The SNMP session in question
     * @param err
     *            The error that occured
     * @param pdu
     *            The PDU object that caused the error
     * 
     */
    void snmpInternalError(SnmpSession session, int err, SnmpSyntax pdu);

    /**
     * <P>
     * This method is invoked when an agent fails to respond in the required
     * time. This method will only be invoked if the total retries exceed the
     * number defined by the session.
     * </P>
     * 
     * @param session
     *            The SNMP Session
     * @param pdu
     *            The PDU object that timed out
     * 
     */
    void snmpTimeoutError(SnmpSession session, SnmpSyntax pdu);
}
