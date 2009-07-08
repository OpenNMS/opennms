/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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


@XmlRootElement(name = "ipInterfaces")
public class OnmsIpInterfaceList extends LinkedList<OnmsIpInterface> {

    private static final long serialVersionUID = 1123252152117491694L;
    private int m_totalCount;

    public OnmsIpInterfaceList() {
        super();
    }

    public OnmsIpInterfaceList(Collection<? extends OnmsIpInterface> c) {
        super(c);
    }

    @XmlElement(name = "ipInterface")
    public List<OnmsIpInterface> getInterfaces() {
        return this;
    }
    
    public void setInterfaces(List<OnmsIpInterface> interfaces) {
        clear();
        addAll(interfaces);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
        return this.size();
    }
    
    @XmlAttribute(name="totalCount")
    public int getTotalCount() {
        return m_totalCount;
    }
    
    public void setTotalCount(int count) {
        m_totalCount = count;
    }

}
