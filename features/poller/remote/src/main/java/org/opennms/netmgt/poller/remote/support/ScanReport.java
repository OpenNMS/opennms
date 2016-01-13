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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.poller.DoubleXmlAdapter;
import org.opennms.netmgt.snmp.InetAddrXmlAdapter;


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

    @XmlAttribute(name="external-ip-address")
    @XmlJavaTypeAdapter(InetAddrXmlAdapter.class)
    private InetAddress m_externalIpAddress;

    @XmlAttribute(name="country-code")
    private String m_countryCode;

    @XmlAttribute(name="region-code")
    private String m_regionCode;

    @XmlAttribute(name="city")
    private String m_city;

    @XmlAttribute(name="zip-code")
    private String m_zipCode;

    @XmlAttribute(name="time-zone")
    private String m_timeZone;

    @XmlAttribute(name="latitude")
    @XmlJavaTypeAdapter(DoubleXmlAdapter.class)
    private Double m_latitude;

    @XmlAttribute(name="longitude")
    @XmlJavaTypeAdapter(DoubleXmlAdapter.class)
    private Double m_longitude;

    @XmlAttribute(name="locale")
    private String m_locale;

    @XmlAttribute(name="timestamp")
    private Date m_timestamp;

    @XmlElementWrapper(name="poll-results")
    @XmlElement(name="poll-result")
    private List<PollResult> m_pollResults = new ArrayList<>();

    public ScanReport() {
    }

    /**
     * Copy constructor.
     *
     * @param pkg
     */
    public ScanReport(final ScanReport pkg) {
        m_city = pkg.getCity();
        m_countryCode = pkg.getCountryCode();
        m_customerAccountNumber = pkg.getCustomerAccountNumber();
        m_customerName = pkg.getCustomerName();
        m_externalIpAddress = pkg.getExternalIpAddress();
        m_latitude = pkg.getLatitude();
        m_locale = pkg.getLocale();
        m_location = pkg.getLocation();
        m_longitude = pkg.getLongitude();
        m_monitoringSystem = pkg.getMonitoringSystem();
        m_pollResults = pkg.getPollResults();
        m_referenceId = pkg.getReferenceId();
        m_regionCode = pkg.getRegionCode();
        m_timestamp = pkg.getTimestamp();
        m_timeZone = pkg.getTimeZone();
        m_zipCode = pkg.getZipCode();
    }

    public String getCustomerAccountNumber() {
        return m_customerAccountNumber;
    }

    public void setCustomerAccountNumber(final String customerAccountNumber) {
        m_customerAccountNumber = customerAccountNumber;
    }

    public String getReferenceId() {
        return m_referenceId;
    }

    public void setReferenceId(final String referenceId) {
        m_referenceId = referenceId;
    }

    public String getCustomerName() {
        return m_customerName;
    }

    public void setCustomerName(final String customerName) {
        m_customerName = customerName;
    }

    public String getLocation() {
        return m_location;
    }

    public void setLocation(final String location) {
        m_location = location;
    }

    public String getMonitoringSystem() {
        return m_monitoringSystem;
    }

    public void setMonitoringSystem(final String monitoringSystem) {
        m_monitoringSystem = monitoringSystem;
    }

    public InetAddress getExternalIpAddress() {
        return m_externalIpAddress;
    }

    public void setExternalIpAddress(final InetAddress inetAddress) {
        m_externalIpAddress = inetAddress;
    }

    public String getCountryCode() {
        return m_countryCode;
    }

    public void setCountryCode(final String countryCode) {
        m_countryCode = countryCode;
    }

    public String getRegionCode() {
        return m_regionCode;
    }

    public void setRegionCode(final String regionCode) {
        m_regionCode = regionCode;
    }

    public String getCity() {
        return m_city;
    }

    public void setCity(final String city) {
        m_city = city;
    }

    public String getZipCode() {
        return m_zipCode;
    }

    public void setZipCode(final String zipCode) {
        m_zipCode = zipCode;
    }

    public String getTimeZone() {
        return m_timeZone;
    }

    public void setTimeZone(String m_timeZone) {
        this.m_timeZone = m_timeZone;
    }

    public Double getLatitude() {
        return m_latitude;
    }

    public void setLatitude(final Double latitude) {
        m_latitude = latitude;
    }

    public Double getLongitude() {
        return m_longitude;
    }

    public void setLongitude(final Double longitude) {
        m_longitude = longitude;
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

    public List<PollResult> getPollResults() {
        return m_pollResults;
    }

    public void setPollResults(final List<PollResult> pollResults) {
        this.m_pollResults = pollResults;
    }

    public boolean addPollResult(final PollResult pollResult) {
        return m_pollResults.add(pollResult);
    }

    public boolean isUp() {
        if (m_pollResults != null) {
            for (final PollResult result : m_pollResults) {
                if (!result.isUp()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScanReport [customerAccountNumber=" + m_customerAccountNumber + ", referenceId=" + m_referenceId + ", customerName=" + m_customerName + ", location=" + m_location
                + ", monitoringSystem=" + m_monitoringSystem + ", externalIpAddress=" + m_externalIpAddress + ", countryCode=" + m_countryCode + ", regionCode=" + m_regionCode + ", city="
                + m_city + ", zipCode=" + m_zipCode + ", timeZone=" + m_timeZone + ", latitude=" + m_latitude + ", longitude=" + m_longitude + ", locale=" + m_locale + ", timestamp="
                + m_timestamp + ", pollResults=" + m_pollResults + "]";
    }
}
