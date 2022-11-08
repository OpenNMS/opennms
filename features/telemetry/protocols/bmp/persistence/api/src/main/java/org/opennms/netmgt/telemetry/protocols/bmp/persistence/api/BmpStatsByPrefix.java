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
@Table(name = "bmp_stats_by_prefix")
public class BmpStatsByPrefix implements Serializable {

    private static final long serialVersionUID = -855237737426116599L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpStatsByPrefixSequence")
    @SequenceGenerator(name = "bmpStatsByPrefixSequence", sequenceName = "bmpstatsbyprefixnxtid")
    @Column(name = "id", nullable = false)
    private Long id;


    @Column(name = "peer_hash_id", nullable = false)
    private String peerHashId;

    @Column(name = "interval_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "updates", nullable = false)
    private Long updates;

    @Column(name = "withdraws", nullable = false)
    private Long withdraws;

    @Column(name = "prefix", nullable = false)
    private String prefix;

    @Column(name = "prefix_len", nullable = false)
    private Integer prefixLen;

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

    public Long getUpdates() {
        return updates;
    }

    public void setUpdates(Long updates) {
        this.updates = updates;
    }

    public Long getWithdraws() {
        return withdraws;
    }

    public void setWithdraws(Long withdraws) {
        this.withdraws = withdraws;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getPrefixLen() {
        return prefixLen;
    }

    public void setPrefixLen(Integer prefixLen) {
        this.prefixLen = prefixLen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpStatsByPrefix that = (BmpStatsByPrefix) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(peerHashId, that.peerHashId) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(updates, that.updates) &&
                Objects.equals(withdraws, that.withdraws) &&
                Objects.equals(prefix, that.prefix) &&
                Objects.equals(prefixLen, that.prefixLen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerHashId, timestamp, updates, withdraws, prefix, prefixLen);
    }

    @Override
    public String toString() {
        return "BmpStatsByPrefix{" +
                "id=" + id +
                ", peerHashId='" + peerHashId + '\'' +
                ", timestamp=" + timestamp +
                ", updates=" + updates +
                ", withdraws=" + withdraws +
                ", prefix='" + prefix + '\'' +
                ", prefixLen=" + prefixLen +
                '}';
    }
}
