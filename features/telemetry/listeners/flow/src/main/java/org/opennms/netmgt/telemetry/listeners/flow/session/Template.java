/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

public final class Template implements Iterable<Field> {

    public enum Type {
        TEMPLATE,
        OPTIONS_TEMPLATE,
    }

    public final int id; //uint16

    public final List<Field> scopes;
    public final List<Field> fields;

    private Template(final int id,
                     final List<Field> scopes,
                     final List<Field> fields) {
        this.id = id;
        this.scopes = Objects.requireNonNull(scopes);
        this.fields = Objects.requireNonNull(fields);
    }

    public Type type() {
        return this.scopes.isEmpty() ? Type.TEMPLATE : Type.OPTIONS_TEMPLATE;
    }

    public int count() {
        return this.scopes.size() + this.fields.size();
    }

    @Override
    public Iterator<Field> iterator() {
        return Iterators.concat(this.scopes.iterator(), this.fields.iterator());
    }

    public Stream<Field> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public static class Builder {
        private final int id;

        private List<Field> scopeFields = new LinkedList<>();
        private List<Field> fields = new LinkedList<>();

        private Builder(final int id) {
            this.id = id;
        }

        public Builder withScopeFields(final List<Field> scopeFields) {
            this.scopeFields = scopeFields;
            return this;
        }

        public Builder withFields(final List<Field> fields) {
            this.fields = fields;
            return this;
        }

        public Template build() {
            Preconditions.checkNotNull(this.scopeFields);
            Preconditions.checkNotNull(this.fields);

            return new Template(this.id, this.scopeFields, this.fields);
        }
    }

    public static Builder builder(final int id) {
        return new Builder(id);
    }
}
