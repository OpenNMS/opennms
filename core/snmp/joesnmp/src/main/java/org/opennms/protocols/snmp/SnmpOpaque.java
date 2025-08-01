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
 * The SnmpOpaque class is an extension of the octet string class and is used to
 * pass opaque data. Opaque data is information that isn't interperted by the
 * manager in general.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
 */
public class SnmpOpaque extends SnmpOctetString {
    /**
     * Required for version control of serialzation format.
     */
    static final long serialVersionUID = -6031084829130590165L;

    /**
     * The ASN.1 type for this class.
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_OPAQUE;

    /**
     * The default constructor for this class.
     * 
     */
    public SnmpOpaque() {
        super();
    }

    /**
     * Constructs an opaque object with the passed data.
     * 
     * @param data
     *            The opaque data.
     * 
     */
    public SnmpOpaque(byte[] data) {
        super(data);
    }

    /**
     * Constructs an object that is a duplicate of the passed object.
     * 
     * @param second
     *            The object to be duplicated.
     * 
     */
    public SnmpOpaque(SnmpOpaque second) {
        super(second);
    }

    /**
     * Constructs an object that is a duplicate of the passed object.
     * 
     * @param second
     *            The object to be duplicated.
     * 
     */
    public SnmpOpaque(SnmpOctetString second) {
        super(second);
    }

    /**
     * Returns the defined ASN.1 type identifier.
     * 
     * @return The ASN.1 identifier.
     * 
     */
    @Override
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Returns a duplicate of the current object.
     * 
     * @return A duplicate of self
     * 
     */
    @Override
    public SnmpSyntax duplicate() {
        return new SnmpOpaque(this);
    }

    /**
     * Returns a duplicate of the current object.
     * 
     * @return A duplicate of self
     * 
     */
    @Override
    public Object clone() {
        return new SnmpOpaque(this);
    }

    /**
     * Returns a string representation of the object.
     * 
     */
    @Override
    public String toString() {
        //
        // format the string for hex
        //
    	final byte[] data = getString();
    	final StringBuilder b = new StringBuilder();
        // b.append("SNMP Opaque [length = " + data.length + ", fmt = HEX] =
        // [");
        for (int i = 0; i < data.length; ++i) {
            int x = (int) data[i] & 0xff;
            if (x < 16)
                b.append('0');
            b.append(Integer.toString(x, 16).toUpperCase());

            if (i + 1 < data.length)
                b.append(' ');
        }
        // b.append(']');
        return b.toString();
    }
}
