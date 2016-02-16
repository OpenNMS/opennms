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
