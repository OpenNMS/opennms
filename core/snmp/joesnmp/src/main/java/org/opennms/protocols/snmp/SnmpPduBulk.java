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
    @Override
    public SnmpSyntax duplicate() {
        return new SnmpPduBulk(this);
    }

    /**
     * Creates a new duplicate object of self that shares no references with the
     * original PDU.
     * 
     * @return A newly created copy of self.
     */
    @Override
    public Object clone() {
        return new SnmpPduBulk(this);
    }
}
