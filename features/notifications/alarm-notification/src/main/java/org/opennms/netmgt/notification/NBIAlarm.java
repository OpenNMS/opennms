/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/ This class represent the
 * alarm object to be forwarded to the script
 */

package org.opennms.netmgt.notification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "m_uiclear", "m_alarmid", "m_eventuei",
		"m_dpname", "m_ipaddr", "m_serviceid", "m_reductionkey", "m_alarmtype",
		"m_counter", "m_severity", "m_firsteventtime", "m_lasteventtime",
		"m_description", "m_logmsg", "m_operinstruct", "m_tticketid",
		"m_tticketstate", "m_suppresseduntil", "m_suppresseduser",
		"m_suppressedtime", "m_alarmackuser", "m_alarmacktime",
		"m_applicationdn", "m_ossprimarykey", "m_x733Alarmtype",
		"m_x733Probablecause", "m_clearkey", "m_ifindex", "m_eventparms",
		"m_ifname" })
@XmlRootElement(name = "alarm")
public class NBIAlarm {

	@XmlElement(name = "uiclear")
	protected String m_uiclear;

	@XmlElement(name = "alarmid")
	protected String m_alarmid;

	@XmlElement(name = "eventuei")
	protected String m_eventuei;

	@XmlElement(name = "dpname")
	protected String m_dpname;

	@XmlElement(name = "ipaddr")
	protected String m_ipaddr;

	@XmlElement(name = "serviceid")
	protected String m_serviceid;

	@XmlElement(name = "reductionkey")
	protected String m_reductionkey;

	@XmlElement(name = "alarmtype")
	protected String m_alarmtype;

	@XmlElement(name = "counter")
	protected String m_counter;

	@XmlElement(name = "severity")
	protected String m_severity;

	@XmlElement(name = "firsteventtime")
	protected String m_firsteventtime;

	@XmlElement(name = "lasteventtime")
	protected String m_lasteventtime;

	@XmlElement(name = "description")
	protected String m_description;

	@XmlElement(name = "logmsg")
	protected String m_logmsg;

	@XmlElement(name = "operinstruct")
	protected String m_operinstruct;

	@XmlElement(name = "tticketid")
	protected String m_tticketid;

	@XmlElement(name = "tticketstate")
	protected String m_tticketstate;

	@XmlElement(name = "suppresseduntil")
	protected String m_suppresseduntil;

	@XmlElement(name = "suppresseduser")
	protected String m_suppresseduser;

	@XmlElement(name = "suppressedtime")
	protected String m_suppressedtime;

	@XmlElement(name = "alarmackuser")
	protected String m_alarmackuser;

	@XmlElement(name = "alarmacktime")
	protected String m_alarmacktime;

	@XmlElement(name = "applicationdn")
	protected String m_applicationdn;

	@XmlElement(name = "ossprimarykey")
	protected String m_ossprimarykey;

	@XmlElement(name = "x733alarmtype")
	protected String m_x733Alarmtype;

	@XmlElement(name = "x733probablecause")
	protected String m_x733Probablecause;

	@XmlElement(name = "clearkey")
	protected String m_clearkey;

	@XmlElement(name = "ifindex")
	protected String m_ifindex;

	@XmlElement(name = "eventparms")
	protected String m_eventparms;

	@XmlElement(name = "ifname")
	protected String m_ifname;

	@XmlTransient
	protected String m_alarmXML;

	@XmlTransient
	protected String m_scriptName;

	@XmlTransient
	protected boolean m_isErrorHandlingEnabled;

	@XmlTransient
	protected Integer m_numberOfRetries;

	@XmlTransient
	protected Integer m_retryInterval;
	
	@XmlTransient
	protected String m_timeoutInSeconds;

