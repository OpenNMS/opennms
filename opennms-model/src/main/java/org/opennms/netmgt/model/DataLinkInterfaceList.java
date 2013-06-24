/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsMapList class.</p>
 */
@XmlRootElement(name = "links")
public class DataLinkInterfaceList extends LinkedList<DataLinkInterface> {



    /**
     * 
     */
    private static final long serialVersionUID = 1980683067851461914L;

    /**
     * <p>Constructor for OnmsMapList.</p>
     */
    public DataLinkInterfaceList() {
        super();
    }

    /**
     * <p>Constructor for OnmsMapList.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public DataLinkInterfaceList(Collection<? extends DataLinkInterface> c) {
        super(c);
    }

    /**
     * <p>getMaps</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name = "link")
    public List<DataLinkInterface> getLinks() {
        return this;
    }

    /**
     * <p>setMaps</p>
     *
     * @param maps a {@link java.util.List} object.
     */
    public void setLinks(List<DataLinkInterface> links) {
        if (links == this) return;
        clear();
        addAll(links);
    }

    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @XmlAttribute(name="count")
    public Integer getCount() {
        return this.size();
    }

}
