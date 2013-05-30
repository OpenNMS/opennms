/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>RequisitionNodeCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="nodes")
public class RequisitionNodeCollection extends LinkedList<RequisitionNode> {

    private static final long serialVersionUID = 7563467532077046047L;

    /**
	 * <p>Constructor for RequisitionNodeCollection.</p>
	 */
	public RequisitionNodeCollection() {
        super();
    }

    /**
     * <p>Constructor for RequisitionNodeCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public RequisitionNodeCollection(Collection<? extends RequisitionNode> c) {
        super(c);
    }

    /**
     * <p>getNodes</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="node")
    public List<RequisitionNode> getNodes() {
        return this;
    }

    /**
     * <p>setNodes</p>
     *
     * @param requisitions a {@link java.util.List} object.
     */
    public void setNodes(List<RequisitionNode> requisitions) {
        if (requisitions == this) return;
        clear();
        addAll(requisitions);
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

