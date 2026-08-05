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

import static org.junit.Assert.assertEquals;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.junit.Test;

public class JaxRsPathsTest {

    /** Annotated interface with an implementation that repeats nothing - the OpenNMS idiom. */
    @Path("/scv")
    public interface ScvLike {
        @GET
        String get();
    }

    public static class ScvLikeImpl implements ScvLike {
        @Override
        public String get() {
            return null;
        }
    }

    /** A resource whose path is the root; it cannot be narrowed down to an endpoint. */
    @Path("/")
    public interface RootLike {
        @GET
        String get();
    }

    public static class RootLikeImpl implements RootLike {
        @Override
        public String get() {
            return null;
        }
    }

    public static class Unannotated {
    }

    @Test
    public void verifyNormalize() {
        assertEquals("/rest", JaxRsPaths.normalize("/rest"));
        assertEquals("/rest", JaxRsPaths.normalize("/rest/"));
        assertEquals("/rest", JaxRsPaths.normalize("/rest/*"));
        assertEquals("/rest", JaxRsPaths.normalize("rest"));
        assertEquals("/api/v2", JaxRsPaths.normalize("/api/v2/*"));
        assertEquals("", JaxRsPaths.normalize("/"));
        assertEquals("", JaxRsPaths.normalize(""));
        assertEquals("", JaxRsPaths.normalize(null));
    }

    /**
     * The annotations live on the interface, not on the implementation, so the lookup has to walk
     * the type hierarchy.
     */
    @Test
    public void verifyResourcePathIsInheritedFromTheInterface() {
        assertEquals("/scv", JaxRsPaths.resourcePath(ScvLikeImpl.class));
        assertEquals("/scv", JaxRsPaths.resourcePath(ScvLike.class));
        assertEquals("", JaxRsPaths.resourcePath(Unannotated.class));
        assertEquals("", JaxRsPaths.resourcePath(null));
    }

    @Test
    public void verifyEndpoint() {
        assertEquals("/rest/scv", JaxRsPaths.endpoint("/rest", ScvLikeImpl.class));
        assertEquals("/rest/scv", JaxRsPaths.endpoint("/rest/*", ScvLikeImpl.class));
        assertEquals("/api/v2/scv", JaxRsPaths.endpoint("/api/v2", ScvLikeImpl.class));
    }

    /**
     * A resource that contributes no path of its own must yield no endpoint at all. Returning the
     * bare base would make the web bridge claim every request below /rest, including everything the
     * ReST API of the web application serves.
     */
    @Test
    public void verifyRootResourceYieldsNoEndpoint() {
        assertEquals("", JaxRsPaths.endpoint("/rest", RootLikeImpl.class));
        assertEquals("", JaxRsPaths.endpoint("/rest", Unannotated.class));
    }
}
