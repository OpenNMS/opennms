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
package org.opennms.netmgt.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OnmsLocationAvailDataPoint {

    @XmlElement(name="time")
    private Date m_time;

    @XmlElement(name="values")
    private List<OnmsLocationAvailDefinition> m_definitions = new ArrayList<>();

    public void setTime(Date time) {
        m_time = time;
    }

    public long getTime() {
        return m_time.getTime();
    }

    public void addAvailDefinition(OnmsLocationAvailDefinition definition) {
        m_definitions.add(definition);
    }

    public List<OnmsLocationAvailDefinition> getDefininitions(){
        return m_definitions;
    }
}
