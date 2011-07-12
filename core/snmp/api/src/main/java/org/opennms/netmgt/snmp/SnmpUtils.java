/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.snmp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.core.io.Resource;

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

    public static SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker... trackers) {
        LogUtils.debugf(SnmpUtils.class, "strategy = %s", getStrategyClassName());
        return getStrategy().createWalker(agentConfig, name, createTooBigTracker(agentConfig, trackers));
    }

    private static TooBigReportingAggregator createTooBigTracker(SnmpAgentConfig agentConfig, CollectionTracker... trackers) {
        return new TooBigReportingAggregator(trackers, agentConfig.getAddress());
    }
    
    public static SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker tracker) {
        return getStrategy().createWalker(agentConfig, name, createTooBigTracker(agentConfig, tracker));
    }

    private static TooBigReportingAggregator createTooBigTracker(SnmpAgentConfig agentConfig, CollectionTracker tracker) {
        return createTooBigTracker(agentConfig, new CollectionTracker[] { tracker });
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

    public static SnmpValue set(final SnmpAgentConfig agentConfig, final SnmpObjId oid, final SnmpValue value) {
    	return getStrategy().set(agentConfig, oid, value);
    }

    public static SnmpValue[] set(final SnmpAgentConfig agentConfig, final SnmpObjId[] oids, final SnmpValue[] values) {
    	return getStrategy().set(agentConfig, oids, values);
    }

    public static Properties getConfig() {
        return (sm_config == null ? System.getProperties() : sm_config);
    }

    public static List<SnmpValue> getColumns(final SnmpAgentConfig agentConfig, final String name, final SnmpObjId oid)  throws InterruptedException {

        final List<SnmpValue> results = new ArrayList<SnmpValue>();
        
        SnmpWalker walker=SnmpUtils.createWalker(agentConfig, name, new ColumnTracker(oid) {
   
            @Override
            protected void storeResult(SnmpResult res) {
                results.add(res.getValue());
            }
           
        });
        walker.start();
        walker.waitFor();
        return results;
    }
    
    public static Map<SnmpInstId, SnmpValue> getOidValues(SnmpAgentConfig agentConfig, String name, SnmpObjId oid) 
	throws InterruptedException {

        final Map<SnmpInstId, SnmpValue> results = new LinkedHashMap<SnmpInstId, SnmpValue>();
        
        SnmpWalker walker=SnmpUtils.createWalker(agentConfig, name, new ColumnTracker(oid) {
   
            @Override
            protected void storeResult(SnmpResult res) {
                results.put(res.getInstance(), res.getValue());
            }
           
        });
	walker.start();
	walker.waitFor();
        return results;
    }
    
    public static void setConfig(Properties config) {
        sm_config = config;
    }
    
    public static SnmpStrategy getStrategy() {
    	final String strategyClass = getStrategyClassName();
        try {
            return (SnmpStrategy)Class.forName(strategyClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate class "+strategyClass, e);
        }
    }
    
    private static String getStrategyClassName() {
        // Use SNMP4J as the default SNMP strategy
        return getConfig().getProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy");
//        return getConfig().getProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy");
    }

    public static void registerForTraps(final TrapNotificationListener listener, final TrapProcessorFactory processorFactory, final InetAddress address, final int snmpTrapPort, final List<SnmpV3User> snmpUsers) throws IOException {
        getStrategy().registerForTraps(listener, processorFactory, address, snmpTrapPort, snmpUsers);
    }

    public static void registerForTraps(final TrapNotificationListener listener, final TrapProcessorFactory processorFactory, final InetAddress address, final int snmpTrapPort) throws IOException {
        getStrategy().registerForTraps(listener, processorFactory, address, snmpTrapPort);
    }
    
    public static void unregisterForTraps(final TrapNotificationListener listener, final InetAddress address, final int snmpTrapPort) throws IOException {
        getStrategy().unregisterForTraps(listener, snmpTrapPort);
    }
    
    public static SnmpValueFactory getValueFactory() {
        return getStrategy().getValueFactory();
    }
    
    public static SnmpV1TrapBuilder getV1TrapBuilder() {
        return getStrategy().getV1TrapBuilder();
    }
    
    public static SnmpTrapBuilder getV2TrapBuilder() {
        return getStrategy().getV2TrapBuilder();
    }

    public static SnmpV3TrapBuilder getV3TrapBuilder() {
        return getStrategy().getV3TrapBuilder();
    }

    public static SnmpV2TrapBuilder getV2InformBuilder() {
        return getStrategy().getV2InformBuilder();
    }

    public static SnmpV3TrapBuilder getV3InformBuilder() {
        return getStrategy().getV3InformBuilder();
    }

    public static String getLocalEngineID() {
    	return getHexString(getStrategy().getLocalEngineID());
    }
    
    static final byte[] HEX_CHAR_TABLE = {
	    (byte)'0', (byte)'1', (byte)'2', (byte)'3',
	    (byte)'4', (byte)'5', (byte)'6', (byte)'7',
	    (byte)'8', (byte)'9', (byte)'a', (byte)'b',
	    (byte)'c', (byte)'d', (byte)'e', (byte)'f'
	};    

	public static String getHexString(byte[] raw) 
	  {
	    byte[] hex = new byte[2 * raw.length];
	    int index = 0;

	    for (byte b : raw) {
	      int v = b & 0xFF;
	      hex[index++] = HEX_CHAR_TABLE[v >>> 4];
	      hex[index++] = HEX_CHAR_TABLE[v & 0xF];
	    }
	    try {
			return new String(hex, "ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}


    /**
     * <p>If the value is in the unprintable ASCII range (< 32) and is not a:</p>
     * <ul>
     *   <li>Tab (9)</li>
     *   <li>Linefeed (10)</li>
     *   <li>Carriage return (13)</li>
     * <ul>
     * <p>or the byte is Delete (127) then this method will return false. Also, if the byte 
     * array has a NULL byte (0) that occurs anywhere besides the last character, return false. 
     * We will allow the NULL byte as a special case at the end of the string.</p>
     */
    public static boolean allBytesDisplayable(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            // Null (0)
            if (b == 0) {
                if (i != (bytes.length - 1)) {
                    return false;
                }
            }
            // Low or high ASCII (excluding Tab, Carriage Return, and Linefeed)
            else if (b < 32 && b != 9 && b != 10 && b != 13) {
                return false;
            }
            // Delete (127)
            else if (b == 127) {
                return false;
            }
        }
        return true;
    }

	/**
	 * <p>loadProperties</p>
	 *
	 * @param propertiesFile a {@link org.springframework.core.io.Resource} object.
	 * @return a {@link java.util.Properties} object.
	 */
	public static  Properties loadProperties(final Resource propertiesFile) {
		final Properties moProps = new Properties();
		InputStream inStream = null;
		try {
	        inStream = propertiesFile.getInputStream();
			moProps.load( inStream );
		} catch (final Exception ex) {
		    LogUtils.warnf(SnmpUtils.class, ex, "Unable to read property file %s", propertiesFile);
			return null;
		} finally {
	        IOUtils.closeQuietly(inStream);
		}
	    return moProps;
	}

	public static SnmpValue parseMibValue(final String mibVal) {
	    if (mibVal.startsWith("OID:"))
	    	return getValueFactory().getObjectId(SnmpObjId.get(mibVal.substring("OID:".length()).trim()));
	    else if (mibVal.startsWith("Timeticks:")) {
	    	String timeticks = mibVal.substring("Timeticks:".length()).trim();
			if (timeticks.contains("(")) {
				timeticks = timeticks.replaceAll("^.*\\((\\d*?)\\).*$", "$1");
			}
			return getValueFactory().getTimeTicks(Long.valueOf(timeticks));
		} else if (mibVal.startsWith("STRING:"))
			return getValueFactory().getOctetString(mibVal.substring("STRING:".length()).trim().getBytes());
	    else if (mibVal.startsWith("INTEGER:"))
			return getValueFactory().getInt32(Integer.valueOf(mibVal.substring("INTEGER:".length()).trim()));
	    else if (mibVal.startsWith("Gauge32:"))
	    	return getValueFactory().getGauge32(Long.valueOf(mibVal.substring("Gauge32:".length()).trim()));
	    else if (mibVal.startsWith("Counter32:"))
	    	return getValueFactory().getCounter32(Long.valueOf(mibVal.substring("Counter32:".length()).trim()));
	    else if (mibVal.startsWith("Counter64:"))
	    	return getValueFactory().getCounter64(BigInteger.valueOf(Long.valueOf(mibVal.substring("Counter64:".length()).trim())));
	    else if (mibVal.startsWith("IpAddress:"))
	    	return getValueFactory().getIpAddress(InetAddressUtils.addr(mibVal.substring("IpAddress:".length()).trim()));
	    else if (mibVal.startsWith("Hex-STRING:"))
			return getValueFactory().getOctetString(mibVal.substring("STRING:".length()).trim().getBytes());
	    else if (mibVal.startsWith("Network Address:"))
			return getValueFactory().getOctetString(mibVal.substring("Network Address:".length()).trim().getBytes());
	    else if (mibVal.startsWith("BITS:"))
			return getValueFactory().getOctetString(mibVal.substring("BITS:".length()).trim().getBytes());
	    else if (mibVal.equals("\"\""))
	    	return getValueFactory().getNull();
	
	    throw new IllegalArgumentException("Unknown Snmp Type: "+mibVal);
	}

}
