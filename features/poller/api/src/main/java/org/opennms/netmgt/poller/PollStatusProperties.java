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
package org.opennms.netmgt.poller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="properties")
@XmlAccessorType(XmlAccessType.NONE)
public class PollStatusProperties {

    @XmlElement(name="property")
    private List<PollStatusProperty> properties;

    public PollStatusProperties() {
        properties = new ArrayList<>(0);
    }

    public PollStatusProperties(List<PollStatusProperty> properties) {
        this.properties = properties;
    }

    public List<PollStatusProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<PollStatusProperty> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PollStatusProperties)) {
            return false;
        }
        PollStatusProperties castOther = (PollStatusProperties) other;
        return Objects.equals(properties, castOther.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties);
    }

}
