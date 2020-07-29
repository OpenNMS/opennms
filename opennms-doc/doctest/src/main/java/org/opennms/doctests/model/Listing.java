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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Intermediate representation of a doctest listing.
 * Values in here are no validated.
 */
public class Listing {
    private final String file;
    private final int line;

    private final String content;

    private final String target;

    private final String id;

    private final List<String> dependencies;

    private final List<String> components;

    private Listing(final Builder builder) {
        this.file = Objects.requireNonNull(builder.file);
        this.line = Objects.requireNonNull(builder.line);
        this.content = Objects.requireNonNull(builder.content);
        this.target = Objects.requireNonNull(builder.target);
        this.id = Objects.requireNonNull(builder.id);
        this.dependencies = ImmutableList.copyOf(builder.dependencies);
        this.components = ImmutableList.copyOf(builder.components);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFile() {
        return this.file;
    }

    public int getLine() {
        return this.line;
    }

    public String getContent() {
        return this.content;
    }

    public String getTarget() {
        return this.target;
    }

    public String getId() {
        return this.id;
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }

    public List<String> getComponents() {
        return this.components;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("file", this.file)
                          .add("line", this.line)
                          .add("content", this.content)
                          .add("target", this.target)
                          .add("id", this.id)
                          .add("dependencies", this.dependencies)
                          .add("components", this.components)
                          .toString();
    }

    public static class Builder {
        private String file;
        private Integer line;

        private String content;

        private String target;

        private String id;

        private List<String> dependencies = new ArrayList<>();

        private List<String> components = new ArrayList<>();

        private Builder() {
        }

        public Builder withFile(final String file) {
            this.file = file;
            return this;
        }

        public Builder withLine(final Integer line) {
            this.line = line;
            return this;
        }

        public Builder withContent(final String content) {
            this.content = content;
            return this;
        }

        public Builder withTarget(final String target) {
            this.target = target;
            return this;
        }

        public Builder withId(final String id) {
            this.id = id;
            return this;
        }

        public Builder withDependencies(final List<String> dependencies) {
            this.dependencies.addAll(dependencies);
            return this;
        }

        public Builder withDependency(final String dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        public Builder withComponents(final List<String> components) {
            this.components.addAll(components);
            return this;
        }

        public Builder withComponent(final String component) {
            this.components.add(component);
            return this;
        }



        public Listing build() {
            return new Listing(this);
        }
    }

}
