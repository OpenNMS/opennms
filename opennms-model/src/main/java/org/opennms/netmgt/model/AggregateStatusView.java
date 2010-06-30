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

package org.opennms.netmgt.model;

import java.util.Set;

/**
 * Really a container class for persisting arrangements of status definitions
 * created by the user.
 *
 * Perhaps a new package called model.config is in order.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class AggregateStatusView {
    
    private Integer m_id;
    private String m_name;
    private String m_tableName;
    private String m_columnName;
    private String m_columnValue;
    private Set<AggregateStatusDefinition> m_statusDefinitions;
    
    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getId() {
        return m_id;
    }
    
    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }
    
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getTableName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTableName() {
        return m_tableName;
    }

    /**
     * <p>setTableName</p>
     *
     * @param tableName a {@link java.lang.String} object.
     */
    public void setTableName(String tableName) {
        m_tableName = tableName;
    }
    
    /**
     * <p>getColumnName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getColumnName() {
        return m_columnName;
    }
    
    /**
     * <p>setColumnName</p>
     *
     * @param columnName a {@link java.lang.String} object.
     */
    public void setColumnName(String columnName) {
        m_columnName = columnName;
    }
    
    /**
     * <p>getColumnValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getColumnValue() {
        return m_columnValue;
    }
    
    /**
     * <p>setColumnValue</p>
     *
     * @param columnValue a {@link java.lang.String} object.
     */
    public void setColumnValue(String columnValue) {
        m_columnValue = columnValue;
    }
    
    /**
     * <p>getStatusDefinitions</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<AggregateStatusDefinition> getStatusDefinitions() {
        return m_statusDefinitions;
    }
    /**
     * <p>setStatusDefinitions</p>
     *
     * @param statusDefinitions a {@link java.util.Set} object.
     */
    public void setStatusDefinitions(Set<AggregateStatusDefinition> statusDefinitions) {
        m_statusDefinitions = statusDefinitions;
    }
    
	/**
	 * Good for debug logs and viewing in a debugger.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		StringBuffer result = new StringBuffer(50);
		result.append("AggregateStatusView { id: ");
		result.append(m_id);
		result.append(", name: ");
		result.append(m_name);
		result.append(", tableName: ");
		result.append(m_tableName);
		result.append(", columnName: ");
		result.append(m_columnName);
		result.append(", columnValue: ");
		result.append(m_columnValue);
		result.append(" }");
		return result.toString();
	}
    
}
