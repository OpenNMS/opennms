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
