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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Table(name = "bmp_routers")
@Entity
public class BmpRouter implements Serializable {
    private static final long serialVersionUID = -5250630896963730366L;

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "bmpRouterSequence")
    @SequenceGenerator(name = "bmpRouterSequence", sequenceName = "bmprouternxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "hash_id", nullable = false)
    private String hashId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "router_as")
    private Integer routerAS;

    @Column(name = "last_updated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "description")
    private String description;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "is_passive")
    private boolean isPassive;

    @Column(name = "term_reason_code")
    private Integer termReasonCode;

    @Column(name = "term_reason_text")
    private String termReasonText;

    @Column(name = "term_data")
    private String termData;

    @Column(name = "init_data")
    private String initData;

    @Column(name = "geo_ip_start")
    private String geoIpStart;

    @Column(name = "collector_hash_id")
    private String collectorHashId;

    @Column(name = "bgp_id")
    private String bgpId;

    @Column(name = "connection_count")
    private Integer connectionCount;

    @OneToMany(mappedBy="bmpRouter", cascade = CascadeType.ALL)
    private Set<BmpPeer> bmpPeers = new LinkedHashSet<>();

    @Transient
    private String action;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getRouterAS() {
        return routerAS;
    }

    public void setRouterAS(Integer routerAS) {
        this.routerAS = routerAS;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isPassive() {
        return isPassive;
    }

    public void setPassive(boolean passive) {
        isPassive = passive;
    }

    public Integer getTermReasonCode() {
        return termReasonCode;
    }

    public void setTermReasonCode(Integer termReasonCode) {
        this.termReasonCode = termReasonCode;
    }

    public String getTermReasonText() {
        return termReasonText;
    }

    public void setTermReasonText(String termReasonText) {
        this.termReasonText = termReasonText;
    }

    public String getTermData() {
        return termData;
    }

    public void setTermData(String termData) {
        this.termData = termData;
    }

    public String getInitData() {
        return initData;
    }

    public void setInitData(String initData) {
        this.initData = initData;
    }

    public String getGeoIpStart() {
        return geoIpStart;
    }

    public void setGeoIpStart(String geoIpStart) {
        this.geoIpStart = geoIpStart;
    }

    public String getCollectorHashId() {
        return collectorHashId;
    }

    public void setCollectorHashId(String collectorHashId) {
        this.collectorHashId = collectorHashId;
    }

    public String getBgpId() {
        return bgpId;
    }

    public void setBgpId(String bgpId) {
        this.bgpId = bgpId;
    }

    public Set<BmpPeer> getBmpPeers() {
        return bmpPeers;
    }

    public void setBmpPeers(Set<BmpPeer> bmpPeers) {
        this.bmpPeers = bmpPeers;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getConnectionCount() {
        if(connectionCount == null){
            return 0;
        }
        return connectionCount;
    }

    public void setConnectionCount(Integer connectionCount) {
        if(connectionCount < 0) {
            connectionCount = 0;
        }
        this.connectionCount = connectionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpRouter bmpRouter = (BmpRouter) o;
        return isPassive == bmpRouter.isPassive &&
                Objects.equals(hashId, bmpRouter.hashId) &&
                Objects.equals(name, bmpRouter.name) &&
                Objects.equals(ipAddress, bmpRouter.ipAddress) &&
                Objects.equals(routerAS, bmpRouter.routerAS) &&
                Objects.equals(timestamp, bmpRouter.timestamp) &&
                Objects.equals(description, bmpRouter.description) &&
                state == bmpRouter.state &&
                Objects.equals(termReasonCode, bmpRouter.termReasonCode) &&
                Objects.equals(termReasonText, bmpRouter.termReasonText) &&
                Objects.equals(termData, bmpRouter.termData) &&
                Objects.equals(initData, bmpRouter.initData) &&
                Objects.equals(geoIpStart, bmpRouter.geoIpStart) &&
                Objects.equals(bgpId, bmpRouter.bgpId) &&
                Objects.equals(collectorHashId, bmpRouter.collectorHashId) &&
                Objects.equals(connectionCount, bmpRouter.connectionCount) &&
                Objects.equals(action, bmpRouter.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashId, name, ipAddress, routerAS, timestamp, description, state, isPassive, termReasonCode, termReasonText, termData, initData, geoIpStart, collectorHashId, bgpId, connectionCount, action);
    }

    @Override
    public String toString() {
        return "BmpRouter{" +
                "id=" + id +
                ", hashId='" + hashId + '\'' +
                ", name='" + name + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", routerAS=" + routerAS +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                ", state=" + state +
                ", isPassive=" + isPassive +
                ", termReasonCode=" + termReasonCode +
                ", termReasonText='" + termReasonText + '\'' +
                ", termData='" + termData + '\'' +
                ", initData='" + initData + '\'' +
                ", geoIpStart='" + geoIpStart + '\'' +
                ", collectorHashId='" + collectorHashId + '\'' +
                ", bgpId='" + bgpId + '\'' +
                ", connectionCount=" + connectionCount +
                ", bmpPeers=" + bmpPeers +
                ", action='" + action + '\'' +
                '}';
    }
}
