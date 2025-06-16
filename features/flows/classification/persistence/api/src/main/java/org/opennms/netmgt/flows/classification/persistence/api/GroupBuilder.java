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
package org.opennms.netmgt.flows.classification.persistence.api;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.base.Strings;

public class GroupBuilder {

    private final Group group = new Group();

    public GroupBuilder withName(String name) {
        group.setName(name);
        return this;
    }

    public GroupBuilder withRule(Rule rule) {
        group.addRule(rule);
        return this;
    }

    public GroupBuilder withDescription(String description) {
        group.setDescription(description);
        return this;
    }

    public GroupBuilder withPosition(int position) {
        group.setPosition(position);
        return this;
    }

    public GroupBuilder withEnabled(boolean enabled) {
        group.setEnabled(enabled);
        return this;
    }

    public GroupBuilder withReadOnly(boolean readOnly) {
        group.setReadOnly(readOnly);
        return this;
    }

    public GroupBuilder withRules(Supplier<List<Rule>> rulesSupplier) {
        Objects.requireNonNull(rulesSupplier);
        for (Rule rule : rulesSupplier.get()) {
            group.addRule(rule);
        }
        return this;
    }

    public Group build() {
        if (Strings.isNullOrEmpty(group.getName())) {
            throw new IllegalStateException("Cannot build group, because required field 'name' is null or empty");
        }
        return group;
    }
}
