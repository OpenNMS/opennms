/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 20, 2007
 *
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
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
    
    public SurveillanceIntersection(SurveillanceGroup rowGroup, SurveillanceGroup columnGroup) {
        m_rowGroup = rowGroup;
        m_columnGroup = columnGroup;
    }

    public SurveillanceGroup getColumnGroup() {
        return m_columnGroup;
    }

    public void setColumnGroup(SurveillanceGroup columnGroup) {
        m_columnGroup = columnGroup;
    }

    public SurveillanceGroup getRowGroup() {
        return m_rowGroup;
    }

    public void setRowGroup(SurveillanceGroup rowGroup) {
        m_rowGroup = rowGroup;
    }
    
    public String toString() {
        return m_columnGroup.getLabel() + " " + m_rowGroup.getLabel();
    }

    public void visit(Visitor v) {
        v.visitIntersection(m_rowGroup, m_columnGroup);
    }

    public String getData() {
        return m_data == null ? "N/A" : m_data;
    }

    public void setData(String data) {
        m_data = data;
    }

    public String getStatus() {
        return m_status == null ? "Unknown" : m_status;
    }

    public void setStatus(String status) {
        m_status = status;
    }
    
    

}
