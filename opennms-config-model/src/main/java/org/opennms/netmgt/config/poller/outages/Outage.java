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
