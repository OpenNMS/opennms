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
// SnmpPduBulk.java,v 1.1.1.1 2001/11/11 17:27:22 ben Exp
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
 * This class defines the SNMPv2 GetBulk request sent from the management
 * platform to the agent. The Get Bulk request is designed to minimize the
 * number of message exchanges to get a large amount of information.
 * 
 * The Get Bulk works in the same way as multiple Get Next requests would work.
 * It returns a set of lexicograpical successors that are selected.
 * 
 * For more information on the use of a GetBulk request see [Stallings99] page
 * 378-383.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version 1.1.1.1
 */
public class SnmpPduBulk extends SnmpPduPacket {
    /**
     * Constructs a default get bulk request.
     */
    public SnmpPduBulk() {
        super(SnmpPduPacket.GETBULK);
    }

    /**
     * Constructs a duplicate get bulk request that is an identical copy of the
     * passed object.
     * 
     * @param second
     *            The object to copy.
     * 
     */
    public SnmpPduBulk(SnmpPduBulk second) {
        super(second);
    }

    /**
     * Constructs a get bulk request with the specified variables,
     * non-repeaters, and maximum repititions.
     * 
     * @param nonRepeaters
     *            The number of non-repeating variables
     * @param maxRepititions
     *            The number of "repeating" variables to get
     * @param vars
     *            The SNMP variables
     */
    public SnmpPduBulk(int nonRepeaters, int maxRepititions, SnmpVarBind[] vars) {
        super(SnmpPduPacket.GETBULK, vars);
        super.m_errStatus = nonRepeaters;
        super.m_errIndex = maxRepititions;
    }

    /**
     * Returns the number of non-repeating elements
     * 
     * @return The non-repeating value
     */
    public int getNonRepeaters() {
        return super.m_errStatus;
    }

    /**
     * Sets the number of non-repeating elements in this PDU.
     * 
     * @param nonreps
     *            The number of non-repeaters
     * 
     */
    public void setNonRepeaters(int nonreps) {
        super.m_errStatus = nonreps;
    }

    /**
     * Used to retreive the number of reptitions to get for the repeating
     * variables.
     * 
     * @return The number of maximum reptitions.
     * 
     */
    public int getMaxRepititions() {
        return super.m_errIndex;
    }

    /**
     * Used to set the number of maximum repititions to be collected by the PDU.
     * 
     * @param maxreps
     *            The maximum number of repititions
     * 
     */
    public void setMaxRepititions(int maxreps) {
        super.m_errIndex = maxreps;
    }

    /**
     * Creates a new duplicate object of self that shares no references with the
     * original PDU.
     * 
     * @return A newly created copy of self.
     */
    public SnmpSyntax duplicate() {
        return new SnmpPduBulk(this);
    }

    /**
     * Creates a new duplicate object of self that shares no references with the
     * original PDU.
     * 
     * @return A newly created copy of self.
     */
    public Object clone() {
        return new SnmpPduBulk(this);
    }
}
