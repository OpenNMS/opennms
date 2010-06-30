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
package org.opennms.netmgt.model;

import java.io.Serializable;

/**
 * <p>OnmsMonitoringLocationDefinition class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsMonitoringLocationDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    private String m_area;

    private String m_name;

    private String m_pollingPackageName;
    
    /**
     * <p>Constructor for OnmsMonitoringLocationDefinition.</p>
     */
    public OnmsMonitoringLocationDefinition() {
        
    }
    
    /**
     * <p>Constructor for OnmsMonitoringLocationDefinition.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param pollingPackageName a {@link java.lang.String} object.
     */
    public OnmsMonitoringLocationDefinition(String name, String pollingPackageName) {
        m_name = name;
        m_pollingPackageName = pollingPackageName;
    }
    
    /**
     * <p>Constructor for OnmsMonitoringLocationDefinition.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param pollingPackageName a {@link java.lang.String} object.
     * @param area a {@link java.lang.String} object.
     */
    public OnmsMonitoringLocationDefinition(String name, String pollingPackageName, String area) {
        m_name = name;
        m_pollingPackageName = pollingPackageName;
        m_area = area;
    }

    /**
     * <p>getArea</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getArea() {
        return m_area;
    }

    /**
     * <p>setArea</p>
     *
     * @param area a {@link java.lang.String} object.
     */
    public void setArea(String area) {
        m_area = area;
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
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getPollingPackageName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPollingPackageName() {
        return m_pollingPackageName;
    }

    /**
     * <p>setPollingPackageName</p>
     *
     * @param pollingPackageName a {@link java.lang.String} object.
     */
    public void setPollingPackageName(String pollingPackageName) {
        m_pollingPackageName = pollingPackageName;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "OnmsMonitoringLocationDefinition@" + Integer.toHexString(hashCode()) + ": Name \"" + m_name + "\", polling package name \"" + m_pollingPackageName + "\", area \"" + m_area + "\"";
    }
}
