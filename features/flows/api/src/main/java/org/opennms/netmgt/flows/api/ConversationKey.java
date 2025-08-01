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
