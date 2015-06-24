/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.vacuumd;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

/**
 * A query to the database with a result set used for actions
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "trigger")
@XmlAccessorType(XmlAccessType.FIELD)
public class Trigger implements Serializable {
    private static final long serialVersionUID = -2490841506653475960L;

    public static final String DEFAULT_DATA_SOURCE = "opennms";

    public static final int DEFAULT_ROW_COUNT = 0;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _name.
     */
    @XmlAttribute(name = "name")
    private String _name;

    /**
     * Field _dataSource.
     */
    @XmlAttribute(name = "data-source")
    private String _dataSource;

    /**
     * only run the action if the row count evaluates with the operator
     * (defaults to > 0)
     */
    @XmlAttribute(name = "operator")
    private String _operator;

    /**
     * Field _rowCount.
     */
    @XmlAttribute(name = "row-count")
    private Integer _rowCount;

    /**
     * Just a generic string used for SQL statements
     */
    @XmlElement(name = "statement")
    private Statement _statement;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Trigger() {
        super();
    }

    public Trigger(final String name, final String dataSource,
            final String operator, final int rowCount,
            final Statement statement) {
        super();
        setName(name);
        setDataSource(dataSource);
        setOperator(operator);
        setRowCount(rowCount);
        setStatement(statement);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Overrides the Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Trigger other = (Trigger) obj;
        if (_dataSource == null) {
            if (other._dataSource != null)
                return false;
        } else if (!_dataSource.equals(other._dataSource))
            return false;
        if (_name == null) {
            if (other._name != null)
                return false;
        } else if (!_name.equals(other._name))
            return false;
        if (_operator == null) {
            if (other._operator != null)
                return false;
        } else if (!_operator.equals(other._operator))
            return false;
        if (_rowCount == null) {
            if (other._rowCount != null)
                return false;
        } else if (!_rowCount.equals(other._rowCount))
            return false;
        if (_statement == null) {
            if (other._statement != null)
                return false;
        } else if (!_statement.equals(other._statement))
            return false;
        return true;
    }

    /**
     * Returns the value of field 'dataSource'.
     *
     * @return the value of field 'DataSource'.
     */
    public String getDataSource() {
        return _dataSource == null ? DEFAULT_DATA_SOURCE : _dataSource;
    }

    /**
     * Returns the value of field 'name'.
     *
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this._name;
    }

    /**
     * Returns the value of field 'operator'. The field 'operator' has the
     * following description: only run the action if the row count evaluates
     * with the operator (defaults to > 0)
     *
     * @return the value of field 'Operator'.
     */
    public String getOperator() {
        return this._operator;
    }

    /**
     * Returns the value of field 'rowCount'.
     *
     * @return the value of field 'RowCount'.
     */
    public int getRowCount() {
        return _rowCount == null ? DEFAULT_ROW_COUNT : _rowCount;
    }

    /**
     * Returns the value of field 'statement'. The field 'statement' has the
     * following description: Just a generic string used for SQL statements
     *
     * @return the value of field 'Statement'.
     */
    public Statement getStatement() {
        return this._statement;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_dataSource == null) ? 0 : _dataSource.hashCode());
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
        result = prime * result
                + ((_operator == null) ? 0 : _operator.hashCode());
        result = prime * result
                + ((_rowCount == null) ? 0 : _rowCount.hashCode());
        result = prime * result
                + ((_statement == null) ? 0 : _statement.hashCode());
        return result;
    }

    /**
     * Sets the value of field 'dataSource'.
     *
     * @param dataSource
     *            the value of field 'dataSource'.
     */
    public void setDataSource(final String dataSource) {
        this._dataSource = dataSource;
    }

    /**
     * Sets the value of field 'name'.
     *
     * @param name
     *            the value of field 'name'.
     */
    public void setName(final String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'operator'. The field 'operator' has the
     * following description: only run the action if the row count evalutes
     * with the operator (defaults to > 0)
     *
     * @param operator
     *            the value of field 'operator'.
     */
    public void setOperator(final String operator) {
        this._operator = operator;
    }

    /**
     * Sets the value of field 'rowCount'.
     *
     * @param rowCount
     *            the value of field 'rowCount'.
     */
    public void setRowCount(final int rowCount) {
        this._rowCount = rowCount;
    }

    /**
     * Sets the value of field 'statement'. The field 'statement' has the
     * following description: Just a generic string used for SQL statements
     *
     * @param statement
     *            the value of field 'statement'.
     */
    public void setStatement(final Statement statement) {
        this._statement = statement;
    }
}
