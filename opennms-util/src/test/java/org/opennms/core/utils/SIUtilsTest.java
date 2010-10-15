//
// This file is part of the OpenNMS(R) Application.
//
// Copyright (C) 2010 The OpenNMS Group.  All rights reserved.
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
//     OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.core.utils;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class SIUtilsTest extends TestCase {
    /**
     * 10 gigabit ethernet.
     */
    public void testTenGig() {
        assertEquals("10 Gbps", SIUtils.getHumanReadableIfSpeed(10000000000L));
    }

    /**
     * Gigabit Ethernet.
     */
    public void testOneGig() {
        assertEquals("1 Gbps", SIUtils.getHumanReadableIfSpeed(1000000000L));
    }

    /**
     * Fast Ethernet.
     */
    public void testOneHundredMeg() {
        assertEquals("100 Mbps", SIUtils.getHumanReadableIfSpeed(100000000L));
    }

    /**
     * Ethernet.
     */
    public void testTenMeg() {
        assertEquals("10 Mbps", SIUtils.getHumanReadableIfSpeed(10000000L));
    }

    /**
     * DS-1 line speed.
     * @link http://en.wikipedia.org/wiki/Digital_Signal_1
     */
    public void testOnePointFiveFourFourMeg() {
        assertEquals("1.544 Mbps", SIUtils.getHumanReadableIfSpeed(1544000L));
    }

    /**
     * 1200bps modem line.
     */
    public void testTwelveHundred() {
        assertEquals("1.2 kbps", SIUtils.getHumanReadableIfSpeed(1200L));
    }
}
