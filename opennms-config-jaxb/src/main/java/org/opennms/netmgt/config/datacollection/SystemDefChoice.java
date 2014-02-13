/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.datacollection;

public class SystemDefChoice implements java.io.Serializable {
    private static final long serialVersionUID = 4037895445383242171L;

    /**
     * system object identifier (sysoid) which uniquely identifies the system.
     */
    private String m_sysoid;

    /**
     * Sysoid mask which can be used to match multiple systems if their sysoid
     * begins with the mask
     */
    private String m_sysoidMask;

    public SystemDefChoice() {
        super();
    }

    /**
     * system object identifier (sysoid) which uniquely identifies the system.
     */
    public String getSysoid() {
        return m_sysoid;
    }

    /**
     * Sysoid mask which can be used to match multiple systems if their sysoid
     * begins with the mask
     */
    public String getSysoidMask() {
        return m_sysoidMask;
    }


    public void setSysoid(final String sysoid) {
        m_sysoid = sysoid == null? null : sysoid.intern();
    }

    public void setSysoidMask(final String sysoidMask) {
        m_sysoidMask = sysoidMask == null? null : sysoidMask.intern();
    }
}
