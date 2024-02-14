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
package org.opennms.netmgt.provision.service.snmp;

import org.opennms.netmgt.snmp.AbstractSnmpStore;
import org.opennms.netmgt.snmp.NamedSnmpVar;

/**
 * <P>
 * This object contains a list of all the elements defined in the MIB-II
 * interface extensions table. An instance object is initialized by calling the
 * constructor and passing in a variable list from an SNMP PDU. The actual data
 * can be recovered via the base class map interface.
 * </P>
 *
 * <P>
 * Once an instance is created and its data set either via the constructor or
 * from the update method, the actual elements can be retrieved using the
 * instance names. The names include: <EM>ifName</EM>,<EM>ifInMulticastPts
 * </EM>,<EM>ifInBroadcastPkts</EM>,<EM>etc al</EM>. The information
 * can also be accessed by using the complete object identifier for the entry.
 * </P>
 *
 * <P>
 * For more information on the individual fields, and to find out their
 * respective object identifiers see RFC1573 from the IETF.
 * </P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 *
 * @see <A HREF="http://www.ietf.org/rfc/rfc1573.txt">RFC1573 </A>
 */
public final class IfXTableEntry extends SnmpTableEntry {
    /**
     * Lookup strings for specific table entries
     */
    public static final String IF_NAME = "ifName";

    /** Constant <code>IF_IN_MCAST_PKTS="ifInMulticastPkts"</code> */
    public static final String IF_IN_MCAST_PKTS = "ifInMulticastPkts";

    /** Constant <code>IF_IN_BCAST_PKTS="ifInBroadcastPkts"</code> */
    public static final String IF_IN_BCAST_PKTS = "ifInBroadcastPkts";

    /** Constant <code>IF_OUT_MCAST_PKTS="ifOutMulticastPkts"</code> */
    public static final String IF_OUT_MCAST_PKTS = "ifOutMulticastPkts";

    /** Constant <code>IF_OUT_BCAST_PKTS="ifOutBroadcastPkts"</code> */
    public static final String IF_OUT_BCAST_PKTS = "ifOutBroadcastPkts";

    /** Constant <code>IF_LINK_UP_DOWN_TRAP_ENABLE="ifLinkUpDownTrapEnable"</code> */
    public static final String IF_LINK_UP_DOWN_TRAP_ENABLE = "ifLinkUpDownTrapEnable";

    /** Constant <code>IF_HIGH_SPEED="ifHighSpeed"</code> */
    public static final String IF_HIGH_SPEED = "ifHighSpeed";

    /** Constant <code>IF_PROMISCUOUS_MODE="ifPromiscuousMode"</code> */
    public static final String IF_PROMISCUOUS_MODE = "ifPromiscuousMode";

    /** Constant <code>IF_CONNECTOR_PRESENT="ifConnectorPresent"</code> */
    public static final String IF_CONNECTOR_PRESENT = "ifConnectorPresent";

    /** Constant <code>IF_ALIAS="ifAlias"</code> */
    public static final String IF_ALIAS = "ifAlias";

    /** Constant <code>IF_COUNTER_DISCONTINUITY_TIME="ifCounterDiscontinuityTime"</code> */
    public static final String IF_COUNTER_DISCONTINUITY_TIME = "ifCounterDiscontinuityTime";

    // 
    // Special case: Lookup string for ifIndex
    //
    // The interface extension table does not include an ifIndex
    // but in order to provide a convenient method for retrieving
    // the ifName of an interface based on its ifIndex we will
    // use the instance id from the returned ifName object identifier
    // as the ifIndex of the entry. This value will be stored
    // in the map along with the "ifIndex" lookup string as key.
    /** Constant <code>IF_INDEX="AbstractSnmpStore.IFINDEX"</code> */
    public static final String IF_INDEX = AbstractSnmpStore.IFINDEX;

    /**
     * Number of object identifiers making up the interface extensions table
     * 
     * WARNING: This value must be incremented by one for each new object added
     * to the ms_elemList variable
     */
    static int NUM_OIDS = 10;
    
    /** Constant <code>ms_elemList</code> */
    public static NamedSnmpVar[] ms_elemList = null;

    /**
     * <P>
     * Initialize the element list for the class. This is class wide data, but
     * will be used by each instance.
     * </P>
     */
    static {
        ms_elemList = new NamedSnmpVar[NUM_OIDS];
        int ndx = 0;

        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, IF_NAME, ".1.3.6.1.2.1.31.1.1.1.1", 1);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32, IF_IN_MCAST_PKTS, ".1.3.6.1.2.1.31.1.1.1.2", 2);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32, IF_IN_BCAST_PKTS, ".1.3.6.1.2.1.31.1.1.1.3", 3);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32, IF_OUT_MCAST_PKTS, ".1.3.6.1.2.1.31.1.1.1.4", 4);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IF_LINK_UP_DOWN_TRAP_ENABLE, ".1.3.6.1.2.1.31.1.1.1.14", 5);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32, IF_HIGH_SPEED, ".1.3.6.1.2.1.31.1.1.1.15", 6);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IF_PROMISCUOUS_MODE, ".1.3.6.1.2.1.31.1.1.1.16", 7);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IF_CONNECTOR_PRESENT, ".1.3.6.1.2.1.31.1.1.1.17", 8);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, IF_ALIAS, ".1.3.6.1.2.1.31.1.1.1.18", 9);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPTIMETICKS, IF_COUNTER_DISCONTINUITY_TIME, ".1.3.6.1.2.1.31.1.1.1.19", 10);
    }

    /**
     * <P>
     * The TABLE_OID is the object identifier that represents the root of the
     * interface extensions table in the MIB forest.
     * </P>
     */
    public static final String TABLE_OID = ".1.3.6.1.2.1.31.1.1.1"; // start of
                                                                    // table
                                                                    // (GETNEXT)

    /**
     * <P>
     * The class constructor used to initialize the object to its initial state.
     * Although the object's attributes and data can be changed after its
     * created, this constructor will initialize all the variables as per their
     * named varbind in the passed array. This array should have been collected
     * from an SnmpPduRequest that was received from a remote host.
     * </P>
     *
     * @param ifIndex a int.
     */
    public IfXTableEntry(final int ifIndex) {
        super(ms_elemList);
        putIfIndex(ifIndex); 
        
    }

    /**
     * <p>getIfName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfName() {
        return getDisplayString(IfXTableEntry.IF_NAME);
    }
    
    /**
     * <p>getIfAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfAlias() {
        return getDisplayString(IfXTableEntry.IF_ALIAS);
    }
    
}
