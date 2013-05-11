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
 * The SnmpPduRequest defines the SNMPv1 and SNMPv2 Protocol Data Unit (PDU) for
 * certain message types. The types include: GetRequest, GetNextRequest,
 * SetRequest, SNMPv2-Trap, InformRequest, and Response.
 * 
 * By default the class is constructed as a SNMP GetRequest, but can be defined
 * to any of the accepted types. For more information see [Stallings99] page
 * 368.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public class SnmpPduRequest extends SnmpPduPacket {
    /**
     * Default class constructor. By default the request is a SNMP GetRequest.
     */
    public SnmpPduRequest() {
        super(SnmpPduPacket.GET);
    }

    /**
     * Class copy constructor. Constructs a new object that is an identical to
     * the passed object.
     * 
     * @param second
     *            The object to make a duplicate of.
     * 
     */
    public SnmpPduRequest(SnmpPduRequest second) {
        super(second);
    }

    /**
     * Constructs a new PDU Request with the specified command.
     * 
     * @param command
     *            The type of PDU to construct
     * 
     * @see SnmpPduPacket#GET
     * @see SnmpPduPacket#GETNEXT
     * @see SnmpPduPacket#SET
     * @see SnmpPduPacket#RESPONSE
     * @see SnmpPduPacket#INFORM
     * @see SnmpPduPacket#V2TRAP
     * @see SnmpPduPacket#REPORT
     */
    public SnmpPduRequest(int command) {
        super(command);
    }

    /**
     * Constructs the PDU with the specified command and the passed variables.
     * 
     * @param command
     *            The type of PDU to construct
     * @param vars
     *            The SNMP variables for the PDU.
     * 
     * @see SnmpPduPacket#GET
     * @see SnmpPduPacket#GETNEXT
     * @see SnmpPduPacket#SET
     * @see SnmpPduPacket#RESPONSE
     * @see SnmpPduPacket#INFORM
     * @see SnmpPduPacket#V2TRAP
     * @see SnmpPduPacket#REPORT
     */
    public SnmpPduRequest(int command, SnmpVarBind[] vars) {
        super(command, vars);
    }

    /**
     * Returns the error status for the request. This is only value on RESPONSE
     * pdu's. Otherwise the value should be equal to zero. For information on
     * error conditions see the SnmpPduPacket class.
     * 
     * @return The error status of the pdu
     * 
     * @see SnmpPduPacket
     */
    public int getErrorStatus() {
        return super.m_errStatus;
    }

    /**
     * Used to set the value of the error status member. This should normally be
     * equal to zero, except for RESPONSE pdu's
     * 
     * @param status
     *            The new error status for the pdu.
     */
    public void setErrorStatus(int status) {
        super.m_errStatus = status;
    }

    /**
     * Returns the index of the variable in error if the error status is
     * non-zero. The index is base one, not zero. Thus an error index equal to
     * one is the first variable in the PDU. An index equal to zero is not
     * valid.
     * 
     * @return The index of the bad variable.
     */
    public int getErrorIndex() {
        return super.m_errIndex;
    }

    /**
     * Sets the current error index in the PDU. the index must be in the range
     * (0..#variables] unless there is no error. If there is no error then the
     * index should be equal to zero.
     * 
     * @param ndx
     *            The new error index.
     */
    public void setErrorIndex(int ndx) {
        super.m_errIndex = ndx;
    }

    /**
     * Fixes a PDU in error. If the error index and error status is non-zero
     * then the variable in error is removed from the PDU. Once the variable is
     * removed the error index and error status are reset to zero.
     * 
     */
    public void fix() {
        if (super.m_errStatus != 0 && super.m_errIndex != 0) {
            removeVarBindAt(super.m_errIndex - 1);
            super.m_errStatus = 0;
            super.m_errIndex = 0;
        }
    }

    /**
     * Used to get a duplicate of self. The duplicate is identical to self but
     * shares no common data.
     * 
     * @return A newly created copy of self.
     */
    @Override
    public SnmpSyntax duplicate() {
        return new SnmpPduRequest(this);
    }

    /**
     * Used to get a duplicate of self. The duplicate is identical to self but
     * shares no common data.
     * 
     * @return A newly created copy of self.
     */
    @Override
    public Object clone() {
        return new SnmpPduRequest(this);
    }
}
