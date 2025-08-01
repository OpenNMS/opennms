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
