/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.doctests.model;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;

import com.google.common.base.MoreObjects;

public class Sequence {
    private final String id;

    private final List<Preparation> preparations;
    private final List<Execution> executions;

    private Sequence(final Builder builder) {
        this.id = Objects.requireNonNull(builder.id);

        this.preparations = Objects.requireNonNull(builder.preparations);
        this.executions = Objects.requireNonNull(builder.executions);
    }

    public static Builder builder(final String id) {
        return new Builder(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("preparations", this.preparations)
                          .add("executions", this.executions)
                          .toString();
    }

    public void execute() throws Exception {
        // Collect all components required by all snippets in that sequence
        final Set<Component> components = Stream.concat(this.preparations.stream(), this.executions.stream())
                                                .flatMap(snippet -> snippet.getComponents().stream())
                                                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Component.class)));

        final OpenNMSProfile.Builder onmsProfile = OpenNMSProfile.newBuilder();
        onmsProfile.withoutDefaultConfig();

        // Apply all preparations before starting the container
        for (final Preparation preparation : this.preparations) {
            preparation.prepare(this, onmsProfile);
        }

        // Build the stack
        final StackModel.Builder stackModel = StackModel.newBuilder();
        stackModel.withOpenNMS(onmsProfile.build());

        for (final Component component : components) {
            switch (component) {
                case MINION:
                    stackModel.withMinion();
                    break;
                case SENTINEL:
                    stackModel.withSentinel();
                    break;
                case ELASTICSEARCH:
                    stackModel.withElasticsearch();
                    break;
                default:
                    throw new IllegalStateException("Unreachable");
            }
        }

        final OpenNMSStack stack = OpenNMSStack.withModel(stackModel.build());

        // Start the containers
        stack.postgres().start();
        stack.opennms().start();

        for (final Component component : components) {
            switch (component) {
                case MINION:
                    stack.minion().start();
                    break;
                case SENTINEL:
                    stack.sentinel().start();
                    break;
                case ELASTICSEARCH:
                    stack.elastic().start();
                    break;
                default:
                    throw new IllegalStateException("Unreachable");
            }
        }

        for (final Execution execution : Sequence.this.executions) {
            execution.execute(stack);
        }
    }

    public String getId() {
        return this.id;
    }

    public static class Builder {
        private String id;

        private List<Preparation> preparations = new LinkedList<>();
        private List<Execution> executions = new LinkedList<>();

        private Builder(final String id) {
            this.id = Objects.requireNonNull(id);
        }

        public Builder withSnippet(final Snippet snippet) {
            snippet.asPreparation().ifPresent(this::withSnippet);
            snippet.asExecution().ifPresent(this::withSnippet);
            return this;
        }

        public Builder withSnippet(final Preparation preparation) {
            this.preparations.add(preparation);
            return this;
        }

        public Builder withSnippet(final Execution execution) {
            this.executions.add(execution);
            return this;
        }

        public Sequence build() {
            return new Sequence(this);
        }
    }
}
