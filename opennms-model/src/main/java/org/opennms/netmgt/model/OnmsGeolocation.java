/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

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
    private Double m_longitude;
    private Double m_latitude;

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
    public Double getLongitude() {
        return m_longitude;
    }

    public void setLongitude(final Double longitude) {
        m_longitude = longitude;
    }

    /**
     * The latitude coordinate of this node.
     * @return
     */
    @Column(name="latitude")
    public Double getLatitude() {
        return m_latitude;
    }

    public void setLatitude(final Double latitude) {
        m_latitude = latitude;
    }

    @Override
    public String toString() {
        return "OnmsGeolocation[" + this.asAddressString() + "]";
    }

    public String asAddressString() {
        final StringBuilder sb = new StringBuilder();

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

    private boolean isAddressEqual(OnmsGeolocation other) {
        return Objects.equals(asAddressString(), other.asAddressString());
    }

    public void mergeGeolocation(final OnmsGeolocation mergeWith) {
        if (mergeWith == null) {
            return;
        }

        // The address has changed, we must reset long/lat to have
        // the GeolocationProvisioningAdapter resolve it.
        boolean addressEqual = isAddressEqual(mergeWith);
        if (!addressEqual) {
            setLatitude(null);
            setLongitude(null);
        }

        // Update address
        setCity(mergeWith.getCity());
        setAddress1(mergeWith.getAddress1());
        setAddress2(mergeWith.getAddress2());
        setZip(mergeWith.getZip());
        setCountry(mergeWith.getCountry());
        setState(mergeWith.getState());

        // If there is long/lat defined, we use it no matter what.
        // This prevents resetting already resolved long/lat information
        // by the GeolocationProvisioningAdapter
        if (mergeWith.getLongitude() != null) {
            setLongitude(mergeWith.getLongitude());
        }
        if (mergeWith.getLatitude() != null) {
            setLatitude(mergeWith.getLatitude());
        }
    }
}