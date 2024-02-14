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
import java.util.Objects;

public class TemplateState {
    public final int templateId;

    public final Duration age;

    public TemplateState(final Builder builder) {
        this.templateId = builder.templateId;

        this.age = Objects.requireNonNull(builder.age);
    }

    public static Builder builder(final int templateId) {
        return new Builder(templateId);
    }

    public static class Builder {
        private int templateId;

        private Duration age;

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

        public TemplateState build() {
            return new TemplateState(this);
        }
    }
}
