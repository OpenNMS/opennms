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
package org.opennms.container.web.bridge.proxy.handlers;

import java.util.List;
import java.util.Optional;

import org.opennms.container.web.bridge.proxy.Utils;
import org.opennms.container.web.bridge.proxy.pattern.PatternMatcher;
import org.opennms.container.web.bridge.proxy.pattern.PatternMatcherFactory;
import org.osgi.framework.ServiceReference;

public class ResourceInfo {
    private final List<String> patterns;
    private final List<PatternMatcher> patternMatchers;
    private final String prefix;

    public ResourceInfo(ServiceReference reference) {
        this.patterns = Utils.getListProperty(reference, "osgi.http.whiteboard.resource.pattern");
        this.prefix = Utils.getStringProperty(reference, "osgi.http.whiteboard.resource.prefix");
        this.patternMatchers = PatternMatcherFactory.determinePatternMatcher(patterns);
    }

    public boolean isValid() {
        return !patterns.isEmpty() && !patternMatchers.isEmpty() && prefix != null && !"".equals(prefix.trim());
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public boolean canHandle(String requestedPath) {
        final Optional<PatternMatcher> any = patternMatchers.stream().filter(pm -> pm.matches(requestedPath)).findAny();
        return any.isPresent();
    }
}
