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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.state;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class OptionState {
    public final int templateId;

    public final Duration age;

    public final List<NamedValue> selectors;
    public final List<NamedValue> values;

    public OptionState(Builder builder) {
        this.templateId = builder.templateId;

        this.age = builder.age;

        this.selectors = Objects.requireNonNull(builder.selectors.build());
        this.values = Objects.requireNonNull(builder.values.build());
    }

    public static Builder builder(final int templateId) {
        return new Builder(templateId);
    }

    public static class Builder {
        private int templateId;

        private Duration age;

        private final ImmutableList.Builder<NamedValue> selectors = ImmutableList.builder();
        private final ImmutableList.Builder<NamedValue> values = ImmutableList.builder();

        private Builder(final int templateId) {
            this.templateId = templateId;
        }

        public Builder withTemplateId(final int templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder withAge(final Duration age) {
            this.age = age;
            return this;
        }

        public Builder withInsertionTime(final Instant insertionTime) {
            return this.withAge(Duration.between(insertionTime, Instant.now()));
        }

        public Builder withSelectors(final Iterable<Value<?>> selectors) {
            for (final Value<?> selector : selectors) {
                this.selectors.add(NamedValue.from(selector));
            }
            return this;
        }

        public Builder withSelector(final Value<?> selector) {
            this.selectors.add(NamedValue.from(selector));
            return this;
        }

        public Builder withValues(final Iterable<Value<?>> values) {
            for (final Value<?> value : values) {
                this.values.add(NamedValue.from(value));
            }
            return this;
        }

        public Builder withValue(final Value<?> value) {
            this.values.add(NamedValue.from(value));
            return this;
        }

        public OptionState build() {
            return new OptionState(this);
        }
    }

    public static class NamedValue {
        public final String name;
        public final String value;

        public NamedValue(final String name, final String value) {
            this.name = Objects.requireNonNull(name);
            this.value = Objects.requireNonNull(value);
        }

        public static NamedValue from(final Value<?> value) {
            return new NamedValue(value.getName(), value.getValue().toString());
        }
    }
}
