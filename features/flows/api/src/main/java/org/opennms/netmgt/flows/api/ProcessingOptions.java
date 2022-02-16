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

package org.opennms.netmgt.flows.api;

import java.util.List;

import org.opennms.netmgt.telemetry.config.api.PackageDefinition;

import com.google.common.collect.Lists;

public class ProcessingOptions {

    public final boolean applicationThresholding;
    public final boolean applicationDataCollection;

    public final List<? extends PackageDefinition> packages;

    private ProcessingOptions(final Builder builder) {
        this.applicationThresholding = builder.applicationThresholding;
        this.applicationDataCollection = builder.applicationDataCollection;
        this.packages = builder.packages;
    }

    public static class Builder {
        private boolean applicationThresholding;
        private boolean applicationDataCollection;

        private List<? extends PackageDefinition> packages = Lists.newArrayList();

        private Builder() {}

        public Builder setApplicationThresholding(final boolean applicationThresholding) {
            this.applicationThresholding = applicationThresholding;
            return this;
        }

        public Builder setApplicationDataCollection(final boolean applicationDataCollection) {
            this.applicationDataCollection = applicationDataCollection;
            return this;
        }

        public Builder setPackages(final List<? extends PackageDefinition> packages) {
            this.packages = packages;
            return this;
        }

        public ProcessingOptions build() {
            return new ProcessingOptions(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
