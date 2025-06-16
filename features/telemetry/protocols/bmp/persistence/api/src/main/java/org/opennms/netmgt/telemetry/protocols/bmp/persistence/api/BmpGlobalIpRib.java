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
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "bmp_global_ip_ribs", uniqueConstraints={@UniqueConstraint(columnNames={"prefix", "recv_origin_as"})})
public class BmpGlobalIpRib implements Serializable {

    private static final long serialVersionUID = 8855311705684588626L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpUnicastSequence")
    @SequenceGenerator(name = "bmpUnicastSequence", sequenceName = "bmpunicastnxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "prefix", nullable = false)
    private String prefix;

    @Column(name = "should_delete", nullable = false)
    private boolean shouldDelete;

    @Column(name = "prefix_len", nullable = false)
    private Integer prefixLen;

    @Column(name = "recv_origin_as", nullable = false)
    private Long recvOriginAs;

    @Column(name = "rpki_origin_as")
    private Long rpkiOriginAs;

    @Column(name = "irr_origin_as")
    private Long irrOriginAs;

    @Column(name = "irr_source")
    private String irrSource;

    @Column(name = "last_updated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

    @Column(name = "num_peers")
    private Integer numPeers;

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

    public boolean isShouldDelete() {
        return shouldDelete;
    }

    public void setShouldDelete(boolean shouldDelete) {
        this.shouldDelete = shouldDelete;
    }

    public Integer getPrefixLen() {
        return prefixLen;
    }

    public void setPrefixLen(Integer prefixLen) {
        this.prefixLen = prefixLen;
    }

    public Long getRecvOriginAs() {
        return recvOriginAs;
    }

    public void setRecvOriginAs(Long recvOriginAs) {
        this.recvOriginAs = recvOriginAs;
    }

    public Long getRpkiOriginAs() {
        return rpkiOriginAs;
    }

    public void setRpkiOriginAs(Long rpkiOriginAs) {
        this.rpkiOriginAs = rpkiOriginAs;
    }

    public Long getIrrOriginAs() {
        return irrOriginAs;
    }

    public void setIrrOriginAs(Long irrOriginAs) {
        this.irrOriginAs = irrOriginAs;
    }

    public String getIrrSource() {
        return irrSource;
    }

    public void setIrrSource(String irrSource) {
        this.irrSource = irrSource;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getNumPeers() {
        return numPeers;
    }

    public void setNumPeers(Integer numPeers) {
        this.numPeers = numPeers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpGlobalIpRib that = (BmpGlobalIpRib) o;
        return shouldDelete == that.shouldDelete &&
                Objects.equals(id, that.id) &&
                Objects.equals(prefix, that.prefix) &&
                Objects.equals(prefixLen, that.prefixLen) &&
                Objects.equals(recvOriginAs, that.recvOriginAs) &&
                Objects.equals(rpkiOriginAs, that.rpkiOriginAs) &&
                Objects.equals(irrOriginAs, that.irrOriginAs) &&
                Objects.equals(irrSource, that.irrSource) &&
                Objects.equals(timeStamp, that.timeStamp) &&
                Objects.equals(numPeers, that.numPeers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, prefix, shouldDelete, prefixLen, recvOriginAs, rpkiOriginAs, irrOriginAs, irrSource, timeStamp, numPeers);
    }

    @Override
    public String toString() {
        return "BmpGlobalIpRib{" +
                "id=" + id +
                ", prefix='" + prefix + '\'' +
                ", shouldDelete=" + shouldDelete +
                ", prefixLen=" + prefixLen +
                ", recvOriginAs=" + recvOriginAs +
                ", rpkiOriginAs=" + rpkiOriginAs +
                ", irrOriginAs=" + irrOriginAs +
                ", irrSource='" + irrSource + '\'' +
                ", timeStamp=" + timeStamp +
                ", numPeers=" + numPeers +
                '}';
    }
}
