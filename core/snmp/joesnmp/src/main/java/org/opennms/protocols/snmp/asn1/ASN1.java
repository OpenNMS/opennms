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
package org.opennms.protocols.snmp.asn1;

/**
 * Public ASN.1 definitions. See "SNMPv1, SNMPv2, SNMPv3 and RMON 1 and 2, 3rd
 * Ed." by William Stallings, Published by Addision Wesley for more information.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 */
public abstract class ASN1 {
    /**
     * Basic data type representing TRUE or FALSE.
     */
    public static final byte BOOLEAN = (byte) 0x01;

    /**
     * Positive and negative whole numbers, including zero.
     */
    public static final byte INTEGER = (byte) 0x02;

    /**
     * A sequence of zero or more bits
     */
    public static final byte BITSTRING = (byte) 0x03;

    /**
     * A sequence of zero or more octets. An octet is an 8-bit value.
     */
    public static final byte OCTETSTRING = (byte) 0x04;

    /**
     * The single value NULL. Commonly used value where several alternatives are
     * possible but none apply.
     */
    public static final byte NULL = (byte) 0x05;

    /**
     * The set of values associated with information objects allocated by the
     * standard.
     */
    public static final byte OBJECTID = (byte) 0x06;

    /**
     * Defined by referencing a fixed, ordered list of types. Each value is an
     * ordered list of values, one from each component type.
     */
    public static final byte SEQUENCE = (byte) 0x10;

    /**
     * Defined by referencing a fixed, unordered list of types, some of which
     * may be declared optional. Each value is an unordered list of values, one
     * from each component type.
     */
    public static final byte SET = (byte) 0x11;

    /**
     * Generally useful, application-independant types and construction
     * mechanisms.
     */
    public static final byte UNIVERSAL = (byte) 0x00;

    /**
     * Relevant to a particular application. These are defined in standards
     * other than ASN.1.
     */
    public static final byte APPLICATION = (byte) 0x40;

    /**
     * Also relevant to a particular application, but limited by context
     */
    public static final byte CONTEXT = (byte) 0x80;

    /**
     * These are types not covered by any standard but instead defined by users.
     */
    public static final byte PRIVATE = (byte) 0xC0;

    /**
     * A primitive data object.
     */
    public static final byte PRIMITIVE = (byte) 0x00;

    /**
     * A constructed data object such as a set or sequence.
     */
    public static final byte CONSTRUCTOR = (byte) 0x20;
}
