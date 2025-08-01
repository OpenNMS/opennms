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
package org.opennms.netmgt.enlinkd.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;

@Entity
@Table(name="user_defined_links")
@XmlRootElement(name="user-defined-link")
@XmlAccessorType(XmlAccessType.NONE)
public class UserDefinedLink {

    @XmlElement(name="node-id-a")
    private Integer nodeIdA;
    @XmlElement(name="component-label-a")
    private String componentLabelA;
    @XmlElement(name="node-id-z")
    private Integer nodeIdZ;
    @XmlElement(name="component-label-z")
    private String componentLabelZ;
    @XmlElement(name="link-id")
    private String linkId;
    @XmlElement(name="link-label")
    private String linkLabel;
    @XmlElement(name="owner")
    private String owner;
    @XmlElement(name="db-id")
    private Integer dbId;

    @Column(name="node_id_a", nullable = false)
    public Integer getNodeIdA() {
        return nodeIdA;
    }

    public void setNodeIdA(Integer nodeIdA) {
        this.nodeIdA = nodeIdA;
    }

    @Column(name="component_label_a")
    public String getComponentLabelA() {
        return componentLabelA;
    }

    public void setComponentLabelA(String componentLabelA) {
        this.componentLabelA = componentLabelA;
    }

    @Column(name="node_id_z", nullable = false)
    public Integer getNodeIdZ() {
        return nodeIdZ;
    }

    public void setNodeIdZ(Integer nodeIdZ) {
        this.nodeIdZ = nodeIdZ;
    }

    @Column(name="component_label_z")
    public String getComponentLabelZ() {
        return componentLabelZ;
    }

    public void setComponentLabelZ(String componentLabelZ) {
        this.componentLabelZ = componentLabelZ;
    }

    @Column(name="link_id", nullable = false)
    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    @Column(name="link_label")
    public String getLinkLabel() {
        return linkLabel;
    }

    public void setLinkLabel(String linkLabel) {
        this.linkLabel = linkLabel;
    }

    @Column(name="owner", nullable = false)
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Id
    @Column(name="id", nullable = false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getDbId() {
        return dbId;
    }

    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nodeIdA", nodeIdA)
                .add("nodeIdZ", nodeIdZ)
                .add("componentLabelA", componentLabelA)
                .add("componentLabelZ", componentLabelZ)
                .add("linkId", linkId)
                .add("linkLabel", linkLabel)
                .add("owner", owner)
                .add("dbId", dbId)
                .toString();
    }
}
