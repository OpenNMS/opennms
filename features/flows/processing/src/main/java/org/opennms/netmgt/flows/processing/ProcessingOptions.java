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
package org.opennms.netmgt.flows.processing;

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
