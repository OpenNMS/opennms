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
@Table(name = "bmp_ip_rib_log")
public class BmpIpRibLog implements Serializable {

    private static final long serialVersionUID = 3774709522470706199L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpIpRibLogSequence")
    @SequenceGenerator(name = "bmpIpRibLogSequence", sequenceName = "bmpipriblognxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "peer_hash_id", nullable = false)
    private String peerHashId;

    @Column(name = "base_attr_hash_id", nullable = false)
    private String baseAttrHashId;

    @Column(name = "last_updated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "prefix", nullable = false)
    private String prefix;

    @Column(name = "prefix_len", nullable = false)
    private Integer prefixLen;

    @Column(name = "origin_as")
    private Long originAs;

    @Column(name = "is_withdrawn", nullable = false)
    private boolean isWithDrawn;

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

    public String getBaseAttrHashId() {
        return baseAttrHashId;
    }

    public void setBaseAttrHashId(String baseAttrHashId) {
        this.baseAttrHashId = baseAttrHashId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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

    public Long getOriginAs() {
        return originAs;
    }

    public void setOriginAs(Long originAs) {
        this.originAs = originAs;
    }

    public boolean isWithDrawn() {
        return isWithDrawn;
    }

    public void setWithDrawn(boolean withDrawn) {
        isWithDrawn = withDrawn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpIpRibLog that = (BmpIpRibLog) o;
        return isWithDrawn == that.isWithDrawn &&
                Objects.equals(peerHashId, that.peerHashId) &&
                Objects.equals(baseAttrHashId, that.baseAttrHashId) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(prefix, that.prefix) &&
                Objects.equals(prefixLen, that.prefixLen) &&
                Objects.equals(originAs, that.originAs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerHashId, baseAttrHashId, timestamp, prefix, prefixLen, originAs, isWithDrawn);
    }

    @Override
    public String toString() {
        return "BmpIpRibLog{" +
                "id=" + id +
                ", peerHashId='" + peerHashId + '\'' +
                ", baseAttrHashId='" + baseAttrHashId + '\'' +
                ", timestamp=" + timestamp +
                ", prefix='" + prefix + '\'' +
                ", prefixLen=" + prefixLen +
                ", originAs=" + originAs +
                ", isWithDrawn=" + isWithDrawn +
                '}';
    }
}
