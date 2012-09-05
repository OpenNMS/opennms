/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dhcpd.jmx;

import org.opennms.core.utils.ThreadCategory;

/**
 * <p>Dhcpd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Dhcpd implements DhcpdMBean {
    /**
     * <p>init</p>
     */
    public void init() {
        // No initialization necessary
    }

    /**
     * <p>start</p>
     */
    public void start() {
        org.opennms.netmgt.dhcpd.Dhcpd dhcpd = org.opennms.netmgt.dhcpd.Dhcpd.getInstance();
        dhcpd.start();
    }

    /**
     * <p>stop</p>
     */
    public void stop() {
        org.opennms.netmgt.dhcpd.Dhcpd dhcpd = org.opennms.netmgt.dhcpd.Dhcpd.getInstance();
        dhcpd.stop();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    public int getStatus() {
        org.opennms.netmgt.dhcpd.Dhcpd dhcpd = org.opennms.netmgt.dhcpd.Dhcpd.getInstance();
        return dhcpd.getStatus();
    }

    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String status() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    /**
     * <p>getStatusText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatusText() {
        int status = getStatus();
        String statusText = org.opennms.core.fiber.Fiber.STATUS_NAMES[status];
        ThreadCategory.getInstance(getClass()).debug("getStatusText: status = "+status+", statusText = "+statusText);
        return statusText;
    }
}
