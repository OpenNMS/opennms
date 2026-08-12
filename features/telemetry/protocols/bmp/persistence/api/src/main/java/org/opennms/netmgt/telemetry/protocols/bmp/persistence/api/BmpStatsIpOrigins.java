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

@Entity
@Table(name = "bmp_stats_ip_origins")
public class BmpStatsIpOrigins implements Serializable {
    
    private static final long serialVersionUID = -8617461006452833814L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpStatsIpOriginsSequence")
    @SequenceGenerator(name = "bmpStatsIpOriginsSequence", sequenceName = "bmpstatsiporiginsnxtid", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "interval_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "asn", nullable = false)
    private Long asn;

    @Column(name = "v4_prefixes", nullable = false)
    private Integer v4prefixes;

    @Column(name = "v6_prefixes", nullable = false)
    private Integer v6prefixes;

    @Column(name = "v4_with_rpki", nullable = false)
    private Integer v4withrpki;

    @Column(name = "v6_with_rpki", nullable = false)
    private Integer v6withrpki;

    @Column(name = "v4_with_irr", nullable = false)
    private Integer v4withirr;

    @Column(name = "v6_with_irr", nullable = false)
    private Integer v6withirr;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getAsn() {
        return asn;
    }

    public void setAsn(Long asn) {
        this.asn = asn;
    }

    public Integer getV4prefixes() {
        return v4prefixes;
    }

    public void setV4prefixes(Integer v4prefixes) {
        this.v4prefixes = v4prefixes;
    }

    public Integer getV6prefixes() {
        return v6prefixes;
    }

    public void setV6prefixes(Integer v6prefixes) {
        this.v6prefixes = v6prefixes;
    }

    public Integer getV4withrpki() {
        return v4withrpki;
    }

    public void setV4withrpki(Integer v4withrpki) {
        this.v4withrpki = v4withrpki;
    }

    public Integer getV6withrpki() {
        return v6withrpki;
    }

    public void setV6withrpki(Integer v6withrpki) {
        this.v6withrpki = v6withrpki;
    }

    public Integer getV4withirr() {
        return v4withirr;
    }

    public void setV4withirr(Integer v4withirr) {
        this.v4withirr = v4withirr;
    }

    public Integer getV6withirr() {
        return v6withirr;
    }

    public void setV6withirr(Integer v6withirr) {
        this.v6withirr = v6withirr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpStatsIpOrigins that = (BmpStatsIpOrigins) o;
        return Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(asn, that.asn) &&
                Objects.equals(v4prefixes, that.v4prefixes) &&
                Objects.equals(v6prefixes, that.v6prefixes) &&
                Objects.equals(v4withrpki, that.v4withrpki) &&
                Objects.equals(v6withrpki, that.v6withrpki) &&
                Objects.equals(v4withirr, that.v4withirr) &&
                Objects.equals(v6withirr, that.v6withirr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, asn, v4prefixes, v6prefixes, v4withrpki, v6withrpki, v4withirr, v6withirr);
    }

    @Override
    public String toString() {
        return "BmpStatsIpOrigins{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", asn=" + asn +
                ", v4prefixes=" + v4prefixes +
                ", v6prefixes=" + v6prefixes +
                ", v4withrpki=" + v4withrpki +
                ", v6withrpki=" + v6withrpki +
                ", v4withirr=" + v4withirr +
                ", v6withirr=" + v6withirr +
                '}';
    }
}
