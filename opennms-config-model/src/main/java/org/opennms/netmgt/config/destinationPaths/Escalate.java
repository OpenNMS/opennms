/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
        if (delay == null) {
            throw new IllegalArgumentException("'delay' is a required attribute!");
        }
        m_delay = delay;
    }

    public List<Target> getTargets() {
        return m_targets;
    }

    public void setTargets(final List<Target> targets) {
        m_targets.clear();
        m_targets.addAll(targets);
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
