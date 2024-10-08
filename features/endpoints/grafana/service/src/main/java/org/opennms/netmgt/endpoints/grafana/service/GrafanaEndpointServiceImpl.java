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
package org.opennms.netmgt.endpoints.grafana.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Pattern;

import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaClient;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaClientFactory;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpointException;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpointService;
import org.opennms.netmgt.endpoints.grafana.persistence.api.GrafanaEndpointDao;

import com.google.common.base.Strings;

public class GrafanaEndpointServiceImpl implements GrafanaEndpointService {

    static final Pattern UID_PATTERN = Pattern.compile("^[a-zA-Z0-9]+[a-zA-Z0-9_-]*$");

    static final String PROVIDE_A_VALUE_TEXT = "Please provide a value";
    static final String URL_NOT_VALID_TEMPLATE = "The provided URL ''{0}'' is not valid: ''{1}''";
    static final String PROVIDED_VALUE_GREATER_ZERO_TEXT = "The provided value must be >= 0";
    static final String UID_INVALID_TEMPLATE = "The provided Grafana ID ''{0}'' is not valid. It does not match the regular expression ''{1}''";

    private final GrafanaEndpointDao endpointDao;
    private final GrafanaClientFactory clientFactory;
    private final SessionUtils sessionUtils;

    public GrafanaEndpointServiceImpl(final GrafanaEndpointDao endpointDao, final GrafanaClientFactory clientFactory, final SessionUtils sessionUtils) {
        this.endpointDao = Objects.requireNonNull(endpointDao);
        this.clientFactory = Objects.requireNonNull(clientFactory);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    @Override
    public List<GrafanaEndpoint> findEndpoints() {
        final List<GrafanaEndpoint> endpoints = sessionUtils.withReadOnlyTransaction(() -> endpointDao.findAll());
        Collections.sort(endpoints, Comparator.comparing(GrafanaEndpoint::getId));
        return endpoints;
    }

    @Override
    public void deleteAllEndpoints() {
        sessionUtils.withTransaction(() -> {
            endpointDao.findAll().forEach(endpoint -> endpointDao.delete(endpoint));
            return null;
        });
    }

    @Override
    public void updateEndpoint(GrafanaEndpoint endpoint) throws NoSuchElementException {
        sessionUtils.withTransaction(() -> {
            validate(endpoint);
            final GrafanaEndpoint persistedEndpoint = findEndpoint(endpoint.getId());
            persistedEndpoint.merge(endpoint);
            endpointDao.update(persistedEndpoint);
            return null;
        });
    }

    @Override
    public void saveEndpoint(GrafanaEndpoint newGrafanaEndpoint) throws NoSuchElementException {
        sessionUtils.withTransaction(() -> {
            validate(newGrafanaEndpoint);
            endpointDao.save(newGrafanaEndpoint);
            return null;
        });
    }

    @Override
    public void deleteEndpoint(GrafanaEndpoint grafanaEndpoint) throws NoSuchElementException {
        sessionUtils.withTransaction(() -> {
            GrafanaEndpoint endpoint = findEndpoint(grafanaEndpoint.getId());
            endpointDao.delete(endpoint);
            return true;
        });
    }

    @Override
    public GrafanaEndpoint getEndpointById(Long endpointId) {
        return sessionUtils.withReadOnlyTransaction(() -> findEndpoint(endpointId));
    }

    @Override
    public GrafanaEndpoint getEndpointByUid(String uid) {
        return sessionUtils.withReadOnlyTransaction(() -> findEndpoint(uid));
    }

    @Override
    public GrafanaClient getClient(GrafanaEndpoint grafanaEndpoint) {
        return clientFactory.createClient(grafanaEndpoint);
    }

    @Override
    public GrafanaClient getClient(String uid) throws NoSuchElementException {
        return clientFactory.createClient(findEndpoint(uid));
    }

    private GrafanaEndpoint findEndpoint(Long endpointId) throws NoSuchElementException {
        final GrafanaEndpoint grafanaEndpoint = endpointDao.get(endpointId);
        if (grafanaEndpoint == null) {
            new NoSuchElementException("Could not find grafana endpoint with id '" + endpointId + "'");
        }
        return grafanaEndpoint;
    }

    private GrafanaEndpoint findEndpoint(String uid) throws NoSuchElementException {
        final GrafanaEndpoint grafanaEndpoint = endpointDao.getByUid(uid);
        if (grafanaEndpoint == null) {
            new NoSuchElementException("Could not find grafana endpoint with uid '" + uid + "'");
        }
        return grafanaEndpoint;
    }

    void validate(GrafanaEndpoint endpoint) {
        if (Strings.isNullOrEmpty(endpoint.getUrl())) {
            throw new GrafanaEndpointException("url", PROVIDE_A_VALUE_TEXT);
        }
        try {
            new URL(endpoint.getUrl());
        } catch (MalformedURLException e) {
            throw new GrafanaEndpointException("url", URL_NOT_VALID_TEMPLATE, endpoint.getUrl(), e.getMessage());
        }
        if (Strings.isNullOrEmpty(endpoint.getApiKey())) {
            throw new GrafanaEndpointException("apiKey", PROVIDE_A_VALUE_TEXT);
        }
        if (Strings.isNullOrEmpty(endpoint.getUid())) {
            throw new GrafanaEndpointException("uid", PROVIDE_A_VALUE_TEXT);
        }
        if (endpoint.getConnectTimeout() != null && endpoint.getConnectTimeout() < 0) {
            throw new GrafanaEndpointException("connectTimeout", PROVIDED_VALUE_GREATER_ZERO_TEXT);
        }
        if (endpoint.getReadTimeout() != null && endpoint.getReadTimeout() < 0) {
            throw new GrafanaEndpointException("readTimeout", PROVIDED_VALUE_GREATER_ZERO_TEXT);
        }
        // Ensure UID is defined correctly
        if (!UID_PATTERN.matcher(endpoint.getUid()).matches()) {
            throw new GrafanaEndpointException("uid", UID_INVALID_TEMPLATE, endpoint.getUid(), UID_PATTERN.pattern());
        }
        // Verify that only one GrafanaEndpoint per UID exists.
        // If an endpoint already exists, ensure it is the same object
        final GrafanaEndpoint byUid = endpointDao.getByUid(endpoint.getUid());
        if (byUid != null && !(byUid.getId().equals(endpoint.getId()))) {
            throw new GrafanaEndpointException("uid", "An endpoint with uid ''{0}'' already exists.", endpoint.getUid());
        }
    }
}
