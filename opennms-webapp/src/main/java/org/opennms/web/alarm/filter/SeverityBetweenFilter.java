/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.alarm.filter;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.filter.BetweenFilter;
import org.opennms.web.filter.SQLType;

public class SeverityBetweenFilter extends BetweenFilter<Integer> implements Filter {
    public static final String TYPE = "severityBetween";
    
    private OnmsSeverity m_severityBegin;
    private OnmsSeverity m_severityEnd;
    
    public SeverityBetweenFilter(OnmsSeverity rangeBegin, OnmsSeverity rangeEnd){
        super(SQLType.INT, "SEVERITY", "severityId", new Integer(rangeBegin.getId()), new Integer(rangeEnd.getId()), "severityBetween");
        m_severityBegin = rangeBegin;
        m_severityEnd = rangeEnd;
    }
    
//    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
//        ps.setInt(parameterIndex, m_severityBegin.getId());
//        ps.setInt(parameterIndex + 1, m_severityEnd.getId());
//        return 2;
//    }
//
//    public String getDescription() {
//        return TYPE + " = " + m_severityBegin.getId() + " and " + m_severityEnd.getId();
//    }
//
//    public String getParamSql() {
//        return " SEVERITY >=? AND SEVERITY <=?";
//    }
//
//    public String getSql() {
//        return " AND SEVERITY >" + m_severityBegin.getId() + " AND SEVERITY <=" + m_severityEnd.getId();
//    }
//
//    public String getTextDescription() {
//        return TYPE + " = " + m_severityBegin.getLabel() + " and " + m_severityEnd.getLabel();
//    }
//    
//    public String toString() {
//        return ("<AlarmCriteria.SeverityBetweenFilter: " + this.getDescription() + ">");
//    }
//
//    public void applyCriteria(OnmsCriteria criteria) {
//        // TODO Auto-generated method stub
//        
//    }

}
