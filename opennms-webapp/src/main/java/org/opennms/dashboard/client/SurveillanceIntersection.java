/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>SurveillanceIntersection class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class SurveillanceIntersection extends SurveillanceSet implements IsSerializable {
    
    private SurveillanceGroup m_rowGroup;
    private SurveillanceGroup m_columnGroup;
    private String m_data;
    private String m_status;
    
    /**
     * Default constructor used for serialization
     */
    public SurveillanceIntersection() {
        this(null, null);
    }
    
    /**
     * <p>Constructor for SurveillanceIntersection.</p>
     *
     * @param rowGroup a {@link org.opennms.dashboard.client.SurveillanceGroup} object.
     * @param columnGroup a {@link org.opennms.dashboard.client.SurveillanceGroup} object.
     */
    public SurveillanceIntersection(SurveillanceGroup rowGroup, SurveillanceGroup columnGroup) {
        m_rowGroup = rowGroup;
        m_columnGroup = columnGroup;
    }

    /**
     * <p>getColumnGroup</p>
     *
     * @return a {@link org.opennms.dashboard.client.SurveillanceGroup} object.
     */
    public SurveillanceGroup getColumnGroup() {
        return m_columnGroup;
    }

    /**
     * <p>setColumnGroup</p>
     *
     * @param columnGroup a {@link org.opennms.dashboard.client.SurveillanceGroup} object.
     */
    public void setColumnGroup(SurveillanceGroup columnGroup) {
        m_columnGroup = columnGroup;
    }

    /**
     * <p>getRowGroup</p>
     *
     * @return a {@link org.opennms.dashboard.client.SurveillanceGroup} object.
     */
    public SurveillanceGroup getRowGroup() {
        return m_rowGroup;
    }

    /**
     * <p>setRowGroup</p>
     *
     * @param rowGroup a {@link org.opennms.dashboard.client.SurveillanceGroup} object.
     */
    public void setRowGroup(SurveillanceGroup rowGroup) {
        m_rowGroup = rowGroup;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return m_columnGroup.getLabel() + " " + m_rowGroup.getLabel();
    }

    /** {@inheritDoc} */
    @Override
    public void visit(Visitor v) {
        v.visitIntersection(m_rowGroup, m_columnGroup);
    }

    /**
     * <p>getData</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getData() {
        return m_data == null ? "N/A" : m_data;
    }

    /**
     * <p>setData</p>
     *
     * @param data a {@link java.lang.String} object.
     */
    public void setData(String data) {
        m_data = data;
    }

    /**
     * <p>getStatus</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatus() {
        return m_status == null ? "Unknown" : m_status;
    }

    /**
     * <p>setStatus</p>
     *
     * @param status a {@link java.lang.String} object.
     */
    public void setStatus(String status) {
        m_status = status;
    }
    
    

}
