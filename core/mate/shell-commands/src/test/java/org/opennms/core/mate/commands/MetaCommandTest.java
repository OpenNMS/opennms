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
