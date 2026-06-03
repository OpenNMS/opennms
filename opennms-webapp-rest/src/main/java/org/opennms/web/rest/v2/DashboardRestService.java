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
package org.opennms.web.rest.v2;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.web.rest.v2.api.DashboardRestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link DashboardRestApi}. Persists the single system-wide
 * dashboard layout JSON document in the {@link JsonStore} (the same key-value
 * store used by e.g. ResourceRestService), keyed by a fixed context/key.
 */
@Service
public class DashboardRestService implements DashboardRestApi {
    private static final Logger LOG = LoggerFactory.getLogger(DashboardRestService.class);

    // One document for the whole system. Future per-user / named dashboards would
    // vary the key while keeping the same store.
    private static final String CONTEXT = "dashboard";
    private static final String KEY = "system";

    @Autowired
    private JsonStore jsonStore;

    @Autowired
    private ServiceTypeDao serviceTypeDao;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Response getSystemLayout() {
        final Optional<String> stored = jsonStore.get(KEY, CONTEXT);
        if (stored.isEmpty()) {
            return Response.status(Status.NOT_FOUND).build();
        }
        try {
            final Map<?, ?> layout = objectMapper.readValue(stored.get(), Map.class);
            return Response.ok(layout).build();
        } catch (final Exception e) {
            LOG.error("Failed to parse stored system dashboard layout", e);
            return Response.serverError().build();
        }
    }

    @Override
    public Response updateSystemLayout(final Map<String, Object> layout) {
        if (layout == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        try {
            final String json = objectMapper.writeValueAsString(layout);
            jsonStore.put(KEY, json, CONTEXT);
            return Response.noContent().build();
        } catch (final Exception e) {
            LOG.error("Failed to store system dashboard layout", e);
            return Response.serverError().build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Response getServiceTypes() {
        try {
            final List<Map<String, Object>> types = serviceTypeDao.findAll().stream()
                    .sorted(Comparator.comparing(OnmsServiceType::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(t -> {
                        final Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", t.getId());
                        m.put("name", t.getName());
                        return m;
                    })
                    .collect(Collectors.toList());
            return Response.ok(types).build();
        } catch (final Exception e) {
            LOG.error("Failed to list service types", e);
            return Response.serverError().build();
        }
    }
}
