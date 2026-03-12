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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.events.api.EventDatabaseConstants;
import org.opennms.netmgt.xml.event.Parm;

/**
 * Plain POJO representing an event parameter.
 * Formerly a JPA entity mapped to the event_parameters table.
 * Now used as a value object embedded in OnmsAlarm and alarm-related DTOs.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.FIELD)
public class OnmsEventParameter implements Serializable {

    private static final long serialVersionUID = 4530678411898489175L;

    /** The name. */
    @XmlAttribute(name="name")
    private String name;

    /** The value. */
    @XmlAttribute(name="value")
    private String value;

    /** The type. */
    @XmlAttribute(name="type")
    private String type;

    /** Helper attribute to maintain the right order of event parameters. */
    @XmlTransient
    private int position;

    /**
     * Instantiates a new OpenNMS event parameter.
     */
    public OnmsEventParameter() {}

    /**
     * Instantiates a new OpenNMS event parameter from an XML Parm.
     *
     * @param parm the Event parameter object
     */
    public OnmsEventParameter(Parm parm) {
        name = parm.getParmName();
        value = EventDatabaseConstants.sanitize(parm.getValue().getContent() == null ? "" : parm.getValue().getContent());
        type = parm.getValue().getType();
    }

    public OnmsEventParameter(final String name,
                              final String value,
                              final String type) {
        this.name = name;
        this.value = EventDatabaseConstants.sanitize(value == null ? "" : value);
        this.type = type;
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
