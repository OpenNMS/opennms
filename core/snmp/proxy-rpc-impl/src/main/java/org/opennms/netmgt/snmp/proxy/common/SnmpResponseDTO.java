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
package org.opennms.netmgt.snmp.proxy.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.snmp.SnmpResult;

@XmlRootElement(name="response")
@XmlAccessorType(XmlAccessType.NONE)
public class SnmpResponseDTO {

    @XmlAttribute(name="correlation-id")
    private String correlationId;

    @XmlElement(name="result")
    private List<SnmpResult> results = new ArrayList<>(0);

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setResults(List<SnmpResult> results) {
        this.results = results;
    }

    public List<SnmpResult> getResults() {
        return results;
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, results);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SnmpResponseDTO other = (SnmpResponseDTO) obj;
        return Objects.equals(this.correlationId, other.correlationId)
                && Objects.equals(this.results, other.results);
    }
}
