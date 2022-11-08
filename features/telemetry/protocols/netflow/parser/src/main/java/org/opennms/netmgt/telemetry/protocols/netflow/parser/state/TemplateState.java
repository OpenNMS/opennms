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
