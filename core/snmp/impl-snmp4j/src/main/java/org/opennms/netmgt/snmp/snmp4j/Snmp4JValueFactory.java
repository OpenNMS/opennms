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
package org.opennms.netmgt.snmp.snmp4j;

import java.math.BigInteger;
import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Opaque;
import org.snmp4j.smi.TimeTicks;

public class Snmp4JValueFactory implements SnmpValueFactory {

    @Override
    public SnmpValue getOctetString(byte[] bytes) {
        return new Snmp4JValue(new OctetString(bytes));
    }

    @Override
    public SnmpValue getCounter32(long val) {
        return new Snmp4JValue(new Counter32(val));
    }

    @Override
    public SnmpValue getCounter64(BigInteger bigInt) {
        return new Snmp4JValue(new Counter64(bigInt.longValue()));
    }

    @Override
    public SnmpValue getGauge32(long val) {
        return new Snmp4JValue(new Gauge32(val));
    }

    @Override
    public SnmpValue getInt32(int val) {
        return new Snmp4JValue(new Integer32(val));
    }

    @Override
    public SnmpValue getIpAddress(InetAddress val) {
        return new Snmp4JValue(new IpAddress(val));
    }

    @Override
    public SnmpValue getObjectId(SnmpObjId objId) {
        return new Snmp4JValue(new OID(objId.getIds()));
    }

    @Override
    public SnmpValue getTimeTicks(long val) {
        return new Snmp4JValue(new TimeTicks(val));
    }

    @Override
    public SnmpValue getNull() {
        return new Snmp4JValue(new Null());
    }

    @Override
    public SnmpValue getValue(int type, byte[] bytes) {
        return new Snmp4JValue(type, bytes);
    }

    @Override
    public SnmpValue getOpaque(byte[] bs) {
        return new Snmp4JValue(new Opaque(bs));
    }


}
