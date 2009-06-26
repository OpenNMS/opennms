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
import java.util.Arrays;
import java.util.List;

/**
 * TwoArgFilter
 *
 * @author brozow
 */
public abstract class MultiArgFilter<T> extends BaseFilter<T> {

    private T[] m_values;
    
    public MultiArgFilter(String filterType, SQLType<T> sqlType, String fieldName, String propertyName, T[] values) {
        super(filterType, sqlType, fieldName, propertyName);
        m_values = values;
    }
    
    public T[] getValues() {
        return m_values;
    }
    
    public List<T> getValuesAsList() {
        return Arrays.asList(m_values);
    }
    
    abstract public String getSQLTemplate();

    @Override
    final public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        for(int i = 0; i < m_values.length; i++) {
            bindValue(ps, parameterIndex+i, m_values[i]);
        }
        return m_values.length;
    }

    @Override
    final public String getValueString() {
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < m_values.length; i++) {
            if (i != 0) {
                buf.append(',');
            }
            buf.append(getValueAsString(m_values[i]));
        }
        return buf.toString();
    }
    
    @Override
    final public String getParamSql() {
        Object[] qmarks = new String[m_values.length];

        Arrays.fill(qmarks, "?");

        return String.format(getSQLTemplate(), qmarks);
    }

    @Override
    final public String getSql() {
        Object[] formattedVals = new String[m_values.length];
        
        for(int i = 0; i < m_values.length; i++) {
            formattedVals[i] = formatValue(m_values[i]);
        }
        return String.format(getSQLTemplate(), formattedVals);
    }

    


}
