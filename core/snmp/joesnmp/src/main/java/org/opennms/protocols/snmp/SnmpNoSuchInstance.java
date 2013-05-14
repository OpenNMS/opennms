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
 * The SnmpNoSuchInstance object is typically returned by an SNMPv2 agent when
 * there is no matching object identifier instance in its tables. The object is
 * an SNMPv2 error condition. This condition can be returned to a manager on a
 * variable by variable basis.
 * 
 * @see SnmpVarBind
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
 */
public class SnmpNoSuchInstance extends SnmpV2Error {
    /**
     * Defines the serialization format.
     */
    final static long serialVersionUID = 909796483768805665L;

    /**
     * The ASN.1 value that defines this variable.
     * 
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_NOSUCHINSTANCE;

    /**
     * The default class construtor.
     */
    public SnmpNoSuchInstance() {
        super();
    }

    /**
     * The class copy constructor.
     * 
     * @param second
     *            The object to copy into self.
     */
    public SnmpNoSuchInstance(SnmpNoSuchInstance second) {
        super(second);
    }

    /**
     * Returns the ASN.1 type for this particular object.
     * 
     * @return ASN.1 identifier
     * 
     */
    @Override
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Returns a duplicate object of self.
     * 
     * @return A duplicate of self
     */
    @Override
    public SnmpSyntax duplicate() {
        return new SnmpNoSuchInstance(this);
    }

    /**
     * Returns a duplicate object of self.
     * 
     * @return A duplicate of self
     */
    @Override
    public Object clone() {
        return new SnmpNoSuchInstance(this);
    }

    /**
     * Returns the string representation of the object.
     * 
     */
    @Override
    public String toString() {
        return "SNMP No-Such-Instance";
    }

}
