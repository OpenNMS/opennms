/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "bmp_ip_ribs")
public class BmpUnicastPrefix implements Serializable {


    private static final long serialVersionUID = -7783081316304433407L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpUnicastSequence")
    @SequenceGenerator(name = "bmpUnicastSequence", sequenceName = "bmpunicastnxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "hash_id", nullable = false)
    private String hashId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "peer_hash_id", referencedColumnName = "hash_id", nullable = false)
    private BmpPeer bmpPeer;

    @Column(name = "base_attr_hash_id", nullable = false)
    private String baseAttrHashId;

    @Column(name = "is_ipv4", nullable = false)
    private boolean isIpv4;

    @Column(name = "origin_as")
    private Long originAs;

    @Column(name = "prefix", nullable = false)
    private String prefix;

    @Column(name = "prefix_len", nullable = false)
    private Integer prefixLen;

    @Column(name = "last_updated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "first_added_timestamp", nullable = false)
    private Date firstAddedTimestamp;

    @Column(name = "is_withdrawn", nullable = false)
    private boolean isWithDrawn;

    @Column(name = "prefix_bits")
    private String prefixBits;

    @Column(name = "path_id")
    private Long pathId;

    @Column(name = "labels")
    private String labels;

    @Column(name = "is_pre_policy", nullable = false)
    private boolean isPrePolicy;

    @Column(name = "is_adj_ribin", nullable = false)
    private boolean isAdjRibIn;

    @Transient
    private String prevBaseAttrHashId;

    @Transient
    private boolean prevWithDrawnState;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHashId() {
        return hashId;
    }

    public void setHashId(String hashId) {
        this.hashId = hashId;
    }

    public String getBaseAttrHashId() {
        return baseAttrHashId;
    }

    public void setBaseAttrHashId(String baseAttrHashId) {
        this.baseAttrHashId = baseAttrHashId;
    }

    public BmpPeer getBmpPeer() {
        return bmpPeer;
    }

    public void setBmpPeer(BmpPeer bmpPeer) {
        this.bmpPeer = bmpPeer;
    }

    public boolean isIpv4() {
        return isIpv4;
    }

    public void setIpv4(boolean ipv4) {
        isIpv4 = ipv4;
    }

    public Long getOriginAs() {
        return originAs;
    }

    public void setOriginAs(Long originAs) {
        this.originAs = originAs;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getFirstAddedTimestamp() {
        return firstAddedTimestamp;
    }

    public void setFirstAddedTimestamp(Date firstAddedTimestamp) {
        this.firstAddedTimestamp = firstAddedTimestamp;
    }

    public boolean isWithDrawn() {
        return isWithDrawn;
    }

    public void setWithDrawn(boolean withDrawn) {
        isWithDrawn = withDrawn;
    }

    public String getPrefixBits() {
        return prefixBits;
    }

    public void setPrefixBits(String prefixBits) {
        this.prefixBits = prefixBits;
    }

    public Long getPathId() {
        return pathId;
    }

    public void setPathId(Long pathId) {
        this.pathId = pathId;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public boolean isPrePolicy() {
        return isPrePolicy;
    }

    public void setPrePolicy(boolean prePolicy) {
        isPrePolicy = prePolicy;
    }

    public boolean isAdjRibIn() {
        return isAdjRibIn;
    }

    public void setAdjRibIn(boolean adjRibIn) {
        isAdjRibIn = adjRibIn;
    }

    public String getPrevBaseAttrHashId() {
        return prevBaseAttrHashId;
    }

    public void setPrevBaseAttrHashId(String prevBaseAttrHashId) {
        this.prevBaseAttrHashId = prevBaseAttrHashId;
    }

    public boolean isPrevWithDrawnState() {
        return prevWithDrawnState;
    }

    public void setPrevWithDrawnState(boolean prevWithDrawnState) {
        this.prevWithDrawnState = prevWithDrawnState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpUnicastPrefix that = (BmpUnicastPrefix) o;
        return isIpv4 == that.isIpv4 &&
                isWithDrawn == that.isWithDrawn &&
                isPrePolicy == that.isPrePolicy &&
                isAdjRibIn == that.isAdjRibIn &&
                prevWithDrawnState == that.prevWithDrawnState &&
                Objects.equals(hashId, that.hashId) &&
                Objects.equals(bmpPeer, that.bmpPeer) &&
                Objects.equals(baseAttrHashId, that.baseAttrHashId) &&
                Objects.equals(originAs, that.originAs) &&
                Objects.equals(prefix, that.prefix) &&
                Objects.equals(prefixLen, that.prefixLen) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(firstAddedTimestamp, that.firstAddedTimestamp) &&
                Objects.equals(prefixBits, that.prefixBits) &&
                Objects.equals(pathId, that.pathId) &&
                Objects.equals(labels, that.labels) &&
                Objects.equals(prevBaseAttrHashId, that.prevBaseAttrHashId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bmpPeer, baseAttrHashId, isIpv4, originAs, prefix, prefixLen, timestamp, firstAddedTimestamp, isWithDrawn, prefixBits, pathId, labels, isPrePolicy, isAdjRibIn, prevBaseAttrHashId, prevWithDrawnState);
    }

    @Override
    public String toString() {
        return "BmpUnicastPrefix{" +
                "id=" + id +
                ", hashId='" + hashId + '\'' +
                ", baseAttrHashId='" + baseAttrHashId + '\'' +
                ", isIpv4=" + isIpv4 +
                ", originAs=" + originAs +
                ", prefix='" + prefix + '\'' +
                ", prefixLen=" + prefixLen +
                ", timestamp=" + timestamp +
                ", firstAddedTimestamp=" + firstAddedTimestamp +
                ", isWithDrawn=" + isWithDrawn +
                ", prefixBits='" + prefixBits + '\'' +
                ", pathId=" + pathId +
                ", labels='" + labels + '\'' +
                ", isPrePolicy=" + isPrePolicy +
                ", isAdjRibIn=" + isAdjRibIn +
                ", prevBaseAttrHashId='" + prevBaseAttrHashId + '\'' +
                ", prevWithDrawnState=" + prevWithDrawnState +
                '}';
    }
}
