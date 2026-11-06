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
package org.opennms.web.navigate;

import javax.servlet.http.HttpServletRequest;

/**
 * A page link that decides, per request and target, whether it shows.
 *
 * @deprecated Nothing reads these links any more. The legacy node page was the only page that
 *     showed them (services registered with {@code Page=node}). The Vue node details page that
 *     replaces it does not show them, so a registered entry has no effect.
 */
@Deprecated(since = "37.0.0")
public interface ConditionalPageNavEntry extends PageNavEntry {
    /**
     * <p>evaluate</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param target an optional object to be evaluated when determining whether to create a link.  (may be null)
     * @return a {@link org.opennms.web.navigate.DisplayStatus} object.
     */
    public abstract DisplayStatus evaluate(HttpServletRequest request, Object target);
}
