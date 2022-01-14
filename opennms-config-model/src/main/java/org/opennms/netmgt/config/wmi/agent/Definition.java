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

package org.opennms.netmgt.config.wmi.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opennms.netmgt.config.utils.ConfigUtils;
import org.opennms.netmgt.config.wmi.WmiAgentConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides a mechanism for associating one or more
 * specific IP addresses and/or IP address ranges with a
 * set of WMI parms which will be used in place of the
 * default values during WMI data collection.
 */

public class Definition implements Serializable {
    private static final long serialVersionUID = 2L;

    private Integer retry;

    private Integer timeout;

    private String username;

    private String domain;

    private String password;

    /**
     * IP address range to which this definition
     * applies.
     */
    @JsonProperty("range")
    private List<Range> ranges = new ArrayList<>();

    /**
     * Specific IP address to which this definition
     * applies.
     */
    @JsonProperty("specific")
    private List<String> specifics = new ArrayList<>();

    /**
     * Match Octets (as in IPLIKE)
     */
    @JsonProperty("ip-match")
    private List<String> ipMatches = new ArrayList<>();

    public Integer getRetry() {
        return this.retry != null ? this.retry : WmiAgentConfig.DEFAULT_RETRIES;
    }

    public void setRetry(final Integer retry) {
        this.retry = retry;
    }

    public Integer getTimeout() {
        return this.timeout != null ? this.timeout : WmiAgentConfig.DEFAULT_TIMEOUT;
    }

    public void setTimeout(final Integer timeout) {
        this.timeout = timeout;
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(this.username);
    }

    public void setUsername(final String username) {
        this.username = ConfigUtils.normalizeString(username);
    }

    public Optional<String> getDomain() {
        return Optional.ofNullable(this.domain);
    }

    public void setDomain(final String domain) {
        this.domain = ConfigUtils.normalizeString(domain);
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(this.password);
    }

    public void setPassword(final String password) {
        this.password = ConfigUtils.normalizeString(password);
    }

    public List<Range> getRanges() {
        return this.ranges;
    }

    public void setRanges(final List<Range> ranges) {
        if (ranges == this.ranges) return;
        this.ranges.clear();
        if (ranges != null) this.ranges.addAll(ranges);
    }

    public void addRange(final Range range) {
        this.ranges.add(range);
    }

    public boolean removeRange(final Range range) {
        return this.ranges.remove(range);
    }

    public List<String> getSpecifics() {
        return this.specifics;
    }

    public void setSpecifics(final List<String> specifics) {
        if (specifics == this.specifics) return;
        this.specifics.clear();
        if (specifics != null) this.specifics.addAll(specifics);
    }

    public void addSpecific(final String specific) {
        this.specifics.add(specific);
    }

    public boolean removeSpecific(final String specific) {
        return this.specifics.remove(specific);
    }

    public List<String> getIpMatches() {
        return this.ipMatches;
    }

    public void setIpMatches(final List<String> ipMatches) {
        if (ipMatches == this.ipMatches) return;
        this.ipMatches.clear();
        if (ipMatches != null) this.ipMatches.addAll(ipMatches);
    }

    public void addIpMatch(final String ipMatch) {
        this.ipMatches.add(ipMatch);
    }

    public boolean removeIpMatch(final String ipMatch) {
        return this.ipMatches.remove(ipMatch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(retry,
                timeout,
                username,
                domain,
                password,
                this.ranges,
                this.specifics,
                this.ipMatches);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Definition) {
            final Definition that = (Definition) obj;
            return Objects.equals(this.retry, that.retry)
                    && Objects.equals(this.timeout, that.timeout)
                    && Objects.equals(this.username, that.username)
                    && Objects.equals(this.domain, that.domain)
                    && Objects.equals(this.password, that.password)
                    && Objects.equals(this.ranges, that.ranges)
                    && Objects.equals(this.specifics, that.specifics)
                    && Objects.equals(this.ipMatches, that.ipMatches);
        }
        return false;
    }

}
