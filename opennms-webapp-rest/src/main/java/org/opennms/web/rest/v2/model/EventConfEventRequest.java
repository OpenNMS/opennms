/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements. See the LICENSE.md file
 * distributed with this work for additional information.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License"); you may not use
 * this file except in compliance with the License.
 * https://www.gnu.org/licenses/agpl-3.0.txt
 */
package org.opennms.web.rest.v2.model;

import org.opennms.netmgt.xml.eventconf.Event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "addEvent")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventConfEventRequest {
    @XmlElement(name = "event", namespace = "http://xmlns.opennms.org/xsd/eventconf")
    private org.opennms.netmgt.xml.eventconf.Event event;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

}
