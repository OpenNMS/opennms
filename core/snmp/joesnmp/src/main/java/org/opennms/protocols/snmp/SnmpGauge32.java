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
 * Defines a SNMPv1 32-bit gauge object. The object is a 32-bit unsigned value
 * that may increase or decrease but does not wrap as a SnmpCounter32.
 * 
 * The object inherits and uses most of the methods defined by the SnmpUInt32
 * class. This class does not define any specific data, but is instead used to
 * override the ASN.1 type of the base class.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public class SnmpGauge32 extends SnmpUInt32 {
    /**
     * for serialzation support
     */
    static final long serialVersionUID = 6399350821456655314L;

    /**
     * Defines the ASN.1 type for this object.
     * 
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_GAUGE32;

    /**
     * Constructs the default counter object. The initial value is defined by
     * the super class default constructor
     */
    public SnmpGauge32() {
        super();
    }

    /**
     * Constructs the object with the specified value.
     * 
     * @param value
     *            The default value for the object.
     */
    public SnmpGauge32(long value) {
        super(value);
    }

    /**
     * Constructs the object with the specified value.
     * 
     * @param value
     *            The default value for the object.
     * 
     */
    public SnmpGauge32(Long value) {
        super(value);
    }

    /**
     * Constructs a new object with the same value as the passed object.
     * 
     * @param second
     *            The object to recover values from.
     * 
     */
    public SnmpGauge32(SnmpGauge32 second) {
        super(second);
    }

    /**
     * Constructs a new object with the value constained in the SnmpUInt32
     * object.
     * 
     * @param uint32
     *            The SnmpUInt32 object to copy.
     * 
     */
    public SnmpGauge32(SnmpUInt32 uint32) {
        super(uint32);
    }

    /**
     * <p>
     * Simple class constructor that is used to create an initialize the new
     * instance with the unsigned value decoded from the passed String argument.
     * If the decoded argument is malformed, null, or evaluates to a negative
     * value then an exception is generated.
     * </p>
     * 
     * @param value
     *            The string encoded value.
     * 
     * @throws java.lang.NumberFormatException
     *             Thrown if the passed value is malformed and cannot be parsed.
     * @throws java.lang.IllegalArgumentException
     *             Throws if the passed value evaluates to a negative value.
     * @throws java.lang.NullPointerException
     *             Throws if the passed value is a null reference.
     */
    public SnmpGauge32(String value) {
        super(value);
    }

    /**
     * Returns the ASN.1 type specific to this object.
     * 
     * @return The ASN.1 value for this object.
     */
    @Override
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Creates a new object that is a duplicate of the current object.
     * 
     * @return The newly created duplicate object.
     * 
     */
    @Override
    public SnmpSyntax duplicate() {
        return new SnmpGauge32(this);
    }

    /**
     * Creates a new object that is a duplicate of the current object.
     * 
     * @return The newly created duplicate object.
     * 
     */
    @Override
    public Object clone() {
        return new SnmpGauge32(this);
    }

}
