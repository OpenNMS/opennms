/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.vacuumd;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "automation")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("vacuumd-configuration.xsd")
public class Automation implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final Boolean DEFAULT_ACTIVE_FLAG = Boolean.TRUE;

    /**
     * The name of this automation
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * How often should this automation run
     */
    @XmlAttribute(name = "interval", required = true)
    private Integer m_interval;

    /**
     * reference a trigger from the collection of triggers
     */
    @XmlAttribute(name = "trigger-name")
    private String m_triggerName;

    /**
     * reference an action from the collection of actions
     */
    @XmlAttribute(name = "action-name", required = true)
    private String m_actionName;

    /**
     * specify an event UEI to send
     */
    @XmlAttribute(name = "auto-event-name")
    private String m_autoEventName;

    /**
     * Create an Event from Result of Trigger
     */
    @XmlAttribute(name = "action-event")
    private String m_actionEvent;

    /**
     * enable/disable this automation
     */
    @XmlAttribute(name = "active")
    private Boolean m_active;

    public Automation() {
    }

    public Automation(final String name, final Integer interval,
        final String triggerName, final String actionName,
        final String autoEventName, final String actionEvent,
        final Boolean active) {
        setName(name);
        setInterval(interval);
        setTriggerName(triggerName);
        setActionName(actionName);
        setAutoEventName(autoEventName);
        setActionEvent(actionEvent);
        setActive(active);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Integer getInterval() {
        return m_interval;
    }

    public void setInterval(final Integer interval) {
        m_interval = interval;
    }

    public Optional<String> getTriggerName() {
        return Optional.ofNullable(m_triggerName);
    }

    public void setTriggerName(final String triggerName) {
        m_triggerName = ConfigUtils.normalizeString(triggerName);
    }

    public String getActionName() {
        return m_actionName;
    }

    public void setActionName(final String actionName) {
        m_actionName = ConfigUtils.assertNotEmpty(actionName, "action-name");
    }

    public Optional<String> getAutoEventName() {
        return Optional.ofNullable(m_autoEventName);
    }

    public void setAutoEventName(final String autoEventName) {
        m_autoEventName = ConfigUtils.normalizeString(autoEventName);
    }

    public Optional<String> getActionEvent() {
        return Optional.ofNullable(m_actionEvent);
    }

    public void setActionEvent(final String actionEvent) {
        m_actionEvent = ConfigUtils.normalizeString(actionEvent);
    }

    public Boolean getActive() {
        return m_active == null ? DEFAULT_ACTIVE_FLAG : m_active;
    }

    public void setActive(final Boolean active) {
        m_active = active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name,
                            m_interval,
                            m_triggerName,
                            m_actionName,
                            m_autoEventName,
                            m_actionEvent,
                            m_active);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Automation) {
            final Automation that = (Automation) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_interval, that.m_interval) &&
                    Objects.equals(this.m_triggerName, that.m_triggerName) &&
                    Objects.equals(this.m_actionName, that.m_actionName) &&
                    Objects.equals(this.m_autoEventName, that.m_autoEventName) &&
                    Objects.equals(this.m_actionEvent, that.m_actionEvent) &&
                    Objects.equals(this.m_active, that.m_active);
        }
        return false;
    }
}
