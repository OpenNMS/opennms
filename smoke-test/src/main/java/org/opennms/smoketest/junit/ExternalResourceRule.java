/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.smoketest.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Inspired by org.junit.rules.ExternalResource with the following additions:
 *  - A boolean is passed to after() indicating if any exceptions were thrown
 *    (did any tests fail?), allowing it to alter the way it tears down resources.
 *  - after() is always called, even when before() fails
 *
 * @author jwhite
 */
public abstract class ExternalResourceRule implements TestRule {
    public Statement apply(Statement base, Description description) {
        return statement(base);
    }

    private Statement statement(final Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                boolean didFail = true;
                try {
                    before();
                    base.evaluate();
                    didFail = false;
                } catch (Throwable t) {
                    throw t;
                } finally {
                    after(didFail);
                }
            }
        };
    }

    /**
     * Override to set up your specific external resource.
     *
     * @throws Throwable if setup fails (which will disable {@code after}
     */
    protected void before() throws Throwable {
        // do nothing
    }

    /**
     * Override to tear down your specific external resource.
     */
    protected void after(boolean didFail) {
        // do nothing
    }
}
