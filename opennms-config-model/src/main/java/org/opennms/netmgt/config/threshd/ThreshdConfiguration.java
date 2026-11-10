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
package org.opennms.netmgt.config.threshd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public Optional<Package> getPackage(String packageName) {
        return getPackages().stream()
                .filter(p -> Objects.equals(packageName, p.getName()))
                .findFirst();
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
