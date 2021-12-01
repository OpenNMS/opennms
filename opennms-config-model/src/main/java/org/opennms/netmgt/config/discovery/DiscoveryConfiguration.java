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


import com.fasterxml.jackson.annotation.JsonProperty;
import org.opennms.netmgt.config.utils.ConfigUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DiscoveryConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The monitoring location where this discovery configuration
     *  will be executed.
     */
    private String location;

    /**
     * The total number of ping packets sent per second from
     *  OpenNMS for discovery
     */
    private Double packetsPerSecond;

    /**
     * The initial pause (in milliseconds) after discovery
     *  starts up before discovery is started.
     */
    private Long initialSleepTime;

    /**
     * The pause (in milliseconds) between discovery passes.
     *  Once the discovery process completes a sweep of all the specified
     *  addresses, it sleeps for this duration before starting another
     *  sweep.
     */
    private Long restartSleepTime;

    /**
     * The default number of times a ping is retried for an
     *  address. If there is no response after the first ping to an address,
     *  it is tried again for the specified number of retries. This retry
     *  count is a default and can be overridden for specific addresses or
     *  sets of addresses that are configured in this file.
     */
    private Integer retries;

    /**
     * The default timeout on each poll. This timeout is a
     *  default and can be overridden for specific addresses or sets of
     *  addresses that are configured in this file.
     */
    private Long timeout;

    private String foreignSource;

    /**
     * The default chunk size used to divide up ranges to be discovered by
     * minions. This size determines the individual unit of work a particular
     * discovery job sent to a minion will encompass.
     */
    private Integer chunkSize;

    /**
     * the specific addresses to be polled
     */
    @JsonProperty("specific")
    private List<Specific> specifics = new ArrayList<>();

    /**
     * the range of addresses to be polled
     */
    @JsonProperty("include-range")
    private List<IncludeRange> includeRanges = new ArrayList<>();

    /**
     * the range of addresses to be excluded from the
     *  polling
     */
    @JsonProperty("exclude-range")
    private List<ExcludeRange> excludeRanges = new ArrayList<>();

    /**
     * a file URL holding specific addresses to be
     *  polled
     */
    @JsonProperty("include-url")
    private List<IncludeUrl> includeUrls = new ArrayList<>();

    @JsonProperty("definition")
    private List<Definition> definitions = new ArrayList<>();

    public DiscoveryConfiguration() {
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(this.location);
    }

    public void setLocation(final String location) {
        this.location = ConfigUtils.normalizeString(location);
    }

    public Optional<Double> getPacketsPerSecond() {
        return Optional.ofNullable(this.packetsPerSecond);
    }

    public void setPacketsPerSecond(final Double packetsPerSecond) {
        this.packetsPerSecond = packetsPerSecond;
    }

    public Optional<Long> getInitialSleepTime() {
        return Optional.ofNullable(this.initialSleepTime);
    }

    public void setInitialSleepTime(final Long initialSleepTime) {
        this.initialSleepTime = initialSleepTime;
    }

    public Optional<Long> getRestartSleepTime() {
        return Optional.ofNullable(this.restartSleepTime);
    }

    public void setRestartSleepTime(final Long restartSleepTime) {
        this.restartSleepTime = restartSleepTime;
    }

    public Optional<Integer> getRetries() {
        return Optional.ofNullable(this.retries);
    }

    public void setRetries(final Integer retries) {
        this.retries = retries;
    }

    public Optional<Long> getTimeout() {
        return Optional.ofNullable(this.timeout);
    }

    public void setTimeout(final Long timeout) {
        if (timeout != null && timeout == 0) {
            throw new IllegalArgumentException("Can't have a 0 timeout!");
        }
        this.timeout = timeout;
    }

    public Optional<String> getForeignSource() {
        return Optional.ofNullable(this.foreignSource);
    }

    public void setForeignSource(final String foreignSource) {
        this.foreignSource = ConfigUtils.normalizeString(foreignSource);
    }

    public Optional<Integer> getChunkSize() {
        return Optional.ofNullable(this.chunkSize);
    }

    public void setChunkSize(final Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public List<Specific> getSpecifics() {
        return this.specifics;
    }

    public void setSpecifics(final List<Specific> specifics) {
        if (specifics == this.specifics) return;
        this.specifics.clear();
        if (specifics != null) this.specifics.addAll(specifics);
    }

    public void addSpecific(final Specific specific) {
        this.specifics.add(specific);
    }

    public boolean removeSpecific(final Specific specific) {
        return this.specifics.remove(specific);
    }

    /**
     */
    public void clearSpecifics() {
        this.specifics.clear();
    }

    public List<IncludeRange> getIncludeRanges() {
        return this.includeRanges;
    }

    public void setIncludeRanges(final List<IncludeRange> includeRanges) {
        if (includeRanges == this.includeRanges) return;
        this.includeRanges.clear();
        if (includeRanges != null) this.includeRanges.addAll(includeRanges);
    }

    public void addIncludeRange(final IncludeRange includeRange) {
        this.includeRanges.add(includeRange);
    }

    public boolean removeIncludeRange(final IncludeRange includeRange) {
        return this.includeRanges.remove(includeRange);
    }

    /**
     */
    public void clearIncludeRanges() {
        this.includeRanges.clear();
    }

    public List<ExcludeRange> getExcludeRanges() {
        return this.excludeRanges;
    }

    public void setExcludeRanges(final List<ExcludeRange> excludeRanges) {
        if (excludeRanges == this.excludeRanges) return;
        this.excludeRanges.clear();
        if (excludeRanges != null) this.excludeRanges.addAll(excludeRanges);
    }

    public void addExcludeRange(final ExcludeRange excludeRange) {
        this.excludeRanges.add(excludeRange);
    }

    public boolean removeExcludeRange(final ExcludeRange excludeRange) {
        return this.excludeRanges.remove(excludeRange);
    }

    /**
     */
    public void clearExcludeRanges() {
        this.excludeRanges.clear();
    }

    public List<IncludeUrl> getIncludeUrls() {
        return this.includeUrls;
    }

    public void setIncludeUrls(final List<IncludeUrl> includeUrls) {
        if (includeUrls == this.includeUrls) return;
        this.includeUrls.clear();
        if (includeUrls != null) this.includeUrls.addAll(includeUrls);
    }

    public void addIncludeUrl(final IncludeUrl includeUrl) {
        this.includeUrls.add(includeUrl);
    }

    public boolean removeIncludeUrl(final IncludeUrl includeUrl) {
        return this.includeUrls.remove(includeUrl);
    }

    /**
     */
    public void clearIncludeUrls() {
        this.includeUrls.clear();
    }

    public List<Definition> getDefinitions() {
        return this.definitions;
    }

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }

    public void addDefinition(Definition definition) {
        this.definitions.add(definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            this.location, 
                            this.packetsPerSecond, 
                            this.initialSleepTime, 
                            this.restartSleepTime, 
                            this.retries, 
                            this.timeout, 
                            this.foreignSource, 
                            this.chunkSize, 
                            this.specifics, 
                            this.includeRanges, 
                            this.excludeRanges, 
                            this.includeUrls);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof DiscoveryConfiguration) {
            final DiscoveryConfiguration that = (DiscoveryConfiguration)obj;
            return Objects.equals(this.location, that.location)
                    && Objects.equals(this.packetsPerSecond, that.packetsPerSecond)
                    && Objects.equals(this.initialSleepTime, that.initialSleepTime)
                    && Objects.equals(this.restartSleepTime, that.restartSleepTime)
                    && Objects.equals(this.retries, that.retries)
                    && Objects.equals(this.timeout, that.timeout)
                    && Objects.equals(this.foreignSource, that.foreignSource)
                    && Objects.equals(this.chunkSize, that.chunkSize)
                    && Objects.equals(this.specifics, that.specifics)
                    && Objects.equals(this.includeRanges, that.includeRanges)
                    && Objects.equals(this.excludeRanges, that.excludeRanges)
                    && Objects.equals(this.includeUrls, that.includeUrls);
        }
        return false;
    }

    @Override
    public String toString() {
        return "DiscoveryConfiguration [location=" + this.location
                + ", packetsPerSecond=" + this.packetsPerSecond
                + ", initialSleepTime=" + this.initialSleepTime
                + ", restartSleepTime=" + this.restartSleepTime
                + ", retries=" + this.retries + ", timeout=" + this.timeout
                + ", foreignSource=" + this.foreignSource + ", chunkSize="
                + this.chunkSize + ", specifics=" + this.specifics
                + ", includeRanges=" + this.includeRanges
                + ", excludeRanges=" + this.excludeRanges + ", includeUrls="
                + this.includeUrls + "]";
    }
}