	/**
	 * Gets the value of the uiclear property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getUiclear() {
		return m_uiclear;
	}

	/**
	 * Sets the value of the uiclear property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setUiclear(String uiclear) {
		this.m_uiclear = uiclear;
	}

	/**
	 * Gets the value of the alarmid property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getAlarmid() {
		return m_alarmid;
	}

	/**
	 * Sets the value of the alarmid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setAlarmid(String value) {
		this.m_alarmid = value;
	}

	/**
	 * Gets the value of the eventuei property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getEventuei() {
		return m_eventuei;
	}

	/**
	 * Sets the value of the eventuei property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setEventuei(String value) {
		this.m_eventuei = value;
	}

	/**
	 * Gets the value of the dpname property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getDpname() {
		return m_dpname;
	}

	/**
	 * Sets the value of the dpname property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setDpname(String value) {
		this.m_dpname = value;
	}

	/**
	 * Gets the value of the ipaddr property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getIpaddr() {
		return m_ipaddr;
	}

	/**
	 * Sets the value of the ipaddr property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setIpaddr(String value) {
		this.m_ipaddr = value;
	}

	/**
	 * Gets the value of the serviceid property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getServiceid() {
		return m_serviceid;
	}

	/**
	 * Sets the value of the serviceid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setServiceid(String value) {
		this.m_serviceid = value;
	}

	/**
	 * Gets the value of the reductionkey property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getReductionkey() {
		return m_reductionkey;
	}

	/**
	 * Sets the value of the reductionkey property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setReductionkey(String value) {
		this.m_reductionkey = value;
	}

	/**
	 * Gets the value of the alarmtype property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getAlarmtype() {
		return m_alarmtype;
	}

	/**
	 * Sets the value of the alarmtype property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setAlarmtype(String value) {
		this.m_alarmtype = value;
	}

	/**
	 * Gets the value of the counter property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getCounter() {
		return m_counter;
	}

	/**
	 * Sets the value of the counter property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setCounter(String value) {
		this.m_counter = value;
	}

	/**
	 * Gets the value of the severity property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getSeverity() {
		return m_severity;
	}

	/**
	 * Sets the value of the severity property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setSeverity(String value) {
		this.m_severity = value;
	}

	/**
	 * Gets the value of the firsteventtime property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getFirsteventtime() {
		return m_firsteventtime;
	}

	/**
	 * Sets the value of the firsteventtime property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setFirsteventtime(String value) {
		this.m_firsteventtime = value;
	}

	/**
	 * Gets the value of the lasteventtime property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getLasteventtime() {
		return m_lasteventtime;
	}

	/**
	 * Sets the value of the lasteventtime property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setLasteventtime(String value) {
		this.m_lasteventtime = value;
	}

	/**
	 * Gets the value of the description property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * Sets the value of the description property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setDescription(String value) {
		this.m_description = value;
	}

	/**
	 * Gets the value of the logmsg property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getLogmsg() {
		return m_logmsg;
	}

	/**
	 * Sets the value of the logmsg property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setLogmsg(String value) {
		this.m_logmsg = value;
	}

	/**
	 * Gets the value of the operinstruct property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getOperinstruct() {
		return m_operinstruct;
	}

	/**
	 * Sets the value of the operinstruct property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setOperinstruct(String value) {
		this.m_operinstruct = value;
	}

	/**
	 * Gets the value of the tticketid property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getTticketid() {
		return m_tticketid;
	}

	/**
	 * Sets the value of the tticketid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setTticketid(String value) {
		this.m_tticketid = value;
	}

	/**
	 * Gets the value of the tticketstate property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getTticketstate() {
		return m_tticketstate;
	}

	/**
	 * Sets the value of the tticketstate property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setTticketstate(String value) {
		this.m_tticketstate = value;
	}

	/**
	 * Gets the value of the suppresseduntil property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getSuppresseduntil() {
		return m_suppresseduntil;
	}

	/**
	 * Sets the value of the suppresseduntil property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setSuppresseduntil(String value) {
		this.m_suppresseduntil = value;
	}

	/**
	 * Gets the value of the suppresseduser property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getSuppresseduser() {
		return m_suppresseduser;
	}

	/**
	 * Sets the value of the suppresseduser property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setSuppresseduser(String value) {
		this.m_suppresseduser = value;
	}

	/**
	 * Gets the value of the suppressedtime property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getSuppressedtime() {
		return m_suppressedtime;
	}

	/**
	 * Sets the value of the suppressedtime property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setSuppressedtime(String value) {
		this.m_suppressedtime = value;
	}

	/**
	 * Gets the value of the alarmackuser property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getAlarmackuser() {
		return m_alarmackuser;
	}

	/**
	 * Sets the value of the alarmackuser property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setAlarmackuser(String value) {
		this.m_alarmackuser = value;
	}

	/**
	 * Gets the value of the alarmacktime property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getAlarmacktime() {
		return m_alarmacktime;
	}

	/**
	 * Sets the value of the alarmacktime property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setAlarmacktime(String value) {
		this.m_alarmacktime = value;
	}

	/**
	 * Gets the value of the applicationdn property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getApplicationdn() {
		return m_applicationdn;
	}

	/**
	 * Sets the value of the applicationdn property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setApplicationdn(String value) {
		this.m_applicationdn = value;
	}

	/**
	 * Gets the value of the ossprimarykey property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getOssprimarykey() {
		return m_ossprimarykey;
	}

	/**
	 * Sets the value of the ossprimarykey property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setOssprimarykey(String value) {
		this.m_ossprimarykey = value;
	}

	/**
	 * Gets the value of the x733Alarmtype property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getX733Alarmtype() {
		return m_alarmtype;
	}

	/**
	 * Sets the value of the x733Alarmtype property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setX733Alarmtype(String value) {
		this.m_alarmtype = value;
	}

	/**
	 * Gets the value of the x733Probablecause property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getX733Probablecause() {
		return m_x733Probablecause;
	}

	/**
	 * Sets the value of the x733Probablecause property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setX733Probablecause(String value) {
		this.m_x733Probablecause = value;
	}

	/**
	 * Gets the value of the clearkey property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getClearkey() {
		return m_clearkey;
	}

	/**
	 * Sets the value of the clearkey property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setClearkey(String value) {
		this.m_clearkey = value;
	}

	/**
	 * Gets the value of the ifindex property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getIfindex() {
		return m_ifindex;
	}

	/**
	 * Sets the value of the ifindex property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setIfindex(String value) {
		this.m_ifindex = value;
	}

	/**
	 * Gets the value of the eventparms property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getEventparms() {
		return m_eventparms;
	}

	/**
	 * Sets the value of the eventparms property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setEventparms(String value) {
		this.m_eventparms = value;
	}

	/**
	 * @return ifname
	 */
	public String getIfname() {
		return m_ifname;
	}

