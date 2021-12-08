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

package org.opennms.netmgt.config.notifd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

public class AutoAcknowledge implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_RESOLUTION_PREFIX = "RESOLVED: ";

    private String resolutionPrefix;

    private String uei;

    private String acknowledge;

    private Boolean notify;

    @JsonProperty("match")
    private List<String> matches = new ArrayList<>();

    public AutoAcknowledge() { }

    public String getResolutionPrefix() {
        return resolutionPrefix != null ? resolutionPrefix : DEFAULT_RESOLUTION_PREFIX;
    }

    public void setResolutionPrefix(final String resolutionPrefix) {
        this.resolutionPrefix = ConfigUtils.normalizeString(resolutionPrefix);
    }

    public String getUei() {
        return uei;
    }

    public void setUei(final String uei) {
        this.uei = ConfigUtils.assertNotEmpty(uei, "uei");
    }

    public String getAcknowledge() {
        return acknowledge;
    }

    public void setAcknowledge(final String acknowledge) {
        this.acknowledge = ConfigUtils.assertNotEmpty(acknowledge, "acknowledge");
    }

    public Boolean getNotify() {
        return notify != null ? notify : Boolean.TRUE;
    }

    public void setNotify(final Boolean notify) {
        this.notify = notify;
    }

    public List<String> getMatches() {
        return matches;
    }

    public void setMatches(final List<String> matches) {
        if (matches == matches) return;
        this.matches.clear();
        if (matches != null) this.matches.addAll(matches);
    }

    public void addMatch(final String match) {
        this.matches.add(match);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolutionPrefix, 
                            uei, 
                            acknowledge, 
                            notify, 
                            matches);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof AutoAcknowledge) {
            final AutoAcknowledge that = (AutoAcknowledge)obj;
            return Objects.equals(this.resolutionPrefix, that.resolutionPrefix)
                    && Objects.equals(this.uei, that.uei)
                    && Objects.equals(this.acknowledge, that.acknowledge)
                    && Objects.equals(this.notify, that.notify)
                    && Objects.equals(this.matches, that.matches);
        }
        return false;
    }
}