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
package org.opennms.netmgt.snmp;

import java.math.BigInteger;
import java.net.InetAddress;


public interface SnmpValueFactory {

    SnmpValue getOctetString(byte[] bytes);

    SnmpValue getCounter32(long val);

    SnmpValue getCounter64(BigInteger val);

    SnmpValue getGauge32(long val);

    SnmpValue getInt32(int val);

    SnmpValue getIpAddress(InetAddress val);

    SnmpValue getObjectId(SnmpObjId objId);

    SnmpValue getTimeTicks(long val);

    SnmpValue getValue(int type, byte[] bytes);

    SnmpValue getNull();

    SnmpValue getOpaque(byte[] bs);
}
