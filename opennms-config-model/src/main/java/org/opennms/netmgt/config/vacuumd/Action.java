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

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * actions modify the database based on results of a trigger
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "action")
@XmlAccessorType(XmlAccessType.FIELD)
public class Action implements Serializable {
    private static final long serialVersionUID = -3299921998796224904L;

    private static final String DEFAULT_DATA_SOURCE = "opennms";

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
     * Just a generic string used for SQL statements
     */
    @XmlElement(name = "statement")
    private Statement _statement;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Action() {
        super();
    }

    public Action(final String name, final String dataSource,
            final Statement statement) {
        super();
        setName(name);
        setDataSource(dataSource);
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
    @Override()
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Action) {

            Action temp = (Action) obj;
            if (this._name != null) {
                if (temp._name == null)
                    return false;
                else if (!(this._name.equals(temp._name)))
                    return false;
            } else if (temp._name != null)
                return false;
            if (this._dataSource != null) {
                if (temp._dataSource == null)
                    return false;
                else if (!(this._dataSource.equals(temp._dataSource)))
                    return false;
            } else if (temp._dataSource != null)
                return false;
            if (this._statement != null) {
                if (temp._statement == null)
                    return false;
                else if (!(this._statement.equals(temp._statement)))
                    return false;
            } else if (temp._statement != null)
                return false;
            return true;
        }
        return false;
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
    public int hashCode() {
        int result = 17;

        if (_name != null) {
            result = 37 * result + _name.hashCode();
        }
        if (_dataSource != null) {
            result = 37 * result + _dataSource.hashCode();
        }
        if (_statement != null) {
            result = 37 * result + _statement.hashCode();
        }

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
