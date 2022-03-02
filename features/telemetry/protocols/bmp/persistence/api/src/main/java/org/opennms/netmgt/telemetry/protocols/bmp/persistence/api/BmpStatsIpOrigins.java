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

@Entity
@Table(name = "bmp_stats_ip_origins")
public class BmpStatsIpOrigins implements Serializable {
    
    private static final long serialVersionUID = -8617461006452833814L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpStatsIpOriginsSequence")
    @SequenceGenerator(name = "bmpStatsIpOriginsSequence", sequenceName = "bmpstatsiporiginsnxtid")
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
