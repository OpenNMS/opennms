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
package org.opennms.netmgt.config.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class JdbcQuery implements Serializable, Comparable<JdbcQuery> {
    
    private static final long serialVersionUID = -9083835215058208854L;

    private static final JdbcColumn[] OF_JDBC_COLUMNS = new JdbcColumn[0];
    
    @XmlAttribute(name="name", required=true)
    private String m_queryName;
    
    @XmlAttribute(name="recheckInterval")
    private int m_recheckInterval;
    
    @XmlAttribute(name="ifType", required=true)
    private String m_ifType;
    
    @XmlAttribute(name="resourceType", required=true)
    private String m_resourceType;

    @XmlElement(name="statement", required=true)
    private JdbcStatement m_jdbcStatement;
    
    @XmlAttribute(name="instance-column", required=false)
    private String m_instanceColumn;
    
    @XmlElementWrapper(name="columns")
    @XmlElement(name="column")
    private List<JdbcColumn> m_jdbcColumns = new ArrayList<>();
    
    @XmlTransient
    public String getQueryName() {
        return m_queryName;
    }
    
    public void setQueryName(String queryName) {
        m_queryName = queryName;
    }
    
    @XmlTransient
    public JdbcStatement getJdbcStatement() {
        return m_jdbcStatement;
    }
    
    public void setJdbcStatement(JdbcStatement jdbcStatement) {
        m_jdbcStatement = jdbcStatement;
    }
    
    @XmlTransient
    public List<JdbcColumn> getJdbcColumns() {
        return m_jdbcColumns;
    }
    
    public void setJdbcColumns(List<JdbcColumn> jdbcColumns) {
        m_jdbcColumns = jdbcColumns;
    }
    
    public void addJdbcColumn(JdbcColumn column) {
        m_jdbcColumns.add(column);
    }
    
    public void removeJdbcColumn(JdbcColumn column) {
        m_jdbcColumns.remove(column);
    }
    
    public void removeColumnByName(String name) {
        for (Iterator<JdbcColumn> itr = m_jdbcColumns.iterator(); itr.hasNext(); ) {
            JdbcColumn column = itr.next();
            if(column.getColumnName().equals(name)) {
                m_jdbcColumns.remove(column);
                return;
            }
        }
    }
    
    @XmlTransient
    public int getRecheckInterval() {
        return m_recheckInterval;
    }

    public void setRecheckInterval(int recheckInterval) {
        m_recheckInterval = recheckInterval;
    }
    
    @XmlTransient
    public String getIfType() {
        return m_ifType;
    }

    public void setIfType(String ifType) {
        m_ifType = ifType;
    }
    
    
    @XmlTransient
    public String getResourceType() {
        return m_resourceType;
    }

    public void setResourceType(String resourceType) {
        m_resourceType = resourceType;
    }

    @XmlTransient
    public String getInstanceColumn() {
        return m_instanceColumn;
    }

    public void setInstanceColumn(String instanceColumn) {
        m_instanceColumn = instanceColumn;
    }

    @Override
    public int compareTo(JdbcQuery obj) {
        return new CompareToBuilder()
            .append(getQueryName(), obj.getQueryName())
            .append(getJdbcStatement(), obj.getJdbcStatement())
            .append(getJdbcColumns().toArray(OF_JDBC_COLUMNS), obj.getJdbcColumns().toArray(OF_JDBC_COLUMNS))
            .append(getRecheckInterval(), obj.getRecheckInterval())
            .append(getIfType(), obj.getIfType())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JdbcQuery) {
            JdbcQuery other = (JdbcQuery) obj;
            return new EqualsBuilder()
                .append(getQueryName(), other.getQueryName())
                .append(getJdbcStatement(), other.getJdbcStatement())
                .append(getJdbcColumns().toArray(OF_JDBC_COLUMNS), other.getJdbcColumns().toArray(OF_JDBC_COLUMNS))
                .isEquals();
        }
        return false;
    }
}
