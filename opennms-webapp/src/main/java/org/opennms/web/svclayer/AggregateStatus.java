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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.web.svclayer;

import java.awt.Color;


/**
 * Use this class to aggregate status to be presented in a view layer technology.
 * 
 * @author david hustace
 *
 */
public class AggregateStatus {
    
    private String m_label;
    private Integer m_totalEntityCount;
    private Integer m_downEntityCount;
    private String m_status;
    
    public static final String NODES_ARE_DOWN = "status-critical";
    public static final String ONE_SERVICE_DOWN = "status-warning";
    public static final String ALL_NODES_UP = "status-normal";
    
    public String getStatus() {
        return m_status;
    }
    public void setStatus(String color) {
        m_status = color;
    }
    public Integer getDownEntityCount() {
        return m_downEntityCount;
    }
    public void setDownEntityCount(Integer downEntityCount) {
        m_downEntityCount = downEntityCount;
    }
    public String getLabel() {
        return m_label;
    }
    public void setLabel(String label) {
        m_label = label;
    }
    public Integer getTotalEntityCount() {
        return m_totalEntityCount;
    }
    public void setTotalEntityCount(Integer totalEntityCount) {
        m_totalEntityCount = totalEntityCount;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(m_label);
        sb.append(": ");
        sb.append(m_downEntityCount);
        sb.append(" down of ");
        sb.append(m_totalEntityCount);
        sb.append(" total.");
        return sb.toString();
    }

}
