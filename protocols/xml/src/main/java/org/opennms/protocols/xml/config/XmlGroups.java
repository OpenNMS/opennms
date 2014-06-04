/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class XmlGroups.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="xml-groups")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGroups implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8626665420339279584L;

    /** The XML groups list. */
    @XmlElement(name="xml-group", required=true)
    private List<XmlGroup> m_xmlGroups = new ArrayList<XmlGroup>();

    /**
     * Instantiates a new XML source.
     */
    public XmlGroups() {
    }

    /**
     * Gets the XML groups.
     *
     * @return the XML groups
     */
    public List<XmlGroup> getXmlGroups() {
        return m_xmlGroups;
    }

    /**
     * Sets the XML groups.
     *
     * @param xmlGroups the new XML groups
     */
    public void setXmlGroups(List<XmlGroup> xmlGroups) {
        m_xmlGroups = xmlGroups;
    }

    /**
     * Adds the XML group.
     *
     * @param group the group
     */
    public void addXmlGroup(XmlGroup group) {
        m_xmlGroups.add(group);
    }

    /**
     * Removes the XML group.
     *
     * @param group the group
     */
    public void removeXmlGroup(XmlGroup group) {
        m_xmlGroups.remove(group);
    }

    /**
     * Removes the group by name.
     *
     * @param name the name
     */
    public void removeGroupByName(String name) {
        for (Iterator<XmlGroup> itr = m_xmlGroups.iterator(); itr.hasNext();) {
            XmlGroup query = itr.next();
            if(query.getName().equals(name)) {
                m_xmlGroups.remove(query);
                return;
            }
        }
    }

}
