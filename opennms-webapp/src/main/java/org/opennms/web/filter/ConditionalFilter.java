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
package org.opennms.web.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public class ConditionalFilter extends LegacyFilter implements Filter {
    public static final String TYPE = "conditionalFilter";
    
    private String m_conditionType;
    private Filter[] m_filters;
    
    public ConditionalFilter(String conditionType, Filter... filters){
        if (filters.length == 0) throw new IllegalArgumentException("You must pass at least one filter");
        m_conditionType = conditionType;
        m_filters = filters;
    }
    
    public Filter[] getFilters() {
        return m_filters;
    }
    
    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        int parametersBound = 0;
        for(int i = 0; i < m_filters.length; i++){
            parametersBound += m_filters[i].bindParam(ps, parameterIndex + parametersBound);
        }
        return parametersBound;
    }

    public String getDescription() {
        if (m_filters.length == 1) return m_filters[0].getDescription();
        
        StringBuilder buf = new StringBuilder(TYPE);
        buf.append("=");
        buf.append(m_conditionType);
        for(Filter filter : m_filters) {
            buf.append('(');
            buf.append(filter.getDescription());
            buf.append(')');
        }
        return buf.toString();
    }

    public String getParamSql() {
        if (m_filters.length == 1) return m_filters[0].getParamSql();
        
        StringBuilder buf = new StringBuilder("( ");
        for(int i = 0; i < m_filters.length; i++){
            if (i != 0) {
                buf.append(m_conditionType);
                buf.append(" ");
            }
            buf.append(m_filters[i].getParamSql());
        }
        buf.append(") ");
        return buf.toString();
    }

    public String getSql() {
        if (m_filters.length == 1) return m_filters[0].getSql();
        
        StringBuilder buf = new StringBuilder("( ");
        for(int i = 0; i < m_filters.length; i++){
            if (i != 0) {
                buf.append(m_conditionType);
                buf.append(" ");
            }
            buf.append(m_filters[i].getSql());
        }
        buf.append(") ");
        return buf.toString();
    }

    public String getTextDescription() {
        if (m_filters.length == 1) return m_filters[0].getTextDescription();
        
        StringBuilder buf = new StringBuilder("( ");
        for(int i = 0; i < m_filters.length; i++){
            if (i != 0) {
                buf.append(m_conditionType);
                buf.append(" ");
            }
            buf.append(m_filters[i].getTextDescription());
        }
        buf.append(")");
        return buf.toString();
    }

}
