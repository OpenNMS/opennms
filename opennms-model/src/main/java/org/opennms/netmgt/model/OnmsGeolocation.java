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
    @Column(name="address1")
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
    @Column(name="address2")
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
    @Column(name="city")
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
    @Column(name="state")
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
    @Column(name="zip")
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
    @Column(name="country")
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