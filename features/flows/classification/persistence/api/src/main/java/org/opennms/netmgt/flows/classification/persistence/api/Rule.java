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
package org.opennms.netmgt.flows.classification.persistence.api;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * A rule defines how a flow should be mapped.
 * From each rule a classifier is created, which allows to classify a flow by this rule.
 *
 * @author mvrueden
 */
@Table(name="classification_rules")
@Entity
public class Rule implements RuleDefinition {

    public static final int MIN_PORT_VALUE = 0;
    public static final int MAX_PORT_VALUE = 65535;

    public boolean canBeReversed() {
        return isOmnidirectional() &&
               (hasSrcPortDefinition() || hasSrcAddressDefinition() || hasDstPortDefinition() || hasDstAddressDefinition());
    }

    @Id
    @SequenceGenerator(name="ruleSequence", sequenceName="ruleNxtId")
    @GeneratedValue(generator="ruleSequence")
    private Integer id;

    /**
     * The name to map to.
     * Must not be null.
     */
    @Column(name="name", nullable=false)
    private String name;

    /**
     * The ip address to map.
     * May contain wildcards, e.g. 192.168.1.*. 192.168.*.*.
     * May be null.
     */
    @Column(name="dst_address")
    private String dstAddress;

    /**
     * The port to map.
     * May define ranges, e.g.
     * 80,8980,8000-9000
     * Must always be provided.
     */
    @Column(name="dst_port")
    private String dstPort;

    // see dstPort
    @Column(name="src_port")
    private String srcPort;

    // see dstAddress
    @Column(name="src_address")
    private String srcAddress;

    @Column(name="exporter_filter")
    private String exporterFilter;

    /**
     * The protocol to map.
     * May contain multiple values,e.g. 2,7,17 or tcp,udp
     */
    @Column(name="protocol")
    private String protocol;

    @ManyToOne(optional=false, fetch= FetchType.LAZY)
    @JoinColumn(name="groupId")
    private Group group;

    /**
     * The position of the rule within it's group.
     * Global order must consider group.position as well.
     * See {@link RulePositionComparator}.
     */
    @Column(name="position", nullable = false)
    private int position;

    @Column(name="omnidirectional")
    private boolean omnidirectional;

    public Rule() {
        
    }

    public Rule(String name, String dstAddress, String dstPort) {
        this.name = name;
        this.dstPort = dstPort;
        this.dstAddress = dstAddress;
    }

    public Rule(String name, String port) {
        this(name, null, port);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDstAddress() {
        return dstAddress;
    }

    public void setDstAddress(String dstAddress) {
        this.dstAddress = dstAddress;
    }

    @Override
    public String getDstPort() {
        return dstPort;
    }

    public void setDstPort(String dstPort) {
        this.dstPort = dstPort;
    }

    @Override
    public String getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(String srcPort) {
        this.srcPort = srcPort;
    }

    @Override
    public String getSrcAddress() {
        return srcAddress;
    }

    public void setSrcAddress(String srcAddress) {
        this.srcAddress = srcAddress;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isOmnidirectional() {
        return this.omnidirectional;
    }

    public void setOmnidirectional(final boolean omnidirectional) {
        this.omnidirectional = omnidirectional;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String getExporterFilter() {
        return exporterFilter;
    }

    public void setExporterFilter(String exporterFilter) {
        this.exporterFilter = exporterFilter;
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public int getGroupPosition() {
        return group == null ? 0 : group.getPosition();
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
            .add("name", name)
            .add("dstAddress", dstAddress)
            .add("dstPort", dstPort)
            .add("srcAddress", srcAddress)
            .add("srcPort", srcPort)
            .add("exporterFilter", exporterFilter)
            .add("protocol", protocol)
            .add("omnidirectional", omnidirectional)
            .add("position", position)
            .add("group", group)
            .toString();
    }
}
