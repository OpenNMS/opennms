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
