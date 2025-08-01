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
package org.opennms.netmgt.config.poller;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.pagesequence.PageSequence;

/**
 * Top-level element for the poller-configuration.xml
 *  configuration file.
 */

@XmlRootElement(name="poller-configuration")
@ValidateUsing("poller-configuration.xsd")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({PageSequence.class})
public class PollerConfiguration implements Serializable {
    private static final long serialVersionUID = 3402898044699865749L;

    /**
     * The maximum number of threads used for polling.
     */
    @XmlAttribute(name="threads")
    private Integer m_threads = 30;

    /**
     * SQL query for getting the next outage ID.
     */
    @XmlAttribute(name="nextOutageId")
    private String m_nextOutageId = "SELECT nextval('outageNxtId')";

    /**
     * Enable/disable serviceUnresponsive behavior
     */
    @XmlAttribute(name="serviceUnresponsiveEnabled")
    private String m_serviceUnresponsiveEnabled = "false";

    /**
     * Flag which indicates if the optional path outage feature is enabled
     */
    @XmlAttribute(name="pathOutageEnabled")
    private String m_pathOutageEnabled = "false";

    @XmlAttribute(name="defaultCriticalPathIp")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress m_defaultCriticalPathIp;

    @XmlAttribute(name="defaultCriticalPathTimeout")
    private Integer m_defaultCriticalPathTimeout;

    @XmlAttribute(name="defaultCriticalPathRetries")
    private Integer m_defaultCriticalPathRetries;

    /**
     * Flag which indicates if the async polling engine is enabled
     */
    @XmlAttribute(name="asyncPollingEngineEnabled")
    private Boolean m_asyncPollingEngineEnabled;

    @XmlAttribute(name="maxConcurrentAsyncPolls")
    private Integer m_maxConcurrentAsyncPolls;

    /**
     * Configuration of node-outage functionality
     */
    @XmlElement(name="node-outage")
    private NodeOutage m_nodeOutage;

    /**
     * Package encapsulating addresses, services to be polled for these
     * addresses, etc..
     */
    @XmlElement(name="package")
    private List<Package> m_packages = new ArrayList<>();

    /**
     * Service monitors
     */
    @XmlElement(name="monitor")
    private List<Monitor> m_monitors = new ArrayList<>();

    /**
     * The maximum number of threads used for polling.
     */
    public Integer getThreads() {
        return m_threads == null? 0 : m_threads;
    }

    public void setThreads(final Integer threads) {
        m_threads = threads;
    }

    /**
     * SQL query for getting the next outage ID.
     */
    public String getNextOutageId() {
        return m_nextOutageId == null? "SELECT nextval('outageNxtId')" : m_nextOutageId;
    }

    public void setNextOutageId(final String nextOutageId) {
        m_nextOutageId = nextOutageId;
    }

    /**
     * Enable/disable serviceUnresponsive behavior
     */
    public String getServiceUnresponsiveEnabled() {
        return m_serviceUnresponsiveEnabled;
    }

    public void setServiceUnresponsiveEnabled(final String serviceUnresponsiveEnabled) {
        m_serviceUnresponsiveEnabled = serviceUnresponsiveEnabled;
    }

    /**
     * Flag which indicates if the optional path outage feature is enabled
     */
    public String getPathOutageEnabled() {
        return m_pathOutageEnabled == null? "false" : m_pathOutageEnabled;
    }

    public void setPathOutageEnabled(final String pathOutageEnabled) {
        m_pathOutageEnabled = pathOutageEnabled;
    }

    /**
     * Configuration of node-outage functionality
     */
    public NodeOutage getNodeOutage() {
        return m_nodeOutage;
    }

    public void setNodeOutage(final NodeOutage nodeOutage) {
        m_nodeOutage = nodeOutage;
    }

    public List<Package> getPackages() {
        if (m_packages == null) {
            m_packages = new ArrayList<>();
        }
        return m_packages;
    }

    public void setPackages(final List<Package> packages) {
        m_packages = new ArrayList<Package>(packages);
    }

    public void addPackage(final Package pack) throws IndexOutOfBoundsException {
        getPackages().add(pack);
    }

    public boolean removePackage(final Package pack) {
        return getPackages().remove(pack);
    }

    public Package getPackage(final String packageName) {
        for (final Package pkg : getPackages()) {
            if (pkg.getName().equals(packageName)) {
                return pkg;
            }
        }
        return null;
    }

    public List<Monitor> getMonitors() {
        if (m_monitors == null) {
            m_monitors = new ArrayList<>();
        }
        return m_monitors;
    }

    public void setMonitors(final List<Monitor> monitors) {
        m_monitors = new ArrayList<Monitor>(monitors);
    }

    public void addMonitor(final Monitor monitor) throws IndexOutOfBoundsException {
        getMonitors().add(monitor);
    }

    public void addMonitor(final String service, final String className) {
        addMonitor(new Monitor(service, className));
    }

    public boolean removeMonitor(final Monitor monitor) {
        return getMonitors().remove(monitor);
    }

