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
@Table(name = "bmp_stats_by_prefix")
public class BmpStatsByPrefix implements Serializable {

    private static final long serialVersionUID = -855237737426116599L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpStatsByPrefixSequence")
    @SequenceGenerator(name = "bmpStatsByPrefixSequence", sequenceName = "bmpstatsbyprefixnxtid", allocationSize = 1)
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
