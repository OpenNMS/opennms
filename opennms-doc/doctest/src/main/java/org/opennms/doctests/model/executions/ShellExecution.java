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

package org.opennms.doctests.model.executions;

import java.util.Objects;

import org.opennms.doctests.Doctest;
import org.opennms.doctests.model.Execution;
import org.opennms.doctests.utils.CommandWithOutput;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.testcontainers.containers.Container;

import com.google.common.base.MoreObjects;

public class ShellExecution extends Execution {
    private final System system;

    private ShellExecution(final Builder builder) {
        super(builder);
        this.system = Objects.requireNonNull(builder.system);
    }

    public static Builder builder() {
        return new Builder();
    }

    public System getSystem() {
        return this.system;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                    .add("system", this.system);
    }

    @Override
    public void execute(final OpenNMSStack stack) throws Exception {
        for (final CommandWithOutput cwo : CommandWithOutput.parse(this.getContent())) {
            final Container.ExecResult result = stack.opennms().execInContainer("bash", "-c", cwo.command);

            if (result.getExitCode() != 0) {
                Doctest.LOG.error("Command returned with exit-code " + result.getExitCode());
                Doctest.LOG.error("  stderr: " + result.getStderr());
                Doctest.LOG.error("  stdout: " + result.getStdout());

                throw new AssertionError("Command returned with exit-code != 0");
            }

            cwo.assertOutput(result.getStdout(), result.getStderr());
        }
    }

    @Override
    public String toString() {
        return this.toStringHelper().toString();
    }

    public static class Builder extends Execution.Builder<Builder> {
        private System system;

        private Builder() {
        }

        @Override
        protected Builder adapt() {
            return this;
        }

        public Builder withSystem(final System system) {
            this.system = system;
            return this;
        }

        public Builder withSystem(final String system) {
            return this.withSystem(System.valueOf(system.toUpperCase()));
        }

        @Override
        public ShellExecution build() {
            return new ShellExecution(this);
        }
    }

    public enum System {
        OPENNMS,
        SENTINEL,
        MINION,
    }
}
