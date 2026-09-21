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
package org.opennms.web.geomap;

import org.opennms.core.utils.WebSecurityUtils;

/**
 * Helpers used by {@code geomap/includes/map.jsp} to render request
 * parameters as JavaScript literals embedded directly into a
 * {@code <script>} block. Centralized here so the conversion is unit
 * testable without spinning up a JSP engine.
 *
 * <p>Background: the prior inline scriptlet emitted the parameter
 * values inside double quotes regardless of whether the parameter was
 * present, producing the literal JavaScript string {@code "null"} when
 * a parameter was missing. The geomap-js client only checks
 * {@code isUndefinedOrNull(...)} (truthy null check), so the string
 * "null" survived the default substitution and was forwarded to
 * {@code /api/v2/geolocation/config?strategy=null}, which the REST
 * endpoint rejects.</p>
 */
public final class GeomapPageHelper {

    private GeomapPageHelper() {}

    /**
     * Renders {@code rawParam} as a JavaScript string literal.
     *
     * <ul>
     *   <li>{@code null} input returns the bare token {@code null} (the
     *       JS literal, no quotes), so client-side
     *       {@code isUndefinedOrNull} default substitution fires.</li>
     *   <li>Non-null input returns the value HTML-encoded and wrapped
     *       in double quotes. HTML-encoding is safe inside a
     *       {@code <script>} block because the HTML parser does not
     *       decode entities in script content.</li>
     * </ul>
     */
    public static String paramAsJsString(final String rawParam) {
        if (rawParam == null) {
            return "null";
        }
        return "\"" + WebSecurityUtils.sanitizeString(rawParam) + "\"";
    }

    /**
     * Renders {@code rawParam} as the JavaScript boolean literal
     * {@code true} when the value matches "true" (case-insensitive),
     * otherwise {@code false}. Null is treated as false.
     */
    public static String paramAsJsBoolean(final String rawParam) {
        return "true".equalsIgnoreCase(rawParam) ? "true" : "false";
    }
}
