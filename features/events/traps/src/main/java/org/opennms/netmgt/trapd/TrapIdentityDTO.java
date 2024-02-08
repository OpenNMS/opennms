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
package org.opennms.netmgt.trapd;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.snmp.TrapIdentity;

import com.google.common.base.MoreObjects;

@XmlRootElement(name="trap-identity")
@XmlAccessorType(value= XmlAccessType.NONE)
public class TrapIdentityDTO {

    @XmlAttribute(name="generic", required=true)
    private int generic;

    @XmlAttribute(name="specific", required=true)
    private int specific;

    @XmlAttribute(name="enterprise-id", required=true)
    private String enterpriseId;

    @XmlAttribute(name="trap-oid")
    private String trapOID;

    // Default Constructor for JAXB
    public TrapIdentityDTO() {

    }

    public TrapIdentityDTO(TrapIdentity trapIdentity) {
        Objects.requireNonNull(trapIdentity);
        this.generic = trapIdentity.getGeneric();
        this.specific = trapIdentity.getSpecific();
        this.enterpriseId = trapIdentity.getEnterpriseId();
        this.trapOID = trapIdentity.getTrapOID();
    }

    public int getGeneric() {
        return generic;
    }

    public void setGeneric(int generic) {
        this.generic = generic;
    }

    public int getSpecific() {
        return specific;
    }

    public void setSpecific(int specific) {
        this.specific = specific;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getTrapOID() {
        return trapOID;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("Generic", getGeneric())
                .add("Specific", getSpecific())
                .add("EnterpriseId", getEnterpriseId())
                .add("trapOID", getTrapOID())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrapIdentityDTO that = (TrapIdentityDTO) o;
        return Objects.equals(generic, that.generic)
                && Objects.equals(specific, that.specific)
                && Objects.equals(enterpriseId, that.enterpriseId)
                && Objects.equals(trapOID, that.trapOID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generic, specific, enterpriseId, trapOID);
    }
}
