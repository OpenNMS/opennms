//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/*
 * Created on Mar 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.collectd;

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

    /**
     * This method is responsible for returning a String object which represents
     * the content of this MibObject. Primarily used for debugging purposes.
     *
     * @return String which represents the content of this MibObject
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        // Build the buffer
        buffer.append("\n   name:     ").append(m_name);
        buffer.append("\n   alias:    ").append(m_alias);
        buffer.append("\n   type:     ").append(m_type);

        return buffer.toString();
    }
}
