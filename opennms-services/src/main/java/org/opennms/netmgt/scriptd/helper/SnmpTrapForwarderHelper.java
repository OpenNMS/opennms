/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpV2TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.xml.event.Event;

public abstract class SnmpTrapForwarderHelper extends AbstractEventForwarder implements
		EventForwarder {

	String source_ip;
	
	String ip;
	String community;
	int port;

	int timeout;
	int retries;

	int securityLevel;
	String securityname;
	String authPassPhrase;
	String authProtocol;
	String privPassPhrase;
	String privprotocol;
	SnmpTrapHelper snmpTrapHelper;

	public SnmpTrapHelper getSnmpTrapHelper() {
		return snmpTrapHelper;
	}

	public void setSnmpTrapHelper(SnmpTrapHelper snmpTrapHelper) {
		this.snmpTrapHelper = snmpTrapHelper;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public SnmpTrapForwarderHelper(String source_ip, String ip, int port, String community,SnmpTrapHelper snmpTrapHelper) {
		this.source_ip = source_ip;
		this.ip = ip;
		this.port=port;
		this.community=community;		
		this.snmpTrapHelper = snmpTrapHelper;
	}

	public SnmpTrapForwarderHelper(String ip, int port, String community, SnmpTrapHelper snmpTrapHelper) {
		this.ip = ip;
		this.port=port;
		this.community=community;
		this.snmpTrapHelper = snmpTrapHelper;
	}

	public SnmpTrapForwarderHelper(String ip, int port, String community, int timeout, int retries, SnmpTrapHelper snmpTrapHelper) {
		this.ip = ip;
		this.port=port;
		this.community=community;
		this.snmpTrapHelper = snmpTrapHelper;
		this.timeout = timeout;
		this.retries = retries;
	}
	
	public SnmpTrapForwarderHelper(String ip, int port, int securityLevel,
			String securityname, String authPassPhrase, String authProtocol,
			String privPassPhrase, String privprotocol, SnmpTrapHelper snmpTrapHelper) {
		super();
		this.ip = ip;
		this.port = port;
		this.securityLevel = securityLevel;
		this.securityname = securityname;
		this.authPassPhrase = authPassPhrase;
		this.authProtocol = authProtocol;
		this.privPassPhrase = privPassPhrase;
		this.privprotocol = privprotocol;
		this.snmpTrapHelper = snmpTrapHelper;

	}

	public SnmpTrapForwarderHelper(String ip, int port, int securityLevel,
			String securityname, String authPassPhrase, String authProtocol,
			String privPassPhrase, String privprotocol, int timeout, int retries, SnmpTrapHelper snmpTrapHelper) {
		super();
		this.ip = ip;
		this.port = port;
		this.securityLevel = securityLevel;
		this.securityname = securityname;
		this.authPassPhrase = authPassPhrase;
		this.authProtocol = authProtocol;
		this.privPassPhrase = privPassPhrase;
		this.privprotocol = privprotocol;
		this.snmpTrapHelper = snmpTrapHelper;
		this.timeout = timeout;
		this.retries = retries;
	}

	public void sendV1StartSyncTrap() {
        try {
    		SnmpTrapBuilder trap = snmpTrapHelper.createV1Trap(".1.3.6.1.4.1.5813.1",getSource_ip(), 6, 5, 0);
			trap.send(getIp(), getPort(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void sendV1EndSyncTrap() {
        try {
    		SnmpTrapBuilder trap = snmpTrapHelper.createV1Trap(".1.3.6.1.4.1.5813.1",getSource_ip(), 6, 6, 0);
			trap.send(getIp(), getPort(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void sendV2StartSyncTrap() {
		long trapTimeStamp = 0;
        try {
    		SnmpTrapBuilder trap = snmpTrapHelper.createV2Trap(".1.3.6.1.4.1.5813.1.5",Long.toString(trapTimeStamp));
			trap.send(getIp(), getPort(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void sendV2EndSyncTrap() {
		long trapTimeStamp = 0;
        try {
    		SnmpTrapBuilder trap = snmpTrapHelper.createV2Trap(".1.3.6.1.4.1.5813.1.6",Long.toString(trapTimeStamp));
			trap.send(getIp(), getPort(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void sendV2StartSyncInform() {
		long trapTimeStamp = 0;
        try {
    		SnmpV2TrapBuilder inform = snmpTrapHelper.createV2Inform(".1.3.6.1.4.1.5813.1.5",Long.toString(trapTimeStamp));
			inform.sendInform(getIp(), getPort(), getTimeout(), getRetries(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV2EndSyncInform() {
		long trapTimeStamp = 0;
        try {
    		SnmpV2TrapBuilder inform = snmpTrapHelper.createV2Inform(".1.3.6.1.4.1.5813.1.6",Long.toString(trapTimeStamp));
			inform.sendInform(getIp(), getPort(), getTimeout(), getRetries(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV3StartSyncTrap() {
		long trapTimeStamp = 0;
        try {
    		SnmpV3TrapBuilder trap = snmpTrapHelper.createV3Trap(".1.3.6.1.4.1.5813.1.5",Long.toString(trapTimeStamp));
			trap.send(getIp(), getPort(), getSecurityLevel(), getSecurityname(), getAuthPassPhrase(), getAuthProtocol(), getPrivPassPhrase(), getPrivprotocol());
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void sendV3EndSyncTrap() {
		long trapTimeStamp = 0;
        try {
    		SnmpV3TrapBuilder trap = snmpTrapHelper.createV3Trap(".1.3.6.1.4.1.5813.1.6",Long.toString(trapTimeStamp));
			trap.send(getIp(), getPort(), getSecurityLevel(), getSecurityname(), getAuthPassPhrase(), getAuthProtocol(), getPrivPassPhrase(), getPrivprotocol());
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void sendV3StartSyncInform() {
		long trapTimeStamp = 0;
        try {
    		SnmpV3TrapBuilder inform = snmpTrapHelper.createV3Inform(".1.3.6.1.4.1.5813.1.5",Long.toString(trapTimeStamp));
			inform.sendInform(getIp(), getPort(), getTimeout(), getRetries(),getSecurityLevel(), getSecurityname(), getAuthPassPhrase(), getAuthProtocol(), getPrivPassPhrase(), getPrivprotocol());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV3EndSyncInform() {
		long trapTimeStamp = 0;
        try {
    		SnmpV3TrapBuilder inform = snmpTrapHelper.createV3Inform(".1.3.6.1.4.1.5813.1.6",Long.toString(trapTimeStamp));
			inform.sendInform(getIp(), getPort(), getTimeout(), getRetries(),getSecurityLevel(), getSecurityname(), getAuthPassPhrase(), getAuthProtocol(), getPrivPassPhrase(), getPrivprotocol());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV1AlarmTrap( Event event, boolean sync) throws UnknownHostException {
		SnmpTrapBuilder trap = snmpTrapHelper.createV1Trap(".1.3.6.1.4.1.5813.1",getSource_ip(), 6, 3, 0);
		trap = buildAlarmTrap(event, sync, trap);
        try {
			trap.send(getIp(), getPort(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV2AlarmTrap( Event event, boolean sync) throws UnknownHostException, SnmpTrapHelperException {
		long trapTimeStamp = 0;
		SnmpTrapBuilder trap = snmpTrapHelper.createV2Trap(".1.3.6.1.4.1.5813.1.3",Long.toString(trapTimeStamp));
		trap=buildAlarmTrap(event, sync, trap);
        try {
			trap.send(getIp(), getPort(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV3AlarmTrap( Event event, boolean sync) throws UnknownHostException, SnmpTrapHelperException {
		long trapTimeStamp = 0;
		SnmpTrapBuilder trap = snmpTrapHelper.createV3Trap(".1.3.6.1.4.1.5813.1.3",Long.toString(trapTimeStamp));
		trap=buildAlarmTrap(event, sync, trap);
		SnmpV3TrapBuilder v3trap = (SnmpV3TrapBuilder) trap;
		try {
			v3trap.send(getIp(), getPort(), getSecurityLevel(), getSecurityname(), getAuthPassPhrase(), getAuthProtocol(), getPrivPassPhrase(), getPrivprotocol());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV2AlarmInform( Event event, boolean sync) throws UnknownHostException, SnmpTrapHelperException {
		long trapTimeStamp = 0;
		SnmpTrapBuilder trap = snmpTrapHelper.createV2Inform(".1.3.6.1.4.1.5813.1.3",Long.toString(trapTimeStamp));
		trap=buildAlarmTrap(event, sync, trap);
		SnmpV2TrapBuilder inform = (SnmpV2TrapBuilder) trap;
        try {
			inform.sendInform(getIp(), getPort(), getTimeout(), getRetries(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV3AlarmInform( Event event, boolean sync) throws UnknownHostException, SnmpTrapHelperException {
		long trapTimeStamp = 0;
		SnmpTrapBuilder trap = snmpTrapHelper.createV3Inform(".1.3.6.1.4.1.5813.1.3",Long.toString(trapTimeStamp));
		trap=buildAlarmTrap(event, sync, trap);
		SnmpV3TrapBuilder inform = (SnmpV3TrapBuilder) trap;
		try {
			inform.sendInform(getIp(), getPort(), getTimeout(), getRetries(),getSecurityLevel(), getSecurityname(), getAuthPassPhrase(), getAuthProtocol(), getPrivPassPhrase(), getPrivprotocol());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV1EventTrap( Event event) throws UnknownHostException {
		SnmpTrapBuilder trap = snmpTrapHelper.createV1Trap(".1.3.6.1.4.1.5813.1",getSource_ip(), 6, 1, 0);
		trap = buildEventTrap(event, trap, null);
        try {
			trap.send(getIp(), getPort(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV2EventTrap( Event event) throws UnknownHostException, SnmpTrapHelperException {
		long trapTimeStamp = 0;
		SnmpTrapBuilder trap = snmpTrapHelper.createV2Trap(".1.3.6.1.4.1.5813.1.1",Long.toString(trapTimeStamp));
		trap = buildEventTrap(event, trap, null);
        try {
			trap.send(getIp(), getPort(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV3EventTrap( Event event) throws UnknownHostException, SnmpTrapHelperException {
		long trapTimeStamp = 0;
		SnmpTrapBuilder trap = snmpTrapHelper.createV3Trap(".1.3.6.1.4.1.5813.1.1",Long.toString(trapTimeStamp));
		trap = buildEventTrap(event, trap,null);
		SnmpV3TrapBuilder v3trap = (SnmpV3TrapBuilder) trap;
		try {
			v3trap.send(getIp(), getPort(), getSecurityLevel(), getSecurityname(), getAuthPassPhrase(), getAuthProtocol(), getPrivPassPhrase(), getPrivprotocol());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV2EventInform( Event event) throws UnknownHostException, SnmpTrapHelperException {
		long trapTimeStamp = 0;
		SnmpTrapBuilder trap = snmpTrapHelper.createV2Inform(".1.3.6.1.4.1.5813.1.1",Long.toString(trapTimeStamp));
		trap=buildEventTrap(event,trap,null);
		SnmpV2TrapBuilder inform = (SnmpV2TrapBuilder) trap;
        try {
			inform.sendInform(getIp(), getPort(), getTimeout(), getRetries(), getCommunity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendV3EventInform( Event event) throws UnknownHostException, SnmpTrapHelperException {
		long trapTimeStamp = 0;
		SnmpTrapBuilder trap = snmpTrapHelper.createV3Inform(".1.3.6.1.4.1.5813.1.1",Long.toString(trapTimeStamp));
		trap=buildEventTrap(event,trap,null);
		SnmpV3TrapBuilder inform = (SnmpV3TrapBuilder) trap;
		try {
			inform.sendInform(getIp(), getPort(), getTimeout(), getRetries(),getSecurityLevel(), getSecurityname(), getAuthPassPhrase(), getAuthProtocol(), getPrivPassPhrase(), getPrivprotocol());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private SnmpTrapBuilder buildAlarmTrap(Event event, boolean sync, SnmpTrapBuilder trap) {
		try {
             if (event.getAlarmData() != null ) {
            	 if (event.getAlarmData().getAlarmType() == 2) {
        			 trap = buildEventTrap(event, trap, "Cleared");
            		 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.3.1.0", "OctetString", "text", event.getAlarmData().getClearKey());
            	 } else {
        			 trap = buildEventTrap(event, trap, null);
            		 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.3.1.0", "OctetString", "text", event.getAlarmData().getReductionKey());     
            	 }
             } else {
    			 trap = buildEventTrap(event, trap, null);
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.3.1.0", "OctetString", "text", "null");            	 
             }
             if (sync)
            	 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.3.2.0", "OctetString", "text", "SYNC");
             else
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.3.2.0", "OctetString", "text", "null");            	 
		} catch (SnmpTrapHelperException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
 		return trap;
	}

	private SnmpTrapBuilder buildEventTrap(Event event, SnmpTrapBuilder trap, String severity) {
		try {
			 Integer t_dbid = Integer.valueOf(event.getDbid());
             if (t_dbid.intValue() > 0)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.1.0", "OctetString", "text", t_dbid.toString());
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.1.0", "OctetString", "text", "null");
             if (event.getDistPoller() != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.2.0", "OctetString", "text", event.getDistPoller());
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.2.0", "OctetString", "text", "null");
             if (event.getCreationTime() != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.3.0", "OctetString", "text", event.getCreationTime());
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.3.0", "OctetString", "text", "null");
             if (event.getMasterStation() != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.4.0", "OctetString", "text", event.getMasterStation());
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.4.0", "OctetString", "text", "null");
             if (event.getUei() != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.6.0", "OctetString", "text", event.getUei());
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.6.0", "OctetString", "text", "null");
             if (event.getSource() != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.7.0", "OctetString", "text", event.getSource());
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.7.0", "OctetString", "text", "null");
             String label=null;
             if (event.hasNodeid()) {
            	 	label = DbHelper.getNodeLabel(Integer.valueOf(Long.valueOf(event.getNodeid()).toString()));
            	 	snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.8.0", "OctetString", "text", Long.valueOf(event.getNodeid()).toString());
             } else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.8.0", "OctetString", "text", "null");
             if (event.getTime() != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.9.0", "OctetString", "text", event.getTime());
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.9.0", "OctetString", "text", "null");
             if (event.getHost() != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.10.0", "OctetString", "text", event.getHost());
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.10.0", "OctetString", "text", "null");
             if (event.getInterface() != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.11.0", "OctetString", "text", event.getInterface());
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.11.0", "OctetString", "text", "null");
             if (event.getSnmphost() != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.12.0", "OctetString", "text", event.getSnmphost());
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.12.0", "OctetString", "text", "null");
             if (event.getService() != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.13.0", "OctetString", "text", event.getService());
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.13.0", "OctetString", "text", "null");    
             if (event.getDescr() != null) {
                 String descrString = event.getDescr().replaceAll("&lt;.*&gt;", " ").replaceAll("\\s+", " ");
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.16.0", "OctetString", "text", descrString);
             } else
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.16.0", "OctetString", "text", "null");
             if (event.getLogmsg() != null && event.getLogmsg().getContent() != null) {
                 String logString = event.getLogmsg().getContent().replaceAll("&lt;.*&gt;", " ").replaceAll("\\s+", " ");
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.17.0", "OctetString", "text", logString);
             } else
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.17.0", "OctetString", "text", "null");
             if (severity == null && event.getSeverity() != null)
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.18.0", "OctetString", "text", event.getSeverity());
             else if (severity != null)
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.18.0", "OctetString", "text", severity);
             else
            	 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.18.0", "OctetString", "text", "null");

             if (event.getPathoutage() != null)
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.19.0", "OctetString", "text", event.getPathoutage());
             else
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.19.0", "OctetString", "text", "null");
             if (event.getOperinstruct() != null)
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.20.0", "OctetString", "text", event.getOperinstruct());
             else
                 snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.20.0", "OctetString", "text", "null");

             String retParmVal = null;
             if (event.getInterface() != null) {
                     retParmVal = event.getInterface();
                     java.net.InetAddress inet = InetAddressUtils.addr(retParmVal);
                     retParmVal = inet.getHostName();
             }
             if (retParmVal != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.21.0", "OctetString", "text", retParmVal);
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.21.0", "OctetString", "text", "null");

             if (label != null)
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.22.0", "OctetString", "text", label);
             else
                     snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.22.0", "OctetString", "text", "null");
             
		} catch (final IllegalArgumentException e) {
		    LogUtils.warnf(this, e, "Failed to look up host.");
		} catch (final SnmpTrapHelperException e) {
		    LogUtils.warnf(this, e, "An SNMP trap helpre error occurred while parsing traps.");
		} catch (final Throwable t) {
		    LogUtils.warnf(this, t, "An unknown error occurred while parsing traps.");
		}		
        return trap;
	}

	public String getSource_ip() {
		return source_ip;
	}

	public void setSource_ip(String source_ip) {
		this.source_ip = source_ip;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(int securityLevel) {
		this.securityLevel = securityLevel;
	}

	public String getSecurityname() {
		return securityname;
	}

	public void setSecurityname(String securityname) {
		this.securityname = securityname;
	}

	public String getAuthPassPhrase() {
		return authPassPhrase;
	}

	public void setAuthPassPhrase(String authPassPhrase) {
		this.authPassPhrase = authPassPhrase;
	}

	public String getAuthProtocol() {
		return authProtocol;
	}

	public void setAuthProtocol(String authProtocol) {
		this.authProtocol = authProtocol;
	}

	public String getPrivPassPhrase() {
		return privPassPhrase;
	}

	public void setPrivPassPhrase(String privPassPhrase) {
		this.privPassPhrase = privPassPhrase;
	}

	public String getPrivprotocol() {
		return privprotocol;
	}

	public void setPrivprotocol(String privprotocol) {
		this.privprotocol = privprotocol;
	}
	
}
