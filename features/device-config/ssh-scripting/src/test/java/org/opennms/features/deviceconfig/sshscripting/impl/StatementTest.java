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
package org.opennms.features.deviceconfig.sshscripting.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StatementTest {

    @Test
    public void splitsLinesAtLinebreakIndicator() {
        var either = Statement.parseScript("send: abc\\nawait: 123");
        assertThat(either.isRight(), is(true));
        assertThat(either.get().size(), is(2));
        assertThat(either.get().get(0).statementType, is(Statement.StatementType.send));
        assertThat(either.get().get(0).string, is("abc"));
        assertThat(either.get().get(1).statementType, is(Statement.StatementType.await));
        assertThat(either.get().get(1).string, is("123"));
    }

    @Test
    public void splitsLinesAtRealLineBreak() {
        var either = Statement.parseScript("send: abc\nawait: 123");
        assertThat(either.isRight(), is(true));
        assertThat(either.get().size(), is(2));
        assertThat(either.get().get(0).statementType, is(Statement.StatementType.send));
        assertThat(either.get().get(0).string, is("abc"));
        assertThat(either.get().get(1).statementType, is(Statement.StatementType.await));
        assertThat(either.get().get(1).string, is("123"));
    }

}
