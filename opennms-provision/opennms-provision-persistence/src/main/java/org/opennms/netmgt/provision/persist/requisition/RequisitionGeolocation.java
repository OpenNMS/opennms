package org.opennms.netmgt.provision.persist.requisition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>RequisitionGeolocation class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="")
@XmlRootElement(name="geolocation")
public class RequisitionGeolocation {

    @XmlAttribute(name="lat", required=true)
    protected Double m_lat;

   
    @XmlAttribute(name="lon", required=true)
    protected Double m_lon;

   
    /**
     * <p>Constructor for RequisitionGeolocation.</p>
     */
    public RequisitionGeolocation() {
    }

    /**
     * <p>Constructor for RequisitionGeolocation.</p>
     *
     * @param lat a {@link java.lang.Double} object.
     * @param lon a {@link java.lang.Double} object. 
     */
    public RequisitionGeolocation(Double lat, Double lon) {
        m_lat = lat;
        m_lon = lon;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    public Double getLat() {
        return m_lat;
    }

    /**
     * <p>setName</p>
     *
     * @param value a {@link java.lang.Double} object.
     * @param value a {@link java.lang.Double} object.
     */
    public void setLat(Double lat) {
        m_lat = lat;
    }
   
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    public Double getLon() {
        return m_lon;
    }

    /**
     * <p>setName</p>
     *
     * @param value a {@link java.lang.Double} object.
     * @param value a {@link java.lang.Double} object.
     */
    public void setLon(Double lon) {
        m_lon = lon;
    }
}
