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
// SnmpPduRequest.java,v 1.1.1.1 2001/11/11 17:27:22 ben Exp
//
//

//
// RFC 1902: Structure of Management Information for SNMPv2
//
//   PDU ::= 
//    SEQUENCE {
//      request-id   INTEGER32
//      error-status INTEGER
//      error-index  INTEGER
//      Variable Bindings
//    }
//
// BulkPDU ::=
//    SEQUENCE {
//      request-id      INTEGER32
//      non-repeaters   INTEGER
//      max-repetitions INTEGER
//      Variable Bindings
//    }
//

//
// RFC 1157: A Simple Network Management Protocol (SNMP)
//
//   PDU ::= 
//    SEQUENCE {
//      request-id   INTEGER
//      error-status INTEGER
//      error-index  INTEGER
//      Variable Bindings
//    }
//
//   TrapPDU ::=
//    SEQUENCE {
//      enterprise    OBJECTID
//	agent-address NetworkAddress
//      generic-trap  INTEGER
//      specific-trap INTEGER
//      time-stamp    TIMETICKS
//      Variable Bindings
//    }
//

package org.opennms.protocols.snmp;

/**
 * The SnmpPduRequest defines the SNMPv1 and SNMPv2 Protocol Data Unit (PDU) for
 * certian message types. The types include: GetRequest, GetNextRequest,
 * SetRequest, SNMPv2-Trap, InformRequest, and Response.
 * 
 * By default the class is constructed as a SNMP GetRequest, but can be defined
 * to any of the accepted types. For more information see [Stallings99] page
 * 368.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version 1.1.1.1
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
    public SnmpSyntax duplicate() {
        return new SnmpPduRequest(this);
    }

    /**
     * Used to get a duplicate of self. The duplicate is identical to self but
     * shares no common data.
     * 
     * @return A newly created copy of self.
     */
    public Object clone() {
        return new SnmpPduRequest(this);
    }
}
