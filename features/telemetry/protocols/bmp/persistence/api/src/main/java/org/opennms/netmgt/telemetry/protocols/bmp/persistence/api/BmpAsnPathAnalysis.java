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

@Table(name = "bmp_asn_path_analysis")
@Entity
public class BmpAsnPathAnalysis implements Serializable {

    private static final long serialVersionUID = 1675259022257268986L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpAsnPathSequence")
    @SequenceGenerator(name = "bmpAsnPathSequence", sequenceName = "bmpasnpathnxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "asn")
    private Long asn;

    @Column(name = "asn_left")
    private Long asnLeft;

    @Column(name = "asn_right")
    private Long asnRight;

    @Column(name = "asn_left_is_peering")
    private boolean asnLeftPeering;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;


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

    public Long getAsnLeft() {
        return asnLeft;
    }

    public void setAsnLeft(Long asnLeft) {
        this.asnLeft = asnLeft;
    }

    public Long getAsnRight() {
        return asnRight;
    }

    public void setAsnRight(Long asnRight) {
        this.asnRight = asnRight;
    }

    public boolean isAsnLeftPeering() {
        return asnLeftPeering;
    }

    public void setAsnLeftPeering(boolean asnLeftPeering) {
        this.asnLeftPeering = asnLeftPeering;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpAsnPathAnalysis that = (BmpAsnPathAnalysis) o;
        return asnLeftPeering == that.asnLeftPeering &&
                Objects.equals(id, that.id) &&
                Objects.equals(asn, that.asn) &&
                Objects.equals(asnLeft, that.asnLeft) &&
                Objects.equals(asnRight, that.asnRight) &&
                Objects.equals(lastUpdated, that.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, asn, asnLeft, asnRight, asnLeftPeering, lastUpdated);
    }

    @Override
    public String toString() {
        return "BmpAsnPathAnalysis{" +
                "id=" + id +
                ", asn=" + asn +
                ", asnLeft=" + asnLeft +
                ", asnRight=" + asnRight +
                ", asnLeftPeering=" + asnLeftPeering +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
