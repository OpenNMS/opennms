/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.snmp;

import java.io.IOException;


public interface SnmpStrategy {

    SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker tracker);

    SnmpValue set(SnmpAgentConfig agentConfig, SnmpObjId oid, SnmpValue value);

    SnmpValue[] set(SnmpAgentConfig agentConfig, SnmpObjId oid[], SnmpValue value[]);

    SnmpValue get(SnmpAgentConfig agentConfig, SnmpObjId oid);
    SnmpValue[] get(SnmpAgentConfig agentConfig, SnmpObjId[] oids);

    SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid);
    SnmpValue[] getNext(SnmpAgentConfig agentConfig, SnmpObjId[] oids);
    
    SnmpValue[] getBulk(SnmpAgentConfig agentConfig, SnmpObjId[] oids);

    void registerForTraps(TrapNotificationListener listener, TrapProcessorFactory processorFactory, int snmpTrapPort) throws IOException;

    void unregisterForTraps(TrapNotificationListener listener, int snmpTrapPort) throws IOException;

    SnmpValueFactory getValueFactory();

    SnmpV1TrapBuilder getV1TrapBuilder();
    
    SnmpTrapBuilder getV2TrapBuilder();

}
