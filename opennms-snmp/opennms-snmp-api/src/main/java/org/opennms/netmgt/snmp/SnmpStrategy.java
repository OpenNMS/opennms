//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp;

import java.io.IOException;


/**
 * <p>SnmpStrategy interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface SnmpStrategy {

    /**
     * <p>createWalker</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param name a {@link java.lang.String} object.
     * @param tracker a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpWalker} object.
     */
    SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker tracker);

    /**
     * <p>set</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oid a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param value a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue set(SnmpAgentConfig agentConfig, SnmpObjId oid, SnmpValue value);

    /**
     * <p>set</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oid an array of {@link org.opennms.netmgt.snmp.SnmpObjId} objects.
     * @param value an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     */
    SnmpValue[] set(SnmpAgentConfig agentConfig, SnmpObjId oid[], SnmpValue value[]);

    /**
     * <p>get</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oid a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue get(SnmpAgentConfig agentConfig, SnmpObjId oid);
    /**
     * <p>get</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oids an array of {@link org.opennms.netmgt.snmp.SnmpObjId} objects.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     */
    SnmpValue[] get(SnmpAgentConfig agentConfig, SnmpObjId[] oids);

    /**
     * <p>getNext</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oid a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid);
    /**
     * <p>getNext</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oids an array of {@link org.opennms.netmgt.snmp.SnmpObjId} objects.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     */
    SnmpValue[] getNext(SnmpAgentConfig agentConfig, SnmpObjId[] oids);
    
    /**
     * <p>getBulk</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oids an array of {@link org.opennms.netmgt.snmp.SnmpObjId} objects.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     */
    SnmpValue[] getBulk(SnmpAgentConfig agentConfig, SnmpObjId[] oids);

    /**
     * <p>registerForTraps</p>
     *
     * @param listener a {@link org.opennms.netmgt.snmp.TrapNotificationListener} object.
     * @param processorFactory a {@link org.opennms.netmgt.snmp.TrapProcessorFactory} object.
     * @param snmpTrapPort a int.
     * @throws java.io.IOException if any.
     */
    void registerForTraps(TrapNotificationListener listener, TrapProcessorFactory processorFactory, int snmpTrapPort) throws IOException;

    /**
     * <p>unregisterForTraps</p>
     *
     * @param listener a {@link org.opennms.netmgt.snmp.TrapNotificationListener} object.
     * @param snmpTrapPort a int.
     * @throws java.io.IOException if any.
     */
    void unregisterForTraps(TrapNotificationListener listener, int snmpTrapPort) throws IOException;

    /**
     * <p>getValueFactory</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpValueFactory} object.
     */
    SnmpValueFactory getValueFactory();

    /**
     * <p>getV1TrapBuilder</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpV1TrapBuilder} object.
     */
    SnmpV1TrapBuilder getV1TrapBuilder();
    
    /**
     * <p>getV2TrapBuilder</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpTrapBuilder} object.
     */
    SnmpTrapBuilder getV2TrapBuilder();

}
