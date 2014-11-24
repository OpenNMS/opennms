/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The Class XRow (XPort Row).
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="row")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class XRow extends Row {

    /** The time stamp expressed in seconds since 1970-01-01 UTC. */
    private Long timestamp;

    /**
     * Gets the time stamp.
     * <p>Expressed in seconds since 1970-01-01 UTC</p>
     * 
     * @return the time stamp
     */
    @XmlElement(name="t")
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the time stamp.
     *
     * @param timestamp the new time stamp
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
