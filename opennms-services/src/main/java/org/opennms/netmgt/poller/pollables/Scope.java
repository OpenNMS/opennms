/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.pollables;

import org.opennms.netmgt.events.api.EventConstants;

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
        if (a == null) {
            return false;
        } else {
            if (b == null) {
                return true;
            } else {
                return a.isLargerThan(b);
            }
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
