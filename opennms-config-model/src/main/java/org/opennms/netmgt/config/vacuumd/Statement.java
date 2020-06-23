/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.vacuumd;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Just a generic string used for SQL statements
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "statement")
@XmlAccessorType(XmlAccessType.FIELD)
public class Statement implements Serializable {
    private static final long serialVersionUID = -3916499875634577541L;

    private final static boolean DEFAULT_TRANSACTIONAL_FLAG = true;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * internal content storage
     */
    @XmlValue
    private String _content = "";

    /**
     * Field _transactional.
     */
    @XmlAttribute(name = "transactional")
    private Boolean _transactional;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Statement() {
        super();
    }

    public Statement(final String value, final boolean transactional) {
        super();
        setContent(value);
        setTransactional(transactional);
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
        Statement other = (Statement) obj;
        if (_content == null) {
            if (other._content != null)
                return false;
        } else if (!_content.equals(other._content))
            return false;
        if (_transactional == null) {
            if (other._transactional != null)
                return false;
        } else if (!_transactional.equals(other._transactional))
            return false;
        return true;
    }

    /**
     * Returns the value of field 'content'. The field 'content' has the
     * following description: internal content storage
     *
     * @return the value of field 'Content'.
     */
    public String getContent() {
        return this._content;
    }

    /**
     * Returns the value of field 'transactional'.
     *
     * @return the value of field 'Transactional'.
     */
    public boolean getTransactional() {
        return _transactional == null ? DEFAULT_TRANSACTIONAL_FLAG
                                     : _transactional;
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
                + ((_content == null) ? 0 : _content.hashCode());
        result = prime * result
                + ((_transactional == null) ? 0 : _transactional.hashCode());
        return result;
    }

    /**
     * Returns the value of field 'transactional'.
     *
     * @return the value of field 'Transactional'.
     */
    public boolean isTransactional() {
        return _transactional == null ? DEFAULT_TRANSACTIONAL_FLAG
                                     : _transactional;
    }

    /**
     * Sets the value of field 'content'. The field 'content' has the
     * following description: internal content storage
     *
     * @param content
     *            the value of field 'content'.
     */
    public void setContent(final String content) {
        this._content = content;
    }

    /**
     * Sets the value of field 'transactional'.
     *
     * @param transactional
     *            the value of field 'transactional'.
     */
    public void setTransactional(final boolean transactional) {
        this._transactional = transactional;
    }
}