    public PollerConfiguration getPollerConfigurationForPackages(final List<String> pollingPackageNames) {
        if (pollingPackageNames == null || pollingPackageNames.size() < 1) return null;
        
        final Set<String> seenMonitors = new HashSet<>();
        final PollerConfiguration newConfig = new PollerConfiguration();
        newConfig.setThreads(getThreads());
        newConfig.setNextOutageId(getNextOutageId());
        newConfig.setServiceUnresponsiveEnabled(getServiceUnresponsiveEnabled());
        newConfig.setPathOutageEnabled(getPathOutageEnabled());
        newConfig.setNodeOutage(getNodeOutage());
        
        // Add all requested polling packages to the config
        boolean foundPackage = false;
        for (String pollingPackageName : pollingPackageNames) {
            final Package pkg = getPackage(pollingPackageName);
            if (pkg != null) {
                newConfig.addPackage(pkg);
                foundPackage = true;
                for (final Service service : pkg.getServices()) {
                    seenMonitors.add(service.getName());
                }
            }
        }
        // If the list of polling packages doesn't match anything, then return null
        if (!foundPackage) return null;
        
        for (final Monitor monitor : getMonitors()) {
            if (seenMonitors.contains(monitor.getService())) {
                newConfig.addMonitor(monitor);
            }
        }
        return newConfig;
    }

    public InetAddress getDefaultCriticalPathIp() {
        return m_defaultCriticalPathIp;
    }

    public void setDefaultCriticalPathIp(final InetAddress ip) {
        m_defaultCriticalPathIp = ip;
    }

    public Integer getDefaultCriticalPathTimeout() {
        return m_defaultCriticalPathTimeout == null? 1500 : m_defaultCriticalPathTimeout;
    }

    public void setDefaultCriticalPathTimeout(final Integer timeout) {
        m_defaultCriticalPathTimeout = timeout;
    }

    public int getDefaultCriticalPathRetries() {
        return m_defaultCriticalPathRetries == null? 0 : m_defaultCriticalPathRetries;
    }

    public void setDefaultCriticalPathRetries(final Integer retries) {
        m_defaultCriticalPathRetries = retries;
    }

    public Boolean getAsyncPollingEngineEnabled() {
        return Objects.requireNonNullElse(m_asyncPollingEngineEnabled, false);
    }

    public void setAsyncPollingEngineEnabled(Boolean asyncPollingEngineEnabled) {
        m_asyncPollingEngineEnabled = asyncPollingEngineEnabled;
    }

    public Integer getMaxConcurrentAsyncPolls() {
        return Objects.requireNonNullElse(m_maxConcurrentAsyncPolls, 100);
    }

    public void setMaxConcurrentAsyncPolls(Integer maxConcurrentAsyncPolls) {
        m_maxConcurrentAsyncPolls = maxConcurrentAsyncPolls;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getThreads(), getNextOutageId(), getServiceUnresponsiveEnabled(), getPathOutageEnabled(),
                getDefaultCriticalPathIp(), getDefaultCriticalPathTimeout(), getDefaultCriticalPathRetries(),
                getNodeOutage(), getPackages(), getMonitors(), getAsyncPollingEngineEnabled(), getMaxConcurrentAsyncPolls());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PollerConfiguration that = (PollerConfiguration) o;
        return Objects.equals(getThreads(), that.getThreads())
                && Objects.equals(getNextOutageId(), that.getNextOutageId())
                && Objects.equals(getServiceUnresponsiveEnabled(), that.getServiceUnresponsiveEnabled())
                && Objects.equals(getPathOutageEnabled(), that.getPathOutageEnabled())
                && Objects.equals(getDefaultCriticalPathIp(), that.getDefaultCriticalPathIp())
                && Objects.equals(getDefaultCriticalPathTimeout(), that.getDefaultCriticalPathTimeout())
                && Objects.equals(getDefaultCriticalPathRetries(), that.getDefaultCriticalPathRetries())
                && Objects.equals(getNodeOutage(), that.getNodeOutage())
                && Objects.equals(getPackages(), that.getPackages())
                && Objects.equals(getMonitors(), that.getMonitors())
                && Objects.equals(getAsyncPollingEngineEnabled(), that.getAsyncPollingEngineEnabled())
                && Objects.equals(getMaxConcurrentAsyncPolls(), that.getMaxConcurrentAsyncPolls());
    }
    @Override
    public String toString() {
        return "PollerConfiguration[" +
                "threads=" + getThreads() +
                ",nextOutageId=" + getNextOutageId() +
                ",serviceUnresponsiveEnabled=" + getServiceUnresponsiveEnabled() +
                ",pathOutageEnabled=" + getPathOutageEnabled() +
                ",pathOutageDefaultCriticalPathIp=" + getDefaultCriticalPathIp() +
                ",pathOutageDefaultCriticalPathTimeout=" + getDefaultCriticalPathTimeout() +
                ",pathOutageDefaultCriticalPathRetries=" + getDefaultCriticalPathRetries() +
                ",nodeOutage=" + getNodeOutage() +
                ",packages=" + getPackages() +
                ",monitors=" + getMonitors() +
                ",asyncPollingEngineEnabled=" + getAsyncPollingEngineEnabled() +
                ",maxConcurrentAsyncPolls=" + getMaxConcurrentAsyncPolls() +
                "]";
    }

}
