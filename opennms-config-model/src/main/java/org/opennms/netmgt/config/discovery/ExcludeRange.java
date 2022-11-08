/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.discovery;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

import com.google.common.base.MoreObjects;

@XmlRootElement(name = "exclude-range")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("discovery-configuration.xsd")
public class ExcludeRange implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Starting address of the range.
     */
    @XmlElement(name = "begin", required = true)
    private String begin;

    /**
     * Ending address of the range.
     */
    @XmlElement(name = "end", required = true)
    private String end;

    /**
     * The monitoring location where this exclude range
     *  will be excluded
     */
    @XmlAttribute(name = "location")
    private String location;


    public ExcludeRange() {
    }

    public ExcludeRange(final String begin, final String end) {
        setBegin(begin);
        setEnd(end);
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(final String begin) {
        this.begin = ConfigUtils.assertNotEmpty(begin, "begin");
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(final String end) {
        this.end = ConfigUtils.assertNotEmpty(end, "end");
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(location);
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            begin, 
                            end,
                            location);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ExcludeRange) {
            final ExcludeRange temp = (ExcludeRange)obj;
            return Objects.equals(temp.begin, begin)
                    && Objects.equals(temp.end, end)
                    && Objects.equals(temp.location, location);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("begin", begin)
                .add("end", end)
                .add("location", location)
                .toString();
    }
}