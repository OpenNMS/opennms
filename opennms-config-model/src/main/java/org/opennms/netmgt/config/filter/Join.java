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
package org.opennms.netmgt.config.filter;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "join")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("database-schema.xsd")
public class Join implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_TYPE = "inner";

    @XmlAttribute(name = "type")
    private String m_type;

    @XmlAttribute(name = "column", required = true)
    private String m_column;

    @XmlAttribute(name = "table", required = true)
    private String m_table;

    @XmlAttribute(name = "table-column", required = true)
    private String m_tableColumn;

    public Join() {
    }

    public String getType() {
        return m_type != null ? m_type : DEFAULT_TYPE;
    }

    public void setType(final String type) {
        m_type = ConfigUtils.normalizeString(type);
    }

    public String getColumn() {
        return m_column;
    }

    public void setColumn(final String column) {
        m_column = ConfigUtils.assertNotEmpty(column, "column");
    }

    public String getTable() {
        return m_table;
    }

    public void setTable(final String table) {
        m_table = ConfigUtils.assertNotEmpty(table, "table");
    }

    public String getTableColumn() {
        return m_tableColumn;
    }

    public void setTableColumn(final String tableColumn) {
        m_tableColumn = ConfigUtils.assertNotEmpty(tableColumn, "table-column");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_type, 
                            m_column, 
                            m_table, 
                            m_tableColumn);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Join) {
            final Join that = (Join)obj;
            return Objects.equals(this.m_type, that.m_type)
                    && Objects.equals(this.m_column, that.m_column)
                    && Objects.equals(this.m_table, that.m_table)
                    && Objects.equals(this.m_tableColumn, that.m_tableColumn);
        }
        return false;
    }

}
