//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.rrd;

/**
 * <p>RrdDataSource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class RrdDataSource {
    private String m_name;
    private String m_type;
    private int m_heartBeat;
    private String m_min;
    private String m_max;
    
    /**
     * <p>Constructor for RrdDataSource.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param type a {@link java.lang.String} object.
     * @param heartBeat a int.
     * @param min a {@link java.lang.String} object.
     * @param max a {@link java.lang.String} object.
     */
    public RrdDataSource(String name, String type, int heartBeat, String min, String max) {
        m_name = name;
        m_type = type;
        m_heartBeat = heartBeat;
        m_min = min;
        m_max = max;
    }

    /**
     * <p>getHeartBeat</p>
     *
     * @return a int.
     */
    public int getHeartBeat() {
        return m_heartBeat;
    }

    /**
     * <p>getMax</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMax() {
        return m_max;
    }

    /**
     * <p>getMin</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMin() {
        return m_min;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return m_type;
    }

}
