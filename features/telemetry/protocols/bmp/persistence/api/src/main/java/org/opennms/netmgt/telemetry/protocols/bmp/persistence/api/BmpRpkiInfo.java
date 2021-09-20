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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Table(name = "bmp_rpki_info")
@Entity
public class BmpRpkiInfo implements Serializable {

    private static final long serialVersionUID = -2194393458129973561L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpRpkiInfoSequence")
    @SequenceGenerator(name = "bmpRpkiInfoSequence", sequenceName = "bmprpkiinfonxtid")
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
