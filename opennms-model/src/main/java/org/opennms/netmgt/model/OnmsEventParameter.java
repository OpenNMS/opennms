/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.events.api.EventDatabaseConstants;
import org.opennms.netmgt.xml.event.Parm;

/**
 * The Class OnmsEventParameter.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Entity
@IdClass(OnmsEventParameter.OnmsEventParameterId.class)
@Table(name="event_parameters")
@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.FIELD)
public class OnmsEventParameter implements Serializable {

    private static final long serialVersionUID = 4530678411898489175L;

    public static class OnmsEventParameterId implements Serializable {
        private OnmsEvent event;
        private String name;

        public OnmsEventParameterId() {
        }

        public OnmsEventParameterId(final OnmsEvent event, final String name) {
            this.event = event;
            this.name = name;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) { return false; }
            if (!(obj instanceof OnmsEventParameterId)) { return false; }

            return Objects.equals(this.event, ((OnmsEventParameterId) obj).event) &&
                   Objects.equals(this.name, ((OnmsEventParameterId) obj).name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.event, this.name);
        }

        public OnmsEvent getEvent() {
            return event;
        }

        public void setEvent(OnmsEvent event) {
            this.event = event;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Id
    @XmlTransient
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="eventID")
    private OnmsEvent event;

    /** The name. */
    @Id
    @XmlAttribute(name="name")
    private String name;

    /** The value. */
    @XmlAttribute(name="value")
    private String value;

    /** The type. */
    @XmlAttribute(name="type")
    private String type;

    /**
     * Instantiates a new OpenNMS event parameter.
     */
    public OnmsEventParameter() {}

    /**
     * Instantiates a new OpenNMS event parameter.
     *
     * @param parm the Event parameter object
     */
    public OnmsEventParameter(OnmsEvent event, Parm parm) {
        this.event = event;
        name = parm.getParmName();
        value = EventDatabaseConstants.escape(parm.getValue().getContent() == null ? "" : parm.getValue().getContent(), EventDatabaseConstants.NAME_VAL_DELIM);
        type = parm.getValue().getType();
    }

    public OnmsEventParameter(final OnmsEvent event,
                              final String name,
                              final String value,
                              final String type) {
        this.event = event;
        this.name = name;
        this.value = EventDatabaseConstants.escape(value == null ? "" : value, EventDatabaseConstants.NAME_VAL_DELIM);
        this.type = type;
    }

    public OnmsEvent getEvent() {
        return this.event;
    }

    public void setEvent(final OnmsEvent event) {
        this.event = event;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return EventDatabaseConstants.escape(value, EventDatabaseConstants.NAME_VAL_DELIM);
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = EventDatabaseConstants.escape(value, EventDatabaseConstants.NAME_VAL_DELIM);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(String type) {
        this.type = type;
    }

}
