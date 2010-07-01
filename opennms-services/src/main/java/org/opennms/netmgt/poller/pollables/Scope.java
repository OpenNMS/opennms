/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.poller.pollables;

import org.opennms.netmgt.EventConstants;

/**
 * <p>Scope class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public enum Scope {
    SERVICE,
    INTERFACE,
    NODE,
    NETWORK;
    
    /**
     * <p>fromUei</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     */
    public static Scope fromUei(String uei) {
        if (EventConstants.NODE_DOWN_EVENT_UEI.equals(uei)) {
            return NODE;
        } else if (EventConstants.INTERFACE_DOWN_EVENT_UEI.equals(uei)) {
            return INTERFACE;
        } else  if (EventConstants.NODE_LOST_SERVICE_EVENT_UEI.equals(uei)) {
            return SERVICE;
        }
        return null;
    }
    
    /**
     * <p>isLargerThan</p>
     *
     * @param s a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @return a boolean.
     */
    public boolean isLargerThan(Scope s) {
        if (s == null) return true;
        return this.ordinal() > s.ordinal();
    }
    
    /**
     * <p>isSmallerThan</p>
     *
     * @param s a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @return a boolean.
     */
    public boolean isSmallerThan(Scope s) {
        if (s == null) return false;
        return this.ordinal() < s.ordinal();
    }
    
    /**
     * <p>isLargerThan</p>
     *
     * @param a a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @param b a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @return a boolean.
     */
    public static boolean isLargerThan(Scope a, Scope b) {
        if (a == null && b == null) {
            return false;
        } else if (a == null && b != null) {
            return false;
        } else if (a != null && b == null) {
            return true;
        } else {
            return a.isLargerThan(b);
        }
    }
    
    /**
     * <p>isSmallerThan</p>
     *
     * @param a a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @param b a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @return a boolean.
     */
    public static boolean isSmallerThan(Scope a, Scope b) {
        return a != b && !isLargerThan(a, b);
    }
    
}
