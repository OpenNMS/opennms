/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.ErrorStatusException;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CiscoVtpTracker extends AggregateTracker
{
	private static final Logger LOG = LoggerFactory.getLogger(CiscoVtpTracker.class);
    /**
     * the bridge type
     */
	//
	// Lookup strings for specific table entries
	//
	public final static	String	VTP_VERSION	= "vtpVersion";
	
	public final static NamedSnmpVar[] ms_elemList = new NamedSnmpVar[] {
		/**
		 * vtpVersion OBJECT-TYPE
    	 * SYNTAX          INTEGER  {
         *               one(1),
         *               two(2),
         *               none(3),
         *               three(4)
         *           }
    	 * MAX-ACCESS      read-only
    	 * STATUS          current
    	 * DESCRIPTION
         *	"The version of VTP in use on the local system.  A device
         *	will report its version capability and not any particular
         *	version in use on the device. If the device does not support
         *	vtp, the version is none(3)." 
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPINT32,VTP_VERSION,".1.3.6.1.4.1.9.9.46.1.1.1")
	};

    private SnmpStore m_store;
	
	/**
	 * <P>The class constructor is used to initialize the collector
	 * and send out the initial SNMP packet requesting data. The
	 * data is then received and store by the object. When all the
	 * data has been collected the passed signaler object is <EM>notified</em>
	 * using the notifyAll() method.</P>
	 *
	 * @param address a {@link java.net.InetAddress} object.
	 */
	public CiscoVtpTracker() {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_store = new SnmpStore(ms_elemList); 
	}

    /** {@inheritDoc} */
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    /** {@inheritDoc} */
    protected void reportGenErr(final String msg) {
        LOG.warn("Error retrieving vtpVersion: {}", msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(final String msg) {
        LOG.info("Error retrieving vtpVersion: {}", msg);
    }

    @Override
    protected void reportFatalErr(final ErrorStatusException ex) {
        LOG.warn("Error retrieving vtpVersion: {}", ex.getMessage(), ex);
    }

    /**
     * <p>getBridgeAddress</p>
     *
     * @return a {@link Integer} object.
     */
    public Integer getVtpVersion() {
        return m_store.getInt32(VTP_VERSION);
    }
            
}
