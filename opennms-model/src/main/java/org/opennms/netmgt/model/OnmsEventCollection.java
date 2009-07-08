/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="events")
public class OnmsEventCollection extends LinkedList<OnmsEvent> {

	private static final long serialVersionUID = 1L;
	private int m_totalCount;

	public OnmsEventCollection() {
        super();
    }

    public OnmsEventCollection(Collection<? extends OnmsEvent> c) {
        super(c);
    }

    @XmlElement(name="onmsEvent")
    public List<OnmsEvent> getEvents() {
        return this;
    }

    public void setEvents(List<OnmsEvent> events) {
        clear();
        addAll(events);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
    
    @XmlAttribute(name="totalCount")
    public Integer getTotalCount() {
        return m_totalCount;
    }
    
    public void setTotalCount(int count) {
        m_totalCount = count;
    }
}

