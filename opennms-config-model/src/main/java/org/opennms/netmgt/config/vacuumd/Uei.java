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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Class Uei.
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "uei")
@XmlAccessorType(XmlAccessType.FIELD)
public class Uei implements Serializable {
    private static final long serialVersionUID = -4848875755202066725L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * internal content storage
     */
    @XmlValue
    private String _content = "";

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Uei() {
        super();
    }

    public Uei(final String value) {
        super();
        setContent(value);
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

        if (obj instanceof Uei) {

            Uei temp = (Uei) obj;
            if (this._content != null) {
                if (temp._content == null)
                    return false;
                else if (!(this._content.equals(temp._content)))
                    return false;
            } else if (temp._content != null)
                return false;
            return true;
        }
        return false;
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
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        if (_content != null) {
            result = 37 * result + _content.hashCode();
        }

        return result;
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
}
