/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.vacuumd;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * actions modify the database based on results of a trigger
 */
@XmlRootElement(name = "action")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("vacuumd-configuration.xsd")
public class Action implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_DATA_SOURCE = "opennms";

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "data-source")
    private String m_dataSource;

    /**
     * Just a generic string used for SQL statements
     */
    @XmlElement(name = "statement", required = true)
    private Statement m_statement;

    public Action() {
    }

    public Action(final String name, final String dataSource, final Statement statement) {
        setName(name);
        setDataSource(dataSource);
        setStatement(statement);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getDataSource() {
        return m_dataSource == null ? DEFAULT_DATA_SOURCE : m_dataSource;
    }

    public void setDataSource(final String dataSource) {
        m_dataSource = ConfigUtils.normalizeString(dataSource);
    }

    public Statement getStatement() {
        return m_statement;
    }

    public void setStatement(final Statement statement) {
        m_statement = ConfigUtils.assertNotNull(statement, "statement");
    }

    public int hashCode() {
        return Objects.hash(m_name, m_dataSource, m_statement);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Action) {
            final Action that = (Action) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_dataSource, that.m_dataSource) &&
                    Objects.equals(this.m_statement, that.m_statement);
        }
        return false;
    }
}
