/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.poller.PollStatus;


/**
 * @author Seth
 */
@XmlRootElement(name="scan-report")
@XmlAccessorType(XmlAccessType.NONE)
public class ScanReport {

	@XmlAttribute(name="customer-account-number")
	private String m_customerAccountNumber;

	@XmlAttribute(name="reference-id")
	private String m_referenceId;

	@XmlAttribute(name="customer-name")
	private String m_customerName;

	@XmlAttribute(name="location")
	private String m_location;

	@XmlAttribute(name="monitoring-system")
	private String m_monitoringSystem;

	@XmlAttribute(name="time-zone")
	private String m_timeZone;

	@XmlAttribute(name="locale")
	private String m_locale;

	@XmlAttribute(name="timestamp")
	private Date m_timestamp;

	@XmlElementWrapper(name="poll-statuses")
	@XmlElement(name="poll-status")
	private List<PollStatus> m_pollStatuses = new ArrayList<PollStatus>();

	public ScanReport() {
	}

	/**
	 * Copy constructor.
	 * 
	 * @param pkg
	 */
	public ScanReport(ScanReport pkg) {
		m_customerAccountNumber = pkg.getCustomerAccountNumber();
		m_customerName = pkg.getCustomerName();
		m_locale = pkg.getLocale();
		m_location = pkg.getLocation();
		m_monitoringSystem = pkg.getMonitoringSystem();
		m_pollStatuses = pkg.getPollStatuses();
		m_referenceId = pkg.getReferenceId();
		m_timestamp = pkg.getTimestamp();
		m_timeZone = pkg.getTimeZone();
	}

	public String getCustomerAccountNumber() {
		return m_customerAccountNumber;
	}

	public void setCustomerAccountNumber(String m_customerAccountNumber) {
		this.m_customerAccountNumber = m_customerAccountNumber;
	}

	public String getReferenceId() {
		return m_referenceId;
	}

	public void setReferenceId(String m_referenceId) {
		this.m_referenceId = m_referenceId;
	}

	public String getCustomerName() {
		return m_customerName;
	}

	public void setCustomerName(String m_customerName) {
		this.m_customerName = m_customerName;
	}

	public String getLocation() {
		return m_location;
	}

	public void setLocation(String m_location) {
		this.m_location = m_location;
	}

	public String getMonitoringSystem() {
		return m_monitoringSystem;
	}

	public void setMonitoringSystem(String m_monitoringSystem) {
		this.m_monitoringSystem = m_monitoringSystem;
	}

	public String getTimeZone() {
		return m_timeZone;
	}

	public void setTimeZone(String m_timeZone) {
		this.m_timeZone = m_timeZone;
	}

	public String getLocale() {
		return m_locale;
	}

	public void setLocale(String m_locale) {
		this.m_locale = m_locale;
	}

	public Date getTimestamp() {
		return m_timestamp;
	}

	public void setTimestamp(Date m_timestamp) {
		this.m_timestamp = m_timestamp;
	}

	public List<PollStatus> getPollStatuses() {
		return m_pollStatuses;
	}

	public void setPollStatuses(List<PollStatus> m_pollStatuses) {
		this.m_pollStatuses = m_pollStatuses;
	}

	public boolean addPollStatus(PollStatus pollStatus) {
		return m_pollStatuses.add(pollStatus);
	}
}
