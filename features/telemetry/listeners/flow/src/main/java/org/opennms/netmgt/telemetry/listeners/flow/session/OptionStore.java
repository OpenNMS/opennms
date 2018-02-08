/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.flow.session;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.netmgt.telemetry.listeners.flow.ie.Value;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class OptionStore<P> {

    private final class Entry {
        private final Scope<P> scope;
        private final List<Value<?>> values;

        private Entry(final Scope<P> scope,
                      final List<Value<?>> values) {
            this.scope = Objects.requireNonNull(scope);
            this.values = Objects.requireNonNull(values);
        }
    }

    private final Map<Template, Set<Entry>> store;

    public OptionStore() {
        this.store = Maps.newHashMap();
    }

    public void retractTemplate(final Template template) {
        this.store.remove(template);
    }

    public void insert(final Template template,
                       final Scope<P> scope,
                       final List<Value<?>> values) {
        final Set<Entry> entries = this.store.computeIfAbsent(template, (t) -> Sets.newHashSet());
        entries.add(new Entry(scope, values));
    }
}
