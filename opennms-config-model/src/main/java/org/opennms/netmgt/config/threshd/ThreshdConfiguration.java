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
    private Integer threads;

    /**
     * Package encapsulating addresses eligible for
     *  thresholding.
     */
    @XmlElement(name = "package", required = true)
    private List<Package> packages = new ArrayList<>();

    /**
     * Service thresholders
     */
    @XmlElement(name = "thresholder")
    private List<Thresholder> thresholder = new ArrayList<>();

    public ThreshdConfiguration() { }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(final Integer threads) {
        this.threads = ConfigUtils.assertNotNull(threads, "threads");
    }

    public List<Package> getPackages() {
        return packages;
    }

    public void setPackages(final List<Package> packages) {
        if (packages == this.packages) return;
        this.packages.clear();
        if (packages != null) this.packages.addAll(packages);
    }

    public Optional<Package> getPackage(String packageName) {
        return getPackages().stream()
                .filter(p -> Objects.equals(packageName, p.getName()))
                .findFirst();
    }

    public void addPackage(final Package p) {
        packages.add(p);
    }

    public boolean removePackage(final Package p) {
        return packages.remove(p);
    }

    public List<Thresholder> getThresholder() {
        return thresholder;
    }

    public void setThresholder(final List<Thresholder> thresholder) {
        if (thresholder == this.thresholder) return;
        this.thresholder.clear();
        if (thresholder != null) this.thresholder.addAll(thresholder);
    }

    public void addThresholder(final Thresholder thresholder) {
        this.thresholder.add(thresholder);
    }

    public boolean removeThresholder(final Thresholder thresholder) {
        return this.thresholder.remove(thresholder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(threads,
                packages,
                thresholder);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ThreshdConfiguration) {
            final ThreshdConfiguration that = (ThreshdConfiguration)obj;
            return Objects.equals(this.threads, that.threads)
                    && Objects.equals(this.packages, that.packages)
                    && Objects.equals(this.thresholder, that.thresholder);
        }
        return false;
    }

}
