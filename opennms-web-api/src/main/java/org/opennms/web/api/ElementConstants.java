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
package org.opennms.web.api;

/**
 * Shared constants for links to network element pages.
 */
public final class ElementConstants {

    /**
     * The Vue node detail page. Callers append a node id or foreignSource:foreignId.
     * It is a hash route, so it takes no query parameters. It is relative to the
     * webapp context root (the pages' {@code <base href>}).
     * <p>
     * As a compile-time constant, its value is copied into every class that uses it. A change
     * takes effect only once those classes are rebuilt, which matters for plugins built
     * outside this repository. {@code NodeResourceType} in opennms-dao keeps its own copy.
     */
    public static final String NODE_DETAILS_URI = "ui/index.html#/node/";

    /**
     * The Vue node list page, relative to the webapp context root. It is a compile-time constant,
     * as {@link #NODE_DETAILS_URI} is.
     */
    public static final String NODE_LIST_URI = "ui/index.html#/nodes";

    private ElementConstants() {
    }
}
