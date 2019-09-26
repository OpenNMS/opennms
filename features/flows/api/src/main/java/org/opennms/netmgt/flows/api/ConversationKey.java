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

package org.opennms.netmgt.flows.api;

import java.util.Objects;

/**
 * Contains all of the fields used to uniquely identify a conversation.
 */
public class ConversationKey {

    private final String location;
    private final Integer protocol;
    private final String lowerIp;
    private final String upperIp;
    private final String application;

    public ConversationKey(String location, Integer protocol, String lowerIp, String upperIp, String application) {
        this.location = location;
        this.protocol = protocol;
        this.lowerIp = lowerIp;
        this.upperIp = upperIp;
        this.application = application;
    }

    public String getLocation() {
        return location;
    }

    public Integer getProtocol() {
        return protocol;
    }

    public String getLowerIp() {
        return lowerIp;
    }

    public String getUpperIp() {
        return upperIp;
    }

    public String getApplication() {
        return application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversationKey that = (ConversationKey) o;
        return Objects.equals(protocol, that.protocol) &&
                Objects.equals(application, that.application) &&
                Objects.equals(location, that.location) &&
                Objects.equals(lowerIp, that.lowerIp) &&
                Objects.equals(upperIp, that.upperIp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, protocol, lowerIp, upperIp, application);
    }

    @Override
    public String toString() {
        return "ConversationKey{" +
                "location='" + location + '\'' +
                ", protocol=" + protocol +
                ", lowerIp='" + lowerIp + '\'' +
                ", upperIp='" + upperIp + '\'' +
                ", application='" + application + '\'' +
                '}';
    }
}
