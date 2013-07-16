/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd.snmp;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * The IfTable uses a SnmpSession to collect the entries in the remote agent's
 * interface table. It implements the SnmpHandler to receive notifications and
 * handle errors associated with the data collection. Data is collected using a
 * series of GETNEXT PDU request to walk multiple parts of the interface table
 * at once. The number of SNMP packets should not exceed the number of interface +
 * 1, assuming no lost packets or error conditions occur.
 * </P>
 *
 * <p>
 * <em>Addition by Jon Whetzel</em>
 * </p>
 * <p>
 * IfTable has an extra class variable for the SNMP version setting. If this is
 * set for SNMPv2, then a GETBULK command will be used for retrieving the
 * necessary data. Otherwise, the method will resort to its previous
 * implementation with GETNEXT commands.
 * </p>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public final class IfTable extends SnmpTable<IfTableEntry> {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(IfTable.class);
    
    /**
     * <P>
     * Constructs an IfTable object that is used to collect the interface
     * elements from the remote agent. Once all the interfaces are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     *
     * @param address TODO
     * @see IfTableEntry
     */
    public IfTable(InetAddress address) {
        super(address, "ifTable", IfTableEntry.ms_elemList);
    }
    
    /** {@inheritDoc} */
    @Override
    protected IfTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new IfTableEntry();
    }


    /**
     * <p>getEntry</p>
     *
     * @param ifIndex a int.
     * @return a {@link org.opennms.netmgt.capsd.snmp.IfTableEntry} object.
     */
    public IfTableEntry getEntry(int ifIndex) {
        for(IfTableEntry entry : this) {
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                return entry;
            }
        }
        
        return null;
    }
    
    /**
     * <p>getOperStatus</p>
     *
     * @param ifIndex a int.
     * @return a int.
     */
    public int getOperStatus(int ifIndex) {
        for(IfTableEntry entry : this) {
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                // found it
                // extract the admin status
                //
                Integer ifStatus = entry.getIfOperStatus();
                if (ifStatus != null)
                    return ifStatus.intValue();
            }
        }
        return -1;
    }
        
    /**
     * <p>getAdminStatus</p>
     *
     * @param ifIndex a int.
     * @return a int.
     */
    public int getAdminStatus(int ifIndex) {
        for(IfTableEntry entry : this) {
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                // found it
                // extract the admin status
                //
                Integer ifStatus = entry.getIfAdminStatus();
                if (ifStatus != null)
                    return ifStatus.intValue();
            }
        }
        return -1;
    }

    /**
     * <p>getIfType</p>
     *
     * @param ifIndex a int.
     * @return a int.
     */
    public int getIfType(int ifIndex) {
        for(IfTableEntry entry : this) {
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                // found it
                // extract the ifType
                //
                Integer ifType = entry.getIfType();
                if (ifType != null)
                    return ifType.intValue();
            }
        }
        return -1;
    }

    /**
     * <p>getIfDescr</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getIfDescr(final int ifIndex) {
        String ifDescr = null;   
        for(IfTableEntry entry : this) {
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                ifDescr = entry.getIfDescr();
            }
        }
        return ifDescr;
    }

    /**
     * <p>getIfSpeed</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.Long} object.
     */
    public Long getIfSpeed(final int ifIndex) {
        Long ifSpeed = null;   
        for(IfTableEntry entry : this) {
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                ifSpeed = entry.getIfSpeed();
            }
        }
        return ifSpeed;
    }
    
    /**
     * <p>getPhysAddr</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getPhysAddr(final int ifIndex) {
        String physAddr = null;   
        for(IfTableEntry entry : this) {
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                physAddr = entry.getPhysAddr();
            }
        }
        return physAddr;
    }

}
