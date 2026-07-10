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
package org.opennms.netmgt.telemetry.protocols.bmp.persistence.api;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Table(name = "bmp_asn_info")
@Entity
public class BmpAsnInfo implements Serializable {

    private static final long serialVersionUID = 1369105193708550084L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpAsnSequence")
    @SequenceGenerator(name = "bmpAsnSequence", sequenceName = "bmpasninfonxtid", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "asn", nullable = false)
    private Long asn;

    @Column(name = "as_name")
    private String asName;

    @Column(name = "org_id")
    private String orgId;

    @Column(name = "org_name")
    private String orgName;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state_prov")
    private String stateProv;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country;

    @Column(name = "raw_output")
    private String rawOutput;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "source")
    private String source;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAsn() {
        return asn;
    }

    public void setAsn(Long asn) {
        this.asn = asn;
    }

    public String getAsName() {
        return asName;
    }

    public void setAsName(String asName) {
        this.asName = asName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        if(orgId != null && orgId.length() > 255) {
            this.orgId = orgId.substring(0, 254);
        } else {
            this.orgId = orgId;
        }
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        if(orgName != null && orgName.length() > 255) {
            this.orgName = orgName.substring(0, 254);
        } else {
            this.orgName = orgName;
        }
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if(city != null && city.length() > 255) {
            this.city = city.substring(0, 254);
        } else {
            this.city = city;
        }
    }

    public String getStateProv() {
        return stateProv;
    }

    public void setStateProv(String stateProv) {
        if(stateProv != null && stateProv.length() > 255) {
            this.stateProv = stateProv.substring(0, 254);
        } else {
            this.stateProv = stateProv;
        }
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        if(postalCode != null && postalCode.length() > 255) {
            this.postalCode = postalCode.substring(0, 254);
        } else {
            this.postalCode = postalCode;
        }
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        if(country != null && country.length() > 255) {
            this.country = country.substring(0, 254);
        } else {
            this.country = country;
        }
    }

    public String getRawOutput() {
        return rawOutput;
    }

    public void setRawOutput(String rawOutput) {
        this.rawOutput = rawOutput;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpAsnInfo that = (BmpAsnInfo) o;
        return Objects.equals(asn, that.asn) &&
                Objects.equals(asName, that.asName) &&
                Objects.equals(orgId, that.orgId) &&
                Objects.equals(orgName, that.orgName) &&
                Objects.equals(remarks, that.remarks) &&
                Objects.equals(address, that.address) &&
                Objects.equals(city, that.city) &&
                Objects.equals(stateProv, that.stateProv) &&
                Objects.equals(postalCode, that.postalCode) &&
                Objects.equals(country, that.country) &&
                Objects.equals(rawOutput, that.rawOutput) &&
                Objects.equals(lastUpdated, that.lastUpdated) &&
                Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asn, asName, orgId, orgName, remarks, address, city, stateProv, postalCode, country, rawOutput, lastUpdated, source);
    }

    @Override
    public String toString() {
        return "BmpAsnInfo{" +
                "asn=" + asn +
                ", asName='" + asName + '\'' +
                ", orgId='" + orgId + '\'' +
                ", orgName='" + orgName + '\'' +
                ", remarks='" + remarks + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", stateProv='" + stateProv + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", rawOutput='" + rawOutput + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", source='" + source + '\'' +
                '}';
    }
}
