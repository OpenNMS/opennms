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

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Properties;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class SnmpUtils {

    private static Properties sm_config;

    private static final class TooBigReportingAggregator extends AggregateTracker {
        private final InetAddress address;

        private TooBigReportingAggregator(CollectionTracker[] children, InetAddress address) {
            super(children);
            this.address = address;
        }

        protected void reportTooBigErr(String msg) {
            ThreadCategory.getInstance(SnmpWalker.class).info("Received tooBig response from "+address+". "+msg);
        }
    }

    public static SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker[] trackers) {
        return createWalker(agentConfig, name, new TooBigReportingAggregator(trackers, agentConfig.getAddress()));
    }
    
    public static SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker tracker) {
        return getStrategy().createWalker(agentConfig, name, tracker);
        
    }
    public static SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, ColumnTracker tracker) {
        return getStrategy().createWalker(agentConfig, name, tracker);
    }
    
    public static SnmpValue get(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        return getStrategy().get(agentConfig, oid);
    }
    
    public static SnmpValue[] get(SnmpAgentConfig agentConfig, SnmpObjId oids[]) {
        return getStrategy().get(agentConfig, oids);
    }

    public static SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        return getStrategy().getNext(agentConfig, oid);
    }
    
    public static SnmpValue[] getNext(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        return getStrategy().getNext(agentConfig, oids);
    }
    
    public static SnmpValue[] getBulk(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        return getStrategy().getBulk(agentConfig, oids);
    }
    
    private static Category log() {
        return ThreadCategory.getInstance(SnmpUtils.class);
    }

    public static Properties getConfig() {
        return (sm_config == null ? System.getProperties() : sm_config);
    }
    
    public static void setConfig(Properties config) {
        sm_config = config;
    }
    
    public static SnmpStrategy getStrategy() {
        String strategyClass = getStrategyClassName();
        try {
            return (SnmpStrategy)Class.forName(strategyClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate class "+strategyClass, e);
        }
    }
    
    private static String getStrategyClassName() {
//        return getConfig().getProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy");
        return getConfig().getProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy");
    }
    
    public static void registerForTraps(TrapNotificationListener listener, TrapProcessorFactory processorFactory, int snmpTrapPort) throws SocketException {
        getStrategy().registerForTraps(listener, processorFactory, snmpTrapPort);
    }
    
    public static void unregisterForTraps(TrapNotificationListener listener, int snmpTrapPort) {
        getStrategy().unregisterForTraps(listener, snmpTrapPort);
    }
    
    public static SnmpValueFactory getValueFactory() {
        return getStrategy().getValueFactory();
    }
    
    public static void sendV1TestTrap(InetAddress agentAddress, int port, String community, SnmpObjId enterpriseId, int generic, int specific, long timeStamp) {
        getStrategy().sendV1TestTrap(agentAddress, port, community, enterpriseId, generic, specific, timeStamp);
    }

    public static void sendV2TestTrap(InetAddress agentAddress, int port, String community, SnmpObjId enterpriseId, int specific, long timeStamp) {
        getStrategy().sendV2TestTrap(agentAddress, port, community, enterpriseId, specific, timeStamp);
    }

}
