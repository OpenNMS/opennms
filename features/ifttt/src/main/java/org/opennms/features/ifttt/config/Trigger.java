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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.ifttt.config;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Trigger {
    /**
     * the IFTTT event to send
     */
    private String eventName = "";
    /**
     * the value1 to be used
     */
    private String value1 = "";
    /**
     * the value2 to be used
     */
    private String value2 = "";
    /**
     * the value3 to be used
     */
    private String value3 = "";
    /**
     * delay (ms) after executing the trigger
     */
    private int delay;

    public Trigger() {
    }

    @XmlAttribute(name = "eventName")
    public String getEventName() {
        return eventName;
    }

    public void setEventName(final String eventName) {
        this.eventName = eventName;
    }

    @XmlElement(name = "value1")
    public String getValue1() {
        return value1;
    }

    public void setValue1(final String value1) {
        this.value1 = value1;
    }

    @XmlElement(name = "value2")
    public String getValue2() {
        return value2;
    }

    public void setValue2(final String value2) {
        this.value2 = value2;
    }

    @XmlElement(name = "value3")
    public String getValue3() {
        return value3;
    }

    public void setValue3(final String value3) {
        this.value3 = value3;
    }

    @XmlAttribute
    public int getDelay() {
        return delay;
    }

    public void setDelay(final int delay) {
        this.delay = delay;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Trigger)) return false;

        final Trigger that = (Trigger) o;

        return Objects.equals(delay, that.delay) &&
               Objects.equals(value1, that.value1) &&
               Objects.equals(value2, that.value2) &&
               Objects.equals(value3, that.value3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventName, value1, value2, value3, delay);
    }
}
