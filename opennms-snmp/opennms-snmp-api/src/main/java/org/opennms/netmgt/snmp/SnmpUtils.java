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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import org.opennms.core.utils.ThreadCategory;

/**
 * <p>SnmpUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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

    /**
     * <p>createWalker</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param name a {@link java.lang.String} object.
     * @param trackers an array of {@link org.opennms.netmgt.snmp.CollectionTracker} objects.
     * @return a {@link org.opennms.netmgt.snmp.SnmpWalker} object.
     */
    public static SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker[] trackers) {
        return getStrategy().createWalker(agentConfig, name, createTooBigTracker(agentConfig, trackers));
    }

    private static TooBigReportingAggregator createTooBigTracker(SnmpAgentConfig agentConfig, CollectionTracker[] trackers) {
        return new TooBigReportingAggregator(trackers, agentConfig.getAddress());
    }
    
    /**
     * <p>createWalker</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param name a {@link java.lang.String} object.
     * @param tracker a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpWalker} object.
     */
    public static SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker tracker) {
        return getStrategy().createWalker(agentConfig, name, createTooBigTracker(agentConfig, tracker));
    }

    private static TooBigReportingAggregator createTooBigTracker(SnmpAgentConfig agentConfig, CollectionTracker tracker) {
        return createTooBigTracker(agentConfig, new CollectionTracker[] { tracker });
    }
    
    /**
     * <p>get</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oid a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public static SnmpValue get(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        return getStrategy().get(agentConfig, oid);
    }
    
    /**
     * <p>get</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oids an array of {@link org.opennms.netmgt.snmp.SnmpObjId} objects.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     */
    public static SnmpValue[] get(SnmpAgentConfig agentConfig, SnmpObjId oids[]) {
        return getStrategy().get(agentConfig, oids);
    }

    /**
     * <p>getNext</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oid a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public static SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        return getStrategy().getNext(agentConfig, oid);
    }
    
    /**
     * <p>getNext</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oids an array of {@link org.opennms.netmgt.snmp.SnmpObjId} objects.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     */
    public static SnmpValue[] getNext(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        return getStrategy().getNext(agentConfig, oids);
    }
    
    /**
     * <p>getBulk</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oids an array of {@link org.opennms.netmgt.snmp.SnmpObjId} objects.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     */
    public static SnmpValue[] getBulk(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        return getStrategy().getBulk(agentConfig, oids);
    }
    
    /**
     * <p>set</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oid a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param value a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public static SnmpValue set(SnmpAgentConfig agentConfig, SnmpObjId oid, SnmpValue value) {
    	return getStrategy().set(agentConfig, oid, value);
    }

    /**
     * <p>set</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oids an array of {@link org.opennms.netmgt.snmp.SnmpObjId} objects.
     * @param values an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     */
    public static SnmpValue[] set(SnmpAgentConfig agentConfig, SnmpObjId[] oids, SnmpValue[] values) {
    	return getStrategy().set(agentConfig, oids, values);
    }

    /**
     * <p>getConfig</p>
     *
     * @return a {@link java.util.Properties} object.
     */
    public static Properties getConfig() {
        return (sm_config == null ? System.getProperties() : sm_config);
    }

    /**
     * <p>getColumns</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param name a {@link java.lang.String} object.
     * @param oid a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link java.util.List} object.
     * @throws java.lang.InterruptedException if any.
     */
    public static List<SnmpValue> getColumns(SnmpAgentConfig agentConfig, String name, SnmpObjId oid) 
	throws InterruptedException {

        final List<SnmpValue> results = new ArrayList<SnmpValue>();
        
        SnmpWalker walker=SnmpUtils.createWalker(agentConfig, name, new ColumnTracker(oid) {
   
            @Override
            protected void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
                results.add(val);
            }
           
        });
	walker.start();
	walker.waitFor();
        return results;
    }
    
    /**
     * <p>getOidValues</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param name a {@link java.lang.String} object.
     * @param oid a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link java.util.Map} object.
     * @throws java.lang.InterruptedException if any.
     */
    public static Map<SnmpInstId, SnmpValue> getOidValues(SnmpAgentConfig agentConfig, String name, SnmpObjId oid) 
	throws InterruptedException {

        final Map<SnmpInstId, SnmpValue> results = new LinkedHashMap<SnmpInstId, SnmpValue>();
        
        SnmpWalker walker=SnmpUtils.createWalker(agentConfig, name, new ColumnTracker(oid) {
   
            @Override
            protected void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
                results.put(inst, val);
            }
           
        });
	walker.start();
	walker.waitFor();
        return results;
    }
    
    /**
     * <p>setConfig</p>
     *
     * @param config a {@link java.util.Properties} object.
     */
    public static void setConfig(Properties config) {
        sm_config = config;
    }
    
    /**
     * <p>getStrategy</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpStrategy} object.
     */
    public static SnmpStrategy getStrategy() {
        String strategyClass = getStrategyClassName();
        try {
            return (SnmpStrategy)Class.forName(strategyClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate class "+strategyClass, e);
        }
    }
    
    private static String getStrategyClassName() {
        return getConfig().getProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy");
//        return getConfig().getProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy");
    }
    
    /**
     * <p>registerForTraps</p>
     *
     * @param listener a {@link org.opennms.netmgt.snmp.TrapNotificationListener} object.
     * @param processorFactory a {@link org.opennms.netmgt.snmp.TrapProcessorFactory} object.
     * @param snmpTrapPort a int.
     * @throws java.io.IOException if any.
     */
    public static void registerForTraps(TrapNotificationListener listener, TrapProcessorFactory processorFactory, int snmpTrapPort) throws IOException {
        getStrategy().registerForTraps(listener, processorFactory, snmpTrapPort);
    }
    
    /**
     * <p>unregisterForTraps</p>
     *
     * @param listener a {@link org.opennms.netmgt.snmp.TrapNotificationListener} object.
     * @param snmpTrapPort a int.
     * @throws java.io.IOException if any.
     */
    public static void unregisterForTraps(TrapNotificationListener listener, int snmpTrapPort) throws IOException {
        getStrategy().unregisterForTraps(listener, snmpTrapPort);
    }
    
    /**
     * <p>getValueFactory</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpValueFactory} object.
     */
    public static SnmpValueFactory getValueFactory() {
        return getStrategy().getValueFactory();
    }
    
    /**
     * <p>getV1TrapBuilder</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpV1TrapBuilder} object.
     */
    public static SnmpV1TrapBuilder getV1TrapBuilder() {
        return getStrategy().getV1TrapBuilder();
    }
    
    /**
     * <p>getV2TrapBuilder</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpTrapBuilder} object.
     */
    public static SnmpTrapBuilder getV2TrapBuilder() {
        return getStrategy().getV2TrapBuilder();
    }

}
