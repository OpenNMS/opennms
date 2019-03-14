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

package org.opennms.container.web.bridge.proxy.pattern;

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.container.web.bridge.proxy.pattern.matchers.ExactPathMatcher;
import org.opennms.container.web.bridge.proxy.pattern.matchers.PathMatcher;

public final class PatternMatcherFactory {

    private PatternMatcherFactory() {}

    public static PatternMatcher createPatternMatcher(String pattern) {
        if (pattern.endsWith("/*")) {
            return new PathMatcher(pattern);
        }
        return new ExactPathMatcher(pattern);
    }

    public static List<PatternMatcher> determinePatternMatcher(List<String> patterns) {
        return patterns.stream().map(pattern -> createPatternMatcher(pattern)).collect(Collectors.toList());
    }
}


