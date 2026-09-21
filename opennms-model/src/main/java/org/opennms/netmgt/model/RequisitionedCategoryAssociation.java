/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId", allocationSize = 1)
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
