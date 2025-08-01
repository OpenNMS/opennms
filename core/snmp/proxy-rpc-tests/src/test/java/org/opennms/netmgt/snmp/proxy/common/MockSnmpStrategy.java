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
package org.opennms.netmgt.snmp.proxy.common;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV2TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.TrapNotificationListener;

public class MockSnmpStrategy implements SnmpStrategy {

    private static boolean firstGetCall = true;
    private static boolean firstSetCall = true;

    @Override
    public SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker tracker) {
        return null;
    }

    @Override
    public SnmpValue set(SnmpAgentConfig agentConfig, SnmpObjId oid, SnmpValue value) {
        return null;
    }

    @Override
    public SnmpValue[] set(SnmpAgentConfig agentConfig, SnmpObjId[] oid, SnmpValue[] value) {
        return null;
    }

    @Override
    public SnmpValue get(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        return null;
    }

    @Override
    public SnmpValue[] get(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        return null;
    }

    @Override
    public CompletableFuture<SnmpValue[]> getAsync(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        if (firstGetCall) {
            firstGetCall = false;
            return SnmpProxyRpcModuleTest.completedFuture;
        }
        return SnmpProxyRpcModuleTest.failedFuture;
    }

    @Override
    public CompletableFuture<SnmpValue[]> setAsync(SnmpAgentConfig agentConfig, SnmpObjId[] oids, SnmpValue[] values) {
        if (firstSetCall) {
            firstSetCall = false;
            return SnmpProxyRpcModuleTest.completedFuture;
        }
        return SnmpProxyRpcModuleTest.failedFuture;
    }

    @Override
    public SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        return null;
    }

    @Override
    public SnmpValue[] getNext(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        return null;
    }

    @Override
    public SnmpValue[] getBulk(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        return null;
    }

    @Override
    public void registerForTraps(TrapNotificationListener listener, InetAddress address, int snmpTrapPort,
            List<SnmpV3User> snmpv3Users) throws IOException {

    }

    @Override
    public void registerForTraps(TrapNotificationListener listener, InetAddress address, int snmpTrapPort)
            throws IOException {

    }

    @Override
    public void registerForTraps(TrapNotificationListener listener, int snmpTrapPort) throws IOException {

    }

    @Override
    public void unregisterForTraps(TrapNotificationListener listener) throws IOException {

    }

    @Override
    public SnmpValueFactory getValueFactory() {
        return null;
    }

    @Override
    public SnmpV1TrapBuilder getV1TrapBuilder() {
        return null;
    }

    @Override
    public SnmpTrapBuilder getV2TrapBuilder() {
        return null;
    }

    @Override
    public SnmpV3TrapBuilder getV3TrapBuilder() {
        return null;
    }

    @Override
    public SnmpV2TrapBuilder getV2InformBuilder() {
        return null;
    }

    @Override
    public SnmpV3TrapBuilder getV3InformBuilder() {
        return null;
    }

    @Override
    public byte[] getLocalEngineID() {
        return null;
    }

    public static void setFirstCall(boolean firstCall) {
        MockSnmpStrategy.firstGetCall = firstCall;
    }
}
