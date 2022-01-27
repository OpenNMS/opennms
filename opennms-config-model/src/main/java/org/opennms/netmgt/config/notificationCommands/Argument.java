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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.notificationCommands;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "argument")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notificationCommands.xsd")
public class Argument implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "streamed", required = true)
    @JsonProperty(value = "streamed", required = true)
    private Boolean streamed;

    @XmlElement(name = "substitution")
    @JsonProperty("substitution")
    private String substitution;

    @XmlElement(name = "switch")
    @JsonProperty("switch")
    private String swiitch;

    public Argument() {
    }

    public Boolean getStreamed() {
        return this.streamed;
    }

    public void setStreamed(final Boolean streamed) {
        this.streamed = ConfigUtils.assertNotNull(streamed, "streamed");
    }

    public Optional<String> getSubstitution() {
        return Optional.ofNullable(substitution);
    }

    public void setSubstitution(final String substitution) {
        this.substitution = ConfigUtils.normalizeString(substitution);
    }

    public Optional<String> getSwiitch() {
        return Optional.ofNullable(this.swiitch);
    }

    public void setSwiitch(final String s) {
        this.swiitch = ConfigUtils.normalizeString(s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.streamed, this.substitution,  this.swiitch);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Argument) {
            final Argument that = (Argument)obj;
            return Objects.equals(this.streamed, that.streamed)
                    && Objects.equals(this.substitution, that.substitution)
                    && Objects.equals(this.swiitch, that.swiitch);
        }
        return false;
    }

}
