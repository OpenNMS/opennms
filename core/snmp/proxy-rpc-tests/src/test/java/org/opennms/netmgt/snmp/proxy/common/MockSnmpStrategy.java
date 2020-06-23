/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

    private static boolean firstCall = true;

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
        if (firstCall) {
            firstCall = false;
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
    public void unregisterForTraps(TrapNotificationListener listener, InetAddress address, int snmpTrapPort)
            throws IOException {

    }

    @Override
    public void unregisterForTraps(TrapNotificationListener listener, int snmpTrapPort) throws IOException {

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

}
