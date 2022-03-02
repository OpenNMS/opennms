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
@Table(name = "bmp_stats_peer_rib")
public class BmpStatsPeerRib implements Serializable {

    private static final long serialVersionUID = 4223809782888345302L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpStatsPeerRibSequence")
    @SequenceGenerator(name = "bmpStatsPeerRibSequence", sequenceName = "bmpstatspeerribnxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "peer_hash_id", nullable = false)
    private String peerHashId;

    @Column(name = "interval_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "v4_prefixes", nullable = false)
    private Integer v4prefixes;

    @Column(name = "v6_prefixes", nullable = false)
    private Integer v6prefixes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPeerHashId() {
        return peerHashId;
    }

    public void setPeerHashId(String peerHashId) {
        this.peerHashId = peerHashId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpStatsPeerRib that = (BmpStatsPeerRib) o;
        return Objects.equals(peerHashId, that.peerHashId) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(v4prefixes, that.v4prefixes) &&
                Objects.equals(v6prefixes, that.v6prefixes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerHashId, timestamp, v4prefixes, v6prefixes);
    }

    @Override
    public String toString() {
        return "BmpStatsPeerRib{" +
                "id=" + id +
                ", peerHashId='" + peerHashId + '\'' +
                ", timestamp=" + timestamp +
                ", v4prefixes=" + v4prefixes +
                ", v6prefixes=" + v6prefixes +
                '}';
    }
}
