/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import com.google.common.base.Strings;

/**
 * A rule defines how a flow should be mapped.
 * From each rule a classifier is created, which allows to classify a flow by this rule.
 *
 * @author mvrueden
 */
@Table(name="classification_rules")
@Entity
public class Rule {

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
    @Column(name="ipaddress")
    private String ipAddress;

    /**
     * The port to map.
     * May define ranges, e.g.
     * 80,8980,8000-9000
     * Must always be provided.
     */
    @Column(name="port")
    private String port;

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
     * Global order must consider group.priority as well.
     * See {@link RuleComparator}.
     */
    @Column(name="position", nullable = false)
    private int position;

    public Rule() {
        
    }

    public Rule(String name, String ipAddress, String port) {
        this.name = name;
        this.port = port;
        this.ipAddress = ipAddress;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
            .add("name", name)
            .add("ipAddress", ipAddress)
            .add("port", port)
            .add("protocol", protocol)
            .add("group", group)
            .toString();
    }

    public boolean hasProtocolDefinition() {
        return !Strings.isNullOrEmpty(getProtocol());
    }

    public boolean hasIpAddressDefinition() {
        return !Strings.isNullOrEmpty(getIpAddress());
    }

    public boolean hasPortDefinition() {
        return !Strings.isNullOrEmpty(getPort());
    }

    public boolean hasDefinition() {
        return hasProtocolDefinition() || hasIpAddressDefinition() || hasPortDefinition();
    }

    public int calculatePriority() {
        int priority = 0;
        if (hasPortDefinition()) priority++;
        if (hasIpAddressDefinition()) priority++;
        if (hasProtocolDefinition()) priority++;
        return priority;
    }
}
