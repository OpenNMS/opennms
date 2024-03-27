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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpObjIdXmlAdapter;

@XmlRootElement(name="snmp-walk-request")
@XmlAccessorType(XmlAccessType.NONE)
public class SnmpWalkRequestDTO {

    public static boolean DEFAULT_SINGLE_INSTANCE = false;

    @XmlAttribute(name="correlation-id")
    private String correlationId;

    @XmlElement(name="oid")
    @XmlJavaTypeAdapter(SnmpObjIdXmlAdapter.class)
    private List<SnmpObjId> oids = new ArrayList<>(0);

    @XmlAttribute(name="max-repetitions")
    private Integer maxRepetitions;

    @XmlAttribute(name="instance")
    @XmlJavaTypeAdapter(SnmpObjIdXmlAdapter.class)
    private SnmpObjId instance;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public List<SnmpObjId> getOids() {
        return oids;
    }

    public void setOids(List<SnmpObjId> oids) {
        this.oids = oids;
    }

    public void setMaxRepetitions(Integer maxRepetitions) {
        this.maxRepetitions = maxRepetitions;
    }

    public Integer getMaxRepetitions() {
        return maxRepetitions;
    }

    public void setInstance(SnmpObjId instance) {
        this.instance = instance;
    }

    public SnmpObjId getInstance() {
        return instance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, oids, maxRepetitions, instance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SnmpWalkRequestDTO other = (SnmpWalkRequestDTO) obj;
        return Objects.equals(this.correlationId, other.correlationId)
                && Objects.equals(this.oids, other.oids)
                && Objects.equals(this.maxRepetitions, other.maxRepetitions)
                && Objects.equals(this.instance, other.instance);
    }
}
