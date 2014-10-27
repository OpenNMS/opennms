/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.vacuumd;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Automation.
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "automation")
@XmlAccessorType(XmlAccessType.FIELD)
public class Automation implements Serializable {
    private static final long serialVersionUID = -1003423077476370628L;

    private static final boolean DEFAULT_ACTIVE_FLAG = true;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * The name of this automation
     */
    @XmlAttribute(name = "name")
    private String _name;

    /**
     * How often should this automation run
     */
    @XmlAttribute(name = "interval")
    private Integer _interval;

    /**
     * reference a trigger from the collection of triggers
     */
    @XmlAttribute(name = "trigger-name")
    private String _triggerName;

    /**
     * reference an action from the collection of actions
     */
    @XmlAttribute(name = "action-name")
    private String _actionName;

    /**
     * specify an event UEI to send
     */
    @XmlAttribute(name = "auto-event-name")
    private String _autoEventName;

    /**
     * Create an Event from Result of Trigger
     */
    @XmlAttribute(name = "action-event")
    private String _actionEvent;

    /**
     * enable/disable this automation
     */
    @XmlAttribute(name = "active")
    private Boolean _active;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Automation() {
        super();
    }

    public Automation(final String name, final int interval,
            final String triggerName, final String actionName,
            final String autoEventName, final String actionEvent,
            final boolean active) {
        super();
        setName(name);
        setInterval(interval);
        setTriggerName(triggerName);
        setActionName(actionName);
        setAutoEventName(autoEventName);
        setActionEvent(actionEvent);
        setActive(active);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Overrides the Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Automation other = (Automation) obj;
        if (_actionEvent == null) {
            if (other._actionEvent != null)
                return false;
        } else if (!_actionEvent.equals(other._actionEvent))
            return false;
        if (_actionName == null) {
            if (other._actionName != null)
                return false;
        } else if (!_actionName.equals(other._actionName))
            return false;
        if (_active == null) {
            if (other._active != null)
                return false;
        } else if (!_active.equals(other._active))
            return false;
        if (_autoEventName == null) {
            if (other._autoEventName != null)
                return false;
        } else if (!_autoEventName.equals(other._autoEventName))
            return false;
        if (_interval == null) {
            if (other._interval != null)
                return false;
        } else if (!_interval.equals(other._interval))
            return false;
        if (_name == null) {
            if (other._name != null)
                return false;
        } else if (!_name.equals(other._name))
            return false;
        if (_triggerName == null) {
            if (other._triggerName != null)
                return false;
        } else if (!_triggerName.equals(other._triggerName))
            return false;
        return true;
    }

    /**
     * Returns the value of field 'actionEvent'. The field 'actionEvent' has
     * the following description: Create an Event from Result of Trigger
     *
     * @return the value of field 'ActionEvent'.
     */
    public String getActionEvent() {
        return this._actionEvent;
    }

    /**
     * Returns the value of field 'actionName'. The field 'actionName' has the
     * following description: reference an action from the collection of
     * actions
     *
     * @return the value of field 'ActionName'.
     */
    public String getActionName() {
        return this._actionName;
    }

    /**
     * Returns the value of field 'active'. The field 'active' has the
     * following description: enable/disable this automation
     *
     * @return the value of field 'Active'.
     */
    public boolean getActive() {
        return _active == null ? DEFAULT_ACTIVE_FLAG : _active;
    }

    /**
     * Returns the value of field 'autoEventName'. The field 'autoEventName'
     * has the following description: specify an event UEI to send
     *
     * @return the value of field 'AutoEventName'.
     */
    public String getAutoEventName() {
        return this._autoEventName;
    }

    /**
     * Returns the value of field 'interval'. The field 'interval' has the
     * following description: How ofter should this autmation run
     *
     * @return the value of field 'Interval'.
     */
    public int getInterval() {
        return this._interval;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the following
     * description: The name of this automation
     *
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this._name;
    }

    /**
     * Returns the value of field 'triggerName'. The field 'triggerName' has
     * the following description: reference a trigger from the collection of
     * triggers
     *
     * @return the value of field 'TriggerName'.
     */
    public String getTriggerName() {
        return this._triggerName;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_actionEvent == null) ? 0 : _actionEvent.hashCode());
        result = prime * result
                + ((_actionName == null) ? 0 : _actionName.hashCode());
        result = prime * result
                + ((_active == null) ? 0 : _active.hashCode());
        result = prime * result
                + ((_autoEventName == null) ? 0 : _autoEventName.hashCode());
        result = prime * result
                + ((_interval == null) ? 0 : _interval.hashCode());
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
        result = prime * result
                + ((_triggerName == null) ? 0 : _triggerName.hashCode());
        return result;
    }

    /**
     * Returns the value of field 'active'. The field 'active' has the
     * following description: enable/disable this automation
     *
     * @return the value of field 'Active'.
     */
    public boolean isActive() {
        return _active == null ? DEFAULT_ACTIVE_FLAG : _active;
    }

    /**
     * Sets the value of field 'actionEvent'. The field 'actionEvent' has the
     * following description: Create an Event from Result of Trigger
     *
     * @param actionEvent
     *            the value of field 'actionEvent'.
     */
    public void setActionEvent(final String actionEvent) {
        this._actionEvent = actionEvent;
    }

    /**
     * Sets the value of field 'actionName'. The field 'actionName' has the
     * following description: reference an action from the collection of
     * actions
     *
     * @param actionName
     *            the value of field 'actionName'.
     */
    public void setActionName(final String actionName) {
        this._actionName = actionName;
    }

    /**
     * Sets the value of field 'active'. The field 'active' has the following
     * description: enable/disable this automation
     *
     * @param active
     *            the value of field 'active'.
     */
    public void setActive(final boolean active) {
        this._active = active;
    }

    /**
     * Sets the value of field 'autoEventName'. The field 'autoEventName' has
     * the following description: specify an event UEI to send
     *
     * @param autoEventName
     *            the value of field 'autoEventName'.
     */
    public void setAutoEventName(final String autoEventName) {
        this._autoEventName = autoEventName;
    }

    /**
     * Sets the value of field 'interval'. The field 'interval' has the
     * following description: How ofter should this autmation run
     *
     * @param interval
     *            the value of field 'interval'.
     */
    public void setInterval(final int interval) {
        this._interval = interval;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the following
     * description: The name of this automation
     *
     * @param name
     *            the value of field 'name'.
     */
    public void setName(final String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'triggerName'. The field 'triggerName' has the
     * following description: reference a trigger from the collection of
     * triggers
     *
     * @param triggerName
     *            the value of field 'triggerName'.
     */
    public void setTriggerName(final String triggerName) {
        this._triggerName = triggerName;
    }
}
