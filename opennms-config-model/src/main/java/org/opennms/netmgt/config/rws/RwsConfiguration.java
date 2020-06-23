/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.rws;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Top-level element for the rws-configuration.xml configuration file.
 */
@XmlRootElement(name = "rws-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("rws-configuration.xsd")
public class RwsConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Base Url(s) for Rancid Server.
     */
    @XmlElement(name = "base-url", required = true)
    private BaseUrl m_baseUrl;

    /**
     * Stand By Url(s) for Rancid Servers.
     */
    @XmlElement(name = "standby-url")
    private List<StandbyUrl> m_standbyUrls = new ArrayList<>();

    public RwsConfiguration() {
    }

    public BaseUrl getBaseUrl() {
        return m_baseUrl;
    }

    public void setBaseUrl(final BaseUrl baseUrl) {
        m_baseUrl = ConfigUtils.assertNotNull(baseUrl, "base-url");
    }

    public List<StandbyUrl> getStandbyUrls() {
        return m_standbyUrls;
    }

    public void setStandbyUrls(final List<StandbyUrl> standbyUrls) {
        if (standbyUrls == m_standbyUrls) return;
        m_standbyUrls.clear();
        if (standbyUrls != null) m_standbyUrls.addAll(standbyUrls);
    }

    public void addStandbyUrl(final StandbyUrl standbyUrl) {
        m_standbyUrls.add(standbyUrl);
    }

    public boolean removeStandbyUrl(final StandbyUrl standbyUrl) {
        return m_standbyUrls.remove(standbyUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_baseUrl, m_standbyUrls);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof RwsConfiguration)) {
            return false;
        }
        RwsConfiguration castOther = (RwsConfiguration) other;
        return Objects.equals(m_baseUrl, castOther.m_baseUrl) && Objects.equals(m_standbyUrls, castOther.m_standbyUrls);
    }

}
