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

// Info object for servlets
public class ServletInfo {
    private String alias;
    private String name;
    private final List<String> patterns;
    private final List<PatternMatcher> patternMatchers;

    public ServletInfo(ServiceReference reference) {
        this.name = Utils.getStringProperty(reference, "osgi.http.whiteboard.servlet.name");
        this.patterns = Utils.getListProperty(reference, "osgi.http.whiteboard.servlet.pattern");
        this.alias = Utils.getStringProperty(reference, "alias");
        this.patternMatchers = PatternMatcherFactory.determinePatternMatcher(this.patterns);
    }

    public boolean canHandle(String path) {
        final Optional<PatternMatcher> any = patternMatchers.stream().filter(pm -> pm.matches(path)).findAny();
        return any.isPresent();
    }

    public boolean isValid() {
        return !patterns.isEmpty() && !patternMatchers.isEmpty();
    }

    public boolean hasAlias() {
        return alias != null;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return name;
    }

    public List<String> getPatterns() {
        return patterns;
    }
}
