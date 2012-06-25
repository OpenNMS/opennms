/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.collectd.jmx;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * <p>Attr class.</p>
 *
 * @author mjamison
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public class Attr {

    private String m_name;

    /**
     * Object's alias (e.g., "sysDescription").
     */
    private String m_alias;

    /**
     * Object's expected data type.
     */
    private String m_type;

    /**
     * Object's maximum value.
     */
    private String m_maxval;

    /**
     * Object's minimum value.
     */
    private String m_minval;

    /**
     * Constructor
     */
    public Attr() {
        m_name = null;
        m_alias = null;
        m_type = null;
        m_maxval = null;
        m_minval = null;
    }

    /**
     * This method is used to assign the object's identifier.
     *
     * @param oid -
     *            object identifier in dotted decimal notation (e.g.,
     *            ".1.3.6.1.2.1.1.1")
     */
    public void setName(String oid) {
        m_name = oid;
    }

    /**
     * This method is used to assign the object's alias.
     *
     * @param alias -
     *            object alias (e.g., "sysDescription")
     */
    public void setAlias(String alias) {
        m_alias = alias;
    }

    /**
     * This method is used to assign the object's expected data type.
     *
     * @param type -
     *            object's data type
     */
    public void setType(String type) {
        m_type = type;
    }

    /**
     * This method is used to assign the object's maximum value.
     *
     * @param maxval
     *            object's maximum value
     */
    public void setMaxval(String maxval) {
        m_maxval = maxval;
    }

    /**
     * This method is used to assign the object's minimum value.
     *
     * @param minval
     *            object's minimum value
     */
    public void setMinval(String minval) {
        m_minval = minval;
    }

    /**
     * Returns the object's identifier.
     *
     * @return The object's identifier string.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the object's maximum value.
     *
     * @return The object's maxval.
     */
    public String getMaxval() {
        return m_maxval;
    }

    /**
     * Returns the object's minimum value.
     *
     * @return The object's minval.
     */
    public String getMinval() {
        return m_minval;
    }

    /**
     * Returns the object's alias.
     *
     * @return The object's alias.
     */
    public String getAlias() {
        return m_alias;
    }

    /**
     * Returns the object's data type.
     *
     * @return The object's data type
     */
    public String getType() {
        return m_type;
    }

    /**
     * {@inheritDoc}
     *
     * This method is responsible for comparing this MibObject with the passed
     * Object to determine if they are equivalent. The objects are equivalent if
     * the argument is a MibObject object with the same object identifier,
     * instance, alias and type.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;

        Attr aMibObject;

        try {
            aMibObject = (Attr) object;
        } catch (ClassCastException cce) {
            return false;
        }

        if (m_name.equals(aMibObject.getName())) {
            return true;
        }
        return false;

    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 11)
            .append(this.getName())
            .append(this.getType())
            .append(this.getAlias())
            .append(this.getMinval())
            .append(this.getMaxval())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * This method is responsible for returning a String object which represents
     * the content of this MibObject. Primarily used for debugging purposes.
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        // Build the buffer
        buffer.append("\n   name:     ").append(m_name);
        buffer.append("\n   alias:    ").append(m_alias);
        buffer.append("\n   type:     ").append(m_type);

        return buffer.toString();
    }
}
