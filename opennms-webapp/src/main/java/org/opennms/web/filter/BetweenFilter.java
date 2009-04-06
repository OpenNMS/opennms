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

import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.model.OnmsCriteria;

public class BetweenFilter<T> extends EqualsFilter<T> implements BaseFilter {
    
    T m_value2;
    
    public BetweenFilter(SQLType<T> type, String fieldName, String daoPropertyName, T value, T value2, String filterName) {
        super(type, fieldName, daoPropertyName, value, filterName);
        m_value2 = value2;
    }

    public void applyCriteria(OnmsCriteria criteria) {
        criteria.add(Restrictions.between(m_daoPropertyName, m_value, m_value2));
    }

    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        m_sqlType.bindParam(ps, parameterIndex, m_value);
        m_sqlType.bindParam(ps, parameterIndex + 1, m_value2);
        return 2;
    }

    public String getDescription() {
        return " " + m_filterName + " = " + m_value  + " and " + m_value2;
    }

    public String getParamSql() {
        return m_fieldName + " >=? AND " + m_fieldName + " <=?";
    }

    public String getSql() {
        return " AND " + m_fieldName + " >" + m_sqlType.formatValue(m_value) + " AND " + m_fieldName + " <=" + m_sqlType.formatValue(m_value2);
    }

    public String getTextDescription() {
        return getDescription();
    }

}
