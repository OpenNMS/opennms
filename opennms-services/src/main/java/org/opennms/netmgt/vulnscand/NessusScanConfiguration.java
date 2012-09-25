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

package org.opennms.netmgt.vulnscand;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Date;

import org.opennms.netmgt.config.VulnscandConfigFactory;

/**
 * This class encapsulates the information about an interface necessary to
 * schedule it for scans.
 */
final class NessusScanConfiguration implements ScheduleTrigger<Runnable> {
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
	public Runnable getJob() {
		return new NessusScan(this);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getAddress().toString();
	}
}
