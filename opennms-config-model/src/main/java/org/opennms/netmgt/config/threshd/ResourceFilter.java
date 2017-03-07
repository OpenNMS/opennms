/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.threshd;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Class ResourceFilter.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "resource-filter")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResourceFilter implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_CONTENT = "";

    /**
     * internal content storage
     */
    @XmlValue
    private String _content;

    @XmlAttribute(name = "field", required = true)
    private String field;

    public ResourceFilter() { }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof ResourceFilter) {
            ResourceFilter temp = (ResourceFilter)obj;
            boolean equals = Objects.equals(temp._content, _content)
                && Objects.equals(temp.field, field);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'content'. The field 'content' has the following
     * description: internal content storage
     * 
     * @return the value of field 'Content'.
     */
    public String getContent() {
        return this._content != null ? this._content : DEFAULT_CONTENT;
    }

    /**
     * Returns the value of field 'field'.
     * 
     * @return the value of field 'Field'.
     */
    public String getField() {
        return this.field;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            _content, 
            field);
        return hash;
    }

    /**
     * Sets the value of field 'content'. The field 'content' has the following
     * description: internal content storage
     * 
     * @param content the value of field 'content'.
     */
    public void setContent(final String content) {
        this._content = content;
    }

    /**
     * Sets the value of field 'field'.
     * 
     * @param field the value of field 'field'.
     */
    public void setField(final String field) {
        this.field = field;
    }

}
