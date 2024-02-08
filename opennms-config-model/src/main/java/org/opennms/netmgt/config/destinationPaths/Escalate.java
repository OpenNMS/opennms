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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "escalate")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("destinationPaths.xsd")
public class Escalate implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "delay", required = true)
    private String m_delay;

    @XmlElement(name = "target", required = true)
    private List<Target> m_targets = new ArrayList<>();

    public Escalate() {
    }

    public String getDelay() {
        return m_delay;
    }

    public void setDelay(final String delay) {
        m_delay = ConfigUtils.assertNotEmpty(delay, "delay");
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

    public void clearTargets() {
        m_targets.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_delay, 
                            m_targets);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Escalate) {
            final Escalate temp = (Escalate)obj;
            return Objects.equals(temp.m_delay, m_delay)
                    && Objects.equals(temp.m_targets, m_targets);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Escalate [delay=" + m_delay + ", targets=" + m_targets
                + "]";
    }

}
