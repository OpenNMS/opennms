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
