package org.opennms.netmgt.model;

import java.beans.PropertyDescriptor;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

@Embeddable
public class OnmsGeolocation implements Serializable {
    private static final long serialVersionUID = -3346555393433178515L;

    public OnmsGeolocation() {}

    private String m_address1;
    private String m_address2;
    private String m_city;
    private String m_state;
    private String m_zip;
    private String m_country;
    private Float m_longitude;
    private Float m_latitude;

    /**
     *--# address1         : Address of geographical location of asset, line 1.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="address1", length=256)
    public String getAddress1() {
        return m_address1;
    }

    /**
     * <p>setAddress1</p>
     *
     * @param address1 a {@link java.lang.String} object.
     */
    public void setAddress1(String address1) {
        m_address1 = address1;
    }

    /**
     *--# address2         : Address of geographical location of asset, line 2.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="address2", length=256)
    public String getAddress2() {
        return m_address2;
    }

    /**
     * <p>setAddress2</p>
     *
     * @param address2 a {@link java.lang.String} object.
     */
    public void setAddress2(String address2) {
        m_address2 = address2;
    }

    /**
     *--# city             : The city where this asset resides.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="city", length=64)
    public String getCity() {
        return m_city;
    }

    /**
     * <p>setCity</p>
     *
     * @param city a {@link java.lang.String} object.
     */
    public void setCity(String city) {
        m_city = city;
    }

    /**
     *--# state            : The state where this asset resides.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="state", length=64)
    public String getState() {
        return m_state;
    }

    /**
     * <p>setState</p>
     *
     * @param state a {@link java.lang.String} object.
     */
    public void setState(String state) {
        m_state = state;
    }

    /**
     *--# zip              : The zip code where this asset resides.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="zip", length=64)
    public String getZip() {
        return m_zip;
    }

    /**
     * <p>setZip</p>
     *
     * @param zip a {@link java.lang.String} object.
     */
    public void setZip(String zip) {
        m_zip = zip;
    }

    /**
     *--# country              : The country where this asset resides.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="country", length=64)
    public String getCountry() {
        return m_country;
    }

    /**
     * <p>setCountry</p>
     *
     * @param country a {@link java.lang.String} object.
     */
    public void setCountry(String country) {
        m_country = country;
    }

    /**
     * The longitude coordinate of this node.
     * @return
     */
    @Column(name="longitude")
    public Float getLongitude() {
        return m_longitude;
    }

    public void setLongitude(final Float longitude) {
        m_longitude = longitude;
    }

    /**
     * The latitude coordinate of this node.
     * @return
     */
    @Column(name="latitude")
    public Float getLatitude() {
        return m_latitude;
    }

    public void setLatitude(final Float latitude) {
        m_latitude = latitude;
    }

    @Override
    public String toString() {
        return "OnmsGeolocation[" + this.asAddressString() + "]";
    }

    public String asAddressString() {
        final StringBuffer sb = new StringBuffer();

        if (hasText(this.getAddress1())) {
            sb.append(this.getAddress1());
            if (hasText(this.getAddress2())) {
                sb.append(" ").append(this.getAddress2());
            }
        }

        if (hasText(this.getCity())) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(this.getCity());
        }
        if (hasText(this.getState())) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(this.getState());
        }
        if (hasText(this.getZip())) {
            if (hasText(this.getState())) {
                sb.append(" ");
            } else if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(this.getZip());
        }
        if (hasText(this.getCountry())) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(this.getCountry());
        }

        if (sb.length() == 0) {
            return null;
        }

        return sb.toString();
    }

    private boolean hasText(final String string) {
        return !(string == null || string.isEmpty() || string.trim().isEmpty());
    }

    public void mergeGeolocation(final OnmsGeolocation from) {
        if (from == null) {
            return;
        }

        final BeanWrapper toBean = PropertyAccessorFactory.forBeanPropertyAccess(this);
        final BeanWrapper fromBean = PropertyAccessorFactory.forBeanPropertyAccess(from);
        final PropertyDescriptor[] pds = fromBean.getPropertyDescriptors();

        for (final PropertyDescriptor pd : pds) {
            final String propertyName = pd.getName();

            if (propertyName.equals("class")) {
                continue;
            }

            final Object propertyValue = fromBean.getPropertyValue(propertyName);
            if (propertyValue != null) {
                toBean.setPropertyValue(propertyName, propertyValue);
            }
        }
    }
}