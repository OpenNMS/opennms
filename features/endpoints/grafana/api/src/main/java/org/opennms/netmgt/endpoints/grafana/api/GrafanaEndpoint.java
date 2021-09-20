/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.endpoints.grafana.api;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

@Entity
@Table(name="endpoints_grafana")
public class GrafanaEndpoint {

    @Id
    @SequenceGenerator(name = "endpointsSequence", sequenceName = "endpointsnxtid")
    @GeneratedValue(generator = "endpointsSequence")
    @Column(name="id", nullable = false)
    private Long id;

    @Column(name="uid", unique=true, nullable=false)
    private String uid;

    @Column(name="url", nullable=false)
    private String url;

    @Column(name="api_key", nullable=false)
    private String apiKey;

    @Column(name="description")
    private String description;

    @Column(name="connect_timeout")
    private Integer connectTimeout;

    @Column(name="read_timeout")
    private Integer readTimeout;

    public GrafanaEndpoint() {

    }

    public GrafanaEndpoint(GrafanaEndpoint endpoint) {
        Objects.requireNonNull(endpoint);
        merge(endpoint);
        setId(endpoint.getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void merge(final GrafanaEndpoint endpoint) {
        Objects.requireNonNull(endpoint);
        setApiKey(endpoint.getApiKey());
        setUid(endpoint.getUid());
        setUrl(endpoint.getUrl());
        setConnectTimeout(endpoint.getConnectTimeout());
        setReadTimeout(endpoint.getReadTimeout());
        setDescription(endpoint.getDescription());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (other instanceof GrafanaEndpoint) {
            final GrafanaEndpoint that = (GrafanaEndpoint) other;
            return Objects.equals(id, that.id)
                    && Objects.equals(uid, that.uid)
                    && Objects.equals(url, that.url)
                    && Objects.equals(apiKey, that.apiKey)
                    && Objects.equals(description, that.description)
                    && Objects.equals(connectTimeout, that.connectTimeout)
                    && Objects.equals(readTimeout, that.readTimeout);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uid, url, apiKey, description, connectTimeout, readTimeout);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uid", uid)
                .add("url", url)
                .add("apiKey", apiKey)
                .add("description", description)
                .add("connectTimeout", connectTimeout)
                .add("readTimeout", readTimeout)
                .toString();
    }
}
