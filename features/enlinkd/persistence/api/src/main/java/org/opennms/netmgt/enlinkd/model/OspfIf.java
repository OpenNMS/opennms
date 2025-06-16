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
package org.opennms.netmgt.enlinkd.model;

import java.net.InetAddress;

public class OspfIf {
    private InetAddress ospfIfIpaddress;
    private InetAddress ospfIfNetmask;
    private Integer ospfIfAddressLessIf;
    private InetAddress ospfIfAreaId;
    private Integer ospfIfIfindex;

    public InetAddress getOspfIfIpaddress() {
        return ospfIfIpaddress;
    }

    public void setOspfIfIpaddress(InetAddress ospfIfIpaddress) {
        this.ospfIfIpaddress = ospfIfIpaddress;
    }

    public InetAddress getOspfIfNetmask() {
        return ospfIfNetmask;
    }

    public void setOspfIfNetmask(InetAddress ospfIfNetmask) {
        this.ospfIfNetmask = ospfIfNetmask;
    }

    public Integer getOspfIfAddressLessIf() {
        return ospfIfAddressLessIf;
    }

    public void setOspfIfAddressLessIf(Integer ospfIfAddressLessIf) {
        this.ospfIfAddressLessIf = ospfIfAddressLessIf;
    }

    public InetAddress getOspfIfAreaId() {
        return ospfIfAreaId;
    }

    public void setOspfIfAreaId(InetAddress ospfIfAreaId) {
        this.ospfIfAreaId = ospfIfAreaId;
    }

    public Integer getOspfIfIfindex() {
        return ospfIfIfindex;
    }

    public void setOspfIfIfindex(Integer ospfIfIfindex) {
        this.ospfIfIfindex = ospfIfIfindex;
    }
}
