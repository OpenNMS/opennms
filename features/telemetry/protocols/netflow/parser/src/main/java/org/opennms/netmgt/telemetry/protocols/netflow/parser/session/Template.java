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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.session;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Preconditions;

public final class Template implements Iterable<Field> {

    public enum Type {
        TEMPLATE,
        OPTIONS_TEMPLATE,
    }

    public final int id; //uint16
    public final Type type;

    public final List<Scope> scopes;
    public final List<Field> fields;
    public final Set<String> scopeNames;

    private Template(final int id,
                     final Type type,
                     final List<Scope> scopes,
                     final List<Field> fields) {
        this.id = id;
        this.type = Objects.requireNonNull(type);
        this.scopes = Objects.requireNonNull(scopes);
        this.fields = Objects.requireNonNull(fields);
        // The set of scope names are used when processing packets - so we build it here once
        // instead of having to re-compute this everytime
        this.scopeNames = scopes.stream().map(Scope::getName).collect(Collectors.toSet());
    }

    public int count() {
        return this.scopes.size() + this.fields.size();
    }

    @Override
    public Iterator<Field> iterator() {
        return this.fields.iterator();
    }

    public Stream<Field> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public static class Builder {
        private final int id;
        private final Type type;

        private List<Scope> scopes = new LinkedList();
        private List<Field> fields = new LinkedList();

        private Builder(final int id,
                        final Type type) {
            this.id = id;
            this.type = Objects.requireNonNull(type);
        }

        public Builder withScopes(final List<? extends Scope> scopes) {
            assert this.type == Type.OPTIONS_TEMPLATE;

            this.scopes.addAll(scopes);
            return this;
        }

        public Builder withFields(final List<? extends Field> fields) {
            this.fields.addAll(fields);
            return this;
        }

        public Template build() {
            Preconditions.checkNotNull(this.scopes);
            Preconditions.checkNotNull(this.fields);

            return new Template(this.id, this.type, this.scopes, this.fields);
        }
    }

    public static Builder builder(final int id, final Type type) {
        return new Builder(id, type);
    }
}
