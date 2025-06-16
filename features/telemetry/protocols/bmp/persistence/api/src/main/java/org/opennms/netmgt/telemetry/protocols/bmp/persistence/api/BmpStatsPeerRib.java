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
