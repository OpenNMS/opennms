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

@Table(name = "bmp_rpki_info")
@Entity
public class BmpRpkiInfo implements Serializable {

    private static final long serialVersionUID = -2194393458129973561L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpRpkiInfoSequence")
    @SequenceGenerator(name = "bmpRpkiInfoSequence", sequenceName = "bmprpkiinfonxtid", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "prefix", nullable = false)
    private String prefix;

    @Column(name = "prefix_len", nullable = false)
    private Integer prefixLen;

    @Column(name = "prefix_len_max", nullable = false)
    private Integer prefixLenMax;

    @Column(name = "origin_as")
    private Long originAs;

    @Column(name = "last_updated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getPrefixLenMax() {
        return prefixLenMax;
    }

    public void setPrefixLenMax(Integer prefixLenMax) {
        this.prefixLenMax = prefixLenMax;
    }

    public Long getOriginAs() {
        return originAs;
    }

    public void setOriginAs(Long originAs) {
        this.originAs = originAs;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpRpkiInfo that = (BmpRpkiInfo) o;
        return Objects.equals(prefix, that.prefix) &&
                Objects.equals(prefixLen, that.prefixLen) &&
                Objects.equals(prefixLenMax, that.prefixLenMax) &&
                Objects.equals(originAs, that.originAs) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, prefixLen, prefixLenMax, originAs, timestamp);
    }

    @Override
    public String toString() {
        return "BmpRpkiInfo{" +
                "id=" + id +
                ", prefix='" + prefix + '\'' +
                ", prefixLen=" + prefixLen +
                ", prefixLenMax=" + prefixLenMax +
                ", originAs=" + originAs +
                ", timestamp=" + timestamp +
                '}';
    }
}
