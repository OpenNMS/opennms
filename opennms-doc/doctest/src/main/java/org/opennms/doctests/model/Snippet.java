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

import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import com.google.common.base.MoreObjects;

public abstract class Snippet {
    private final Path file;
    private final int line;

    private final String content;

    private final String id;

    private final EnumSet<Component> components;

    protected Snippet(final Builder<?> builder) {
        this.file = Objects.requireNonNull(builder.file);
        this.line = Objects.requireNonNull(builder.line);
        this.content = Objects.requireNonNull(builder.content);
        this.id = Objects.requireNonNull(builder.id);
        this.components = Objects.requireNonNull(builder.components);
    }

    public Path getFile() {
        return this.file;
    }

    public int getLine() {
        return this.line;
    }

    public String getContent() {
        return this.content;
    }

    public String getId() {
        return this.id;
    }

    public abstract Optional<Preparation> asPreparation();

    public abstract Optional<Execution> asExecution();

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                          .add("file", this.file)
                          .add("line", this.line)
                          .add("content", this.content)
                          .add("id", this.id)
                          .add("components", this.components);
    }

    public EnumSet<Component> getComponents() {
        return this.components;
    }

    public static abstract class Builder<R extends Builder<R>> {
        private Path file;
        private Integer line;

        private String content;

        private String id;

        private EnumSet<Component> components = EnumSet.noneOf(Component.class);

        protected Builder() {
        }

        protected abstract R adapt();

        public abstract Snippet build();

        public R withFile(final Path file) {
            this.file = file;
            return this.adapt();
        }

        public R withLine(final Integer line) {
            this.line = line;
            return this.adapt();
        }

        public R withContent(final String content) {
            this.content = content;
            return this.adapt();
        }

        public R withId(final String id) {
            this.id = id;
            return this.adapt();
        }

        public R withComponents(final Collection<Component> component) {
            this.components.addAll(component);
            return this.adapt();
        }

        public R withComponent(final Component component) {
            this.components.add(component);
            return this.adapt();
        }
    }
}
