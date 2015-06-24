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

package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>NodeRtc class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeRtc implements IsSerializable {

    private String m_nodeLabel;
    private String m_nodeId;
    private String m_availability;
    private int m_downServiceCount;
    private int m_serviceCount;
    private String m_serviceStyle;
    private String m_availabilityStyle;
    private boolean m_isDashboardRole;

    /**
     * <p>setNodeLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setNodeLabel(String label) {
        m_nodeLabel = label;
    }

    /**
     * <p>setAvailability</p>
     *
     * @param availability a {@link java.lang.String} object.
     */
    public void setAvailability(String availability) {
        m_availability = availability;
    }

    /**
     * <p>setDownServiceCount</p>
     *
     * @param downServiceCount a int.
     */
    public void setDownServiceCount(int downServiceCount) {
        m_downServiceCount = downServiceCount;
    }

    /**
     * <p>setServiceCount</p>
     *
     * @param serviceCount a int.
     */
    public void setServiceCount(int serviceCount) {
        m_serviceCount = serviceCount;
    }

    /**
     * <p>getAvailability</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAvailability() {
        return m_availability;
    }

    /**
     * <p>getDownServiceCount</p>
     *
     * @return a int.
     */
    public int getDownServiceCount() {
        return m_downServiceCount;
    }

    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    /**
     * <p>getServiceCount</p>
     *
     * @return a int.
     */
    public int getServiceCount() {
        return m_serviceCount;
    }

    /**
     * <p>getServiceStyle</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceStyle() {
        return m_serviceStyle;
    }

    /**
     * <p>getAvailabilityStyle</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAvailabilityStyle() {
        return m_availabilityStyle;
    }

    /**
     * <p>setServiceStyle</p>
     *
     * @param serviceStyle a {@link java.lang.String} object.
     */
    public void setServiceStyle(String serviceStyle) {
        m_serviceStyle = serviceStyle;
    }

    /**
     * <p>setAvailabilityStyle</p>
     *
     * @param availabilityStyle a {@link java.lang.String} object.
     */
    public void setAvailabilityStyle(String availabilityStyle) {
        m_availabilityStyle = availabilityStyle;
    }
    
    /**
     * <p>setIsDashboardRole</p>
     *
     * @param isDashboardRole a boolean.
     */
    public void setIsDashboardRole(boolean isDashboardRole) {
        m_isDashboardRole = isDashboardRole;
    }

    /**
     * <p>getIsDashboardRole</p>
     *
     * @return a boolean.
     */
    public boolean getIsDashboardRole() {
        return m_isDashboardRole;
    }
    
    /**
     * <p>setNodeId</p>
     *
     * @param nodeId a {@link java.lang.String} object.
     */
    public void setNodeId(String nodeId) {
        m_nodeId = nodeId;
    }
    
    /**
     * <p>getNodeId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeId() {
        return m_nodeId;
    }
}
