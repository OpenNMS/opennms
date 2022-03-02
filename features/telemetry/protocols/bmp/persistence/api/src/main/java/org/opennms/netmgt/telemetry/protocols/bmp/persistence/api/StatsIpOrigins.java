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

import java.math.BigInteger;
import java.util.Date;

public class StatsIpOrigins {

    private Date intervalTime;

    private BigInteger recvOriginAs;

    private BigInteger v4prefixes;

    private BigInteger v6prefixes;

    private BigInteger v4withrpki;

    private BigInteger v6withrpki;

    private BigInteger v4withirr;

    private BigInteger v6withirr;

    public StatsIpOrigins(Date intervalTime, BigInteger asn, BigInteger v4prefixes, BigInteger v6prefixes, BigInteger v4withrpki, BigInteger v6withrpki, BigInteger v4withirr, BigInteger v6withirr) {
        this.intervalTime = intervalTime;
        this.recvOriginAs = asn;
        this.v4prefixes = v4prefixes;
        this.v6prefixes = v6prefixes;
        this.v4withrpki = v4withrpki;
        this.v6withrpki = v6withrpki;
        this.v4withirr = v4withirr;
        this.v6withirr = v6withirr;
    }

    public Date getIntervalTime() {
        return intervalTime;
    }

    public Long getRecvOriginAs() {
        return recvOriginAs != null ? recvOriginAs.longValue() : null;
    }

    public Integer getV4prefixes() {
        return v4prefixes != null ? v4prefixes.intValue() : null;
    }

    public Integer getV6prefixes() {
        return v6prefixes != null ? v6prefixes.intValue() : null;
    }

    public Integer getV4withrpki() {
        return v4withrpki != null ? v4withrpki.intValue() : null;
    }

    public Integer getV6withrpki() {
        return v6withrpki != null ? v6withrpki.intValue() : null;
    }

    public Integer getV4withirr() {
        return v4withirr != null ? v4withirr.intValue() : null;
    }

    public Integer getV6withirr() {
        return v6withirr != null ? v6withirr.intValue() : null;
    }
}
