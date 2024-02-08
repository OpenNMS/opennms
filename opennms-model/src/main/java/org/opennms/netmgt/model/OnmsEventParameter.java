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

    /** helper attribute to maintain the right order of event parameters when saving and retrieving to/from database. */
    @XmlTransient
    private int position;

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
        value = EventDatabaseConstants.sanitize(parm.getValue().getContent() == null ? "" : parm.getValue().getContent());
        type = parm.getValue().getType();
    }

    public OnmsEventParameter(final OnmsEvent event,
                              final String name,
                              final String value,
                              final String type) {
        this.event = event;
        this.name = name;
        this.value = EventDatabaseConstants.sanitize(value == null ? "" : value);
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
        return value;
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
        this.value = EventDatabaseConstants.sanitize(value);
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

    int getPosition() {
        return position;
    }

    void setPosition(int position) {
        this.position = position;
    }

}
