/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2004, 2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.report.datablock;

/**
 * This class gives a name to the object.
 * 
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 * @author <A HREF="http://www.oculan.com">oculan.com </A>
 */
public class StandardNamedObject extends Object {
    /**
     * The name of the object
     */
    private String m_name;

    /**
     * Default Constructor.
     */
    public StandardNamedObject() {
        m_name = new String();
    }

    /**
     * Constructor.
     */
    public StandardNamedObject(String name) {
        m_name = new String(name);
    }

    /**
     * Set the name
     * 
     * @param name
     *            The name to be set.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Return the name
     * 
     * @return the name.
     */
    public String getName() {
        return m_name;
    }
}
