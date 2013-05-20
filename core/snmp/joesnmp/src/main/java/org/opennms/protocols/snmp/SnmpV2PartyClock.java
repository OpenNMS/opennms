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

import org.opennms.protocols.snmp.asn1.ASN1;

//
// this class exist because of some undefined type out of
// CMU that Linux supports!
//

/**
 * Defines an SNMPv2 Party Clock. The Party Clock is currently Obsolete, but
 * included for backwards compatability
 * 
 * @deprecated Obsoleted in RFC 1902.
 * 
 * @author Brian Weaver <weave@oculan.com>
 * @version 1.1.1.1
 */
public class SnmpV2PartyClock extends SnmpUInt32 {
    /**
     * Used to define the serialization formation
     */
    static final long serialVersionUID = -1875039304592596058L;

    /**
     * The ASN.1 type for this object
     */
    public static final byte ASNTYPE = (ASN1.APPLICATION | 0x07);

    /**
     * The class constructor. Constructs a default object with the default
     * value. See the super class constructor for more infomation
     */
    public SnmpV2PartyClock() {
        super();
    }

    /**
     * Creates a SNMPv2 Party Clock with the specific value.
     * 
     * @param value
     *            The unsigned value for the party clock
     * 
     */
    public SnmpV2PartyClock(long value) {
        super(value);
    }

    /**
     * Creates a SNMPv2 Party Clock with the specific value.
     * 
     * @param value
     *            The unsigned value for the party clock
     * 
     */
    public SnmpV2PartyClock(Long value) {
        super(value);
    }

    /**
     * Class copy constructor. Constructs a duplicate party clock.
     * 
     * @param second
     *            The party clock to duplicate
     * 
     */
    public SnmpV2PartyClock(SnmpV2PartyClock second) {
        super(second);
    }

    /**
     * Constructs a SNMPv2 Party Clock with the specified value.
     * 
     * @param uint32
     *            The super class value to initialize self with.
     * 
     */
    public SnmpV2PartyClock(SnmpUInt32 uint32) {
        super(uint32);
    }

    /**
     * Used to retreive the ASN.1 value for this object.
     * 
     * @return The ASN.1 type.
     * 
     */
    @Override
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Creates and returns a duplicate object of self.
     * 
     * @return A newly created copy of self.
     * 
     */
    @Override
    public SnmpSyntax duplicate() {
        return new SnmpV2PartyClock(this);
    }

    /**
     * Creates and returns a duplicate object of self.
     * 
     * @return A newly created copy of self.
     * 
     */
    @Override
    public Object clone() {
        return new SnmpV2PartyClock(this);
    }

    /**
     * Returns the string representation of the object.
     * 
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        long time = getValue();
        long tmp = 0;
        if ((tmp = (time / (24 * 3600 * 100))) > 0) {
            buf.append(tmp).append("d ");
            time = time % (24 * 3600 * 100);
        } else
            buf.append("0d ");

        if ((tmp = time / (3600 * 100)) > 0) {
            buf.append(tmp).append("h ");
            time = time % (3600 * 100);
        } else
            buf.append("0h ");

        if ((tmp = time / 6000) > 0) {
            buf.append(tmp).append("m ");
            time = time % 6000;
        } else
            buf.append("0m ");

        if ((tmp = time / 100) > 0) {
            buf.append(tmp).append("s ");
            time = time % 100;
        } else
            buf.append("0s ");

        buf.append(tmp * 10).append("ms");

        return buf.toString();

    }
}
