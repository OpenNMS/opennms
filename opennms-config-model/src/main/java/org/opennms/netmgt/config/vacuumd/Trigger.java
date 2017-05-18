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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * A query to the database with a result set used for actions
 */
@XmlRootElement(name = "trigger")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("vacuumd-configuration.xsd")
public class Trigger implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final List<String> ALLOWED_OPERATORS = Arrays.asList("<", "<=", "=", ">=", ">");
    public static final String DEFAULT_DATA_SOURCE = "opennms";
    public static final Integer DEFAULT_ROW_COUNT = 0;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "data-source")
    private String m_dataSource;

    /**
     * only run the action if the row count evaluates with the operator
     * (defaults to > 0)
     */
    @XmlAttribute(name = "operator")
    private String m_operator;

    @XmlAttribute(name = "row-count")
    private Integer m_rowCount;

    /**
     * Just a generic string used for SQL statements
     */
    @XmlElement(name = "statement", required = true)
    private Statement m_statement;

    public Trigger() {
    }

    public Trigger(final String name, final String dataSource, final String operator, final Integer rowCount, final Statement statement) {
        setName(name);
        setDataSource(dataSource);
        setOperator(operator);
        setRowCount(rowCount);
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

    public String getOperator() {
        return m_operator;
    }

    public void setOperator(final String operator) {
        m_operator = ConfigUtils.assertOnlyContains(ConfigUtils.normalizeString(operator), ALLOWED_OPERATORS, "operator");
    }

    public Integer getRowCount() {
        return m_rowCount == null ? DEFAULT_ROW_COUNT : m_rowCount;
    }

    public void setRowCount(final Integer rowCount) {
        m_rowCount = rowCount;
    }

    public Statement getStatement() {
        return m_statement;
    }

    public void setStatement(final Statement statement) {
        m_statement = ConfigUtils.assertNotNull(statement, "statement");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name,
                            m_dataSource,
                            m_operator,
                            m_rowCount,
                            m_statement);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Trigger) {
            final Trigger that = (Trigger) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_dataSource, that.m_dataSource) &&
                    Objects.equals(this.m_operator, that.m_operator) &&
                    Objects.equals(this.m_rowCount, that.m_rowCount) &&
                    Objects.equals(this.m_statement, that.m_statement);
        }
        return false;
    }
}
