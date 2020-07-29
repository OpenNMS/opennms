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

package org.opennms.doctests.utils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

public class CommandWithOutput {
    public final String command;
    public final List<String> output;

    public CommandWithOutput(final Builder builder) {
        this.command = builder.command;
        this.output = builder.output.build();
    }

    public static class Builder {
        private String command;
        private ImmutableList.Builder<String> output;

        private Builder(final String command) {
            this.command = Objects.requireNonNull(command);
            this.output = ImmutableList.builder();
        }

        public Builder withCommand(final String command) {
            this.command = Objects.requireNonNull(command);
            return this;
        }

        public Builder withOutputLine(final String line) {
            this.output.add(line);
            return this;
        }

        public CommandWithOutput build() {
            return new CommandWithOutput(this);
        }
    }

    public void assertOutput(final String stdout, final String stderr) {
        final PeekingIterator<String> out = Iterators.peekingIterator(lines(stdout).iterator());
        final PeekingIterator<String> err = Iterators.peekingIterator(lines(stderr).iterator());

        for (final String line : this.output) {
            if (out.hasNext() && Objects.equals(out.peek(), line)) {
                out.next();
                continue;
            }

            if (err.hasNext() && Objects.equals(err.peek(), line)) {
                err.next();
                continue;
            }

            throw new AssertionError("Unexpected output line: " + line);
        }

        if (out.hasNext()) {
            throw new AssertionError("Missing output line: " + out.peek());
        }

        if (err.hasNext()) {
            throw new AssertionError("Missing output line: " + err.peek());
        }
    }

    public static Builder build(final String command) {
        return new Builder(command);
    }

    public static List<CommandWithOutput> parse(final String content) {
        final ImmutableList.Builder<CommandWithOutput> result = ImmutableList.builder();

        final PeekingIterator<String> lines = Iterators.peekingIterator(CommandWithOutput.lines(content).iterator());
        while (lines.hasNext()) {
            final String line = lines.next();
            assert line.startsWith("#");

            final Builder builder = CommandWithOutput.build(line.substring(1).trim());

            while (lines.hasNext() && !lines.peek().startsWith("#")) {
                builder.withOutputLine(lines.next());
            }

            result.add(builder.build());
        }

        return result.build();
    }

    private static Iterable<String> lines(final String text) {
        return Splitter.on(Pattern.compile("\r?\n"))
                       .trimResults()
                       .omitEmptyStrings()
                       .split(text);
    }
}
