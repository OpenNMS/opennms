/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
