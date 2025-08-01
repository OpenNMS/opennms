/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.model;

import java.util.Set;

/**
 * Really a container class for persisting arrangements of status definitions
 * created by the user.
 *
 * Perhaps a new package called model.config is in order.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
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
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder(50);
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
