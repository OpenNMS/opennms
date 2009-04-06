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
import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.model.OnmsCriteria;

public class InFilter<T> implements BaseFilter {
    
    SQLType<T> m_sqlType;
    T[] m_values;
    String m_fieldName;
    String m_filterName;
    String m_daoPropertyName;
    
    public InFilter(SQLType<T> type, String fieldName, String daoPropertyName, T[] values, String filterName){
        m_sqlType = type;
        m_fieldName = fieldName;
        m_values = values;
        m_filterName = filterName;
        m_daoPropertyName = daoPropertyName;
    }
    
    public void applyCriteria(OnmsCriteria criteria) {
        criteria.add(Restrictions.in(m_daoPropertyName, getValuesAsList()));

    }

    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        for(int i = 0; i < m_values.length; i++){
            m_sqlType.bindParam(ps, parameterIndex, m_values[i]);
        }
        return m_values.length;
    }

    public String getDescription() {
         StringBuilder buf = new StringBuilder("alarmId in ");
         appendIdList(buf);
         return buf.toString();
    }

    public String getParamSql() {
        StringBuilder buf = new StringBuilder(m_values.length*3 + 20);
        
        buf.append(" " + m_fieldName + " IN ");
        
        buf.append('(');
        for(int i = 0; i < m_values.length; i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append('?');
        }
        
        buf.append(')');
        return buf.toString();
    }

    public String getSql() {
        StringBuilder buf = new StringBuilder(m_values.length*5 + 20);
        buf.append(" " + m_fieldName + " IN " );
        appendIdList(buf);
        return buf.toString();
    }

    public String getTextDescription() {
        return getDescription();
    }
    
    private void appendIdList(StringBuilder buf) {
        buf.append("(");
        for(int i = 0; i < m_values.length; i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append(m_sqlType.formatValue(m_values[i]));
        }
        
        buf.append(")");
        
    }
    
    private List<T> getValuesAsList(){
        List<T> ids = new ArrayList<T>();
        
        for(int i = 0; i < m_values.length; i++){
            ids.add(m_values[i]);
        }
        
        return ids;
    }

}
