/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

    public GroupBuilder withPriority(int priority) {
        group.setPriority(priority);
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
            throw new IllegalStateException("Cannot build rule, because required field 'name' is null or empty");
        }
        return group;
    }
}
