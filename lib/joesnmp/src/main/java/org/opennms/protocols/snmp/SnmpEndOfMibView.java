/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2004, 2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


//
// Log:
//
//	5/15/00 - Weave
//		Added the toString() method
//

package org.opennms.protocols.snmp;

/**
 * The SnmpEndOfMibView object is typically returned by an SNMPv2 agent when
 * there is no lexagraphically next object identifier in its tables. The object
 * is an SNMPv2 error condition. This condition can be returned to a manager on
 * a variable by variable basis.
 * 
 * @see SnmpVarBind
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version 1.1.1.1
 * 
 */
public class SnmpEndOfMibView extends SnmpV2Error {
    /**
     * Required for version control on serialization support.
     * 
     */
    static final long serialVersionUID = 1186449358464703772L;

    /**
     * The ASN.1 value that defines this variable.
     * 
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_ENDOFMIBVIEW;

    /**
     * The default class construtor.
     */
    public SnmpEndOfMibView() {
        super();
    }

    /**
     * The class copy constructor.
     * 
     * @param second
     *            The object to copy into self.
     */
    public SnmpEndOfMibView(SnmpEndOfMibView second) {
        super(second);
    }

    /**
     * Returns the ASN.1 type for this particular object.
     * 
     * @return ASN.1 identifier
     * 
     */
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Returns a duplicate object of self.
     * 
     * @return A duplicate of self
     */
    public SnmpSyntax duplicate() {
        return new SnmpEndOfMibView(this);
    }

    /**
     * Returns a duplicate object of self.
     * 
     * @return A duplicate of self
     */
    public Object clone() {
        return new SnmpEndOfMibView(this);
    }

    /**
     * Returns the string representation of the object.
     * 
     */
    public String toString() {
        return "SNMP End-of-MIB-View";
    }

}
