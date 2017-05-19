/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.poller.outages;

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.ValidateUsing;

/**
 * A scheduled outage
 * 
 */

@XmlRootElement(name="outage", namespace="http://xmlns.opennms.org/xsd/config/poller/outages")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("poll-outages.xsd")
public class Outage extends BasicSchedule implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * List of interfaces to which the outage applies.
     */
    @XmlElement(name="interface")
    private List<Interface> m_interfaces = new ArrayList<>();

    /**
     * List of nodes to which the outage
     *  applies.
     */
    @XmlElement(name="node")
    private List<Node> m_nodes = new ArrayList<>();

    public Outage() {
    }

    public List<Interface> getInterfaces() {
        return m_interfaces;
    }

    public void setInterfaces(final List<Interface> interfaces) {
        if (interfaces == m_interfaces) return;
        m_interfaces.clear();
        if (interfaces != null) m_interfaces.addAll(interfaces);
    }

    public void addInterface(final Interface iface) {
        m_interfaces.add(iface);
    }

    public boolean removeInterface(final Interface iface) {
        return m_interfaces.remove(iface);
    }

    public void clearInterfaces() {
        m_interfaces.clear();
    }

    public List<Node> getNodes() {
        return m_nodes;
    }

    /**
     * Sets the value of 'm_nodes' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param nodes the Vector to copy.
     */
    public void setNodes(final List<Node> nodes) {
        if (nodes == m_nodes) return;
        m_nodes.clear();
        if (nodes != null) m_nodes.addAll(nodes);
    }

    public void addNode(final Node node) {
        m_nodes.add(node);
    }

    public boolean removeNode(final Node node) {
        return m_nodes.remove(node);
    }

    public void clearNodes() {
        m_nodes.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_interfaces, m_nodes);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Outage) {
            final Outage that = (Outage) obj;
            return super.equals(obj) &&
                    Objects.equals(this.m_interfaces, that.m_interfaces) &&
                    Objects.equals(this.m_nodes, that.m_nodes);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Outage[name=" + getName() +
                ",type=" + getType() +
                ",times=" + getTimes() +
                ",interfaces=" + m_interfaces +
                ",nodes=" + m_nodes + "]";
    }

    public static Outage unmarshal(Reader reader) {
        return JaxbUtils.unmarshal(Outage.class, reader);
    }

    public void marshal(Writer writer) {
        JaxbUtils.marshal(this, writer);
    }
}
