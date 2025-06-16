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
const { Address4, Address6 } = require('ip-address');

const verifyIPv4Address = function(ip) {
    try {
        new Address4(ip);
    } catch (err) {
        return false;
    }
    return true;
};

const verifyIPv6Address = function(ip) {
    try {
        new Address6(ip);
    } catch (err) {
        return false;
    }
    return true;
};

const isValidIPAddress = function(ip) {
    return verifyIPv4Address(ip) || verifyIPv6Address(ip);
};

const checkIpRange = function(ip1, ip2){
    if (verifyIPv4Address(ip1) && verifyIPv4Address(ip2)) {
        const a = new Address4(ip1).bigInteger();
        const b = new Address4(ip2).bigInteger();
        return b >= a;
    }
    if (verifyIPv6Address(ip1) && verifyIPv6Address(ip2)) {
        const a = new Address6(ip1).bigInteger();
        const b = new Address6(ip2).bigInteger();
        return b.compareTo(a) >= 0;
    }
    return false;
};

console.log('init: ipaddress-js'); // eslint-disable-line no-console

module.exports = {
    Address4: Address4,
    Address6: Address6,
    verifyIPv4Address: verifyIPv4Address,
    verifyIPv6Address: verifyIPv6Address,
    isValidIPAddress: isValidIPAddress,
    checkIpRange: checkIpRange
};

Object.assign(window, module.exports);
