/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.xml.bind.annotation.XmlTransient;

@Entity(name = "Situation")
@DiscriminatorValue("Situation")
public class Situation extends OnmsAlarm {

    private static final long serialVersionUID = 1L;
    private Set<OnmsAlarm> alarms = new HashSet<>();
    
    /**
     * <p>getAlarms</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @XmlTransient
    @ElementCollection
    @JoinTable(name="alarm_situations", joinColumns = @JoinColumn(name="situation_id"))
    @Column(name="alarm_id", nullable=false)
    public Set<OnmsAlarm> getAlarms() {
        return alarms;
    }

    /**
     * <p>setDetails</p>
     *
     * @param alarms a {@link java.util.Set} object.
     */
    public void setAlarms(Set<OnmsAlarm> alarms) {
        this.alarms = alarms;
    }

    public void addAlarm(OnmsAlarm alarm) {
        alarms.add(alarm);
    }
    
}
