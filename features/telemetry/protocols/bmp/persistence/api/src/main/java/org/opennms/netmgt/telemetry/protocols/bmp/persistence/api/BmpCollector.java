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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Table(name = "bmp_collectors")
@Entity
public class BmpCollector implements Serializable {

    private static final long serialVersionUID = 3094029180922290726L;

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "bmpCollectorSequence")
    @SequenceGenerator(name = "bmpCollectorSequence", sequenceName = "bmpcollectornxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "hash_id", nullable = false)
    private String hashId;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "admin_id", nullable = false)
    private String adminId;

    @Column(name = "routers_count", nullable = false)
    private Integer routersCount;

    @Column(name = "last_updated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "name")
    private String name;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "routers")
    private String routers;

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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public Integer getRoutersCount() {
        return routersCount;
    }

    public void setRoutersCount(Integer routersCount) {
        this.routersCount = routersCount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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

    public String getRouters() {
        return routers;
    }

    public void setRouters(String routers) {
        this.routers = routers;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    @Override
    public String toString() {
        return "BmpCollector{" +
                "id=" + id +
                ", hashId='" + hashId + '\'' +
                ", state=" + state +
                ", adminId='" + adminId + '\'' +
                ", routersCount=" + routersCount +
                ", timestamp=" + timestamp +
                ", name='" + name + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", routers='" + routers + '\'' +
                ", action='" + action + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BmpCollector that = (BmpCollector) o;
        return Objects.equals(hashId, that.hashId) &&
                state == that.state &&
                Objects.equals(adminId, that.adminId) &&
                Objects.equals(routersCount, that.routersCount) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(name, that.name) &&
                Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(routers, that.routers) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashId, state, adminId, routersCount, timestamp, name, ipAddress, routers, action);
    }
}
