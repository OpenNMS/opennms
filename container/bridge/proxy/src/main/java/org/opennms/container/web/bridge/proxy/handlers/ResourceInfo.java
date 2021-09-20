/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
