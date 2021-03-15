/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

@Table(name = "bmp_route_info")
@Entity
public class BmpRouteInfo implements Serializable {

    private static final long serialVersionUID = 640224421099299300L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpRouteInfoSequence")
    @SequenceGenerator(name = "bmpRouteInfoSequence", sequenceName = "bmprouteinfonxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "prefix", nullable = false)
    private String prefix;

    @Column(name = "prefix_len", nullable = false)
    private Integer prefixLen;

    @Column(name = "descr", nullable = false)
    private String descr;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "origin_as", nullable = false)
    private Long originAs;

    @Column(name = "last_updated", nullable = false)
    private Date lastUpdated;

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

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getOriginAs() {
        return originAs;
    }

    public void setOriginAs(Long originAs) {
        this.originAs = originAs;
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
        BmpRouteInfo that = (BmpRouteInfo) o;
        return Objects.equals(prefix, that.prefix) &&
                Objects.equals(prefixLen, that.prefixLen) &&
                Objects.equals(descr, that.descr) &&
                Objects.equals(source, that.source) &&
                Objects.equals(originAs, that.originAs) &&
                Objects.equals(lastUpdated, that.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, prefixLen, descr, source, originAs, lastUpdated);
    }

    @Override
    public String toString() {
        return "BmpRouteInfo{" +
                "id=" + id +
                ", prefix='" + prefix + '\'' +
                ", prefixLen=" + prefixLen +
                ", descr='" + descr + '\'' +
                ", source='" + source + '\'' +
                ", originAs=" + originAs +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
