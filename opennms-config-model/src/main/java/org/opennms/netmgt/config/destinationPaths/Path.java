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
package org.opennms.netmgt.config.destinationPaths;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "path")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("destinationPaths.xsd")
public class Path implements Serializable {
    public static final String DEFAULT_INITIAL_DELAY = "0s";
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "initial-delay")
    private String m_initialDelay;

    @XmlElement(name = "target", required = true)
    private List<Target> m_targets = new ArrayList<>();

    @XmlElement(name = "escalate")
    private List<Escalate> m_escalates = new ArrayList<>();

    public Path() {
    }

    public Path(final String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Optional<String> getInitialDelay() {
        return Optional.ofNullable(m_initialDelay);
    }

    public void setInitialDelay(final String initialDelay) {
        m_initialDelay = ConfigUtils.normalizeString(initialDelay);
    }

    public List<Target> getTargets() {
        return m_targets;
    }

    public void setTargets(final List<Target> targets) {
        if (targets == m_targets) return;
        m_targets.clear();
        if (targets != null) m_targets.addAll(targets);
    }

    public void addTarget(final Target target) {
        m_targets.add(target);
    }

    public boolean removeTarget(final Target target) {
        return m_targets.remove(target);
    }

    public void clearTargets() {
        m_targets.clear();
    }

    public List<Escalate> getEscalates() {
        return m_escalates;
    }

    public void setEscalates(final List<Escalate> escalates) {
        if (escalates == m_escalates) return;
        m_escalates.clear();
        if (escalates != null) m_escalates.addAll(escalates);
    }

    public void addEscalate(final Escalate escalate) {
        m_escalates.add(escalate);
    }

    public void addEscalate(final int index, final Escalate escalate) {
        m_escalates.add(index, escalate);
    }

    public boolean removeEscalate(final Escalate escalate) {
        return m_escalates.remove(escalate);
    }

    public void clearEscalates() {
        m_escalates.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_name, 
                            m_initialDelay, 
                            m_targets, 
                            m_escalates);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Path) {
            final Path temp = (Path)obj;
            return Objects.equals(temp.m_name, m_name)
                    && Objects.equals(temp.m_initialDelay, m_initialDelay)
                    && Objects.equals(temp.m_targets, m_targets)
                    && Objects.equals(temp.m_escalates, m_escalates);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Path [name=" + m_name + ", initialDelay=" + m_initialDelay
                + ", targets=" + m_targets + ", escalates=" + m_escalates
                + "]";
    }

}
