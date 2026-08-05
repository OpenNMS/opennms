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

import javax.ws.rs.Path;

import org.apache.cxf.jaxrs.utils.AnnotationUtils;

/**
 * Path arithmetic for the ReST endpoints published by {@link JaxRsPublisher}.
 */
public final class JaxRsPaths {

    private JaxRsPaths() {}

    /**
     * Turns an application base such as {@code /rest}, {@code rest}, {@code /rest/} or
     * {@code /rest/*} into the canonical {@code /rest}. An empty or root base yields {@code ""}.
     */
    public static String normalize(final String path) {
        if (path == null) {
            return "";
        }
        String normalized = path.trim();
        while (normalized.endsWith("*") || normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!normalized.isEmpty() && !normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }

    /**
     * Returns the value of the {@code @Path} annotation of a ReST resource, normalized.
     *
     * OpenNMS puts the JAX-RS annotations on the <em>interface</em> rather than on the
     * implementation, so the annotation has to be looked up along the type hierarchy. CXF's
     * {@link AnnotationUtils#getClassAnnotation} does exactly that - it walks super classes as well
     * as interfaces - and it is the same lookup the CXF runtime itself uses when it builds the
     * resource model, so the endpoint reported here cannot drift from the one actually served.
     *
     * @return the normalized path, or {@code ""} if the class carries no {@code @Path} at all
     */
    public static String resourcePath(final Class<?> resourceClass) {
        if (resourceClass == null) {
            return "";
        }
        final Path path = AnnotationUtils.getClassAnnotation(resourceClass, Path.class);
        return path == null ? "" : normalize(path.value());
    }

    /**
     * The endpoint a resource is reachable under, i.e. the application base followed by the path of
     * the resource, e.g. {@code /rest} + {@code /scv} = {@code /rest/scv}.
     *
     * Deliberately <em>not</em> the bare application base: the web bridge matches these endpoints
     * with startsWith, so a resource that contributed only the base would make the bridge claim
     * every request below it - including everything the ReST implementation of the web application
     * itself serves under /rest.
     *
     * @return the endpoint, or {@code ""} if the resource has no path of its own to narrow it down
     */
    public static String endpoint(final String base, final Class<?> resourceClass) {
        final String resourcePath = resourcePath(resourceClass);
        if (resourcePath.isEmpty()) {
            return "";
        }
        return normalize(base) + resourcePath;
    }
}
