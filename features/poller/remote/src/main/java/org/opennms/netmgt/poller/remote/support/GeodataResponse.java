/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;


@XmlRootElement(name="Response")
@XmlAccessorType(XmlAccessType.NONE)
public class GeodataResponse {
    private InetAddress m_ip;
    private String m_countryCode;
    private String m_countryName;
    private String m_regionCode;
    private String m_regionName;
    private String m_city;
    private String m_zipCode;
    private String m_timeZone;
    private Double m_latitude;
    private Double m_longitude;
    private Long m_metroCode;

    public GeodataResponse() {}

    @XmlElement(name="IP")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getIp() {
        return m_ip;
    }

    public void setIp(final InetAddress ip) {
        m_ip = ip;
    }

    @XmlElement(name="CountryCode")
    public String getCountryCode() {
        return m_countryCode;
    }

    public void setCountryCode(final String countryCode) {
        m_countryCode = emptyToNull(countryCode);
    }

    @XmlElement(name="CountryName")
    public String getCountryName() {
        return m_countryName;
    }

    public void setCountryName(final String countryName) {
        m_countryName = emptyToNull(countryName);
    }

    @XmlElement(name="RegionCode")
    public String getRegionCode() {
        return m_regionCode;
    }

    public void setRegionCode(final String regionCode) {
        m_regionCode = emptyToNull(regionCode);
    }

    @XmlElement(name="RegionName")
    public String getRegionName() {
        return m_regionName;
    }

    public void setRegionName(final String regionName) {
        m_regionName = emptyToNull(regionName);
    }

    @XmlElement(name="City")
    public String getCity() {
        return m_city;
    }

    public void setCity(final String city) {
        m_city = emptyToNull(city);
    }

    @XmlElement(name="ZipCode")
    public String getZipCode() {
        return m_zipCode;
    }

    public void setZipCode(final String zipCode) {
        m_zipCode = emptyToNull(zipCode);
    }

    @XmlElement(name="TimeZone")
    public String getTimeZone() {
        return m_timeZone;
    }

    public void setTimeZone(final String timeZone) {
        m_timeZone = emptyToNull(timeZone);
    }

    @XmlElement(name="Latitude")
    public Double getLatitude() {
        return m_latitude;
    }

    public void setLatitude(final Double latitude) {
        m_latitude = latitude;
    }

    @XmlElement(name="Longitude")
    public Double getLongitude() {
        return m_longitude;
    }

    public void setLongitude(final Double longitude) {
        m_longitude = longitude;
    }

    @XmlElement(name="MetroCode")
    public Long getMetroCode() {
        return m_metroCode;
    }

    public void setMetroCode(final Long metroCode) {
        m_metroCode = metroCode;
    }

    private String emptyToNull(final String s) {
        if (s == null || "".equals(s.trim())) {
            return null;
        }
        return s;
    }
}
