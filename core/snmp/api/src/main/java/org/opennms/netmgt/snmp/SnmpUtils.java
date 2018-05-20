/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SnmpUtils {

    private static final transient Logger LOG = LoggerFactory.getLogger(SnmpUtils.class);

    private static final ClassBasedStrategyResolver s_classBasedStrategyResolver = new ClassBasedStrategyResolver();

    private static Properties sm_config;
    private static StrategyResolver s_strategyResolver;
    private static final boolean canUseClassBasedStrategy = checkIfClassBasedStrategyIsInstantiable();

    private static final class TooBigReportingAggregator extends AggregateTracker {
        private final InetAddress address;

        private TooBigReportingAggregator(CollectionTracker[] children, InetAddress address) {
            super(children);
            this.address = address;
        }

        @Override
        protected void reportTooBigErr(String msg) {
            LOG.info("Received tooBig response from {}. {}", address, msg);
        }
    }

    public static SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker... trackers) {
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
    
    public static SnmpValue[] get(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        return getStrategy().get(agentConfig, oids);
    }

    public static CompletableFuture<SnmpValue[]> getAsync(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        return getStrategy().getAsync(agentConfig, oids);
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
        
        try(SnmpWalker walker=SnmpUtils.createWalker(agentConfig, name, new ColumnTracker(oid) {
            @Override
            protected void storeResult(SnmpResult res) {
                results.add(res.getValue());
            }
        })) {
            walker.start();
            walker.waitFor();
        }
        return results;
    }

    public static Map<SnmpInstId, SnmpValue> getOidValues(SnmpAgentConfig agentConfig, String name, SnmpObjId oid) 
	throws InterruptedException {

        final Map<SnmpInstId, SnmpValue> results = new LinkedHashMap<SnmpInstId, SnmpValue>();
        
        try(SnmpWalker walker=SnmpUtils.createWalker(agentConfig, name, new ColumnTracker(oid) {
            @Override
            protected void storeResult(SnmpResult res) {
                results.put(res.getInstance(), res.getValue());
            }
        })) {
            walker.start();
            walker.waitFor();
        }
        return results;
    }

    public static void setConfig(Properties config) {
        sm_config = config;
    }
    
    public static SnmpStrategy getStrategy() {
        if (isClassBasedStrategyInstantiable()) {
            return s_classBasedStrategyResolver.getStrategy();
        }
    	return getStrategyResolver().getStrategy();
    }

    private static boolean checkIfClassBasedStrategyIsInstantiable() {
        try {
            s_classBasedStrategyResolver.getStrategy();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isClassBasedStrategyInstantiable() {
        return canUseClassBasedStrategy;
    }

    public static StrategyResolver getStrategyResolver() {
    	return s_strategyResolver != null ? s_strategyResolver : s_classBasedStrategyResolver;
    }

    public static void setStrategyResolver(StrategyResolver strategyResolver) {
        if (!isClassBasedStrategyInstantiable()) {
            s_strategyResolver = strategyResolver;
        }
    }

    public static void unsetStrategyResolver() {
    	s_strategyResolver = null;
    }

    public static String getStrategyClassName() {
        // Use SNMP4J as the default SNMP strategy
        return getConfig().getProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy");
    }

    public static void registerForTraps(final TrapNotificationListener listener, final InetAddress address, final int snmpTrapPort, final List<SnmpV3User> snmpUsers) throws IOException {
        getStrategy().registerForTraps(listener, address, snmpTrapPort, snmpUsers);
    }

    public static void registerForTraps(final TrapNotificationListener listener, final InetAddress address, final int snmpTrapPort) throws IOException {
        getStrategy().registerForTraps(listener, address, snmpTrapPort);
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
		    LOG.warn("Failed to get hex string", e);
		    return null;
		}
	}

	public static Long getProtoCounter63Value(SnmpValue value) {
		Long retval = getProtoCounter63Value(value.getBytes()); 
		if (retval != null && value.isDisplayable()) {
			LOG.info("Value '{}' is entirely displayable but still meets our other checks to be treated as a proto-Counter64. This may not be what you want.", new String(value.getBytes()));
		}
		return retval;
	}

	/**
	 * <p>Enable the SNMP code to digest OCTET STRING values acting as proto-Counter64
	 * objects as seen in the FCMGMT-MIB with the following comment:</p>
	 * 
	 * <p>There is one and only one statistics table for each
	 * individual port. For all objects in statistics table, if the object is not
	 * supported by the conn unit then the high order bit is set to 1 with all other
	 * bits set to zero. The high order bit is reserved to indicate if the object
	 * if supported or not. All objects start at a value of zero at hardware
	 * initialization and continue incrementing till end of 63 bits and then
	 * wrap to zero.</p>
	 * 
	 * @see <a href="http://issues.opennms.org/browse/NMS-5423">NMS-5423</a>
	 */
	public static Long getProtoCounter63Value(byte[] valBytes) {
	    if (valBytes.length != 8) {
	        LOG.trace("Value should be 8 bytes long for a proto-Counter63 but this one is {} bytes.", valBytes);
	        return null;
	    } else if (Arrays.equals(valBytes, new byte[]{ (byte)0x80, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 })) {
	        LOG.trace("Value has high-order bit set and all others zero, which indicates \"not supported\" in FCMGMT-MIB convention");
	        return null;
	    } else if ((valBytes[0] & 0x80) == 0x80) {
	        LOG.trace("Value has high-order bit set but proto-Counter63 should only be 63 bits");
	        return null;
	    }

	    // Check to see if each byte is an ASCII decimal digit. If all of the bytes are
	    // decimal digits, then do not interpret this value as a 64-bit counter and return
	    // null. It is probably not a 64-bit counter; it is most likely a decimal string
	    // value.
	    //
	    // @see http://issues.opennms.org/browse/NMS-6202
	    //
	    boolean onlyNumeric = true;
	    for (byte digit : valBytes) {
	        if (digit < 0x30 /* 0 */ || digit > 0x39 /* 9 */) {
	            onlyNumeric = false;
	            break;
	        }
	    }

	    if (onlyNumeric) {
	        LOG.trace("Value contains only ASCII decimal numbers so it should be interpreted as a decimal counter");
	        return null;
	    }

	    Long retVal = Long.decode(String.format("0x%02x%02x%02x%02x%02x%02x%02x%02x", valBytes[0], valBytes[1], valBytes[2], valBytes[3], valBytes[4], valBytes[5], valBytes[6], valBytes[7]));
	    LOG.trace("Converted octet-string {} as a proto-Counter63 of value {}", String.format("0x%02x%02x%02x%02x%02x%02x%02x%02x", valBytes[0], valBytes[1], valBytes[2], valBytes[3], valBytes[4], valBytes[5], valBytes[6], valBytes[7]), retVal);
	    return retVal;
	}
}
