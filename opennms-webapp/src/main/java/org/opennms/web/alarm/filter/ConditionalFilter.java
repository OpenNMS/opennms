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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.opennms.web.filter.LegacyFilter;
import org.opennms.web.filter.Filter;

public class ConditionalFilter extends LegacyFilter {
    public static final String TYPE = "conditionalFilter";
    
    private Filter[] m_filters;
    private String m_conditionType;
    
    public ConditionalFilter(String conditionType, Filter... filters){
        m_filters = filters;
        m_conditionType = conditionType;
    }
    
    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        return bindFilterParams(ps, parameterIndex);
    }

    public String getDescription() {

        return TYPE + " = " + getFilterDescriptions();
    }

    public String getParamSql() {

        return getFilterParamSql();
    }

    public String getSql() {
        return getFilterSql();
    }

    public String getTextDescription() {
        return TYPE + " = " + getFilterTextDescriptions();
    }

    private String getFilterDescriptions() {
        String desc = " (";
        
        for(int i = 0; i < m_filters.length; i++){
              desc += m_filters[i].getDescription();
              if(i < m_filters.length - 1){
                  desc += " " + m_conditionType + " "; 
              }
        }
        desc += ") ";
        return desc;
    }
    
    private String getFilterParamSql() {
        String param = " (";
        
        for(int i = 0; i < m_filters.length; i++){
            param += " " + m_filters[i].getParamSql();
            if(i < m_filters.length - 1){
                param += " " + m_conditionType;
            }
        }
        param += ") ";
        return param;
    }
    
    private String getFilterSql(){
        String sql = " (";
        
        for(int i = 0; i < m_filters.length; i++){
            sql += " " + m_filters[i].getSql();
            if(i < m_filters.length - 1){
                sql += " " + m_conditionType;
            }
        }
        sql += ") ";
        return sql;
    }

    private String getFilterTextDescriptions() {
        String tDescriptions = " (";
        
        for(int i = 0; i < m_filters.length; i++){
            tDescriptions += " " + m_filters[i].getTextDescription();
            if(i < m_filters.length - 1){
                tDescriptions += " " + m_conditionType;
            }
        }
        tDescriptions += ") ";
        return tDescriptions;
    }
    
    private int bindFilterParams(PreparedStatement ps, int parameterIndex) throws SQLException {
        int retVal = 0;
        for(int i = 0; i < m_filters.length; i++){
            retVal += m_filters[i].bindParam(ps, parameterIndex + retVal);
        }
        
        return retVal;
    }

}
