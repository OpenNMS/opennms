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
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

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
        if (name == null) {
            throw new IllegalArgumentException("'name' is a required attribute!");
        }
        m_name = name;
    }

    public Optional<String> getInitialDelay() {
        return Optional.ofNullable(m_initialDelay);
    }

    public void setInitialDelay(final String initialDelay) {
        m_initialDelay = initialDelay;
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
        m_escalates.clear();
        m_escalates.addAll(escalates);
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
