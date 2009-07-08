/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.rrd;

public class RrdDataSource {
    private String m_name;
    private String m_type;
    private int m_heartBeat;
    private String m_min;
    private String m_max;
    
    public RrdDataSource(String name, String type, int heartBeat, String min, String max) {
        m_name = name;
        m_type = type;
        m_heartBeat = heartBeat;
        m_min = min;
        m_max = max;
    }

    public int getHeartBeat() {
        return m_heartBeat;
    }

    public String getMax() {
        return m_max;
    }

    public String getMin() {
        return m_min;
    }

    public String getName() {
        return m_name;
    }

    public String getType() {
        return m_type;
    }

}
