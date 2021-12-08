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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opennms.netmgt.config.utils.ConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutoAcknowledgeAlarm implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    @JsonIgnore
    private static final String DEFAULT_RESOLUTION_PREFIX = "RESOLVED: ";

    private String resolutionPrefix;

    private Boolean notify;

    @JsonProperty("uei")
    private List<String> ueis = new ArrayList<>();

    public AutoAcknowledgeAlarm() { }

    public String getResolutionPrefix() {
        return resolutionPrefix != null ? resolutionPrefix : DEFAULT_RESOLUTION_PREFIX;
    }

    public void setResolutionPrefix(final String resolutionPrefix) {
        this.resolutionPrefix = ConfigUtils.normalizeString(resolutionPrefix);
    }

    public Boolean getNotify() {
        return notify != null ? notify : Boolean.TRUE;
    }

    public void setNotify(final Boolean notify) {
        this.notify = notify;
    }

    public List<String> getUeis() {
        return ueis;
    }

    public void setUei(final List<String> ueis) {
        if (this.ueis == ueis) return;
        ueis.clear();
        if (ueis != null) ueis.addAll(ueis);
    }

    public void addUei(final String uei) {
        ueis.add(uei);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof AutoAcknowledgeAlarm) {
            final AutoAcknowledgeAlarm that = (AutoAcknowledgeAlarm)obj;
            return Objects.equals(this.resolutionPrefix, that.resolutionPrefix)
                    && Objects.equals(this.notify, that.notify)
                    && Objects.equals(this.ueis, that.ueis);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolutionPrefix,
                            notify,
                            ueis);
    }
}
