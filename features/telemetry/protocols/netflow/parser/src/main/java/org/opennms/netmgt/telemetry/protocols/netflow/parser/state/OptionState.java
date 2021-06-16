/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
