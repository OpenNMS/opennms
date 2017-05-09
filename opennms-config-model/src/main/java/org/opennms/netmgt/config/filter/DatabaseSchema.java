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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the database-schema.xml
 *  configuration file.
 */
@XmlRootElement(name = "database-schema")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("database-schema.xsd")
public class DatabaseSchema implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "table")
    private List<Table> m_tables = new ArrayList<>();

    public DatabaseSchema() {
    }

    public List<Table> getTables() {
        return m_tables;
    }

    public void setTables(final List<Table> tables) {
        if (tables == m_tables) return;
        m_tables.clear();
        if (tables != null) m_tables.addAll(tables);
    }

    public void addTable(final Table table) {
        m_tables.add(table);
    }

    public boolean removeTable(final Table table) {
        return m_tables.remove(table);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_tables);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof DatabaseSchema) {
            final DatabaseSchema that = (DatabaseSchema)obj;
            return Objects.equals(this.m_tables, that.m_tables);
        }
        return false;
    }

}
