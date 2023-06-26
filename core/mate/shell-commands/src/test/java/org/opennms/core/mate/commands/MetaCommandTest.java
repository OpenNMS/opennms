/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.core.mate.commands;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.MapScope;
import org.opennms.core.mate.api.Scope;

public class MetaCommandTest {
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Test
    public void testNMS15640_SecureCredentialsVault() {
        final MetaCommand metaCommand = new MetaCommand();
        final Scope scope = new MapScope(Scope.ScopeName.GLOBAL, Map.of(new ContextKey("scv","firstalias:username"), "beaker", new ContextKey("scv","firstalias:password"), "Pittsboro", new ContextKey("scv","secondalias:username"), "bunsen", new ContextKey("scv", "secondalias:password"), "Raleigh"));
        metaCommand.printScope(scope);
        assertThat(systemOutRule.getLog(), containsString("firstalias:username='beaker @ GLOBAL'"));
        assertThat(systemOutRule.getLog(), containsString("secondalias:username='bunsen @ GLOBAL'"));
        assertThat(systemOutRule.getLog(), containsString("firstalias:password='<output omitted> @ GLOBAL'"));
        assertThat(systemOutRule.getLog(), containsString("secondalias:password='<output omitted> @ GLOBAL'"));
        assertThat(systemOutRule.getLog(), not(containsString("Pittsboro")));
        assertThat(systemOutRule.getLog(), not(containsString("Raleigh")));
    }

    @Test
    public void testNMS15640_otherContext() {
        final MetaCommand metaCommand = new MetaCommand();
        final Scope scope = new MapScope(Scope.ScopeName.GLOBAL, Map.of(new ContextKey("requisition","foo"), "bar", new ContextKey("requisition","someSecret"), "Pittsboro", new ContextKey("requisition","somePassword"), "Raleigh"));
        metaCommand.printScope(scope);
        assertThat(systemOutRule.getLog(), containsString("foo='bar @ GLOBAL'"));
        assertThat(systemOutRule.getLog(), containsString("somePassword='<output omitted> @ GLOBAL'"));
        assertThat(systemOutRule.getLog(), containsString("someSecret='<output omitted> @ GLOBAL'"));
        assertThat(systemOutRule.getLog(), not(containsString("Pittsboro")));
        assertThat(systemOutRule.getLog(), not(containsString("Raleigh")));
    }
}

