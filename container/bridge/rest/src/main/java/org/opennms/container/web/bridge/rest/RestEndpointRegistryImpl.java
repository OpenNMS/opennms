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
package org.opennms.container.web.bridge.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opennms.container.web.bridge.api.RestEndpointRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.runtime.JaxrsServiceRuntime;
import org.osgi.service.jaxrs.runtime.dto.ApplicationDTO;
import org.osgi.service.jaxrs.runtime.dto.ResourceDTO;
import org.osgi.service.jaxrs.runtime.dto.ResourceMethodInfoDTO;
import org.osgi.service.jaxrs.runtime.dto.RuntimeDTO;

/**
 * Reports the ReST endpoints published by the OSGi JAX-RS Whiteboard (OSGi cmpn R7, chapter 151),
 * so that the web bridge knows which requests have to be forwarded to the OSGi container.
 *
 * The whiteboard only reports the paths of the individual resource <em>methods</em>, e.g.
 * {@code /scv/{alias}}. Those are reduced to their static prefix ({@code /scv}) and prepended with
 * the base URI of the application they belong to, which yields the same endpoint granularity the
 * osgi-jax-rs-connector reported before, e.g. {@code /rest/scv}.
 */
@Component(name="restEndpointRegistry")
public class RestEndpointRegistryImpl implements RestEndpointRegistry {

    private JaxrsServiceRuntime jaxrsServiceRuntime;

    @Override
    public List<String> getRestEndpoints() {
        final RuntimeDTO runtimeDTO = jaxrsServiceRuntime.getRuntimeDTO();
        final Set<String> endpoints = new LinkedHashSet<>();
        addEndpoints(endpoints, runtimeDTO.defaultApplication);
        if (runtimeDTO.applicationDTOs != null) {
            for (ApplicationDTO applicationDTO : runtimeDTO.applicationDTOs) {
                addEndpoints(endpoints, applicationDTO);
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(endpoints));
    }

    @Reference
    public void setJaxrsServiceRuntime(JaxrsServiceRuntime jaxrsServiceRuntime) {
        this.jaxrsServiceRuntime = jaxrsServiceRuntime;
    }

    private static void addEndpoints(Set<String> endpoints, ApplicationDTO applicationDTO) {
        if (applicationDTO == null || applicationDTO.resourceDTOs == null) {
            return;
        }
        final String base = normalizeBase(applicationDTO.base);
        for (ResourceDTO resourceDTO : applicationDTO.resourceDTOs) {
            if (resourceDTO.resourceMethods == null) {
                continue;
            }
            for (ResourceMethodInfoDTO resourceMethodInfoDTO : resourceDTO.resourceMethods) {
                final String path = base + staticPrefix(resourceMethodInfoDTO.path);
                if (!path.isEmpty()) {
                    endpoints.add(path);
                }
            }
        }
    }

    /**
     * Turns an application base such as {@code /rest}, {@code /rest/} or {@code /rest/*} into {@code /rest}.
     */
    static String normalizeBase(String base) {
        if (base == null) {
            return "";
        }
        String normalized = base.trim();
        while (normalized.endsWith("*") || normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * Returns the leading, non-templated part of a resource method path, e.g. {@code /scv} for
     * both {@code /scv} and {@code /scv/{alias}}.
     */
    static String staticPrefix(String path) {
        if (path == null) {
            return "";
        }
        final StringBuilder prefix = new StringBuilder();
        for (String segment : path.split("/")) {
            if (segment.isEmpty()) {
                continue;
            }
            if (segment.indexOf('{') >= 0) {
                break;
            }
            prefix.append('/').append(segment);
        }
        return prefix.toString();
    }
}
