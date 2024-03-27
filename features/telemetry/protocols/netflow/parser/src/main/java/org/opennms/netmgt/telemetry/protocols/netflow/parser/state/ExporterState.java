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

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

public class ExporterState {
    public final String key;

    public final List<TemplateState> templates;
    public final List<OptionState> options;

    public ExporterState(Builder builder) {
        this.key = Objects.requireNonNull(builder.key);

        this.templates = Objects.requireNonNull(builder.templates.build());
        this.options = Objects.requireNonNull(builder.options.build());
    }

    public static Builder builder(final String sessionKey) {
        return new Builder(sessionKey);
    }

    public static class Builder {
        private final String key;

        private final ImmutableList.Builder<TemplateState> templates = ImmutableList.builder();
        private final ImmutableList.Builder<OptionState> options = ImmutableList.builder();

        private Builder(final String key) {
            this.key = Objects.requireNonNull(key);
        }

        public Builder withTemplate(final TemplateState state) {
            this.templates.add(state);
            return this;
        }

        public Builder withTemplate(final TemplateState.Builder state) {
            return this.withTemplate(state.build());
        }

        public Builder withOptions(final OptionState state) {
            this.options.add(state);
            return this;
        }

        public Builder withOptions(final OptionState.Builder state) {
            return this.withOptions(state.build());
        }

        public ExporterState build() {
            return new ExporterState(this);
        }
    }
}
