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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "bmp_peers")
public class BmpPeer implements Serializable {

    private static final long serialVersionUID = 910756667828959198L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpPeerSequence")
    @SequenceGenerator(name = "bmpPeerSequence", sequenceName = "bmppeernxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "hash_id", nullable = false)
    private String hashId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "router_hash_id", referencedColumnName = "hash_id", nullable = false)
    private BmpRouter bmpRouter;

    @Column(name = "peer_rd", nullable = false)
    private String peerRd;

    @Column(name = "is_ipv4", nullable = false)
    private boolean isIpv4;

    @Column(name = "peer_addr", nullable = false)
    private String peerAddr;

    @Column(name = "name")
    private String name;

    @Column(name = "peer_bgp_id")
    private String peerBgpId;

    @Column(name = "peer_asn")
    private Long peerAsn;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "is_l3vpn_peer", nullable = false)
    private boolean isL3VPNPeer;

    @Column(name = "last_updated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "is_pre_policy", nullable = false)
    private boolean isPrePolicy;

    @Column(name = "geo_ip_start")
    private String geoIpStart;

    @Column(name = "local_ip")
    private String localIp;

    @Column(name = "local_bgp_id")
    private String localBgpId;

    @Column(name = "local_port")
    private Integer localPort;

    @Column(name = "local_hold_time")
    private Long localHoldTime;

    @Column(name = "local_asn")
    private Long localAsn;

    @Column(name = "remote_port")
    private Integer remotePort;

    @Column(name = "remote_hold_time")
    private Long remoteHoldTime;

    @Column(name = "sent_capabilities")
    private String sentCapabilities;

    @Column(name = "recv_capabilities")
    private String receivedCapabilities;

    @Column(name = "bmp_reason")
    private Integer bmpReason;

    @Column(name = "bgp_err_code")
    private Integer bgpErrCode;

    @Column(name = "bgp_err_subcode")
    private Integer bgpErrSubCode;

    @Column(name = "error_text")
    private String errorText;

    @Column(name = "is_loc_rib", nullable = false)
    private boolean isLocRib;

    @Column(name = "is_loc_rib_filtered", nullable = false)
    private boolean isLocRibFiltered;

    @Column(name = "table_name")
    private String tableName;

    @OneToMany(mappedBy="bmpPeer", cascade = CascadeType.ALL, orphanRemoval=true)
    private Set<BmpUnicastPrefix> bmpUnicastPrefixes = new LinkedHashSet<>();

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

    public BmpRouter getBmpRouter() {
        return bmpRouter;
    }

    public void setBmpRouter(BmpRouter bmpRouter) {
        this.bmpRouter = bmpRouter;
    }

    public String getPeerRd() {
        return peerRd;
    }

    public void setPeerRd(String peerRd) {
        this.peerRd = peerRd;
    }

    public boolean isIpv4() {
        return isIpv4;
    }

    public void setIpv4(boolean ipv4) {
        isIpv4 = ipv4;
    }

    public String getPeerAddr() {
        return peerAddr;
    }

    public void setPeerAddr(String peerAddr) {
        this.peerAddr = peerAddr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPeerBgpId() {
        return peerBgpId;
    }

    public void setPeerBgpId(String peerBgpId) {
        this.peerBgpId = peerBgpId;
    }

    public Long getPeerAsn() {
        return peerAsn;
    }

    public void setPeerAsn(Long peerAsn) {
        this.peerAsn = peerAsn;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isL3VPNPeer() {
        return isL3VPNPeer;
    }

    public void setL3VPNPeer(boolean l3VPNPeer) {
        isL3VPNPeer = l3VPNPeer;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isPrePolicy() {
        return isPrePolicy;
    }

    public void setPrePolicy(boolean prePolicy) {
        isPrePolicy = prePolicy;
    }

    public String getGeoIpStart() {
        return geoIpStart;
    }

    public void setGeoIpStart(String geoIpStart) {
        this.geoIpStart = geoIpStart;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getLocalBgpId() {
        return localBgpId;
    }

    public void setLocalBgpId(String localBgpId) {
        this.localBgpId = localBgpId;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public Long getLocalHoldTime() {
        return localHoldTime;
    }

    public void setLocalHoldTime(Long localHoldTime) {
        this.localHoldTime = localHoldTime;
    }

    public Long getLocalAsn() {
        return localAsn;
    }

    public void setLocalAsn(Long localAsn) {
        this.localAsn = localAsn;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public Long getRemoteHoldTime() {
        return remoteHoldTime;
    }

    public void setRemoteHoldTime(Long remoteHoldTime) {
        this.remoteHoldTime = remoteHoldTime;
    }

    public String getSentCapabilities() {
        return sentCapabilities;
    }

    public void setSentCapabilities(String sentCapabilities) {
        this.sentCapabilities = sentCapabilities;
    }

    public String getReceivedCapabilities() {
        return receivedCapabilities;
    }

    public void setReceivedCapabilities(String receivedCapabilities) {
        this.receivedCapabilities = receivedCapabilities;
    }

    public Integer getBmpReason() {
        return bmpReason;
    }

    public void setBmpReason(Integer bmpReason) {
        this.bmpReason = bmpReason;
    }

    public Integer getBgpErrCode() {
        return bgpErrCode;
    }

    public void setBgpErrCode(Integer bgpErrCode) {
        this.bgpErrCode = bgpErrCode;
    }

    public Integer getBgpErrSubCode() {
        return bgpErrSubCode;
    }

    public void setBgpErrSubCode(Integer bgpErrSubCode) {
        this.bgpErrSubCode = bgpErrSubCode;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public boolean isLocRib() {
        return isLocRib;
    }

    public void setLocRib(boolean locRib) {
        isLocRib = locRib;
    }

    public boolean isLocRibFiltered() {
        return isLocRibFiltered;
    }

    public void setLocRibFiltered(boolean locRibFiltered) {
        isLocRibFiltered = locRibFiltered;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Set<BmpUnicastPrefix> getBmpUnicastPrefixes() {
        return bmpUnicastPrefixes;
    }

    public void setBmpUnicastPrefixes(Set<BmpUnicastPrefix> bmpUnicastPrefixes) {
        this.bmpUnicastPrefixes.clear();
        this.bmpUnicastPrefixes.addAll(bmpUnicastPrefixes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpPeer bmpPeer = (BmpPeer) o;
        return isIpv4 == bmpPeer.isIpv4 &&
                isL3VPNPeer == bmpPeer.isL3VPNPeer &&
                isPrePolicy == bmpPeer.isPrePolicy &&
                isLocRib == bmpPeer.isLocRib &&
                isLocRibFiltered == bmpPeer.isLocRibFiltered &&
                Objects.equals(hashId, bmpPeer.hashId) &&
                Objects.equals(bmpRouter, bmpPeer.bmpRouter) &&
                Objects.equals(peerRd, bmpPeer.peerRd) &&
                Objects.equals(peerAddr, bmpPeer.peerAddr) &&
                Objects.equals(name, bmpPeer.name) &&
                Objects.equals(peerBgpId, bmpPeer.peerBgpId) &&
                Objects.equals(peerAsn, bmpPeer.peerAsn) &&
                state == bmpPeer.state &&
                Objects.equals(timestamp, bmpPeer.timestamp) &&
                Objects.equals(geoIpStart, bmpPeer.geoIpStart) &&
                Objects.equals(localIp, bmpPeer.localIp) &&
                Objects.equals(localBgpId, bmpPeer.localBgpId) &&
                Objects.equals(localPort, bmpPeer.localPort) &&
                Objects.equals(localHoldTime, bmpPeer.localHoldTime) &&
                Objects.equals(localAsn, bmpPeer.localAsn) &&
                Objects.equals(remotePort, bmpPeer.remotePort) &&
                Objects.equals(remoteHoldTime, bmpPeer.remoteHoldTime) &&
                Objects.equals(sentCapabilities, bmpPeer.sentCapabilities) &&
                Objects.equals(receivedCapabilities, bmpPeer.receivedCapabilities) &&
                Objects.equals(bmpReason, bmpPeer.bmpReason) &&
                Objects.equals(bgpErrCode, bmpPeer.bgpErrCode) &&
                Objects.equals(bgpErrSubCode, bmpPeer.bgpErrSubCode) &&
                Objects.equals(errorText, bmpPeer.errorText) &&
                Objects.equals(tableName, bmpPeer.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashId, bmpRouter, peerRd, isIpv4, peerAddr, name, peerBgpId, peerAsn, state, isL3VPNPeer, timestamp, isPrePolicy, geoIpStart, localIp, localBgpId, localPort, localHoldTime, localAsn, remotePort, remoteHoldTime, sentCapabilities, receivedCapabilities, bmpReason, bgpErrCode, bgpErrSubCode, errorText, isLocRib, isLocRibFiltered, tableName);
    }

    @Override
    public String toString() {
        return "BmpPeer{" +
                "id=" + id +
                ", hashId='" + hashId + '\'' +
                ", peerRd='" + peerRd + '\'' +
                ", isIpv4=" + isIpv4 +
                ", peerAddr='" + peerAddr + '\'' +
                ", name='" + name + '\'' +
                ", peerBgpId='" + peerBgpId + '\'' +
                ", peerAsn=" + peerAsn +
                ", state=" + state +
                ", isL3VPNPeer=" + isL3VPNPeer +
                ", timestamp=" + timestamp +
                ", isPrePolicy=" + isPrePolicy +
                ", geoIpStart='" + geoIpStart + '\'' +
                ", localIp='" + localIp + '\'' +
                ", localBgpId='" + localBgpId + '\'' +
                ", localPort=" + localPort +
                ", localHoldTime=" + localHoldTime +
                ", localAsn=" + localAsn +
                ", remotePort=" + remotePort +
                ", remoteHoldTime=" + remoteHoldTime +
                ", sentCapabilities='" + sentCapabilities + '\'' +
                ", receivedCapabilities='" + receivedCapabilities + '\'' +
                ", bmpReason=" + bmpReason +
                ", bgpErrCode=" + bgpErrCode +
                ", bgpErrSubCode=" + bgpErrSubCode +
                ", errorText='" + errorText + '\'' +
                ", isLocRib=" + isLocRib +
                ", isLocRibFiltered=" + isLocRibFiltered +
                ", tableName='" + tableName + '\'' +
                ", bmpUnicastPrefixes=" + bmpUnicastPrefixes +
                '}';
    }
}

