/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
    @SequenceGenerator(name = "bmpAsnSequence", sequenceName = "bmpasninfonxtid")
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
