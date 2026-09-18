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
package org.opennms.netmgt.config.wsman.eventlog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** Root of {@code wsman-eventlog-configuration.xml}. */
@XmlRootElement(name = "wsman-eventlog-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class WsmanEventlogConfiguration {

    /** Concurrent node polls across every package. */
    @XmlAttribute(name = "threads")
    private Integer threads;

    /** WS-Man retries per read, on top of the first attempt. */
    @XmlAttribute(name = "retries")
    private Integer retries;

    /** How long the node list behind a package filter is reused before it is recomputed. */
    @XmlAttribute(name = "target-refresh-interval")
    private String targetRefreshInterval;

    @XmlElement(name = "package")
    private List<Package> packages = new ArrayList<>();

    public int getThreads() {
        return threads == null ? 4 : threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public int getRetries() {
        return retries == null ? 1 : retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public String getTargetRefreshInterval() {
        return targetRefreshInterval == null ? "5m" : targetRefreshInterval;
    }

    public void setTargetRefreshInterval(String targetRefreshInterval) {
        this.targetRefreshInterval = targetRefreshInterval;
    }

    public List<Package> getPackages() {
        return packages;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages == null ? new ArrayList<>() : packages;
    }

    @Override
    public int hashCode() {
        return Objects.hash(threads, retries, targetRefreshInterval, packages);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WsmanEventlogConfiguration)) {
            return false;
        }
        final WsmanEventlogConfiguration other = (WsmanEventlogConfiguration) obj;
        return Objects.equals(threads, other.threads)
                && Objects.equals(retries, other.retries)
                && Objects.equals(targetRefreshInterval, other.targetRefreshInterval)
                && Objects.equals(packages, other.packages);
    }
}
