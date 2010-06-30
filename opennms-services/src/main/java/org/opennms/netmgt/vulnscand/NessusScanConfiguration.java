//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.vulnscand;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Date;

import org.opennms.netmgt.config.VulnscandConfigFactory;

/**
 * This class encapsulates the information about an interface necessary to
 * schedule it for scans.
 */
final class NessusScanConfiguration implements ScheduleTrigger {
    /**
     * Nessus username
     */
    public String username;

    /**
     * Nessus password
     */
    public String password;

    /**
     * Hostname of the Nessus daemon
     */
    public InetAddress hostname;

    /**
     * Port number where Nessusd is running
     */
    public int hostport;

    /**
     * Target of the Nessus scan
     */
    public InetAddress targetAddress;

    /**
     * Level of intrusiveness of the scanning (1-4)
     */
    public int scanLevel;

    /**
     * Timestamp of last scanned time
     */
    Timestamp lastScan;

    /**
     * Milliseconds between the end of a scan and the beginning of the next scan
     * of this interface
     */
    long interval;

    /**
     * Marker that tells whether or not the object is currently scheduled
     */
    boolean scheduled;

    NessusScanConfiguration(InetAddress address, int newScanLevel, Timestamp newLastScan, long newInterval) {
        targetAddress = address;
        scanLevel = newScanLevel;
        lastScan = newLastScan;
        interval = newInterval;

        init();
    }

    NessusScanConfiguration(InetAddress address, int newScanLevel, Date newLastScan, long newInterval) {
        targetAddress = address;
        scanLevel = newScanLevel;
        lastScan = new Timestamp(newLastScan.getTime());
        interval = newInterval;

        init();
    }

    /**
     * Enter values from the configuration
     */
    private void init() {
        VulnscandConfigFactory config = VulnscandConfigFactory.getInstance();

        scheduled = false;
        hostname = config.getServerAddress();
        hostport = config.getServerPort();
        username = config.getServerUsername();
        password = config.getServerPassword();
    }

    /* (non-Javadoc)
	 * @see org.opennms.netmgt.vulnscand.ScheduleTrigger#isScheduled()
	 */
    /**
     * <p>isScheduled</p>
     *
     * @return a boolean.
     */
    public boolean isScheduled() {
        return scheduled;
    }

    InetAddress getAddress() {
        return targetAddress;
    }

    Timestamp getLastScanned() {
        return lastScan;
    }

    long getRescanInterval() {
        return interval;
    }

    /* (non-Javadoc)
	 * @see org.opennms.netmgt.vulnscand.ScheduleTrigger#setScheduled(boolean)
	 */
    /** {@inheritDoc} */
    public void setScheduled(boolean newScheduled) {
        scheduled = newScheduled;
    }

    void setLastScanned(Date newLastScan) {
        lastScan = new Timestamp(newLastScan.getTime());
    }

    void setLastScanned(Timestamp newLastScan) {
        lastScan = newLastScan;
    }

    /* (non-Javadoc)
	 * @see org.opennms.netmgt.vulnscand.ScheduleTrigger#isTimeForRescan()
	 */
    /**
     * <p>isTimeForRescan</p>
     *
     * @return a boolean.
     */
    public boolean isTimeForRescan() {
        if (System.currentTimeMillis() >= (lastScan.getTime() + interval))
            return true;
        else
            return false;
    }

    /**
     * Validation function.
     *
     * @return a boolean.
     */
    public boolean isValid() {
        // Category log = ThreadCategory.getInstance(getClass());

        boolean retval = ((hostname != null) && (username != null) && (username != "") && (password != null) && (password != "") && (scanLevel > 0) && (scanLevel < 5) && (targetAddress != null) && (hostport > 0) && (hostport < (1 << 16)));
        return retval;
    }

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.vulnscand.ScheduleTrigger#getJob()
	 */
	/**
	 * <p>getJob</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	public Object getJob() {
		return new NessusScan(this);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getAddress().toString();
	}
}
