/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="requisitioned_categories")
public class RequisitionedCategoryAssociation implements Serializable, Comparable<RequisitionedCategoryAssociation> {
    private static final long serialVersionUID = 1L;

    private Integer m_id;
    private OnmsNode m_node;
    private OnmsCategory m_category;

    public RequisitionedCategoryAssociation() {
    }

    public RequisitionedCategoryAssociation(final OnmsNode node, final OnmsCategory category) {
        m_node = node;
        m_category = category;
    }

    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")
    @Column(name="id")
    public Integer getId() {
        return m_id;
    }

    public void setId(final Integer id) {
        m_id = id;
    }

    @ManyToOne
    @JoinColumn(name="nodeId", nullable=false)
    public OnmsNode getNode() {
        return m_node;
    }

    public void setNode(final OnmsNode node) {
        m_node = node;
    }

    @ManyToOne
    @JoinColumn(name="categoryId", nullable=false)
    public OnmsCategory getCategory() {
        return m_category;
    }

    public void setCategory(final OnmsCategory category) {
        m_category = category;
    }

    @Override
    public String toString() {
        return "RequisitionedCategoryAssociation [id=" + m_id + ", node=" + m_node.getId() + ", category=" + m_category.getName() + "]";
    }

    @Override
    public int compareTo(final RequisitionedCategoryAssociation o) {
        int ret = m_node.compareTo(o.m_node);
        if (ret == 0) {
            ret = m_category.compareTo(o.m_category);
        }
        return ret;
    }

}
