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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.opennms.netmgt.collection.api.AttributeType;

@XmlRootElement(name="column")
public class JdbcColumn implements Serializable, Comparable<JdbcColumn> {

    private static final long serialVersionUID = 2519632811400677757L;

    @XmlAttribute(name="name", required=true)
    private String m_columnName;
    
    @XmlAttribute(name="data-source-name", required=false)
    private String m_dataSourceName;

    @XmlAttribute(name="type", required=true)    
    private AttributeType m_dataType;

    @XmlAttribute(name="alias", required=true)
    private String m_alias;
    
    @XmlTransient
    public String getColumnName() {
        return m_columnName;
    }
    
    public void setColumnName(String columnName) {
        m_columnName = columnName;
    }
    
    @XmlTransient
    public String getDataSourceName() {
        return m_dataSourceName;
    }
    
    public void setDataSourceName(String dataSourceName) {
        m_dataSourceName = dataSourceName;
    }

    @XmlTransient
    public AttributeType getDataType() {
        return m_dataType;
    }

    public void setDataType(AttributeType dataType) {
        m_dataType = dataType;
    }

    @XmlTransient
    public String getAlias() {
        return m_alias;
    }

    public void setAlias(String alias) {
        m_alias = alias;
    }

    @Override
    public int compareTo(JdbcColumn obj) {
        return new CompareToBuilder()
            .append(getColumnName(), obj.getColumnName())
            .append(getDataSourceName(), obj.getDataSourceName())
            .append(getDataType(), obj.getDataType())
            .append(getAlias(), obj.getAlias())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JdbcColumn) {
            JdbcColumn other = (JdbcColumn) obj;
            return new EqualsBuilder()
                .append(getColumnName(), other.getColumnName())
                .append(getDataSourceName(), other.getDataSourceName())
                .append(getDataType(), other.getDataType())
                .append(getAlias(), other.getAlias())
                .isEquals();
        }
        return false;
    }
}
