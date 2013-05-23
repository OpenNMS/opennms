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
 * <p>OnmsNodeList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "nodes")
public class OnmsNodeList extends LinkedList<OnmsNode> {

    private static final long serialVersionUID = 8031737923157780179L;
    private int m_totalCount;
    
    /**
     * <p>Constructor for OnmsNodeList.</p>
     */
    public OnmsNodeList() {
        super();
    }

    /**
     * <p>Constructor for OnmsNodeList.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsNodeList(Collection<? extends OnmsNode> c) {
        super(c);
    }

    /**
     * <p>getNodes</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name = "node")
    public List<OnmsNode> getNodes() {
        return this;
    }
    
    /**
     * <p>setNodes</p>
     *
     * @param nodes a {@link java.util.List} object.
     */
    public void setNodes(List<OnmsNode> nodes) {
        if (nodes == this) return;
        clear();
        addAll(nodes);
    }
    
    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @XmlAttribute(name="count")
    public int getCount() {
        return this.size();
    }

    // The property has a getter "" but no setter. For unmarshalling, please define setters.
    public void setCount(final int count) {
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
