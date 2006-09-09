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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Really a container class for persisting arrangements of status definitions
 * created by the user.
 * 
 * Perhaps a new package called model.config is in order.
 * 
 * @author david
 *
 */
@Entity
@Table(name="aggregate_status_views")
public class AggregateStatusView {
    
    private Integer m_id;
    private String m_name;
    private String m_tableName;
    private String m_columnName;
    private String m_columnValue;
    private Set<AggregateStatusDefinition> m_statusDefinitions;
    
    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    
    public void setId(Integer id) {
        m_id = id;
    }
    
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getTableName() {
        return m_tableName;
    }

    public void setTableName(String tableName) {
        m_tableName = tableName;
    }
    
    public String getColumnName() {
        return m_columnName;
    }
    
    public void setColumnName(String columnName) {
        m_columnName = columnName;
    }
    
    public String getColumnValue() {
        return m_columnValue;
    }
    
    public void setColumnValue(String columnValue) {
        m_columnValue = columnValue;
    }
    
    @ManyToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    @JoinTable(
    		name ="statusview_statusdef",
    		joinColumns = {@JoinColumn(name="statusViewId")},
    		inverseJoinColumns = {@JoinColumn(name="statusDefId")}
    )
    public Set<AggregateStatusDefinition> getStatusDefinitions() {
        return m_statusDefinitions;
    }
    public void setStatusDefinitions(Set<AggregateStatusDefinition> statusDefinitions) {
        m_statusDefinitions = statusDefinitions;
    }
    
    
    /**
     * Good for debug logs and viewing in a debugger.
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
