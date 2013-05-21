/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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
 * <p>OnmsIpInterfaceList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "ipInterfaces")
public class OnmsIpInterfaceList extends LinkedList<OnmsIpInterface> {

    private static final long serialVersionUID = 1123252152117491694L;
    private int m_totalCount;

    /**
     * <p>Constructor for OnmsIpInterfaceList.</p>
     */
    public OnmsIpInterfaceList() {
        super();
    }

    /**
     * <p>Constructor for OnmsIpInterfaceList.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsIpInterfaceList(Collection<? extends OnmsIpInterface> c) {
        super(c);
    }

    /**
     * <p>getInterfaces</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name = "ipInterface")
    public List<OnmsIpInterface> getInterfaces() {
        return this;
    }
    
    /**
     * <p>setInterfaces</p>
     *
     * @param interfaces a {@link java.util.List} object.
     */
    public void setInterfaces(List<OnmsIpInterface> interfaces) {
        if (interfaces == this) return;
        clear();
        addAll(interfaces);
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
    
    /**
     * <p>getTotalCount</p>
     *
     * @return a int.
     */
    @XmlAttribute(name="totalCount")
    public int getTotalCount() {
        return m_totalCount;
    }
    
    /**
     * <p>setTotalCount</p>
     *
     * @param count a int.
     */
    public void setTotalCount(int count) {
        m_totalCount = count;
    }

}
