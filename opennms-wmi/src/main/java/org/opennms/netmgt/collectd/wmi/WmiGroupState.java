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

package org.opennms.netmgt.collectd.wmi;

import java.util.Date;

/**
 * <p>WmiGroupState class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WmiGroupState {
    private boolean available = false;
    private Date lastChecked;

    /**
     * <p>Constructor for WmiGroupState.</p>
     *
     * @param isAvailable a boolean.
     */
    public WmiGroupState(final boolean isAvailable) {
        this(isAvailable, new Date());
    }

    /**
     * <p>Constructor for WmiGroupState.</p>
     *
     * @param isAvailable a boolean.
     * @param lastChecked a {@link java.util.Date} object.
     */
    public WmiGroupState(final boolean isAvailable, final Date lastChecked) {
        this.available = isAvailable;
        this.lastChecked = lastChecked;
    }

    /**
     * <p>isAvailable</p>
     *
     * @return a boolean.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * <p>Setter for the field <code>available</code>.</p>
     *
     * @param available a boolean.
     */
    public void setAvailable(final boolean available) {
        this.available = available;
    }

    /**
     * <p>Getter for the field <code>lastChecked</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getLastChecked() {
        return lastChecked;
    }

    /**
     * <p>Setter for the field <code>lastChecked</code>.</p>
     *
     * @param lastChecked a {@link java.util.Date} object.
     */
    public void setLastChecked(final Date lastChecked) {
        this.lastChecked = lastChecked;
    }
}
