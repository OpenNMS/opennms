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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is the top-level element for wmi-config.xml
 */

public class WmiConfig implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Default timeout (in milliseconds).
     */
    private Integer timeout;

    /**
     * Default number of retries.
     */
    private Integer retry;

    /**
     * Default username.
     */
    private String username;

    /**
     * Default Windows Domain.
     */
    private String domain;

    /**
     * Default user password.
     */
    private String password;

    /**
     * Maps IP addresses to specific SNMP parmeters
     *  (retries, timeouts...)
     */
    @JsonProperty("definition")
    private List<Definition> definitions = new ArrayList<>();

    public Optional<Integer> getTimeout() {
        return Optional.ofNullable(this.timeout);
    }

    public void setTimeout(final Integer timeout) {
        this.timeout = timeout;
    }

    public Optional<Integer> getRetry() {
        return Optional.ofNullable(this.retry);
    }

    public void setRetry(final Integer retry) {
        this.retry = retry;
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

    public List<Definition> getDefinitions() {
        return this.definitions;
    }

    public void setDefinitions(final List<Definition> definitions) {
        if (definitions == this.definitions) return;
        this.definitions.clear();
        if (definitions != null) this.definitions.addAll(definitions);
    }

    public void addDefinition(final Definition definition) {
        this.definitions.add(definition);
    }

    public boolean removeDefinition(final Definition definition) {
        return this.definitions.remove(definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeout,
                            retry,
                            username,
                            domain,
                            password,
                            definitions);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof WmiConfig) {
            final WmiConfig that = (WmiConfig)obj;
            return Objects.equals(this.timeout, that.timeout)
                    && Objects.equals(this.retry, that.retry)
                    && Objects.equals(this.username, that.username)
                    && Objects.equals(this.domain, that.domain)
                    && Objects.equals(this.password, that.password)
                    && Objects.equals(this.definitions, that.definitions);
        }
        return false;
    }

}
