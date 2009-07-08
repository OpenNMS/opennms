/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.provision.persist.foreignsource;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="foreign-sources")
public class ForeignSourceCollection extends LinkedList<ForeignSource> {

	private static final long serialVersionUID = 1L;

	public ForeignSourceCollection() {
        super();
    }

    public ForeignSourceCollection(Collection<? extends ForeignSource> c) {
        super(c);
    }

    @XmlElement(name="foreign-source")
    public List<ForeignSource> getForeignSources() {
        return this;
    }

    public void setForeignSources(List<ForeignSource> foreignSources) {
        clear();
        addAll(foreignSources);
    }

    @XmlAttribute(name="count")
    public Integer getCount() {
        return this.size();
    }
}

