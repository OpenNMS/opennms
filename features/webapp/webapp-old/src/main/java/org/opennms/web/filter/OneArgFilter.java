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

/**
 * OneArgFilter
 *
 * @author brozow
 */
public abstract class OneArgFilter<T> extends BaseFilter<T> {
    
    private T m_value;
    
    public OneArgFilter(String filterType, SQLType<T> sqlType, String fieldName, String propertyName, T value) {
        super(filterType, sqlType, fieldName, propertyName);
        m_value = value;
    }
    
    final public T getValue() { return m_value; };

    abstract public String getSQLTemplate();
    
    public T getBoundValue(T value) {
        return value;
    }

    @Override
    final public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        bindValue(ps, parameterIndex, getBoundValue(m_value));
        return 1;
    }
    
    @Override
    final public String getValueString() {
        return getValueAsString(m_value);
    }
    

    @Override
    final public String getParamSql() {
        return String.format(getSQLTemplate(), "?");
    }

    @Override
    final public String getSql() {
        return String.format(getSQLTemplate(), formatValue(m_value));
    }

}
