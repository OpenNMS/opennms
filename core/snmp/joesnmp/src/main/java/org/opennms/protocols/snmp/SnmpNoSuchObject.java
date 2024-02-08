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
 * The SnmpNoSuchObject object is typically returned by an SNMPv2 agent when
 * there is no matching object identifier for the agent. The object is an SNMPv2
 * error condition. This condition can be returned to a manager on a variable by
 * variable basis.
 * 
 * @see SnmpVarBind
 * 
 * @author Brian Weaver
 * 
 */
public class SnmpNoSuchObject extends SnmpV2Error {
    /**
     * Defines the serialization format version.
     * 
     */
    static final long serialVersionUID = -6750389210834760320L;

    /**
     * The ASN.1 value that defines this variable.
     * 
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_NOSUCHOBJECT;

    /**
     * The default class construtor.
     */
    public SnmpNoSuchObject() {
        super();
    }

    /**
     * The class copy constructor.
     */
    public SnmpNoSuchObject(SnmpNoSuchObject second) {
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
        return new SnmpNoSuchObject(this);
    }

    /**
     * Returns a duplicate object of self.
     * 
     * @return A duplicate of self
     */
    @Override
    public Object clone() {
        return new SnmpNoSuchObject(this);
    }

    /**
     * Returns the string representation of the object.
     * 
     */
    @Override
    public String toString() {
        return "SNMP No-Such-Object";
    }
}
