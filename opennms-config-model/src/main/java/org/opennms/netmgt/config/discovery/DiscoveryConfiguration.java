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

package org.opennms.netmgt.config.discovery;


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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.DoubleAdapter;
import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "discovery-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("discovery-configuration.xsd")
public class DiscoveryConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The monitoring location where this discovery configuration
     *  will be executed.
     */
    @XmlAttribute(name = "location")
    private String m_location;

    /**
     * The total number of ping packets sent per second from
     *  OpenNMS for discovery
     */
    @XmlAttribute(name = "packets-per-second", required = true)
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    private Double m_packetsPerSecond;

    /**
     * The initial pause (in milliseconds) after discovery
     *  starts up before discovery is started.
     */
    @XmlAttribute(name = "initial-sleep-time", required = true)
    private Long m_initialSleepTime;

    /**
     * The pause (in milliseconds) between discovery passes.
     *  Once the discovery process completes a sweep of all the specified
     *  addresses, it sleeps for this duration before starting another
     *  sweep.
     */
    @XmlAttribute(name = "restart-sleep-time", required = true)
    private Long m_restartSleepTime;

    /**
     * The default number of times a ping is retried for an
     *  address. If there is no response after the first ping to an address,
     *  it is tried again for the specified number of retries. This retry
     *  count is a default and can be overridden for specific addresses or
     *  sets of addresses that are configured in this file.
     */
    @XmlAttribute(name = "retries")
    private Integer m_retries;

    /**
     * The default timeout on each poll. This timeout is a
     *  default and can be overridden for specific addresses or sets of
     *  addresses that are configured in this file.
     */
    @XmlAttribute(name = "timeout")
    private Long m_timeout;

    @XmlAttribute(name = "foreign-source")
    private String m_foreignSource;

    /**
     * The default chunk size used to divide up ranges to be discovered by
     * minions. This size determines the individual unit of work a particular
     * discovery job sent to a minion will encompass.
     */
    @XmlAttribute(name = "chunk-size")
    private Integer m_chunkSize;

    /**
     * the specific addresses to be polled
     */
    @XmlElement(name = "specific")
    private List<Specific> m_specifics = new ArrayList<>();

    /**
     * the range of addresses to be polled
     */
    @XmlElement(name = "include-range")
    private List<IncludeRange> m_includeRanges = new ArrayList<>();

    /**
     * the range of addresses to be excluded from the
     *  polling
     */
    @XmlElement(name = "exclude-range")
    private List<ExcludeRange> m_excludeRanges = new ArrayList<>();

    /**
     * a file URL holding specific addresses to be
     *  polled
     */
    @XmlElement(name = "include-url")
    private List<IncludeUrl> m_includeUrls = new ArrayList<>();

    public DiscoveryConfiguration() {
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(m_location);
    }

    public void setLocation(final String location) {
        m_location = location;
    }

    public Optional<Double> getPacketsPerSecond() {
        return Optional.ofNullable(m_packetsPerSecond);
    }

    public void setPacketsPerSecond(final Double packetsPerSecond) {
        m_packetsPerSecond = packetsPerSecond;
    }

    public Optional<Long> getInitialSleepTime() {
        return Optional.ofNullable(m_initialSleepTime);
    }

    public void setInitialSleepTime(final Long initialSleepTime) {
        m_initialSleepTime = initialSleepTime;
    }

    public Optional<Long> getRestartSleepTime() {
        return Optional.ofNullable(m_restartSleepTime);
    }

    public void setRestartSleepTime(final Long restartSleepTime) {
        m_restartSleepTime = restartSleepTime;
    }

    public Optional<Integer> getRetries() {
        return Optional.ofNullable(m_retries);
    }

    public void setRetries(final Integer retries) {
        m_retries = retries;
    }

    public Optional<Long> getTimeout() {
        return Optional.ofNullable(m_timeout);
    }

    public void setTimeout(final Long timeout) {
        if (timeout != null && timeout == 0) {
            throw new IllegalArgumentException("Can't have a 0 timeout!");
        }
        m_timeout = timeout;
    }

    public Optional<String> getForeignSource() {
        return Optional.ofNullable(m_foreignSource);
    }

    public void setForeignSource(final String foreignSource) {
        m_foreignSource = foreignSource;
    }

    public Optional<Integer> getChunkSize() {
        return Optional.ofNullable(m_chunkSize);
    }

    public void setChunkSize(final Integer chunkSize) {
        m_chunkSize = chunkSize;
    }

    public List<Specific> getSpecifics() {
        return m_specifics;
    }

    public Specific getSpecific(final int index) {
        return getSpecifics().get(index);
    }

    public void setSpecifics(final List<Specific> specifics) {
        m_specifics.clear();
        m_specifics.addAll(specifics);
    }

    public void addSpecific(final Specific specific) {
        m_specifics.add(specific);
    }

    public boolean removeSpecific(final Specific specific) {
        return m_specifics.remove(specific);
    }

    /**
     */
    public void clearSpecifics() {
        m_specifics.clear();
    }

    public List<IncludeRange> getIncludeRanges() {
        return m_includeRanges;
    }

    public IncludeRange getIncludeRange(final int index) {
        return getIncludeRanges().get(index);
    }

    public void setIncludeRanges(final List<IncludeRange> includeRanges) {
        m_includeRanges.clear();
        m_includeRanges.addAll(includeRanges);
    }

    public void addIncludeRange(final IncludeRange includeRange) {
        m_includeRanges.add(includeRange);
    }

    public boolean removeIncludeRange(final IncludeRange includeRange) {
        return m_includeRanges.remove(includeRange);
    }

    /**
     */
    public void clearIncludeRanges() {
        m_includeRanges.clear();
    }

    public List<ExcludeRange> getExcludeRanges() {
        return m_excludeRanges;
    }

    public ExcludeRange getExcludeRange(final int index) {
        return getExcludeRanges().get(index);
    }

    public void setExcludeRanges(final List<ExcludeRange> excludeRanges) {
        m_excludeRanges.clear();
        m_excludeRanges.addAll(excludeRanges);
    }

    public void addExcludeRange(final ExcludeRange excludeRange) {
        m_excludeRanges.add(excludeRange);
    }

    public boolean removeExcludeRange(final ExcludeRange excludeRange) {
        return m_excludeRanges.remove(excludeRange);
    }

    /**
     */
    public void clearExcludeRanges() {
        m_excludeRanges.clear();
    }

    public List<IncludeUrl> getIncludeUrls() {
        return m_includeUrls;
    }

    public IncludeUrl getIncludeUrl(final int index) {
        return getIncludeUrls().get(index);
    }

    public void setIncludeUrls(final List<IncludeUrl> includeUrls) {
        m_includeUrls.clear();
        m_includeUrls.addAll(includeUrls);
    }

    public void addIncludeUrl(final IncludeUrl includeUrl) {
        m_includeUrls.add(includeUrl);
    }

    public boolean removeIncludeUrl(final IncludeUrl includeUrl) {
        return m_includeUrls.remove(includeUrl);
    }

    /**
     */
    public void clearIncludeUrls() {
        m_includeUrls.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_location, 
                            m_packetsPerSecond, 
                            m_initialSleepTime, 
                            m_restartSleepTime, 
                            m_retries, 
                            m_timeout, 
                            m_foreignSource, 
                            m_chunkSize, 
                            m_specifics, 
                            m_includeRanges, 
                            m_excludeRanges, 
                            m_includeUrls);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof DiscoveryConfiguration) {
            final DiscoveryConfiguration temp = (DiscoveryConfiguration)obj;
            return Objects.equals(temp.m_location, m_location)
                    && Objects.equals(temp.m_packetsPerSecond, m_packetsPerSecond)
                    && Objects.equals(temp.m_initialSleepTime, m_initialSleepTime)
                    && Objects.equals(temp.m_restartSleepTime, m_restartSleepTime)
                    && Objects.equals(temp.m_retries, m_retries)
                    && Objects.equals(temp.m_timeout, m_timeout)
                    && Objects.equals(temp.m_foreignSource, m_foreignSource)
                    && Objects.equals(temp.m_chunkSize, m_chunkSize)
                    && Objects.equals(temp.m_specifics, m_specifics)
                    && Objects.equals(temp.m_includeRanges, m_includeRanges)
                    && Objects.equals(temp.m_excludeRanges, m_excludeRanges)
                    && Objects.equals(temp.m_includeUrls, m_includeUrls);
        }
        return false;
    }

    @Override
    public String toString() {
        return "DiscoveryConfiguration [location=" + m_location
                + ", packetsPerSecond=" + m_packetsPerSecond
                + ", initialSleepTime=" + m_initialSleepTime
                + ", restartSleepTime=" + m_restartSleepTime
                + ", retries=" + m_retries + ", timeout=" + m_timeout
                + ", foreignSource=" + m_foreignSource + ", chunkSize="
                + m_chunkSize + ", specifics=" + m_specifics
                + ", includeRanges=" + m_includeRanges
                + ", excludeRanges=" + m_excludeRanges + ", includeUrls="
                + m_includeUrls + "]";
    }

}
