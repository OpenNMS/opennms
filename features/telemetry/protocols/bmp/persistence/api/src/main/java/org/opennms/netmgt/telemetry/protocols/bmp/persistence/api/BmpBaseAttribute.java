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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "bmp_base_attributes")
public class BmpBaseAttribute implements Serializable {


    private static final long serialVersionUID = 6992640443613316262L;

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "bmpBaseAttrsSequence")
    @SequenceGenerator(name = "bmpBaseAttrsSequence", sequenceName = "baseattrsnxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "hash_id", nullable = false)
    private String hashId;

    @Column(name = "peer_hash_id", nullable = false)
    private String peerHashId;

    @Column(name = "origin", nullable = false)
    private String origin;

    @Column(name = "as_path", nullable = false)
    private String asPath;

    @Column(name = "as_path_count", nullable = false)
    private Integer asPathCount;

    @Column(name = "origin_as")
    private Long originAs;

    @Column(name = "next_hop")
    private String nextHop;

    @Column(name = "med")
    private Long med;

    @Column(name = "local_pref")
    private Long localPref;

    @Column(name = "aggregator")
    private String aggregator;

    @Column(name = "community_list")
    private String communityList;

    @Column(name = "ext_community_list")
    private String extCommunityList;

    @Column(name = "large_community_list")
    private String largeCommunityList;

    @Column(name = "cluster_list")
    private String clusterList;

    @Column(name = "is_atomic_agg")
    private boolean isAtomicAgg;

    @Column(name = "is_nexthop_ipv4")
    private boolean isNextHopIpv4;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "originator_id")
    private String originatorId;


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

    public String getPeerHashId() {
        return peerHashId;
    }

    public void setPeerHashId(String peerHashId) {
        this.peerHashId = peerHashId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getAsPath() {
        return asPath;
    }

    public void setAsPath(String asPath) {
        this.asPath = asPath;
    }

    public Integer getAsPathCount() {
        return asPathCount;
    }

    public void setAsPathCount(Integer asPathCount) {
        this.asPathCount = asPathCount;
    }

    public Long getOriginAs() {
        return originAs;
    }

    public void setOriginAs(Long originAs) {
        this.originAs = originAs;
    }

    public String getNextHop() {
        return nextHop;
    }

    public void setNextHop(String nextHop) {
        this.nextHop = nextHop;
    }

    public Long getMed() {
        return med;
    }

    public void setMed(Long med) {
        this.med = med;
    }

    public Long getLocalPref() {
        return localPref;
    }

    public void setLocalPref(Long localPref) {
        this.localPref = localPref;
    }

    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public String getCommunityList() {
        return communityList;
    }

    public void setCommunityList(String communityList) {
        this.communityList = communityList;
    }

    public String getExtCommunityList() {
        return extCommunityList;
    }

    public void setExtCommunityList(String extCommunityList) {
        this.extCommunityList = extCommunityList;
    }

    public String getLargeCommunityList() {
        return largeCommunityList;
    }

    public void setLargeCommunityList(String largeCommunityList) {
        this.largeCommunityList = largeCommunityList;
    }

    public String getClusterList() {
        return clusterList;
    }

    public void setClusterList(String clusterList) {
        this.clusterList = clusterList;
    }

    public boolean isAtomicAgg() {
        return isAtomicAgg;
    }

    public void setAtomicAgg(boolean atomicAgg) {
        isAtomicAgg = atomicAgg;
    }

    public boolean isNextHopIpv4() {
        return isNextHopIpv4;
    }

    public void setNextHopIpv4(boolean nextHopIpv4) {
        isNextHopIpv4 = nextHopIpv4;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpBaseAttribute that = (BmpBaseAttribute) o;
        return isAtomicAgg == that.isAtomicAgg &&
                isNextHopIpv4 == that.isNextHopIpv4 &&
                Objects.equals(hashId, that.hashId) &&
                Objects.equals(peerHashId, that.peerHashId) &&
                Objects.equals(origin, that.origin) &&
                Objects.equals(asPath, that.asPath) &&
                Objects.equals(asPathCount, that.asPathCount) &&
                Objects.equals(originAs, that.originAs) &&
                Objects.equals(nextHop, that.nextHop) &&
                Objects.equals(med, that.med) &&
                Objects.equals(localPref, that.localPref) &&
                Objects.equals(aggregator, that.aggregator) &&
                Objects.equals(communityList, that.communityList) &&
                Objects.equals(extCommunityList, that.extCommunityList) &&
                Objects.equals(largeCommunityList, that.largeCommunityList) &&
                Objects.equals(clusterList, that.clusterList) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(originatorId, that.originatorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashId, peerHashId, origin, asPath, asPathCount, originAs, nextHop, med, localPref, aggregator, communityList, extCommunityList, largeCommunityList, clusterList, isAtomicAgg, isNextHopIpv4, timestamp, originatorId);
    }

    @Override
    public String toString() {
        return "BmpBaseAttribute{" +
                "id=" + id +
                ", hashId='" + hashId + '\'' +
                ", peerHashId='" + peerHashId + '\'' +
                ", origin='" + origin + '\'' +
                ", asPath='" + asPath + '\'' +
                ", asPathCount=" + asPathCount +
                ", originAs=" + originAs +
                ", nextHop='" + nextHop + '\'' +
                ", med=" + med +
                ", localPref=" + localPref +
                ", aggregator='" + aggregator + '\'' +
                ", communityList='" + communityList + '\'' +
                ", extCommunityList='" + extCommunityList + '\'' +
                ", largeCommunityList='" + largeCommunityList + '\'' +
                ", clusterList='" + clusterList + '\'' +
                ", isAtomicAgg=" + isAtomicAgg +
                ", isNextHopIpv4=" + isNextHopIpv4 +
                ", timestamp=" + timestamp +
                ", originatorId='" + originatorId + '\'' +
                '}';
    }
}
