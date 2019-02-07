/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.utils.mate;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

public class PatternScope implements Scope {
    private final String context;
    private final Matcher matcher;

    public PatternScope(final String context, final Matcher matcher) {
        this.context = Objects.requireNonNull(context);
        this.matcher = Objects.requireNonNull(matcher);
    }

    @Override
    public Optional<String> get(final ContextKey contextKey) {
        if (Objects.equals(this.context, contextKey.context)) {

        }

        return Optional.empty();
    }

    @Override
    public Set<ContextKey> keys() {
        // We can't figure out the available keys here...
        return Collections.emptySet();
    }
}