	/**
	 * @param ifname
	 */
	public void setIfname(String ifname) {
		this.m_ifname = ifname;
	}

	/**
	 * @return String
	 */
	public String getAlarmXML() {
		return m_alarmXML;
	}

	/**
	 * @param alarmXML
	 */
	public void setAlarmXML(String alarmXML) {
		this.m_alarmXML = alarmXML;
	}

	/**
	 * @return
	 */
	public String getScriptName() {
		return m_scriptName;
	}

	/**
	 * @param scriptName
	 */
	public void setScriptName(String scriptName) {
		this.m_scriptName = scriptName;
	}

	/**
	 * @return
	 */
	public boolean isErrorHandlingEnabled() {
		return m_isErrorHandlingEnabled;
	}

	/**
	 * @param isErrorHandlingEnabled
	 */
	public void setErrorHandlingEnabled(boolean isErrorHandlingEnabled) {
		this.m_isErrorHandlingEnabled = isErrorHandlingEnabled;
	}

	/**
	 * @return
	 */
	public Integer getNumberOfRetries() {
		return m_numberOfRetries;
	}

	/**
	 * @param numberOfRetries
	 */
	public void setNumberOfRetries(Integer numberOfRetries) {
		this.m_numberOfRetries = numberOfRetries;
	}

	/**
	 * @return
	 */
	public Integer getRetryInterval() {
		return m_retryInterval;
	}

	/**
	 * @param retryInterval
	 */
	public void setRetryInterval(Integer retryInterval) {
		this.m_retryInterval = retryInterval;
	}

	
	
	public String getTimeoutInSeconds() {
		return m_timeoutInSeconds;
	}

	public void setTimeoutInSeconds(String timeoutInSeconds) {
		this.m_timeoutInSeconds = timeoutInSeconds;
	}

	@Override
	public String toString() {

		String nbiAlarm = "uiclear:" + this.m_uiclear + " ,alarmid:"
				+ this.m_alarmid + " ,eventuei:" + this.m_eventuei
				+ " ,dpname:" + this.m_dpname + " ,ipaddr:" + this.m_ipaddr
				+ " ,serviceid:" + this.m_serviceid + " ,reductionkey:"
				+ this.m_reductionkey + " ,alarmtype:" + this.m_alarmtype
				+ " ,counter:" + this.m_counter + " ,severity:"
				+ this.m_severity + " ,firsteventime:" + this.m_firsteventtime
				+ " ,lasteventtime:" + this.m_lasteventtime + " ,description:"
				+ this.m_description + " ,logmsg:" + this.m_logmsg
				+ " ,operinstruct:" + this.m_operinstruct + " ,tticketid:"
				+ this.m_tticketid + " ,tticketstate:" + this.m_tticketstate
				+ " ,suppresseduntil:" + this.m_suppresseduntil
				+ " ,suppresseduser:" + this.m_suppresseduser
				+ " ,suppressedtime:" + this.m_suppressedtime
				+ " ,alarmackuser:" + this.m_alarmackuser + " ,alarmacktime:"
				+ this.m_alarmacktime + " ,applicationdn:"
				+ this.m_applicationdn + " ,ossprimarykey:"
				+ this.m_ossprimarykey + " ,x733Alarmtype:" + this.m_alarmtype
				+ " ,x733Probablecause:" + this.m_x733Probablecause
				+ " ,clearkey:" + this.m_clearkey + " ,ifindex:"
				+ this.m_ifindex + " ,eventparms:" + this.m_eventparms
				+ " ,ifName:" + this.m_ifname;
		return nbiAlarm;
	}

}
