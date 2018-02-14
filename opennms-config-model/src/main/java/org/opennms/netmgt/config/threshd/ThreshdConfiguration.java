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

package org.opennms.netmgt.config.threshd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Top-level element for the threshd-configuration.xml
 *  configuration file.
 */
@XmlRootElement(name = "threshd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("thresholding.xsd")
public class ThreshdConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Maximum number of threads used for
     *  thresholding.
     */
    @XmlAttribute(name = "threads", required = true)
    private Integer m_threads;

    /**
     * Package encapsulating addresses eligible for
     *  thresholding.
     */
    @XmlElement(name = "package", required = true)
    private List<Package> m_packages = new ArrayList<>();

    /**
     * Service thresholders
     */
    @XmlElement(name = "thresholder")
    private List<Thresholder> m_thresholders = new ArrayList<>();

    public ThreshdConfiguration() { }

    public Integer getThreads() {
        return m_threads;
    }

    public void setThreads(final Integer threads) {
        m_threads = ConfigUtils.assertNotNull(threads, "threads");
    }

    public List<Package> getPackages() {
        return m_packages;
    }

    public void setPackages(final List<Package> packages) {
        if (packages == m_packages) return;
        m_packages.clear();
        if (packages != null) m_packages.addAll(packages);
    }

    public void addPackage(final Package p) {
        m_packages.add(p);
    }

    public boolean removePackage(final Package p) {
        return m_packages.remove(p);
    }

    public List<Thresholder> getThresholders() {
        return m_thresholders;
    }

    public void setThresholders(final List<Thresholder> thresholders) {
        if (thresholders == m_thresholders) return;
        m_thresholders.clear();
        if (thresholders != null) m_thresholders.addAll(thresholders);
    }

    public void addThresholder(final Thresholder thresholder) {
        m_thresholders.add(thresholder);
    }

    public boolean removeThresholder(final Thresholder thresholder) {
        return m_thresholders.remove(thresholder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_threads, 
                            m_packages, 
                            m_thresholders);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ThreshdConfiguration) {
            final ThreshdConfiguration that = (ThreshdConfiguration)obj;
            return Objects.equals(this.m_threads, that.m_threads)
                    && Objects.equals(this.m_packages, that.m_packages)
                    && Objects.equals(this.m_thresholders, that.m_thresholders);
        }
        return false;
    }

}
